package com.airbnb.payments.featuresengine;

public class SimpleArgument<TReturn> extends Argument<TReturn> {
    public SimpleArgument(String name, Class<TReturn> returnType) {
        super(name, returnType);
    }

    protected Object fetch(ArgumentRegistry registry,
                           ArgumentProvider provider,
                           EvalSession session) {
        return provider.get(this.getName());
    }

    public boolean fromExpression() {
        return false;
    }
}
