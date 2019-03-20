package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import org.codehaus.commons.compiler.CompileException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
    public void register(Argument argument) {
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
    public Object value(String name, EvalSession session) {
        if (!this.exists(name)) {
            throw new EvaluationException("Argument %s not registered", name);
        }

        return this.arguments.get(name).value(session);
    }

    /**
     * Fetches (or possibly computes) the value for the argument with the given @name
     *
     * @param name    Name of the argument
     * @param session Current evaluation session
     * @param executor Executor to run the fetching on
     * @return The argument value
     * @throws EvaluationException If the given argument is not registered or if the
     *                             argument's fetching/computing fails
     */
    public CompletableFuture<Object> valueAsync(
            String name, EvalSession session, Executor executor) {
        if (!this.exists(name)) {
            throw new EvaluationException("Argument %s not registered", name);
        }

        return this.arguments.get(name).valueAsync(session, executor);
    }

    /**
     * Gets the Argument object, not its value
     * @param name The name of the argument
     */
    public Argument get(String name) {
        if (!this.exists(name)) {
            throw new CompilationException("Argument %s not registered", name);
        }

        return this.arguments.get(name);
    }

    /**
     *
     * @param arguments
     * @param session
     * @param executor
     * @return
     */
    public CompletableFuture<Map<String, Object>> allValuesAsync(
            String[] arguments, EvalSession session, Executor executor) {
        Map<String, CompletableFuture<Object>> futures
                = new HashMap<>(arguments.length);

        for (String argument : arguments) {
            futures.put(
                    argument,
                    this.valueAsync(argument, session, executor));
        }

        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(
                        futures.values().toArray(new CompletableFuture[0]));

        return allDoneFuture.thenApply(
                (v) -> futures
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                x -> x.getKey(),
                                x -> x.getValue().join()))

        );
    }
}
