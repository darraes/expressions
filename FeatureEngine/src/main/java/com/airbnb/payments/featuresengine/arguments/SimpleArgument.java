package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;

public class SimpleArgument extends Argument {
    public SimpleArgument(String name, Class<?> returnType) {
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
