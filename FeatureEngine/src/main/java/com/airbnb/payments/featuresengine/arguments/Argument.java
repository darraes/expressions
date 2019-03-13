package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;

import java.lang.reflect.InvocationTargetException;

public abstract class Argument {
    private String name;
    private Class<?> returnType;

    Argument(String name, Class<?> returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    String getName() {
        return name;
    }

    Class<?> getReturnType() {
        return returnType;
    }

    Object value(ArgumentRegistry registry,
                        ArgumentProvider provider,
                        EvalSession session) {
        try {
            Object result = this.fetch(registry, provider, session);
            if (this.returnType.isInstance(result)
                    || this.returnType.isAssignableFrom(result.getClass())) {
                return result;
            }
        } catch (InvocationTargetException e) {
            //TODO handle
        }
        return null; // TODO throw
    }

    protected abstract Object fetch(ArgumentRegistry registry,
                                    ArgumentProvider provider,
                                    EvalSession session) throws InvocationTargetException;

    abstract boolean fromExpression();
}
