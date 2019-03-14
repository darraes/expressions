package com.airbnb.payments.featuresengine;

public class EvaluationException extends Exception {
    public EvaluationException(String msgFmt, Object... args) {
        super(String.format(msgFmt, args));
    }

    public EvaluationException(Exception cause, String msgFmt, Object... args) {
        super(String.format(msgFmt, args), cause);
    }
}
