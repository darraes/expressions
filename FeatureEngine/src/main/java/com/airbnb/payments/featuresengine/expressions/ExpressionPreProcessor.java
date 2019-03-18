package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.errors.CompilationException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To simplify expression and prevent avoid the need to explicity do casting, access
 * the registry and any other necessary trick to access the argument, the preprocessor
 * understands the syntax $argumentName and will properly replace that for the right and
 * runnable way to access the argument.
 */
public class ExpressionPreProcessor {
    private static Pattern regex;

    static {
        regex = Pattern.compile("(\\$[A-Za-z_][A-Za-z_]*)");
    }

    public static String process(
            ArgumentRegistry registry, String expression) throws CompilationException {
        Matcher matcher = regex.matcher(expression);

        // Captures all argument reading
        var matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group(0));
        }

        // Gets the argument definition from the registry
        var arguments = new ArrayList<Argument>();
        for (String s : matches) {
            arguments.add(registry.get(s.substring(1)));
        }

        // We must handle the lengthier names first to prevent arguments with matching
        // prefixes to harm the end expression.
        // Eg.: $b + $big - If we process $b first, we will wrongly target ($b)ig
        arguments.sort((a1, a2) -> a2.getName().length() - a1.getName().length());

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
}
