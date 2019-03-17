package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import java.util.HashMap;

public class HashMapInputProvider implements IInputProvider {
    private HashMap<String, Object> arguments;

    public HashMapInputProvider() {
        this.arguments = new HashMap<>();
    }

    public HashMapInputProvider(HashMap<String, Object> args) {
        this.arguments = args;
    }

    /**
     * Adds a new name/value pair to the input provider
     *
     * @param name  The name
     * @param value The value
     */
    public void put(String name, Object value) throws CompilationException {
        // We work with a single namespace and arguments must be uniquely identified
        // by its name
        if (this.arguments.containsKey(name)) {
            throw new CompilationException(
                    "Input %s already registered", name);
        }

        this.arguments.put(name, value);
    }

    /**
     * Gets the value for the @name
     *
     * @param name The key for the value
     * @return The value stored under @name
     * @throws EvaluationException If a value under @name is not available
     */
    @Override
    public Object get(String name) throws EvaluationException {
        if (!this.exists(name)) {
            throw new EvaluationException("Argument %s doesn't exist", name);
        }

        return this.arguments.get(name);
    }

    /**
     * Checks if the given input exists
     *
     * @param name The name to check against
     * @return True if name exists. False otherwise.
     */
    @Override
    public boolean exists(String name) {
        return this.arguments.containsKey(name);
    }
}
