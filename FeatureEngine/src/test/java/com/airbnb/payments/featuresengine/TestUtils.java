package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_c",
                        Integer.class.getName(),
                        "TestUtils.asyncAdd($i_int_a, $i_int_b)",
                        true,
                        true,
                        new String[]{TestUtils.class.getName()}));

        return new EvalSession(provider, registry, cache);
    }

    public static CompletableFuture<Integer> asyncPow(int x, int pow) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        CompletableFuture.runAsync(
                () -> {
                    result.complete((int) Math.pow(x, pow));
                });
        return result;
    }

    public static CompletableFuture<Integer> asyncAdd(int x, int y) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        CompletableFuture.runAsync(
                () -> {
                    result.complete(x + y);
                });
        return result;
    }

    public CompletableFuture<Map> asyncMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return CompletableFuture.supplyAsync(() -> map);
    }
}
