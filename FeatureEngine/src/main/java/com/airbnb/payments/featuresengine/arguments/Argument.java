package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import java.util.HashMap;
import java.util.Map;

public abstract class Argument {
    // Name (or key) of the argument
    private String name;
    // Argument type
    private Class<?> returnType;
    // If the argument is cacheable
    private boolean cacheable;

    /**
     * Map with primitive type and their boxed versions as key->value.
     * Used for type checking on the argument fetching.
     */
    private static final Map<Class<?>, Class<?>> primitiveEquivalenceMap
            = new HashMap<>();

    /**
     * Initiates the type equivalence map
     */
    static {
        primitiveEquivalenceMap.put(Boolean.class, boolean.class);
        primitiveEquivalenceMap.put(Byte.class, byte.class);
        primitiveEquivalenceMap.put(Character.class, char.class);
        primitiveEquivalenceMap.put(Double.class, double.class);
        primitiveEquivalenceMap.put(Float.class, float.class);
        primitiveEquivalenceMap.put(Integer.class, int.class);
        primitiveEquivalenceMap.put(Long.class, long.class);
        primitiveEquivalenceMap.put(Short.class, short.class);

        primitiveEquivalenceMap.put(boolean.class, Boolean.class);
        primitiveEquivalenceMap.put(byte.class, Byte.class);
        primitiveEquivalenceMap.put(char.class, Character.class);
        primitiveEquivalenceMap.put(double.class, Double.class);
        primitiveEquivalenceMap.put(float.class, Float.class);
        primitiveEquivalenceMap.put(int.class, Integer.class);
        primitiveEquivalenceMap.put(long.class, Long.class);
        primitiveEquivalenceMap.put(short.class, Short.class);
    }

    /**
     * Constructor
     *
     * @param name       The name of the argument
     * @param returnType The type of the argument
     * @param cacheable  If the argument, once computed, should be cached on further
     *                   fetches
     */
    public Argument(String name, Class<?> returnType, boolean cacheable) {
        this.name = name;
        this.returnType = returnType;
        this.cacheable = cacheable;
    }

    /**
     * @return the name (or key) of this argument
     */
    public String getName() {
        return name;
    }

    /**
     * @return the argument type
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Cacheable arguments, once fetched, will never be fetched again
     *
     * @return True if the argument is cacheable. False otherwise.
     */
    public boolean isCacheable() {
        return this.cacheable;
    }

    /**
     * Gets the value of of the current argument. If the argument is cacheable, the
     * first call will cache the result and further calls will grab the result from
     * the session cache.
     *
     * @param session Session of the individual request
     * @return Result of the argument fetching
     * @throws EvaluationException
     */
    final Object value(EvalSession session) throws EvaluationException {

        if (session.stack().contains(this.getName())) {
            throw new EvaluationException(
                    "Circular dependency found on argument %s",
                    this.getName());
        }

        session.stack().push(this.getName());

        if (this.isCacheable() && session.cache().contains(this.getName())) {
            return session.cache().get(this.getName());
        }

        Object result;
        try {
            result = this.fetch(session);
        } finally {
            String name = session.stack().pop();
        }

        if (result != null) {
            if (this.returnType.isInstance(result)
                    || this.returnType.isAssignableFrom(result.getClass())
                    || (primitiveEquivalenceMap.containsKey(this.returnType)
                    && primitiveEquivalenceMap.get(
                    this.returnType).isInstance(result))) {
                if (this.isCacheable()) {
                    session.cache().put(this.getName(), result);
                }
                return result;
            } else {
                throw new EvaluationException(
                        "Argument %s (type: %s) is not assignable to"
                                + " expected type %s",
                        this.getName(),
                        result.getClass(),
                        this.getReturnType());
            }
        }

        throw new EvaluationException(
                String.format("Argument %s not found", this.getName()));

    }

    /**
     * Does the actual fetching of the argument.
     *
     * @param session The current evaluation session
     * @return The actual argument value
     * @throws EvaluationException If anything goes wrong with the evaluation of the
     *                             value
     */
    protected abstract Object fetch(EvalSession session) throws EvaluationException;
}
