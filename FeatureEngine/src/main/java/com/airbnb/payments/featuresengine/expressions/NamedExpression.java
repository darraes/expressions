package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import com.airbnb.payments.featuresengine.arguments.Argument;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Those expressions can be accessed from any other expression using their names.
 * Eg.: expName1 = 1 + 2
 * expName2 = Math.PI
 * expName3 = $expName1 * $expName2
 * <p>
 * Since they have "names", they can be used in the same way as user provided arguments.
 */
public class NamedExpression extends Argument {

    // The expression that evaluates to this argument
    private Expression expression;
    // Text of the expression
    private String expressionText;

    /**
     * Constructor
     *
     * @param name       The name of the expression
     * @param returnType The type of the expression
     * @param expression The computing expression that evaluates to the desired value
     * @param cacheable  If the expression, once computed, should be cached on further
     *                   fetches for this particular session
     * @throws CompilationException Thrown if the expression can't be compiled
     */
    public NamedExpression(String name,
                           Class<?> returnType,
                           String expression,
                           boolean cacheable,
                           boolean isAsync)
            throws CompilationException {
        super(name, returnType, cacheable, isAsync);
        this.expressionText = expression;
        this.expression = new Expression(expression, returnType);
    }

    /**
     * Gets the original expression text
     */
    public String getExpressionText() {
        return this.expressionText;
    }

    /**
     * Fetches the value by actually computing the evaluation of the compiled
     * expression
     */
    @Override
    protected Object fetch(EvalSession session) throws EvaluationException {
        return this.expression.eval(session);
    }

    /**
     * Fetches the value by actually computing the evaluation of the compiled
     * expression
     */
    protected CompletableFuture<Object> fetchAsync(
            EvalSession session, Executor executor) throws EvaluationException {
        return this.expression.evalAsync(session, executor);
    }
}
