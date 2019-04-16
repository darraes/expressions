package com.airbnb.payments.featuresengine.config;

import lombok.Getter;

@Getter
public class ExpressionConfig {
    private String returnType;
    private String expression;
    private boolean isAsync;
    private String[] dependencies;

    public ExpressionConfig(String expression,
                            String returnType
    ) {
        this(expression, returnType, false, new String[0]);
    }

    public ExpressionConfig(String expression,
                            String returnType,
                            boolean isAsync) {
        this(expression, returnType, isAsync, new String[0]);
    }

    public ExpressionConfig(String expression,
                            String returnType,
                            String[] dependencies) {
        this(expression, returnType, false, dependencies);
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
}
