package com.airbnb.payments.featuresengine.core;

import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.IInputProvider;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.helpers.ExpressionStack;

import java.util.Map;

/**
 * Wrapper around the evaluation session and the arguments fetched asynchronously
 */
public class AsyncEvalSession {
    private EvalSession session;
    private Map<String, Object> asyncValues;


    public AsyncEvalSession(EvalSession session, Map<String, Object> asyncValues) {
        this.session = session;
        this.asyncValues = asyncValues;
    }

    /**
     * This ctor, with an Object as the values map instead of a Map<>, exists because
     * Janino library loses the type information when anonymous classes are used as
     * function parameters.
     *
     * IMPORTANT: Don't call this ctor directly
     *
     * @param session Caller's evaluation session
     * @param asyncValuesMap A Map<String, Object> with the arguments
     */
    @SuppressWarnings(value = "unchecked")
    public AsyncEvalSession(EvalSession session, Object asyncValuesMap) {
        this.session = session;
        this.asyncValues = (Map<String, Object>) asyncValuesMap;
    }

    /**
     * @return The inner EvalSession
     */
    public EvalSession inner() { return this.session; }

    /**
     * User provided input
     */
    public IInputProvider inputs() {
        return this.session.inputs();
    }

    /**
     * Single registry to access all arguments, user provided or derived expressions.
     */
    public ArgumentRegistry registry() {
        return this.session.registry();
    }

    /**
     * Cache used to store arguments after fetching. Some fetching might be expensive,
     * like expression evaluations. The cache will save re-computations.
     */
    public ICache cache() {
        return this.session.cache();
    }

    /**
     * Stack to keep track of the nested expressions being current evaluated.
     */
    public ExpressionStack stack() {
        return this.session.stack();
    }

    /**
     * All values fetched asynchronously and ready to go
     */
    public Map<String, Object> asyncValues() { return this.asyncValues; }
}
