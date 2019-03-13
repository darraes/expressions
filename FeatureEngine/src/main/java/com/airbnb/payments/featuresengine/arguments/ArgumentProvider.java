package com.airbnb.payments.featuresengine.arguments;

import java.util.HashMap;

public class ArgumentProvider {
    private HashMap<String, Object> arguments;

    public ArgumentProvider() {
        this.arguments = new HashMap<>();
    }

    public void put(String key, Object value) {
        this.arguments.put(key, value);
    }

    public Object get(String key) {
        return this.arguments.get(key);
    }
}
