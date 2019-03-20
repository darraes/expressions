package com.airbnb.payments.featuresengine.config;

public class ArgumentConfig {
    private String name;
    private String returnType;
    private String expression;
    private boolean cacheable;
    private boolean isAsync;
    private String[] dependencies;

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
        this.name = name;
        this.returnType = returnType;
        this.expression = expression;
        this.cacheable = cacheable;
        this.isAsync = isAsync;
        this.dependencies = dependencies;
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

    public String[] getDependencies() {
        return dependencies;
    }
}
