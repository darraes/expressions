package com.airbnb.payments.featuresengine;

public class SimpleArgument<TReturn> extends ArgumentBase<TReturn> {
    public SimpleArgument(String name, Class<TReturn> returnType) {
        super(name, returnType);
    }

    protected Object fetch(ArgumentProvider provider, EvalSession session) {
        return provider.get(this.getName(), this.getReturnType());
    }

    public boolean fromExpression() {
        return false;
    }
}
