package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.Argument;
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

    static Expression expression(String exp, Class<?> type) {
        return expression(exp, type, null);
    }

    static Expression expression(
            String exp, Class<?> type, EvalSession session) {
        return expression(exp, type, session, false);
    }

    static Expression asyncExpression(
            String exp, Class<?> type, EvalSession session) {
        return expression(exp, type, session, true);
    }

    static Expression expression(
            String exp, Class<?> type, EvalSession session, boolean isAsync) {
        ArgumentRegistry registry = (session != null)
                ? session.registry() : new ArgumentRegistry();
        return ExpressionFactory.create(
                registry,
                new ExpressionConfig(
                        exp,
                        type.getName(),
                        isAsync,
                        new String[]{TestUtils.class.getName()}));
    }

    static Argument argument(String name, Class<?> type) {
        return argument(name, type, true, false);
    }

    static Argument argument(String name, Class<?> type, ArgumentRegistry registry) {
        return argument(name, type, true, false, registry);
    }

    static Argument argument(
            String name, Class<?> type, boolean cacheable, boolean isAsync) {
        return argument(name, type, cacheable, isAsync, new ArgumentRegistry());
    }

    static Argument argument(
            String name, Class<?> type, boolean cacheable, boolean isAsync, ArgumentRegistry registry) {
        return ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        name,
                        type.getName(),
                        cacheable,
                        isAsync
                ));
    }

    static Argument argument(String name, Class<?> type, String expression, ArgumentRegistry registry) {
        return argument(name, type, true, false, expression, registry);
    }

    static Argument argument(String name,
                             Class<?> type,
                             boolean cacheable,
                             boolean isAsync,
                             String expression,
                             ArgumentRegistry registry) {
        return ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        name,
                        type.getName(),
                        expression,
                        cacheable,
                        isAsync
                ));
    }

    static EvalSession testSession() {
        return testSession(null);
    }

    static EvalSession testSession(ICache cache) {
        if (cache == null) {
            cache = new HashMapCache();
        }

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("i_int_a", 1);
        provider.put("i_int_b", 8);
        provider.put("i_int_c", 30);
        provider.put("i_string_a", "sa");
        provider.put("i_string_b", "sb");
        provider.put("i_double_a", 0.035);

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
                        "i_int_c",
                        Integer.class.getName(),
                        false,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_int_missing",
                        Integer.class.getName(),
                        false,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_string_a",
                        String.class.getName(),
                        false,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_string_b",
                        String.class.getName(),
                        false,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "i_double_a",
                        Double.class.getName(),
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
                        "e_int_e",
                        Integer.class.getName(),
                        "(int)($i_int_c / 2)",
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
                        "async_int_g",
                        Integer.class.getName(),
                        "2 * $e_int_e",
                        true,
                        true));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "async_int_h",
                        Integer.class.getName(),
                        "$async_int_g + $async_int_c",
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
                        "sync_map",
                        Map.class.getName(),
                        "(new TestUtils()).syncMap(\"key_1\", 100)",
                        true,
                        false,
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

    public Map syncMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public CompletableFuture<Map> asyncMap(String key, Object value) {
        return CompletableFuture.supplyAsync(() -> syncMap(key, value));
    }
}
