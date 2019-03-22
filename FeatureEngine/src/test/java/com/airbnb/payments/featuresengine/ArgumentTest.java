package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.*;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import com.airbnb.payments.featuresengine.arguments.NamedExpression;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;


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
                    ((NamedExpression) arg1).getExpression().info().getExpression());
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
                    ((NamedExpression) arg1).getExpression().info().getExpression());
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
                        true,
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
    public void evaluateAllValuesAsync()
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
                        true,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "d",
                        Integer.class.getName(),
                        "ArgumentTest.someAsyncMethod($b - $a)",
                        true,
                        true,
                        new String[]{"com.airbnb.payments.featuresengine.ArgumentTest"}));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "e",
                        Integer.class.getName(),
                        "$c - $a - $d",
                        true,
                        true));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "f",
                        Integer.class.getName(),
                        "2*$e",
                        true,
                        true));

        EvalSession session = new EvalSession(provider, registry, cache);
        Executor executor = Executors.newFixedThreadPool(2);

        int result = (int) registry.valueAsync("f", session, executor).get();
        assertEquals(38, result);
    }

    @Test
    public void callMethodOnAsyncInstanceArgument() {
        // TODO implement
    }

    @Test
    public void argumentTypeMismatch() {
        // TODO implement
    }


    public static CompletableFuture<Integer> someAsyncMethod(int x) {
        return CompletableFuture.supplyAsync(() -> 10 * x);
    }
}
