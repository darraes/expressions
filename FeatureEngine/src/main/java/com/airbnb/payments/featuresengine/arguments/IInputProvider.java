package com.airbnb.payments.featuresengine.arguments;

public interface IInputProvider {
    /**
     *
     * @param key
     * @return
     */
    Object get(String key);

    /**
     *
     * @param key
     * @return
     */
    boolean exists(String key);
}
