package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;

import java.util.ArrayList;
import java.util.List;
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
                + "static Integer execute(AsyncEvalSession session) {\n"
                + "    return %s;\n"
                + "}\n"
                + "return session.registry().allValuesAsync("
                + "                                   new String[]{%s},\n"
                + "                                   session,\n"
                + "                                   executor)\n"
                + "    .thenApply(new Function<Map, Integer>() {\n"
                + "                  public Integer apply(Map asyncValues) {\n"
                + "                      return %s.execute(\n"
                + "                              new AsyncEvalSession(\n"
                + "                                      session,\n"
                + "                                      asyncValues));\n"
                + "                  }\n"
                + "     });";
    }

    public static Expression create(ArgumentRegistry registry, ExpressionConfig config)
            throws ClassNotFoundException {
        List<Argument> arguments = parseArguments(registry, config.getExpression());

        if (!config.isAsync()) {
            String finalExpression = processExpression(
                    config.getExpression(),
                    arguments);
            
            return new Expression(new ExpressionInfo(
                    finalExpression,
                    Class.forName(config.getReturnType()),
                    arguments,
                    false,
                    config.getDependencies()));
        }
        return null;
    }

    public static String processExpression(String expression, List<Argument> arguments) {
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
}
