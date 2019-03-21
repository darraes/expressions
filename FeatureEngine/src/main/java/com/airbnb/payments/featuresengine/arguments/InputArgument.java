package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Represents an |Argument| that is directed provided by an IInputProvider.
 * Those are most likely the parameters provided by the user of the engine.
 */
public class InputArgument extends Argument {

    /**
     * See |Argument| class for details
     */
    InputArgument(
            String name, Class<?> returnType, boolean cacheable, boolean isAsync) {
        super(name, returnType, cacheable, isAsync);
    }

    /**
     * Fetches the argument from the IInputProvider provided by the caller
     */
    @Override
    protected final Object fetch(EvalSession session) {
        if (!session.inputs().exists(this.getName())) {
            throw new EvaluationException(
                    "Argument %s not found on argument provider", this.getName());
        }

        return session.inputs().get(this.getName());
    }

    /**
     * Fetches the value by actually computing the evaluation of the compiled
     * expression
     */
    protected CompletableFuture<Object> fetchAsync(
            EvalSession session, Executor executor) {
        if (!session.inputs().exists(this.getName())) {
            throw new EvaluationException(
                    "Argument %s not found on argument provider", this.getName());
        }

        // We don't support user provided async arguments, Those must always be an
        // expression.
        // We wrap the sync fetch inside a suppleAsync call to make this method work
        // on all scenarios
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return session.inputs().get(this.getName());
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, executor);
    }
}
