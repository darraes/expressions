package com.airbnb.payments.featuresengine.arguments;

import java.util.HashMap;

public class HashMapArgumentProvider implements IArgumentProvider {
    private HashMap<String, Object> arguments;

    public HashMapArgumentProvider() {
        this.arguments = new HashMap<>();
    }

    public HashMapArgumentProvider(HashMap<String, Object> args) {
        this.arguments = args;
    }

    public void put(String key, Object value) {
        this.arguments.put(key, value);
    }

    public Object get(String key) {
        return this.arguments.get(key);
    }

    public boolean exists(String key) {
        return this.arguments.containsKey(key);
    }
}
