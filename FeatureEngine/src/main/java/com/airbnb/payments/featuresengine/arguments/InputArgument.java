package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;

/**
 * Represents an |Argument| that is directed provided by an IInputProvider.
 * Those are most likely the parameters provided by the user of the engine.
 */
public class InputArgument extends Argument {

    /**
     * See |Argument| class for details
     */
    public InputArgument(String name, Class<?> returnType, boolean cacheable) {
        super(name, returnType, cacheable);
    }

    /**
     * Fetches the argument from the IInputProvider provided by the caller
     */
    @Override
    protected final Object fetch(EvalSession session) throws EvaluationException {
        if (!session.inputs().exists(this.getName())) {
            throw new EvaluationException(
                    "Argument %s not found on argument provider", this.getName());
        }

        return session.inputs().get(this.getName());
    }
}
