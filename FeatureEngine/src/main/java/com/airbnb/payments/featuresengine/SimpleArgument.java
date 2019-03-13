package com.airbnb.payments.featuresengine;

import java.lang.reflect.InvocationTargetException;

public class SimpleArgument<TReturn> extends Argument<TReturn> {
    public SimpleArgument(String name, Class<TReturn> returnType) {
        super(name, returnType);
    }

    protected Object fetch(ArgumentRegistry registry,
                           ArgumentProvider provider,
                           EvalSession session) throws InvocationTargetException {
        return provider.get(this.getName(), this.getReturnType());
    }

    public boolean fromExpression() {
        return false;
    }
}
