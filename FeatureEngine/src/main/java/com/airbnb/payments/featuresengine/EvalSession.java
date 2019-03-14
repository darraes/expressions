package com.airbnb.payments.featuresengine;

import java.util.HashMap;
import java.util.Map;

public class EvalSession {
    private Map<String, Object> cache;

    public EvalSession() {
        this.cache = new HashMap<>();
    }

    public void putInCache(String key, Object value) {
        this.cache.put(key, value);
    }

    public boolean inCache(String key) {
        return this.cache.containsKey(key);
    }

    public Object getFromCache(String key) {
        return this.cache.get(key);
    }
}
