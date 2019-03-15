package com.airbnb.payments.featuresengine.arguments;


import com.airbnb.payments.featuresengine.expressions.NamedExpression;
import org.codehaus.commons.compiler.CompileException;

/**
 * Responsible for creating all types of Argument (InputArguments, NamedExpressions, ...)
 * It also registers the arguments with the registry.
 */
public class ArgumentFactory {

    /**
     *
     * @param registry
     * @param name
     * @param returnType
     * @param cacheable
     * @return
     */
    public static Argument create(ArgumentRegistry registry,
                                  String name,
                                  Class<?> returnType,
                                  boolean cacheable) {
        if (registry == null) {
            throw new RuntimeException("ArgumentFactory cannot be used before init() call");
        }

        var argument = new InputArgument(name, returnType, cacheable);
        registry.register(argument);

        return argument;
    }

    /**
     *
     * @param registry
     * @param name
     * @param returnType
     * @param expression
     * @param cacheable
     * @return
     * @throws CompileException
     */
    public static Argument create(ArgumentRegistry registry,
                                  String name,
                                  Class<?> returnType,
                                  String expression,
                                  boolean cacheable) throws CompileException {
        if (registry == null) {
            throw new RuntimeException("ArgumentFactory cannot be used without a registry");
        }

        var argument = new NamedExpression(name, returnType, expression, cacheable);
        registry.register(argument);

        return argument;
    }
}
