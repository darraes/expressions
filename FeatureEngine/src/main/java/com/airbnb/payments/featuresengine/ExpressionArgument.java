package com.airbnb.payments.featuresengine;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;

public class ExpressionArgument<TReturn> extends Argument<TReturn> {
    private String expressionText;
    private ExpressionEvaluator eval;

    public ExpressionArgument(String name, Class<TReturn> returnType, String expression)
            throws CompileException {
        super(name, returnType);

        this.expressionText = expression;

        this.eval = new ExpressionEvaluator();
        this.eval.setParameters(
                new String[]{"registry", "provider", "session"},
                new Class[]{ArgumentRegistry.class, ArgumentProvider.class, EvalSession.class});
        this.eval.cook(expression);
    }

    protected Object fetch(ArgumentRegistry registry,
                           ArgumentProvider provider,
                           EvalSession session) throws InvocationTargetException {
        return this.eval.evaluate(new Object[]{registry, provider, session});
    }

    public boolean fromExpression() {
        return true;
    }
}
