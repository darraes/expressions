package com.airbnb.payments.featuresengine.arguments;

import java.util.HashMap;

public class HashMapInputProvider implements IInputProvider {
    private HashMap<String, Object> arguments;

    public HashMapInputProvider() {
        this.arguments = new HashMap<>();
    }

    public HashMapInputProvider(HashMap<String, Object> args) {
        this.arguments = args;
    }

    public void put(String key, Object value) {
        this.arguments.put(key, value);
    }

    @Override
    public Object get(String key) {
        return this.arguments.get(key);
    }

    @Override
    public boolean exists(String key) {
        return this.arguments.containsKey(key);
    }
}
