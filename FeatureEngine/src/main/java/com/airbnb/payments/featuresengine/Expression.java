package com.airbnb.payments.featuresengine;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;

class Expression {
    private String expressionText;
    private ExpressionEvaluator eval;

    Expression(String expression)
            throws CompileException {
        this.expressionText = expression;

        this.eval = new ExpressionEvaluator();
        this.eval.setParameters(
                new String[]{"registry", "provider", "session"},
                new Class[]{ArgumentRegistry.class, ArgumentProvider.class, EvalSession.class});
        this.eval.cook(expression);
    }

    public String getExpressionText() {
        return expressionText;
    }

    Object eval(ArgumentRegistry registry,
                ArgumentProvider provider,
                EvalSession session) throws InvocationTargetException {
        return this.eval.evaluate(new Object[]{registry, provider, session});
    }
}
