package com.airbnb.payments.featuresengine.cache;

import com.airbnb.payments.featuresengine.EvaluationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache implementation using a HashMap therefore no evicting policy is is place.
 */
public class HashMapCache implements ICache {

    private Map<String, Object> cache;

    public HashMapCache() {
        this.cache = new HashMap<>();
    }

    /**
     * Checks if the key is in the hashmap
     *
     * @param key searching key
     * @return True if it is. False otherwise.
     */
    @Override
    public boolean contains(String key) {
        return this.cache.containsKey(key);
    }

    /**
     * Adds the key/value to the hashmap.
     *
     * @param key   Key to search for the value
     * @param value Value to be cached
     */
    @Override
    public void put(String key, Object value) {
        this.cache.put(key, value);
    }

    /**
     * @param key The key to search for
     * @return The value if any
     * @throws EvaluationException if key is not present
     */
    @Override
    public Object get(String key) throws EvaluationException {
        if (!this.cache.containsKey(key)) {
            throw new EvaluationException("Key %s not present in cache", key);
        }

        return this.cache.get(key);
    }
}
