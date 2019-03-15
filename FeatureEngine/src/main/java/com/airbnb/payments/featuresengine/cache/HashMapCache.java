package com.airbnb.payments.featuresengine.cache;

import java.util.HashMap;
import java.util.Map;

public class HashMapCache implements ICache {
    private Map<String, Object> cache;

    public HashMapCache() {
        this.cache = new HashMap<>();
    }

    @Override
    public boolean contains(String key) {
        return this.cache.containsKey(key);
    }

    @Override
    public void put(String key, Object value) {
        this.cache.put(key, value);
    }

    @Override
    public Object get(String key) throws RuntimeException {
        return this.cache.get(key);
    }
}
