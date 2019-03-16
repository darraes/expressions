package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvaluationException;

/**
 * All user input will be provided to the engine via an implementation of this interface
 * <p>
 * The most simple way to implement this interface would be a HashMap. However users
 * can choose to have arguments directly provided from DBs, files, system calls...
 */
public interface IInputProvider {
    /**
     * Gets the object mapping to the @key
     *
     * @param name The key for the value
     * @return The object if any
     * @throws EvaluationException If argument doesn't exist
     */
    Object get(String name) throws EvaluationException;

    /**
     * Checks if the given input exists
     *
     * @param name The name to check against
     * @return True if it exists. False otherwise.
     */
    boolean exists(String name);
}
