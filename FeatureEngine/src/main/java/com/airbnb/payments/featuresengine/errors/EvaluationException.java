package com.airbnb.payments.featuresengine.errors;

/**
 * All exceptions that happen while the expression are getting evaluated during runtime
 * will be wrapped inside one of those.
 */
public class EvaluationException extends RuntimeException {
    public EvaluationException(String msgFmt, Object... args) {
        super(String.format(msgFmt, args));
    }

    public EvaluationException(Exception cause, String msgFmt, Object... args) {
        super(String.format(msgFmt, args), cause);
    }

    public EvaluationException(Throwable cause, String msgFmt, Object... args) {
        super(String.format(msgFmt, args), cause);
    }
}
