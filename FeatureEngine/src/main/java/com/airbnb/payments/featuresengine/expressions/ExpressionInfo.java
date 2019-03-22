package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;

import java.util.List;

class ExpressionInfo {
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

    String getID() {
        return id;
    }

    String getExpression() {
        return expression;
    }

    String getSourceExpression() {
        return sourceExpression;
    }

    Class<?> getReturnType() {
        return returnType;
    }

    List<Argument> getAccessedArguments() {
        return accessedArguments;
    }

    boolean isAsync() {
        return isAsync;
    }

    String[] getDependencies() {
        return dependencies;
    }
}
