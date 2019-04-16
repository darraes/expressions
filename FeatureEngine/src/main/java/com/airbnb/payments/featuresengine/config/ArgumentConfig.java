package com.airbnb.payments.featuresengine.config;

import lombok.Getter;

@Getter
public class ArgumentConfig extends ExpressionConfig {
    private String name;
    private boolean cacheable;

    public ArgumentConfig(String name,
                          String returnType) {
        this(name, returnType, true, false);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          boolean cacheable,
                          boolean isAsync) {
        this(name, returnType, null, cacheable, isAsync, new String[0]);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          String expression) {
        this(name, returnType, expression, true, false);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          String expression,
                          boolean cacheable,
                          boolean isAsync) {
        this(name, returnType, expression, cacheable, isAsync, new String[0]);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          String expression,
                          boolean cacheable,
                          boolean isAsync,
                          String[] dependencies) {
        super(expression, returnType, isAsync, dependencies);
        this.name = name;
        this.cacheable = cacheable;
    }
}
