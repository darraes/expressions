package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;

import java.util.HashMap;
import java.util.Map;

public abstract class Argument {
    private String name;
    private Class<?> returnType;
    private boolean cacheable;

    /**
     * Map with primitive type and their boxed versions as key->value.
     * Used for type checking on the argument fetching.
     */
    private static final Map<Class<?>, Class<?>> primitiveEquivalenceMap
            = new HashMap<>(32);

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

    public Argument(String name, Class<?> returnType, boolean cacheable) {
        this.name = name;
        this.returnType = returnType;
        this.cacheable = cacheable;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public boolean isCacheable() {
        return this.cacheable;
    }

    final Object value(ArgumentRegistry registry,
                       ArgumentProvider provider,
                       EvalSession session) throws EvaluationException {

        Object result = this.fetch(registry, provider, session);

        if (result != null) {
            if (this.returnType.isInstance(result)
                    || this.returnType.isAssignableFrom(result.getClass())
                    || (primitiveEquivalenceMap.containsKey(this.returnType)
                    && primitiveEquivalenceMap.get(this.returnType).isInstance(result))) {
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

    protected abstract Object fetch(ArgumentRegistry registry,
                                    ArgumentProvider provider,
                                    EvalSession session) throws EvaluationException;

    public abstract boolean fromExpression();
}
