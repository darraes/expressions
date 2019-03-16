package com.airbnb.payments.featuresengine;

public class CompilationException extends Exception {
    public CompilationException(String msgFmt, Object... args) {
        super(String.format(msgFmt, args));
    }

    public CompilationException(Exception cause, String msgFmt, Object... args) {
        super(String.format(msgFmt, args), cause);
    }
}
