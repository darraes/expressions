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
     * See |Argument| class for details
     */
    protected final Object fetch(ArgumentRegistry registry,
                                 IArgumentProvider provider,
                                 EvalSession session) throws EvaluationException {
        if (!provider.exists(this.getName())) {
            throw new EvaluationException(
                    "Argument %s not found on argument provider", this.getName());
        }

        return provider.get(this.getName());
    }

    /**
     * See |Argument| class for details
     */
    public boolean derived() {
        return false;
    }
}
