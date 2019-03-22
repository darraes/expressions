package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;

import java.util.List;

public class ExpressionInfo {
    private String id;
    private String expression;
    private String sourceExpression;
    private Class<?> returnType;
    private List<Argument> accessedArguments;
    private boolean isAsync;
    private String[] dependencies;

    ExpressionInfo(String id,
                   String sourceExpression,
                   String expression,
                   Class<?> returnType,
                   List<Argument> accessedArguments,
                   boolean isFromScript,
                   String[] dependencies) {
        this.id = id;
        this.sourceExpression = sourceExpression;
        this.expression = expression;
        this.returnType = returnType;
        this.accessedArguments = accessedArguments;
        this.isAsync = isFromScript;
        this.dependencies = dependencies;
    }

    public String getID() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public String getSourceExpression() {
        return sourceExpression;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public List<Argument> getAccessedArguments() {
        return accessedArguments;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public String[] getDependencies() {
        return dependencies;
    }
}
