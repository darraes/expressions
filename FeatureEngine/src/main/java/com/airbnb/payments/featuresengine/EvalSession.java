package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.IArgumentProvider;
import com.airbnb.payments.featuresengine.cache.ICache;


public class EvalSession {
    private ICache cache;
    private ArgumentRegistry registry;
    private IArgumentProvider arguments;

    public EvalSession(IArgumentProvider provider, ArgumentRegistry registry, ICache cache) {
        this.cache = cache;
        this.registry = registry;
        this.arguments = provider;
    }

    public IArgumentProvider inputs() {
        return this.arguments;
    }

    public ArgumentRegistry registry() {
        return this.registry;
    }

    public ICache cache() {
        return this.cache;
    }
}
