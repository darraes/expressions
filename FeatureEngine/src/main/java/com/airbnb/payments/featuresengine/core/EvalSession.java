package com.airbnb.payments.featuresengine.core;

import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.IInputProvider;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.helpers.ExpressionStack;

/**
 * The evaluation session will hold all information pertinent to the current user
 * request.
 * It will hold the request cache, used to prevent duplicated work when computing
 * expressions, the argument registry, where all arguments can be access and read from,
 * the user's input data; among other things.
 * The session last only the duration of the top level request, but it is shared across
 * all rules and expression evaluations.
 */
public class EvalSession {
    private ICache cache;
    private ArgumentRegistry registry;
    private IInputProvider arguments;


    public EvalSession(IInputProvider provider,
                       ArgumentRegistry registry,
                       ICache cache) {
        this.cache = cache;
        this.registry = registry;
        this.arguments = provider;
    }

    /**
     * User provided input
     */
    public IInputProvider inputs() {
        return this.arguments;
    }

    /**
     * Single registry to access all arguments, user provided or derived expressions.
     */
    public ArgumentRegistry registry() {
        return this.registry;
    }

    /**
     * Cache used to store arguments after fetching. Some fetching might be expensive,
     * like expression evaluations. The cache will save re-computations.
     */
    public ICache cache() {
        return this.cache;
    }
}
