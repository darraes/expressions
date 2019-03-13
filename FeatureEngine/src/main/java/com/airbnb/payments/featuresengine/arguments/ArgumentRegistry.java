package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;

import java.util.HashMap;

public class ArgumentRegistry {
    private HashMap<String, Argument> arguments;

    public ArgumentRegistry() {
        this.arguments = new HashMap<>();
    }

    public void put(Argument arg) {
        this.arguments.put(arg.getName(), arg);
    }

    public Argument get(String key) {
        return this.arguments.get(key);
    }

    public Object value(String key,
                        ArgumentProvider provider,
                        EvalSession session) {
        return this.arguments.get(key).value(this, provider, session);
    }
}
