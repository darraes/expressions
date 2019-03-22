package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.cache.ICache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestCache implements ICache {

    private Map<String, Object> cache;
    private Set<String> servedFromCache;

    public TestCache() {
        this.cache = new HashMap<>();
        this.servedFromCache = new HashSet<>();
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
    public Object get(String key) {
        this.servedFromCache.add(key);
        return this.cache.get(key);
    }

    public boolean served(String key) {
        if (!this.servedFromCache.contains(key)) {
            return false;
        }
        return true;
    }
}
