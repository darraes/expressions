package com.airbnb.payments.featuresengine.config;

public class ExpressionConfig {
    private String returnType;
    private String expression;
    private boolean isAsync;
    private String[] dependencies;

    public ExpressionConfig(String expression,
                            String returnType
    ) {
        this(expression,
                returnType,
                false,
                new String[0]);
    }

    public ExpressionConfig(String name,
                            String expression,
                            String returnType,
                            boolean isAsync) {
        this(expression,
                returnType,
                isAsync,
                new String[0]);
    }

    public ExpressionConfig(String expression,
                            String returnType,
                            String[] dependencies) {
        this(expression,
                returnType,
                false,
                dependencies);
    }

    public ExpressionConfig(String expression,
                            String returnType,
                            boolean isAsync,
                            String[] dependencies) {
        this.returnType = returnType;
        this.expression = expression;
        this.isAsync = isAsync;
        this.dependencies = dependencies;
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
