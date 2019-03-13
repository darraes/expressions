package com.airbnb.payments.featuresengine;

import java.lang.reflect.InvocationTargetException;

public abstract class Argument<TReturn> {
    private String name;
    private Class<TReturn> returnType;

    Argument(String name, Class<TReturn> returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    String getName() {
        return name;
    }

    Class<TReturn> getReturnType() {
        return returnType;
    }

    @SuppressWarnings(value = "unchecked")
    public TReturn get(ArgumentRegistry registry,
                       ArgumentProvider provider,
                       EvalSession session) {
        try {
            Object result = this.fetch(registry, provider, session);
            if (this.returnType.isInstance(result)
                    || this.returnType.isAssignableFrom(result.getClass())) {
                return (TReturn) result;
            }
        } catch (InvocationTargetException e) {
            System.out.println("InvocationTargetException");
        }
        return null; // TODO throw
    }

    protected abstract Object fetch(ArgumentRegistry registry,
                                    ArgumentProvider provider,
                                    EvalSession session) throws InvocationTargetException;

    abstract boolean fromExpression();
}
