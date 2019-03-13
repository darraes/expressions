package com.airbnb.payments.featuresengine;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

public class ExpressionArgument<TReturn> extends ArgumentBase<TReturn> {
    private String expressionText;
    private ExpressionEvaluator eval;

    public ExpressionArgument(String name, Class<TReturn> returnType, String expression)
            throws CompileException {
        super(name, returnType);

        this.eval = new ExpressionEvaluator();
        this.eval.setParameters(
                new String[]{"provider", "session"},
                new Class[]{int.class, int.class});
        this.eval.cook(expression);
    }

    protected Object fetch(ArgumentProvider provider, EvalSession session) {
        return provider.get(this.getName(), this.getReturnType());
    }

    public boolean fromExpression() {
        return true;
    }
}
