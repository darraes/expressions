package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.airbnb.payments.featuresengine.expressions.ExpressionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class TestUtils {

    public static Expression expression(String exp, Class<?> type) {
        return ExpressionFactory.create(
                new ArgumentRegistry(),
                new ExpressionConfig(
                        exp,
                        type.getName()));
    }

    public static EvalSession testSession() {
        return testSession(null);
    }

    public static EvalSession testSession(ICache cache) {
        if (cache == null) {
            cache = new HashMapCache();
        }

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("i_int_a", 1);
        provider.put("i_int_b", 8);

        ArgumentRegistry registry = new ArgumentRegistry();

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_int_a",
                        Integer.class.getName(),
                        true,
                        false));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_int_b",
                        Integer.class.getName(),
                        false,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "e_int_c",
                        Integer.class.getName(),
                        "$i_int_a + $i_int_b"));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "e_int_d",
                        Integer.class.getName(),
                        "$i_int_b - $i_int_a",
                        false,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_c",
                        Integer.class.getName(),
                        "TestUtils.asyncAdd($i_int_a, $i_int_b)",
                        true,
                        true,
                        new String[]{TestUtils.class.getName()}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_d",
                        Integer.class.getName(),
                        "TestUtils.asyncSub(2*$i_int_b - $i_int_b, $i_int_a)",
                        true,
                        true,
                        new String[]{TestUtils.class.getName()}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_e",
                        Integer.class.getName(),
                        "10 * $async_int_c - $i_int_a - 10* $async_int_d",
                        true,
                        true));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_f",
                        Integer.class.getName(),
                        "2 * $async_int_e",
                        true,
                        true));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_map",
                        Map.class.getName(),
                        "(new TestUtils()).asyncMap(\"key_1\", 100)",
                        true,
                        true,
                        new String[]{TestUtils.class.getName()}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_from_map",
                        Integer.class.getName(),
                        "(Integer) $async_map.get(\"key_1\")",
                        true,
                        true));

        return new EvalSession(provider, registry, cache);
    }

    public static Object add(int x, int y) {
        return x + y;
    }

    public static CompletableFuture<Integer> asyncPow(int x, int pow) {
        return asyncLambda(x, pow, (a, p) -> (int) Math.pow(a, p));
    }

    public static CompletableFuture<Integer> asyncAdd(int x, int y) {
        return asyncLambda(x, y, (a, b) -> a + b);
    }

    public static CompletableFuture<Integer> asyncSub(int x, int y) {
        return asyncLambda(x, y, (a, b) -> a - b);
    }

    public static CompletableFuture<Integer> asyncMul(int x, int y) {
        return asyncLambda(x, y, (a, b) -> x * b);
    }

    private static CompletableFuture<Integer> asyncLambda(
            int x, int y, BiFunction<Integer, Integer, Integer> op) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        CompletableFuture.runAsync(
                () -> {
                    result.complete(op.apply(x, y));
                });
        return result;
    }

    public CompletableFuture<Map> asyncMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return CompletableFuture.supplyAsync(() -> map);
    }
}
