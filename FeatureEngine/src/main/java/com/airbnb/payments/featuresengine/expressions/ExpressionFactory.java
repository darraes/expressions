package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.errors.CompilationException;

import java.util.*;
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

    static {
        regex = Pattern.compile("(\\$[A-Za-z_][A-Za-z_]*)");
    }

    public static Expression create(
            ArgumentRegistry registry, ExpressionConfig config) {
        try {
            List<Argument> arguments = parseArguments(registry, config.getExpression());

            if (!config.isAsync() && arguments.stream().anyMatch(Argument::isAsync)) {
                throw new CompilationException(
                        "Expression %s is not marked as async but it has at least one"
                                + " async argument dependency",
                        config.getExpression());
            }

            String finalExpression = processSyncExpression(
                    config.getExpression(),
                    arguments);

            return new Expression(
                    new ExpressionInfo(
                            generateID(),
                            config.getExpression(),
                            finalExpression,
                            Class.forName(config.getReturnType()),
                            arguments,
                            config.isAsync(),
                            config.getDependencies()));
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

    private static List<Argument> parseArguments(
            ArgumentRegistry registry, String expression) {
        Matcher matcher = regex.matcher(expression);

        // Captures all argument reading
        ArrayList<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }

        // Gets the argument definition from the registry
        Set<String> seenArguments = new HashSet<>();
        ArrayList<Argument> arguments = new ArrayList<>();
        for (String s : matches) {
            String name = s.substring(1);
            if (seenArguments.contains(name)) {
                continue;
            }

            seenArguments.add(name);
            arguments.add(registry.get(name));
        }

        // We must handle the lengthier names first to prevent arguments with matching
        // prefixes to harm the end expression.
        // Eg.: $b + $big. If we create $b first, we would wrongly target ($b)ig
        arguments.sort((a1, a2) -> a2.getName().length() - a1.getName().length());
        return arguments;
    }

    private static String generateID() {
        return "Exp_"
                + UUID.randomUUID()
                .toString()
                .toUpperCase()
                .replace("-", "");
    }
}
