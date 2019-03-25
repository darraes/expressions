package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpressionMetadata {
    private String id;
    private String expression;
    private String sourceExpression;
    private Class<?> returnType;
    private List<Argument> accessedArguments;
    private Set<Argument> dependentArguments;
    private boolean commonDependency;
    private boolean isAsync;
    private String[] imports;

    ExpressionMetadata(String id,
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

    public boolean hasCommonDependencies() {
        return this.commonDependency;
    }

    private void loadDependencies() {
        this.commonDependency = false;
        this.dependentArguments = new HashSet<>();

        for (Argument argument : this.accessedArguments) {
            if (!Sets.intersection(
                    this.dependentArguments,
                    argument.dependencies()).isEmpty()) {
                this.commonDependency = true;
            }

            this.dependentArguments.add(argument);
            this.dependentArguments.addAll(argument.dependencies());
        }
    }
}
