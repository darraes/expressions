package com.airbnb.payments.featuresengine.config;

public class ArgumentConfig {
    private String name;
    private String returnType;
    private boolean cacheable;
    private boolean isAsync;
    private String expression;

    public ArgumentConfig(String name,
                          String returnType,
                          boolean cacheable,
                          boolean isAsync) {
        this.name = name;
        this.returnType = returnType;
        this.cacheable = cacheable;
        this.isAsync = isAsync;
    }

    public ArgumentConfig(String name,
                          String returnType,
                          String expression,
                          boolean cacheable,
                          boolean isAsync) {
        this(name, returnType, cacheable, isAsync);
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public String getExpression() {
        return this.expression;
    }
}
