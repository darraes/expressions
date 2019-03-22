package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.*;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.core.AsyncEvalSession;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import com.airbnb.payments.featuresengine.arguments.NamedExpression;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;
import org.junit.Test;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static org.junit.Assert.*;

class TestCache implements ICache {

    private Map<String, Object> cache;
    private Set<String> servedFromCache;

    public TestCache() {
        this.cache = new HashMap<>();
        this.servedFromCache = new HashSet<>();
    }

    @Override
    public boolean contains(String key) {
        return this.cache.containsKey(key);
    }

    @Override
    public void put(String key, Object value) {
        this.cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        this.servedFromCache.add(key);
        return this.cache.get(key);
    }

    public boolean served(String key) {
        if (!this.servedFromCache.contains(key)) {
            return false;
        }
        return true;
    }
}

public class ArgumentTest {
    @Test
    public void accessProperties() throws CompilationException {
        {
            ArgumentRegistry registry = new ArgumentRegistry();
            Argument arg1 = ArgumentFactory.create(
                    registry,
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            true,
                            false
                    ));

            assertTrue(arg1.isCacheable());
            assertFalse(arg1.isAsync());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            ArgumentRegistry registry = new ArgumentRegistry();
            Argument arg1 = ArgumentFactory.create(
                    registry,
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            false,
                            false
                    ));

