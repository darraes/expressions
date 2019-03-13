package com.airbnb.payments.featuresengine;

import java.util.HashMap;

public class ArgumentProvider {
    private HashMap<String, Object> arguments;

    public ArgumentProvider() {
        this.arguments = new HashMap<>();
    }

    public void put(String key, Object value) {
        this.arguments.put(key, value);
    }

    @SuppressWarnings(value = "unchecked")
    public <T> T get(String key, Class<T> type) {
        Object result = this.arguments.get(key);
        if (type.isInstance(result) || type.isAssignableFrom(result.getClass())) {
            return (T) result;
        }
        return null; // TODO throw
    }
}
