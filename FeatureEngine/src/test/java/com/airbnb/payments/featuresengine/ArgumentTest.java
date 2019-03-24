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
                            Double.class.getName(),
                            false,
                            false
                    ));

            assertFalse(arg1.isCacheable());
            assertFalse(arg1.isAsync());
            assertEquals(Double.class, arg1.getReturnType());
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
                            Double.class.getName(),
                            "3.0 + 7.0",
                            false,
                            false
                    ));

            assertFalse(arg1.isCacheable());
            assertFalse(arg1.isAsync());
            assertEquals(Double.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
            assertEquals("3.0 + 7.0",
                    ((NamedExpression) arg1).getExpression().info().getExpression());
        }
    }

    @Test
    public void expressionArgument() throws EvaluationException, CompilationException {
        EvalSession session = TestUtils.testSession();
        assertEquals(9, session.registry().value("e_int_c", session));
    }

    @Test
    public void cachingEvaluations() throws EvaluationException, CompilationException {
        TestCache cache = new TestCache();
        EvalSession session = TestUtils.testSession(cache);

        assertEquals(9, session.registry().value("e_int_c", session));
        assertFalse(cache.served("i_int_a"));
        assertFalse(cache.served("i_int_b"));
        assertFalse(cache.served("e_int_c"));

        assertEquals(9, session.registry().value("e_int_c", session));

        assertFalse(cache.served("i_int_a"));
        assertFalse(cache.served("i_int_b"));
        assertTrue(cache.served("e_int_c"));

        assertTrue(cache.contains("i_int_a"));
        assertTrue(cache.contains("e_int_c"));
        assertFalse(cache.contains("i_int_b"));

        assertEquals(7, session.registry().value("e_int_d", session));
        assertFalse(cache.served("e_int_d"));
        assertEquals(7, session.registry().value("e_int_d", session));
        assertFalse(cache.served("e_int_d"));
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
    public void argumentTypeMismatch() {
        {
            // For sync expressions, we do type checking during compilation
            try {
                EvalSession session = TestUtils.testSession();
                ArgumentFactory.create(
                        session.registry(),
                        new ArgumentConfig(
                                "a",
                                Integer.class.getName(),
                                "new String(\"there\")"));
                fail();
            } catch (CompilationException e) {
                assertTrue(
                        e.getCause().getMessage().contains("conversion not possible"));
            }
        }

        {
            // Type casting errors show during evaluation
            try {
                EvalSession session = TestUtils.testSession();
                ArgumentFactory.create(
                        session.registry(),
                        new ArgumentConfig(
                                "a",
                                String.class.getName(),
                                "(String) TestUtils.add(1, 1)",
                                true,
                                false,
                                new String[]{TestUtils.class.getName()}));
                session.registry().value("a", session);
                fail();
            } catch (EvaluationException e) {
                assertTrue(e.getCause().getCause() instanceof ClassCastException);
            } catch (Exception e) {
                fail();
            }
        }

        {
            // For async expression returning CompletableFuture, we can only catch
            // type mismatches during evaluations
            try {
                Executor executor = Executors.newFixedThreadPool(2);
                EvalSession session = TestUtils.testSession();
                ArgumentFactory.create(
                        session.registry(),
                        new ArgumentConfig(
                                "a",
                                String.class.getName(),
                                "TestUtils.asyncSub($i_int_b, $i_int_a)",
                                true,
                                true,
                                new String[]{TestUtils.class.getName()}));
                session.registry().valueAsync("a", session, executor).get();
                fail();
            } catch (ExecutionException e) {
                assertTrue(e.getCause().getMessage().contains("not assignable"));
            } catch (Exception e) {
                fail();
            }
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
        try {
            ICache cache = new HashMapCache();

            HashMapInputProvider provider = new HashMapInputProvider();
            ArgumentRegistry registry = new ArgumentRegistry();

            ArgumentFactory.create(
                    registry,
                    new ArgumentConfig(
                            "a",
                            Integer.class.getName(),
                            "$b"));

            ArgumentFactory.create(
                    registry,
                    new ArgumentConfig(
                            "b",
                            Integer.class.getName(),
                            "$a"));

            EvalSession session = new EvalSession(provider, registry, cache);

            registry.value("a", session);
            fail();
        } catch (CompilationException e) {
            assertTrue(e.getMessage().contains("not registered"));
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
}
