package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;
import org.codehaus.commons.compiler.CompileException;

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
            = new HashMap<>(32);

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
     * @param cacheable  If the argument, once computed, should be cached on further fetches
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
     * @param registry The engine's argument registry
     * @param provider The caller's argument provider
     * @param session  Session of the individual request
     * @return Result of the argument fetching
     * @throws EvaluationException
     */
    final Object value(ArgumentRegistry registry,
                       IArgumentProvider provider,
                       EvalSession session) throws EvaluationException {
        if (this.isCacheable() && session.inCache(this.getName())) {
            return session.getFromCache(this.getName());
        }

        Object result = this.fetch(registry, provider, session);

        if (result != null) {
            if (this.returnType.isInstance(result)
                    || this.returnType.isAssignableFrom(result.getClass())
                    || (primitiveEquivalenceMap.containsKey(this.returnType)
                    && primitiveEquivalenceMap.get(this.returnType).isInstance(result))) {
                if (this.isCacheable()) {
                    session.putInCache(this.getName(), result);
                }

                return result;
            } else {
                throw new EvaluationException(
                        String.format(
                                "Argument %s (type: %s) is not assinable to expected type %s",
                                this.getName(), result.getClass(), this.getReturnType()));
            }
        }

        throw new EvaluationException(
                String.format("Argument %s not found", this.getName()));
    }

    /**
     * Does the actual fetching of the argument.
     *
     * @param registry Registry where all argument information is stored
     * @param provider User provided argument provider object. Used to fetch raw arguments.
     * @param session  The current evaluation session
     * @return The actual argument value
     * @throws EvaluationException
     */
    protected abstract Object fetch(ArgumentRegistry registry,
                                    IArgumentProvider provider,
                                    EvalSession session) throws EvaluationException;

    /**
     * If this argument is derived from an expression.
     *
     * @return True if the current instance is derived. False otherwise.
     */
    public abstract boolean derived();
}
