package com.airbnb.payments.featuresengine;

public class EvaluationException extends Exception {
    public EvaluationException(String msg) {
        super(msg);
    }

    public EvaluationException(String msg, Exception cause) {
        super(msg, cause);
    }
}
