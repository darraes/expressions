package com.airbnb.payments.featuresengine;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;

public class ExpressionArgument<TReturn> extends Argument<TReturn> {
    private Expression expression;
    private ExpressionEvaluator eval;

    public ExpressionArgument(String name, Class<TReturn> returnType, String expression)
            throws CompileException {
        super(name, returnType);
        this.expression = new Expression(expression);
    }

    protected Object fetch(ArgumentRegistry registry,
                           ArgumentProvider provider,
                           EvalSession session) throws InvocationTargetException {
        return this.expression.eval(registry, provider, session);
    }

    public boolean fromExpression() {
        return true;
    }
}
