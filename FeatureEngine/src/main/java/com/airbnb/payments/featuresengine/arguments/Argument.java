package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Getter
public abstract class Argument {
    // Name (or key) of the argument
    private String name;
    // Argument type
    private Class<?> returnType;
    // If the argument is cacheable
    private boolean cacheable;
    // If the given argument needs async fetching
    private boolean isAsync;

    /**
     * Map with primitive type and their boxed versions as key->value.
     * Used for type checking on the argument fetching.
     */
    private static final Map<Class<?>, Class<?>> primitives
            = new HashMap<>();

    /**
     * Initiates the type equivalence map
     */
    static {
        primitives.put(Boolean.class, boolean.class);
        primitives.put(Byte.class, byte.class);
        primitives.put(Character.class, char.class);
        primitives.put(Double.class, double.class);
        primitives.put(Float.class, float.class);
        primitives.put(Integer.class, int.class);
        primitives.put(Long.class, long.class);
        primitives.put(Short.class, short.class);

        primitives.put(boolean.class, Boolean.class);
        primitives.put(byte.class, Byte.class);
        primitives.put(char.class, Character.class);
        primitives.put(double.class, Double.class);
        primitives.put(float.class, Float.class);
        primitives.put(int.class, Integer.class);
        primitives.put(long.class, Long.class);
        primitives.put(short.class, Short.class);
    }

    /**
     * Constructor
     *
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param cacheable  If the argument, once computed, should be cached on further
     *                   fetches
     * @param isAsync    If the given argument needs async fetching
     */
    Argument(String name,
             Class<?> returnType,
             boolean cacheable,
             boolean isAsync) {
        this.name = name;
        this.returnType = returnType;
        this.cacheable = cacheable;
        this.isAsync = isAsync;

        if (this.isAsync && !this.cacheable) {
            throw new CompilationException(
                    "Async arguments must be cacheable. Argument %s is not",
                    this.getName());
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Argument)) {
            return false;
        }

        return this.name.equals(((Argument) that).getName());
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.name, this.returnType.getName());
    }

    /**
     * Gets the value of of the current argument. If the argument is cacheable, the
     * first call will cache the result and further calls will grab the result from
     * the session cache.
     *
     * @param session Session of the individual request
     * @return Result of the argument fetching
     */
    final Object value(EvalSession session) {
        if (this.isCacheable() && session.cache().contains(this.getName())) {
            return session.cache().get(this.getName());
        }

        Object result = this.fetch(session);
        return this.processResult(session, result);
    }

    /**
     * Gets the value of of the current argument. If the argument is cacheable, the
     * first call will cache the result and further calls will grab the result from
     * the session cache.
     *
     * @param session  Session of the individual request
     * @param executor Executor to run the fetching on
     * @return Result of the argument fetching
     */
    public final CompletableFuture<Object> valueAsync(
            EvalSession session, Executor executor) {
        CompletableFuture<Object> result = new CompletableFuture<>();
        CompletableFuture.runAsync(
                () -> {
                    try {
                        if (this.isCacheable()
                                && session.cache().contains(this.getName())) {
                            result.complete(session.cache().get(this.getName()));
                            return;
                        }

                        this.fetchAsync(session, executor)
                                .thenAccept((res) -> {
                                    result.complete(processResult(session, res));
                                })
                                .exceptionally((e) -> {
                                    result.completeExceptionally(e.getCause());
                                    return null;
                                });
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                }, executor);
        return result;
    }

    /**
     * Does the final type checking to make sure the argument's computed type matches
     * the expected argument type.
     *
     * @param session Caller evaluation session
     * @param result  Raw evaluation result
     * @return The final, processed, result
     */
    private Object processResult(EvalSession session, Object result) {
        if (result != null) {
            if (this.returnType.isInstance(result)
                    || this.returnType.isAssignableFrom(result.getClass())
                    || (primitives.containsKey(this.returnType)
                    && primitives.get(this.returnType).isInstance(result))) {
                if (this.isCacheable()) {
                    session.cache().put(this.getName(), result);
                }
                return result;
            } else {
                throw new EvaluationException(
                        "Argument %s (type: %s) is not assignable to"
                                + " expected (type: %s)",
                        this.getName(),
                        result.getClass(),
                        this.getReturnType());
            }
        } else {
            throw new EvaluationException(
                    "Argument %s not found", this.getName());
        }
    }

    /**
     * Does the actual fetching of the argument.
     *
     * @param session The current evaluation session
     * @return The actual argument value
     * @throws EvaluationException If anything goes wrong with the evaluation of the
     *                             value
     */
    protected abstract Object fetch(EvalSession session);

    /**
     * Does the actual fetching of the argument.
     *
     * @param session  The current evaluation session
     * @param executor Executor to run the fetching on
     * @return The actual argument value
     * @throws EvaluationException If anything goes wrong with the evaluation of the
     *                             value
     */
    protected abstract CompletableFuture<Object> fetchAsync(
            EvalSession session, Executor executor);

    /**
     * Returns all arguments, recursively, that this argument depends on
     */
    public abstract Set<Argument> dependencies();
}