            assertFalse(arg1.isCacheable());
            assertFalse(arg1.isAsync());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            ArgumentRegistry registry = new ArgumentRegistry();
            Argument arg1 = ArgumentFactory.create(registry,
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            "3 + 7",
                            true,
                            false
                            ));

            assertTrue(arg1.isCacheable());
            assertFalse(arg1.isAsync());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
            assertEquals("3 + 7",
                    ((NamedExpression) arg1).getExpression().getExpressionText());
        }

        {
            ArgumentRegistry registry = new ArgumentRegistry();
            Argument arg1 = ArgumentFactory.create(registry,
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            "3 + 7",
                            false,
                            false
                    ));

            assertFalse(arg1.isCacheable());
            assertFalse(arg1.isAsync());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
            assertEquals("3 + 7",
                    ((NamedExpression) arg1).getExpression().getExpressionText());
        }
    }

    @Test
    public void expressionArgument() throws EvaluationException, CompilationException {
        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        ICache cache = new HashMapCache();

        ArgumentRegistry registry = new ArgumentRegistry();
        // Using class 'int' to test the boxed type checking
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "a",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig("b",
                        Integer.class.getName()));
        ArgumentFactory.create(registry,
                new ArgumentConfig(
                        "c",
                        Integer.class.getName(),
                        "$a + $b"));
        ArgumentFactory.create(registry,
                new ArgumentConfig(
                        "d",
                        Integer.class.getName(),
                        "10 * $c"));

        EvalSession session = new EvalSession(provider, registry, cache);

        assertTrue(registry.exists("a"));
        assertTrue(registry.exists("b"));
        assertTrue(registry.exists("c"));
        assertTrue(registry.exists("d"));
        assertFalse(registry.exists("e"));

        assertEquals(90, registry.value("d", session));
    }

    @Test
    public void cachingEvaluations() throws EvaluationException, CompilationException {
        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);


        TestCache cache = new TestCache();

        ArgumentRegistry registry = new ArgumentRegistry();
        // Using class 'int' to test the boxed type checking
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "a",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "c",
                        Integer.class.getName(),
                        "$a + $b"));

        EvalSession session = new EvalSession(provider, registry, cache);

        assertEquals(9, registry.value("c", session));
        assertFalse(cache.served("a"));
        assertFalse(cache.served("b"));
        assertFalse(cache.served("c"));

        assertEquals(9, registry.value("c", session));

        assertFalse(cache.served("a"));
        assertFalse(cache.served("b"));
        assertTrue(cache.served("c"));

        assertTrue(cache.contains("a"));
        assertTrue(cache.contains("b"));
        assertTrue(cache.contains("c"));
    }

    @Test
    public void argumentNotRegistered() throws CompilationException {
        ArgumentRegistry registry = new ArgumentRegistry();

        try {
            ArgumentFactory.create(registry,
                    new ArgumentConfig(
                            "c",
                            Integer.class.getName(),
                            "$a + $b",
                            true, false));
            fail();
        } catch (CompilationException e) {
            assertTrue(e.getMessage().contains("not registered"));
        }

    }

    @Test
    public void circularDependency() throws CompilationException {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        ArgumentRegistry registry = new ArgumentRegistry();

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "a",
                        Integer.class.getName(),
                        "1 + ((Integer)session.registry().value(\"b\", session))",
                        true,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName(),
                        "1 + ((Integer)session.registry().value(\"a\", session))",
                        true,
                        false));

        EvalSession session = new EvalSession(provider, registry, cache);

        try {
            registry.value("a", session);
            fail();
        } catch (EvaluationException e) {
            assertTrue(e.getMessage().contains("Circular"));
        }
    }


    @Test
    public void evaluateSimpleAsyncExpression()
            throws CompilationException, ExecutionException, InterruptedException {
        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);


        TestCache cache = new TestCache();

        ArgumentRegistry registry = new ArgumentRegistry();
        // Using class 'int' to test the boxed type checking
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "a",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "c",
                        Integer.class.getName(),
                        "ArgumentTest.someAsyncMethod($a + $b)",
                        true,
                        false,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        EvalSession session = new EvalSession(provider, registry, cache);
        Executor executor = Executors.newFixedThreadPool(2);

        registry.valueAsync("a", session, executor)
                .thenAccept(res -> assertEquals(1, res)).get();

        registry.valueAsync("c", session, executor)
                .thenAccept(res -> assertEquals(90, res)).get();

        registry.valueAsync("b", session, executor)
                .thenAccept(res -> assertEquals(8, res)).get();

        registry.valueAsync("c", session, executor)
                .thenAccept(res -> assertEquals(90, res)).get();

    }

    @Test
    public void evaluateAllValuesAsyncOld()
            throws CompileException, CompilationException, ExecutionException, InterruptedException, InvocationTargetException {
        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);


        TestCache cache = new TestCache();

        ArgumentRegistry registry = new ArgumentRegistry();
        // Using class 'int' to test the boxed type checking
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "a",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "c",
                        Integer.class.getName(),
                        "ArgumentTest.someAsyncMethod($a + $b)",
                        true,
                        false,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "d",
                        Integer.class.getName(),
                        "ArgumentTest.someAsyncMethod($b - $a)",
                        true,
                        false,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        EvalSession session = new EvalSession(provider, registry, cache);
        Executor executor = Executors.newFixedThreadPool(2);

        CompletableFuture<AsyncEvalSession> allAsyncDone = registry.allValuesAsync(new String[]{"c", "d"}, session, executor)
                .thenApply(new Function<Map<String, Object>, AsyncEvalSession>() {
                    public AsyncEvalSession apply(Map<String, Object> asyncValues) {
                        return new AsyncEvalSession(session, asyncValues);
                    }
                });
        int result = allAsyncDone.thenApply(
                new Function<AsyncEvalSession, Integer>() {
                    public Integer apply(AsyncEvalSession aSession) {
                        return ((Integer) aSession.asyncValues().get("c"))
                                - ((Integer) aSession.asyncValues().get("d"));
                    }
                })
                .get();
        assertEquals(20, result);

        ScriptEvaluator se = new ScriptEvaluator();
        se.setReturnType(CompletableFuture.class);
        se.setParameters(
                new String[]{"session", "executor"},
                new Class[]{EvalSession.class, Executor.class});
        se.setClassName("ExpressionWOW");
        se.setDefaultImports(
                "com.airbnb.payments.featuresengine.core.AsyncEvalSession",
                "java.util.concurrent.CompletableFuture",
                "java.util.function.Function");

        se.cook("static Integer exec(AsyncEvalSession session) {\n"
                + "    return ((Integer)session.registry().value(\"a\", session.inner())) + ((Integer) session.asyncValues().get(\"c\")) - ((Integer) session.asyncValues().get(\"d\"));\n"
                + "}\n"
                + "return session.registry().allValuesAsync(new String[]{\"c\", \"d\"}, session, executor)\n" +
                "                .thenApply(new Function<Map, Integer>() {\n" +
                "                    public Integer apply(Map asyncValues) {\n" +
                "                        return ExpressionWOW.exec(new AsyncEvalSession(session, asyncValues));\n" +
                "                    }\n" +
                "                });"
        );
        assertEquals(21,
                ((CompletableFuture)se.evaluate(
                        new Object[]{session, executor})).get());
    }

    @Test
    public void evaluateAllValuesAsync()
            throws CompileException, CompilationException, ExecutionException, InterruptedException, InvocationTargetException {
        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);


        TestCache cache = new TestCache();

        ArgumentRegistry registry = new ArgumentRegistry();
        // Using class 'int' to test the boxed type checking
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "a",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName()));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "c",
                        Integer.class.getName(),
                        "ArgumentTest.someAsyncMethod($a + $b)",
                        true,
                        true,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "d",
                        Integer.class.getName(),
                        "ArgumentTest.someAsyncMethod($b - $a)",
                        true,
                        false,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "e",
                        Integer.class.getName(),
                        "$c",
                        true,
                        true));

        EvalSession session = new EvalSession(provider, registry, cache);
        Executor executor = Executors.newFixedThreadPool(2);

        int result = (int) registry.valueAsync("e", session, executor).get();
        assertEquals(21, result);


    }


    public static CompletableFuture<Integer> someAsyncMethod(int x) {
        return CompletableFuture.supplyAsync(() -> 10 * x);
    }
}
