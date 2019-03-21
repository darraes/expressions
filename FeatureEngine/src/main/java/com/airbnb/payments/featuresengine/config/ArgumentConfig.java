package com.airbnb.payments.featuresengine.config;

public class ArgumentConfig extends ExpressionConfig {
    private String name;
    private boolean cacheable;

    public ArgumentConfig(String name,
                          String returnType) {
        this(name,
                returnType,
                null,
                true,
                false,
                new String[0]);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          boolean cacheable,
                          boolean isAsync) {
        this(name,
                returnType,
                null,
                cacheable,
                isAsync,
                new String[0]);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          String expression) {
        this(name,
                returnType,
                expression,
                true,
                false,
                new String[0]);
    }

    public ArgumentConfig(String name,
                          String returnType,
                          String expression,
                          boolean cacheable,
                          boolean isAsync) {
        this(name,
                returnType,
                expression,
                cacheable,
                isAsync,
                new String[0]);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }
}
