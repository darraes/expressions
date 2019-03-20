package com.airbnb.payments.featuresengine.errors;

public class EvaluationException extends RuntimeException {
    public EvaluationException(String msgFmt, Object... args) {
        super(String.format(msgFmt, args));
    }

    public EvaluationException(Exception cause, String msgFmt, Object... args) {
        super(String.format(msgFmt, args), cause);
    }
}
