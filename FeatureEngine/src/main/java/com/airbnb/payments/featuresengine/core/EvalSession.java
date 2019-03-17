package com.airbnb.payments.featuresengine.core;

import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.IInputProvider;
import com.airbnb.payments.featuresengine.cache.ICache;


public class EvalSession {
    private ICache cache;
    private ArgumentRegistry registry;
    private IInputProvider arguments;

    public EvalSession(IInputProvider provider, ArgumentRegistry registry, ICache cache) {
        this.cache = cache;
        this.registry = registry;
        this.arguments = provider;
    }

    public IInputProvider inputs() {
        return this.arguments;
    }

    public ArgumentRegistry registry() {
        return this.registry;
    }

    public ICache cache() {
        return this.cache;
    }

    // TODO save named expression stack to prevent cycles
}
