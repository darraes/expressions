package com.airbnb.payments.featuresengine.arguments;

public interface IArgumentProvider {
    void put(String key, Object value);

    Object get(String key);

    boolean exists(String key);
}
