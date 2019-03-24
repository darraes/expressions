package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;

import java.util.List;
import java.util.Set;

public class ExpressionInfo {
    private String id;
    private String expression;
    private String sourceExpression;
    private Class<?> returnType;
    private List<Argument> accessedArguments;
    private Set<Argument> dependentArguments;
    private boolean isAsync;
    private String[] imports;

    ExpressionInfo(String id,
                   String sourceExpression,
                   String expression,
                   Class<?> returnType,
                   List<Argument> accessedArguments,
                   boolean isFromScript,
                   String[] imports) {
        this.id = id;
        this.sourceExpression = sourceExpression;
        this.expression = expression;
        this.returnType = returnType;
        this.accessedArguments = accessedArguments;
        this.isAsync = isFromScript;
        this.imports = imports;

        this.loadDependencies();
    }

    public String getID() {
        return id;
    }

    public String getExpression() {
        return expression;
    }

    public String getSrcExpression() {
        return sourceExpression;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public List<Argument> getAccessedArguments() {
        return accessedArguments;
    }

    public Set<Argument> getDependentArguments() {
        return dependentArguments;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public String[] getImports() {
        return imports;
    }

    public boolean hasIntersectingChains() {
        // TODO Implement
        return true;
    }

    private void loadDependencies() {
        // TODO Implement
    }
}
