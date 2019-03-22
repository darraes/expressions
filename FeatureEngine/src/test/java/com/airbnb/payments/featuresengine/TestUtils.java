package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;

public class TestUtils {
    public static EvalSession testSession() {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("i_int_a", 1);
        provider.put("i_int_b", 8);

        ArgumentRegistry registry = new ArgumentRegistry();

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_int_a",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_int_b",
                        Integer.class.getName()));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "e_int_c",
                        Integer.class.getName(),
                        "$i_int_a + $i_int_b"));

        return new EvalSession(provider, registry, cache);
    }
}
