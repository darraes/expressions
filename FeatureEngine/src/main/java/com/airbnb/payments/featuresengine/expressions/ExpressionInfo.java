package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;

import java.util.List;

public class ExpressionInfo {
    private String id;
    private String expression;
    private Class<?> returnType;
    private List<Argument> accessedArguments;
    private boolean isFromScript;
    private String[] dependencies;

    ExpressionInfo(String id,
                   String expression,
                   Class<?> returnType,
                   List<Argument> accessedArguments,
                   boolean isFromScript,
                   String[] dependencies) {
        this.id = id;
        this.expression = expression;
        this.returnType = returnType;
        this.accessedArguments = accessedArguments;
        this.isFromScript = isFromScript;
        this.dependencies = dependencies;
    }

    public String getID() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public List<Argument> getAccessedArguments() {
        return accessedArguments;
    }

    public boolean isFromScript() {
        return isFromScript;
    }

    public String[] getDependencies() {
        return dependencies;
    }
}
