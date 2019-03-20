package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;

import java.util.List;

public class ExpressionInfo {
    private String name;
    private String expression;
    private Class<?> returnType;
    private List<Argument> accessedArguments;
    private boolean isFromScript;
    private String[] dependencies;

    public ExpressionInfo(String name,
                          String expression,
                          Class<?> returnType,
                          List<Argument> accessedArguments,
                          boolean isFromScript,
                          String[] dependencies) {
        this.name = name;
        this.expression = expression;
        this.returnType = returnType;
        this.accessedArguments = accessedArguments;
        this.isFromScript = isFromScript;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public List<Argument> getAccessedArguments() {
        return accessedArguments;
    }

    public void setAccessedArguments(List<Argument> accessedArguments) {
        this.accessedArguments = accessedArguments;
    }

    public boolean isFromScript() {
        return isFromScript;
    }

    public void setFromScript(boolean fromScript) {
        isFromScript = fromScript;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }
}
