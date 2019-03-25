package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds on to all information (metadata) specific to a single expression.
 * Anything we want to know about the expression, should be here.
 */
public class ExpressionMetadata {
    // Unique id given to each individual expression
    private String id;
    // The final expression, after the argument pre-processing
    private String expression;
    // The original user inputted expression
    private String sourceExpression;
    // The type the expression evaluates to
    private Class<?> returnType;
    // The arguments **directly** accessed by the expression
    private Set<Argument> accessedArguments;
    // All arguments in the nested dependencies used on this expression (recursively)
    private Set<Argument> dependentArguments;
    // All class imports used on this expression
    private String[] imports;
    // If the expression is asynchronous
    private boolean isAsync;
    // If any of the expression top level arguments have a common argument dependency
    // down into their argument chain
    private boolean commonDependency;

    /**
     * @param id
     * @param sourceExpression
     * @param expression
     * @param returnType
     * @param accessedArguments
     * @param isAsync
     * @param imports
     */
    ExpressionMetadata(String id,
                       String sourceExpression,
                       String expression,
                       Class<?> returnType,
                       Set<Argument> accessedArguments,
                       boolean isAsync,
                       String[] imports) {
        this.id = id;
        this.sourceExpression = sourceExpression;
        this.expression = expression;
        this.returnType = returnType;
        this.accessedArguments = accessedArguments;
        this.isAsync = isAsync;
        this.imports = imports;

        this.loadDependencies();
    }

    /**
     * The expression uniquely generated ID.
     *
     * @return The ID.
     */
    public String getID() {
        return id;
    }

    /**
     * The final expression, after the argument pre-processing or any other
     * pre-processing
     *
     * @return The expression text
     */
    public String getExpression() {
        return expression;
    }

    /**
     * The original user inputted expression.
     *
     * @return The source expression text
     */
    public String getSrcExpression() {
        return sourceExpression;
    }

    /**
     * The type the expression evaluates to.
     *
     * @return The class object for the expression type
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * The arguments <b>directly</b> accessed by the expression
     *
     * @return Set with all unique top top level arguments
     */
    public Set<Argument> getAccessedArguments() {
        return accessedArguments;
    }

    /**
     * All arguments in the nested dependencies used on this expression (recursively).
     *
     * @return Set with all unique arguments and their child arguments recursively
     */
    public Set<Argument> getDependentArguments() {
        return dependentArguments;
    }

    /**
     * If the expression is asynchronous
     *
     * @return True if async, false otherwise.
     */
    public boolean isAsync() {
        return isAsync;
    }

    /**
     * All classes imports used on this expression
     *
     * @return Array with all imports, one per class.
     */
    public String[] getImports() {
        return imports;
    }

    /**
     * If any of the expression top level arguments have a common argument dependency
     * down into their argument chain.
     *
     * @return True if there are common dependencies on chain, false otherwise
     */
    public boolean hasCommonDependencies() {
        return this.commonDependency;
    }

    /**
     * Does all the work to track common dependencies and ready this metadata object
     * for faster dependencies queries.
     */
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
