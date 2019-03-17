package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;

public class Expression {
    // Original expression text
    private String expressionText;
    // The class type this expression evaluates to
    private Class<?> expressionType;
    // Actual expression evaluator
    private ExpressionEvaluator eval;

    public Expression(String expression, Class<?> type)
            throws CompilationException {
        this.expressionText = expression;
        this.expressionType = type;
        this.eval = new ExpressionEvaluator();

        // All expressions will only feed of the arguments therefore all we need are
        // the argument registry, the argument provider and the evaluation session
        this.eval.setParameters(
                new String[]{"session"},
                new Class[]{EvalSession.class});
        this.eval.setThrownExceptions(new Class[]{EvaluationException.class});

        // TODO Check when the expression gets destructed the compilation doesn't leak
        // Leave the expression already compiled for faster performance on evaluation
        try {
            this.eval.cook(expression);
        } catch (CompileException e) {
            throw new CompilationException(e, "Failed compiling %s", expression);
        }
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
     * @return The class type this expression evaluates to
     */
    public Class<?> getExpressionType() {
        return expressionType;
    }

    /**
     * Evaluates the final value of the expression and returns that value.
     * <p>
     * All arguments must be registered int the [@session.registry()] object and all
     * user inputted arguments must be available on the [@session.provider()].
     * <p>
     * [@session] will record all events and be used as cache to prevent
     * re-computations.
     * <p>
     *
     * @param session  Session of the individual request
     * @return Result of the expression computation
     */
    public final Object eval(EvalSession session) throws EvaluationException {
        try {
            return this.eval.evaluate(new Object[]{session});
        } catch (InvocationTargetException e) {
            throw new EvaluationException(
                    e, "Error evaluation expression %s", this.getExpressionText());
        }

    }
}
