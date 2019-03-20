package com.airbnb.payments.featuresengine.helpers;

import com.airbnb.payments.featuresengine.errors.EvaluationException;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ExpressionStack {
    // Members handling the stack of expressions being evaluated. Preventing infinite
    // loops is one of the problems handled by those.
    private Set<String> inProgressEvals;
    private Stack<String> evalFrames;

    public ExpressionStack() {
        this.inProgressEvals = new HashSet<>();
        this.evalFrames = new Stack<>();
    }

    public void push(String key) {
        if (this.inProgressEvals.contains(key)){
            throw new EvaluationException("Expression %s already in the stack", key);
        }

        this.evalFrames.push(key);
        this.inProgressEvals.add(key);
    }

    public String pop() {
        String key = this.evalFrames.pop();
        this.inProgressEvals.remove(key);
        return key;
    }

    public boolean contains(String key) {
        return this.inProgressEvals.contains(key);
    }
}
