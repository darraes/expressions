package com.airbnb.payments.featuresengine.arguments;

import com.airbnb.payments.featuresengine.EvalSession;
import com.airbnb.payments.featuresengine.EvaluationException;

import java.util.HashMap;

public class ArgumentRegistry {
    private HashMap<String, Argument> arguments;

    public ArgumentRegistry() {
        this.arguments = new HashMap<>();
    }

    public void put(Argument arg) {
        this.arguments.put(arg.getName(), arg);
    }

    public boolean exists(String key) {
        return this.arguments.containsKey(key);
    }

    public Object value(String key,
                        EvalSession session) throws EvaluationException {
        if (!this.exists(key)) {
            throw new EvaluationException("Argument %s not registered", key);
        }

        return this.arguments.get(key).value(session);
    }
}
