package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.expressions.ExpressionFactory;

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
                                  ArgumentConfig config) {
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
                        ExpressionFactory.create(registry, config),
                        Class.forName(
                                config.getReturnType()),
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
}
