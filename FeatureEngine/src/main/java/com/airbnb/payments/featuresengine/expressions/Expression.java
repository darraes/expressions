package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;
import com.airbnb.payments.featuresengine.arguments.ArgumentProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;

public class Expression {
    // Original expression text
    private String expressionText;
    // Actual expression evaluator
    private ExpressionEvaluator eval;

    public Expression(String expression)
            throws CompileException {
        this.expressionText = expression;
        this.eval = new ExpressionEvaluator();

        // All expressions will only feed of the arguments therefore all we need are the argument
        // registry, the argument provider and the evaluation session
        this.eval.setParameters(
                new String[]{"registry", "provider", "session"},
                new Class[]{ArgumentRegistry.class, ArgumentProvider.class, EvalSession.class});
        this.eval.setThrownExceptions(new Class[]{EvaluationException.class});

        // TODO (darraes) Check if when the expression gets destructed this compilation doesn't leak
        // Leave the expression already compiled for faster performance on evaluation
        this.eval.cook(expression);
    }

    /**
     * Getter.
     *
     * @return The original expression text before compiling it
     */
    public final String getExpressionText() {
        return expressionText;
    }

    /**
     * Evaluates the final value of the expression and returns that value.
     * <p>
     * All arguments must be registered int the [@registry] object and all user
     * inputted arguments must be available on the [@provider].
     *
     * [@session] will record all events and be used as cache to prevent re-computations.
     * <p>
     *
     * @param registry The engine's argument registry
     * @param provider The caller's argument provider
     * @param session  Session of the individual request
     *
     * @return Result of the expression computation
     */
    public final Object eval(ArgumentRegistry registry,
                             ArgumentProvider provider,
                             EvalSession session) throws EvaluationException {
        try {
            return this.eval.evaluate(new Object[]{registry, provider, session});
        } catch (InvocationTargetException e) {
            throw new EvaluationException(
                    String.format("Error evaluation expression %s", this.getExpressionText()), e);
        }

    }
}
