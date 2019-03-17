package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.expressions.NamedExpression;

/**
 * Responsible for creating all types of Argument (InputArguments, NamedExpressions,
 * ...).
 * It also registers the arguments with the registry.
 */
public class ArgumentFactory {

    /**
     * Factory creation method for InputArguments
     *
     * @param registry Registry to register the argument
     * @param config   All necessary knobs to create the argument
     * @return Newly created and registered argument
     * @throws CompilationException When the argument is duplicated
     */
    public static Argument create(ArgumentRegistry registry,
                                  ArgumentConfig config) throws CompilationException {
        if (registry == null) {
            throw new RuntimeException(
                    "ArgumentFactory cannot be used before init() call");
        }

        try {

            Argument argument;
            if (config.getExpression() == null) {
                argument = new InputArgument(
                        config.getName(),
                        Class.forName(
                                config.getReturnType()),
                        config.isCacheable(),
                        config.isAsync());
            } else {
                argument = new NamedExpression(
                        config.getName(),
                        Class.forName(
                                config.getReturnType()),
                        config.getExpression(),
                        config.isCacheable(),
                        config.isAsync());
            }
            registry.register(argument);

            return argument;
        } catch (ClassNotFoundException e) {
            throw new CompilationException
                    (e, "Class %s not found", config.getReturnType());
        }
    }

    /**
     * Factory creation method for InputArguments
     *
     * @param registry   Registry to register the argument
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param cacheable  If the argument, once computed, should be cached on further
     *                   fetches
     * @param isAsync    If the given argument needs async fetching
     * @return Newly created and registered argument
     * @throws CompilationException When the argument is duplicated
     */
    public static Argument create(ArgumentRegistry registry,
                                  String name,
                                  Class<?> returnType,
                                  boolean cacheable,
                                  boolean isAsync) throws CompilationException {
        if (registry == null) {
            throw new RuntimeException(
                    "ArgumentFactory cannot be used before init() call");
        }

        var argument = new InputArgument(name, returnType, cacheable, isAsync);
        registry.register(argument);

        return argument;
    }

    /**
     * Factory creation method for NamedExpressions
     *
     * @param registry   Registry to register the argument
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param expression Textual representation of the expression
     * @param cacheable  If the argument, once computed, should be cached on further
     *                   fetches
     * @param isAsync    If the given argument needs async fetching
     * @return Newly created and registered argument
     * @throws CompilationException When the argument is duplicated or
     *                              the expression can't be compiled
     */
    public static Argument create(ArgumentRegistry registry,
                                  String name,
                                  Class<?> returnType,
                                  String expression,
                                  boolean cacheable,
                                  boolean isAsync) throws CompilationException {
        if (registry == null) {
            throw new RuntimeException(
                    "ArgumentFactory cannot be used without a registry");
        }

        // Any named expression can be used as an argument
        var argument = new NamedExpression(
                name, returnType, expression, cacheable, isAsync);
        registry.register(argument);

        return argument;
    }
}
