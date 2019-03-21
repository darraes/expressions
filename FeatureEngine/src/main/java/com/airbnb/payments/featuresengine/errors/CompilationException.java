package com.airbnb.payments.featuresengine.errors;

/**
 * All exceptions that happen while the expression are getting compiled and loaded into
 * memory will be wrapped inside one of those.
 */
public class CompilationException extends RuntimeException {
    public CompilationException(String msgFmt, Object... args) {
        super(String.format(msgFmt, args));
    }

    public CompilationException(Exception cause, String msgFmt, Object... args) {
        super(String.format(msgFmt, args), cause);
    }
}
