package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.expressions.Expression;
import org.codehaus.commons.compiler.CompileException;

import java.lang.reflect.InvocationTargetException;

public class ExpressionArgument extends Argument {
    private Expression expression;

    public ExpressionArgument(String name, Class<?> returnType, String expression)
            throws CompileException {
        super(name, returnType);
        this.expression = new Expression(expression);
    }

    protected Object fetch(ArgumentRegistry registry,
                           ArgumentProvider provider,
                           EvalSession session) throws InvocationTargetException {
        return this.expression.eval(registry, provider, session);
    }

    public boolean fromExpression() {
        return true;
    }
}
