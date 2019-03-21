package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.expressions.Expression;

import java.util.concurrent.CompletableFuture;
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

    /**
     * Constructor
     *
     * @param name       The name of the expression
     * @param expression The computing expression that evaluates to the desired value
     * @param returnType The type of the expression
     * @param cacheable  If the expression, once computed, should be cached on further
     *                   fetches for this particular session
     * @throws CompilationException Thrown if the expression can't be compiled
     */
    NamedExpression(String name,
                    Expression expression,
                    Class<?> returnType,
                    boolean cacheable,
                    boolean isAsync) {
        super(name, returnType, cacheable, isAsync);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    /**
     * Fetches the value by actually computing the evaluation of the compiled
     * expression
     */
    @Override
    protected Object fetch(EvalSession session) {
        return this.expression.eval(session);
    }

    /**
     * Fetches the value by actually computing the evaluation of the compiled
     * expression
     */
    protected CompletableFuture<Object> fetchAsync(
            EvalSession session, Executor executor) {
        return this.expression.evalAsync(session, executor);
    }
}
