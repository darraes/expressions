package com.airbnb.payments.featuresengine.arguments;

public class ArgumentFactory {
    private ArgumentRegistry registry;

    private static ArgumentFactory ourInstance = new ArgumentFactory();

    public static ArgumentFactory getInstance() {
        return ourInstance;
    }

    private ArgumentFactory() {
    }

    public void init(ArgumentRegistry registry) {

    }

    public Argument create(String name, Class<?> returnType, boolean cacheable) {
        return null;
    }

    public Argument create(String name, Class<?> returnType, String expression, boolean cacheable) {
        return null;
    }
}
