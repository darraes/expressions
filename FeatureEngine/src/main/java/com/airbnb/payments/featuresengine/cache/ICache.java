package com.airbnb.payments.featuresengine.cache;

import com.airbnb.payments.featuresengine.errors.EvaluationException;

public interface ICache {
    /**
     * If the @key is present in the cache
     *
     * @param key searching key
     * @return True if the @key is present in the cache. False otherwise
     */
    boolean contains(String key);

    /**
     * Caches the @value under the @key
     *
     * @param key   Key to search for the value
     * @param value Value to be cached
     */
    void put(String key, Object value);

    /**
     * Returns the value store on the @key if any. Throws
     *
     * @param key The key to search for
     * @return The value stored under the key
     * @throws EvaluationException If @key is not present
     */
    Object get(String key);
}
