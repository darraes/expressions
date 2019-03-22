package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.errors.CompilationException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To simplify expression and prevent avoid the need to explicity do casting, access
 * the registry and any other necessary trick to access the argument, the preprocessor
 * understands the syntax $argumentName and will properly replace that for the right and
 * runnable way to access the argument.
 */
public class ExpressionFactory {
    private static Pattern regex;
    private static String ASYNC_SCRIPT_TEMPLATE;

    static {
        regex = Pattern.compile("(\\$[A-Za-z_][A-Za-z_]*)");

        /**
         * The first substitution is the actual expression adjusted to work with the
         * AsyncEvalSession. Eg.:
         *     ((Integer)session.registry().value("arg1", session.inner()))
         *         + ((Integer) session.asyncValues().get("arg2"))
         *
         * The second substitution is a comma separated list of all async arguments.
         * Eg.: "asyncArg1", "asyncArg2"
         *
         * The third substitution is the name of the class being compiled to serve
         * the expression.
         *
         * The choice for a anonymous class instead of a lambda is due to the fact that
         * Janino API doesn't support lambdas yet.
         */
        ASYNC_SCRIPT_TEMPLATE = ""
                + "static Object execute(AsyncEvalSession session) {\n"
                + "    return %s;\n"
                + "}\n"
                + "return session.registry().allValuesAsync(\n"
                + "                                   new String[]{%s},\n"
                + "                                   session,\n"
                + "                                   executor)\n"
                + "    .thenCompose(new Function<Map, Integer>() {\n"
                + "                 public Object apply(Map asyncValues) {\n"
                + "                      return %s.execute(\n"
                + "                              new AsyncEvalSession(\n"
                + "                                      session,\n"
                + "                                      asyncValues));\n"
                + "                  }\n"
                + "     });";
    }

    public static Expression create(
            ArgumentRegistry registry, ExpressionConfig config) {
        try {
            List<Argument> arguments = parseArguments(registry, config.getExpression());

            if (!config.isAsync()) {
                if (arguments.stream().anyMatch(Argument::isAsync)) {
                    throw new CompilationException(
                            "Expression %s is not marked as async but it has at least one"
                                    + " async argument dependency",
                            config.getExpression());
                }

                String finalExpression = processSyncExpression(
                        config.getExpression(),
                        arguments);

                return new Expression(new ExpressionInfo(
                        generateID(),
                        finalExpression,
                        Class.forName(config.getReturnType()),
                        arguments,
                        false,
                        config.getDependencies()));
            } else {
                String finalExpression = processAsyncExpression(
                        config.getExpression(),
                        arguments);

                String asyncArgs = String.join(
                        ",",
                        arguments
                                .stream()
                                .filter(Argument::isAsync)
                                .map(a -> "\"" + a.getName() + "\"")
                                .toArray(String[]::new));

                String expressionID = generateID();

                String finalScript = String.format(
                        ASYNC_SCRIPT_TEMPLATE,
                        finalExpression,
                        asyncArgs,
                        expressionID);

                /*
                System.out.println(finalScript);

                finalScript = String.format("static Integer exec(AsyncEvalSession session) {\n"
                        + "    return ((Integer)session.registry().value(\"a\", session.inner())) + ((Integer) session.asyncValues().get(\"c\")) - ((Integer) session.asyncValues().get(\"d\"));\n"
                        + "}\n"
                        + "return session.registry().allValuesAsync(new String[]{\"c\", \"d\"}, session, executor)\n" +
                        "                .thenApply(new Function<Map, Integer>() {\n" +
                        "                    public Integer apply(Map asyncValues) {\n" +
                        "                        return %s.exec(new AsyncEvalSession(session, asyncValues));\n" +
                        "                    }\n" +
                        "                });", expressionID);

                System.out.println(finalScript);
                */

                return new Expression(new ExpressionInfo(
                        expressionID,
                        finalScript,
                        Class.forName(config.getReturnType()),
                        arguments,
                        true,
                        config.getDependencies()));

            }
        } catch (ClassNotFoundException e) {
            throw new CompilationException(e,
                    "Class not found when compiling expression %s",
                    config.getExpression());
        }
    }

    private static String processSyncExpression(
            String expression, List<Argument> arguments) {
        // Replaces the compressed syntax by the argument access logic
        String result;
        result = expression;
        for (Argument argument : arguments) {
            result = result.replace(
                    String.format("$%s", argument.getName()),
                    String.format(
                            "((%s)session.registry().value(\"%s\", session))",
                            argument.getReturnType().getName(),
                            argument.getName()));
        }

        return result;
    }

    private static String processAsyncExpression(
            String expression, List<Argument> arguments) {
        // Replaces the compressed syntax by the argument access logic
        String result;
        result = expression;
        for (Argument argument : arguments) {
            if (!argument.isAsync()) {
                result = result.replace(
                        String.format("$%s", argument.getName()),
                        String.format(
                                "((%s)session.registry().value(\"%s\", session.inner()))",
                                argument.getReturnType().getName(),
                                argument.getName()));
            } else {
                result = result.replace(
                        String.format("$%s", argument.getName()),
                        String.format(
                                "((%s)session.asyncValues().get(\"%s\"))",
                                argument.getReturnType().getName(),
                                argument.getName()));
            }
        }

        return result;
    }

    private static List<Argument> parseArguments(
            ArgumentRegistry registry, String expression) {
        Matcher matcher = regex.matcher(expression);

        // Captures all argument reading
        ArrayList<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }

        // Gets the argument definition from the registry
        ArrayList<Argument> arguments = new ArrayList<>();
        for (String s : matches) {
            arguments.add(registry.get(s.substring(1)));
        }

        // We must handle the lengthier names first to prevent arguments with matching
        // prefixes to harm the end expression.
        // Eg.: $b + $big. If we create $b first, we would wrongly target ($b)ig
        arguments.sort((a1, a2) -> a2.getName().length() - a1.getName().length());
        return arguments;
    }

    private static String generateID() {
        return "E_"
                + UUID.randomUUID()
                .toString()
                .toUpperCase()
                .replace("-", "");
    }
}
