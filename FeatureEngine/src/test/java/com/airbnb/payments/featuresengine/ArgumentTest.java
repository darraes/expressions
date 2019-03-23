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
        try {
            EvalSession session = TestUtils.testSession();
            ArgumentFactory.create(
                    session.registry(),
                    new ArgumentConfig(
                            "not_there",
                            Integer.class.getName(),
                            "$a + $b"));
            fail();
        } catch (CompilationException e) {
            assertTrue(e.getMessage().contains("not registered"));
        }

    }

    @Test
    public void duplicatedArgument() throws CompilationException {
        try {
            EvalSession session = TestUtils.testSession();
            ArgumentFactory.create(
                    session.registry(),
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            "1 + 1"));

            ArgumentFactory.create(
                    session.registry(),
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            "1 + 1"));
            fail();
        } catch (CompilationException e) {
            assertTrue(e.getMessage().contains("registered"));
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
                        "((Integer)session.registry().value(\"b\", session))"));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName(),
                        "((Integer)session.registry().value(\"a\", session))"));

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
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        session.registry().valueAsync("i_int_a", session, executor)
                .thenAccept(res -> assertEquals(1, res)).get();

        session.registry().valueAsync("async_int_c", session, executor)
                .thenAccept(res -> assertEquals(9, res)).get();

        session.registry().valueAsync("i_int_b", session, executor)
                .thenAccept(res -> assertEquals(8, res)).get();

        session.registry().valueAsync("async_int_c", session, executor)
                .thenAccept(res -> assertEquals(9, res)).get();

    }

    @Test
    public void evaluateAllValuesAsync()
            throws CompilationException, ExecutionException, InterruptedException {
        Executor executor = Executors.newFixedThreadPool(2);


        {
            // Loading order of dependants first
            EvalSession session = TestUtils.testSession();
            assertEquals(38, session.registry().valueAsync(
                    "async_int_f", session, executor).get());

            assertEquals(19, session.registry().valueAsync(
                    "async_int_e", session, executor).get());

            assertEquals(9, session.registry().valueAsync(
                    "async_int_c", session, executor).get());
        }

        {
            // Loading order of dependencies first
            EvalSession session = TestUtils.testSession();
            assertEquals(9, session.registry().valueAsync(
                    "async_int_c", session, executor).get());

            assertEquals(19, session.registry().valueAsync(
                    "async_int_e", session, executor).get());

            assertEquals(38, session.registry().valueAsync(
                    "async_int_f", session, executor).get());
        }
    }

    @Test
    public void callMethodOnAsyncInstanceArgument()
            throws CompilationException, ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        session.registry().valueAsync("async_int_from_map", session, executor)
                .thenAccept(res -> assertEquals(100, res)).get();
    }

    @Test
    public void argumentTypeMismatch() {
        // TODO implement
    }
}
