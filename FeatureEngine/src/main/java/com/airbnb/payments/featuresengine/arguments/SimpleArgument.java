package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;

/**
 * Represents an |Argument| that is directed provided by an IArgumentProvider.
 */
public class SimpleArgument extends Argument {
    /**
     * See |Argument| class for details
     */
    public SimpleArgument(String name, Class<?> returnType) {
        this(name, returnType, true);
    }

    /**
     * See |Argument| class for details
     */
    public SimpleArgument(String name, Class<?> returnType, boolean cacheable) {
        super(name, returnType, cacheable);
    }

    /**
     * Fetches the argument from the IArgumentProvider provided by the caller
     */
    @Override
    protected final Object fetch(EvalSession session) throws EvaluationException {
        if (!session.arguments().exists(this.getName())) {
            throw new EvaluationException(
                    "Argument %s not found on argument provider", this.getName());
        }

        return session.arguments().get(this.getName());
    }
}
