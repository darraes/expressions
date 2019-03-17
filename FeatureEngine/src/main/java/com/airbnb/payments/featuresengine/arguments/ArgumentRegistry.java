package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import java.util.HashMap;

/**
 * Single access point to get the value of any argument.
 * <p>
 * All arguments of the engine must be registered with the registry and expressions
 * will further only access the arguments via registry.
 */
public class ArgumentRegistry {
    private HashMap<String, Argument> arguments;

    public ArgumentRegistry() {
        this.arguments = new HashMap<>();
    }

    /**
     * Registers the argument within the registry.
     *
     * @param argument The argument to be registered
     * @throws CompilationException If an argument with the same name is already
     *                              registered
     */
    public void register(Argument argument) throws CompilationException {
        // We work with a single namespace and arguments must be uniquely identified
        // by its name
        if (this.exists(argument.getName())) {
            throw new CompilationException(
                    "Argument %s already registered", argument.getName());
        }

        this.arguments.put(argument.getName(), argument);
    }

    /**
     * Checks if an argument with the given name is already registered.
     *
     * @param name The name to be checked
     * @return True if name is registered. False otherwise.
     */
    public boolean exists(String name) {
        return this.arguments.containsKey(name);
    }

    /**
     * Fetches (or possibly computes) the value for the argument with the given @name
     *
     * @param name    Name of the argument
     * @param session Current evaluation session
     * @return The argument value
     * @throws EvaluationException If the given argument is not registered or if the
     *                             argument's fetching/computing fails
     */
    public Object value(String name,
                        EvalSession session) throws EvaluationException {
        if (!this.exists(name)) {
            throw new EvaluationException("Argument %s not registered", name);
        }

        return this.arguments.get(name).value(session);
    }
}
