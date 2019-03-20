package com.airbnb.payments.featuresengine.config;

public class ExpressionConfig {
    private String name;
    private String returnType;
    private String expression;
    private boolean isAsync;
    private String[] dependencies;

    public ExpressionConfig(String name,
                            String expression,
                            String returnType
                            ) {
        this(name,
                expression,
                returnType,
                false,
                new String[0]);
    }

    public ExpressionConfig(String name,
                            String expression,
                            String returnType,
                            boolean isAsync) {
        this(name,
                expression,
                returnType,
                isAsync,
                new String[0]);
    }

    public ExpressionConfig(String name,
                            String expression,
                            String returnType,
                            String[] dependencies) {
        this(name,
                expression,
                returnType,
                false,
                dependencies);
    }

    public ExpressionConfig(String name,
                            String expression,
                            String returnType,
                            boolean isAsync,
                            String[] dependencies) {
        this.name = name;
        this.returnType = returnType;
        this.expression = expression;
        this.isAsync = isAsync;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }
}
