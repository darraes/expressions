package com.airbnb.payments.featuresengine;

public abstract class ArgumentBase<TReturn> {
    private String name;
    private Class<TReturn> returnType;

    public ArgumentBase(String name, Class<TReturn> returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public Class<TReturn> getReturnType() {
        return returnType;
    }

    public TReturn get(ArgumentProvider provider, EvalSession session) {
        Object result = this.fetch(provider, session);
        if (this.returnType.isInstance(result)
                || this.returnType.isAssignableFrom(result.getClass())) {
            return (TReturn) result;
        }
        return null; // TODO throw
    }

    protected abstract Object fetch(ArgumentProvider provider, EvalSession session);

    public abstract boolean fromExpression();
}
