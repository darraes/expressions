package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;
import com.airbnb.payments.featuresengine.expressions.Expression;
import org.codehaus.commons.compiler.CompileException;

/**
 * Derived arguments are not access from an argument provided. They are computed from
 * an expression if necessary.
 */
public class DerivedArgument extends Argument {

    // The expression that evaluates to this argument
    private Expression expression;

    /**
     * Constructor
     *
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param expression The computing expression that evaluates to the argument
     * @throws CompileException Thrown if the expression can't be compiled
     */
    public DerivedArgument(String name, Class<?> returnType, String expression)
            throws CompileException {
        this(name, returnType, expression, true);
    }

    /**
     * Constructor
     *
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param expression The computing expression that evaluates to the argument
     * @param cacheable  If the argument, once computed, should be cached on further fetches
     * @throws CompileException Thrown if the expression can't be compiled
     */
    public DerivedArgument(String name, Class<?> returnType, String expression, boolean cacheable)
            throws CompileException {
        super(name, returnType, cacheable);
        this.expression = new Expression(expression, returnType);
    }

    /**
     * See |Argument| class for details
     */
    protected Object fetch(ArgumentRegistry registry,
                           IArgumentProvider provider,
                           EvalSession session) throws EvaluationException {
        return this.expression.eval(registry, provider, session);
    }

    /**
     * See |Argument| class for details
     */
    public boolean derived() {
        return true;
    }
}
