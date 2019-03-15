package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;
import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.IArgumentProvider;
import org.codehaus.commons.compiler.CompileException;

/**
 * Those expressions can be accessed from any other expression using their names.
 * Eg.: expName1 = 1 + 2
 * expName2 = Math.PI
 * expName3 = $expName1 * $expName2
 */
public class NamedExpression extends Argument {

    // The expression that evaluates to this argument
    private Expression expression;

    /**
     * Constructor
     *
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param expression The computing expression that evaluates to the desired value
     * @throws CompileException Thrown if the expression can't be compiled
     */
    public NamedExpression(String name, Class<?> returnType, String expression)
            throws CompileException {
        this(name, returnType, expression, true);
    }

    /**
     * Constructor
     *
     * @param name       The name of the expression
     * @param returnType The type of the expression
     * @param expression The computing expression that evaluates to the desired value
     * @param cacheable  If the expression, once computed, should be cached on further fetches
     *                   for this particular session
     * @throws CompileException Thrown if the expression can't be compiled
     */
    public NamedExpression(String name, Class<?> returnType, String expression, boolean cacheable)
            throws CompileException {
        super(name, returnType, cacheable);
        this.expression = new Expression(expression, returnType);
    }

    /**
     * Fetches the value by actually computing the evaluation of the compiled expression
     */
    @Override
    protected Object fetch(EvalSession session) throws EvaluationException {
        return this.expression.eval(session);
    }
}
