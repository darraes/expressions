package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class ExpressionTest {

    @Test
    public void evaluateAllTypesStaticMethod() {
        {
            Expression expression = TestUtils.expression(
                    "Math.max(3, 10) - Math.min(3, 10)", Integer.class);
            assertEquals(7, expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "Math.max(3L, 10L) - Math.min(3L, 10L)", Long.class);
            assertEquals(7L, expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "Math.max(3.0, 10.0) - Math.min(3.0, 10.0)", Double.class);
            assertEquals(7.0, expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "Math.max(3.0f, 10.0f) - Math.min(3.0f, 10.0f)", Float.class);
            assertEquals(7.0f, expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "Short.decode(\"7\")", Short.class);
            assertEquals((short) 7, expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "String.format(new String(\"test %s\"), \'A\')", String.class);
            assertEquals("test A", expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "Character.toLowerCase('A')", Character.class);
            assertEquals('a', expression.eval(null));
        }

        {
            Expression expression = TestUtils.expression(
                    "Byte.parseByte(\"1\")", Byte.class);
            assertEquals(((byte) 1), expression.eval(null));
        }
    }

    @Test
    public void evaluateInstanceMethod() {
        Expression expression = TestUtils.expression(
                "\"  trim_me  \".trim()", String.class);
        assertEquals("trim_me", expression.eval(null));
    }

    @Test
    public void evaluateConstructor() {
        Expression expression = TestUtils.expression(
                "(new String(\" trim_me \")).trim()", String.class);
        assertEquals("trim_me", expression.eval(null));
    }

    @Test
    public void evaluateInputArgument() {
        EvalSession session = TestUtils.testSession();
        Expression expression = TestUtils.expression(
                "$i_int_a + $i_int_b", Integer.class, session);
        assertEquals(9, expression.eval(session));
    }

    @Test
    public void evaluateRecursiveArguments() {
        EvalSession session = TestUtils.testSession();
        Expression expression = TestUtils.expression(
                "Math.sqrt($e_int_c)", Double.class, session);
        assertEquals(3.0, expression.eval(session));
    }

    @Test
    public void handleExceptions() {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        {
            // Argument not declared
            try {
                TestUtils.expression("$dont_exists", Integer.class);
                fail();
            } catch (CompilationException e) {
            } catch (Exception e) {
                fail();
            }
        }

        {
            // Argument not found
            try {
                Expression exp = TestUtils.expression(
                        "$i_int_missing", Integer.class, session);
                exp.eval(session);
                fail();
            } catch (EvaluationException e) {
            } catch (Exception e) {
                fail();
            }
        }

        {
            // Argument not found (async)
            try {
                Expression exp = TestUtils.expression(
                        "$i_int_missing", Integer.class, session, true);
                exp.evalAsync(session, executor).get();
                fail();
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof EvaluationException);
            } catch (Exception e) {
                fail();
            }
        }

        {
            // Type mismatch
            try {
                TestUtils.expression("new String(\"test\")", Integer.class);
                fail();
            } catch (CompilationException e) {
            } catch (Exception e) {
                fail();
            }

            try {
                TestUtils.expression("1L", String.class);
                fail();
            } catch (CompilationException e) {
            } catch (Exception e) {
                fail();
            }
        }

        {
            // Async can't be sync
            try {
                Expression exp = TestUtils.expression(
                        "($async_int_e / 10) < 0.5",
                        Boolean.class,
                        session,
                        true);
                exp.eval(session);
                fail();
            } catch (EvaluationException e) {
            } catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    public void evaluateSimpleAsyncExpression()
            throws ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        { // Grab a primitive type async
            Expression expression = TestUtils.asyncExpression(
                    "TestUtils.asyncPow(10, 2)", Integer.class, session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(100, res)).get();
        }

        { // Grab an object async
            Expression expression = TestUtils.asyncExpression(
                    "(new TestUtils()).asyncMap(\"key_1\", 100)",
                    Map.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept((res) -> {
                        assertEquals(100, ((Map) res).get("key_1"));
                    }).get();
        }

        { // Grab the completable future (async as sync)
            Expression expression = TestUtils.expression(
                    "(new TestUtils()).asyncMap(\"key_1\", 100)",
                    CompletableFuture.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept((res) -> {
                        assertEquals(100, ((Map) res).get("key_1"));
                    }).get();
        }

        { // Sync expressions can be evaluated as async
            Expression expression = TestUtils.asyncExpression(
                    "Math.pow(10, 2)",
                    Double.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(100.0, res)).get();
        }

        { // Sync expressions can be evaluated as async
            Expression expression = TestUtils.asyncExpression(
                    "new String(\"test\")",
                    String.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals("test", res)).get();
        }

        { // Sync arguments can be evaluated as async
            Expression expression = TestUtils.asyncExpression(
                    "2.0 * $i_int_a",
                    Double.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(2.0, res)).get();
        }
    }

    @Test
    public void expressionArgumentDependencies() {
        EvalSession session = TestUtils.testSession();

        { // Checks if the argument dependency is exactly how it should be
            Expression expression = TestUtils.asyncExpression(
                    "$async_int_f > 1.0",
                    Boolean.class,
                    session);
            Set<?> deps1 = expression.info().getDependentArguments();
            Set<?> deps2 = new HashSet<>(
                    Arrays.asList(
                            TestUtils.argument(
                                    "async_int_f",
                                    Integer.class),
                            TestUtils.argument(
                                    "async_int_e",
                                    Integer.class),
                            TestUtils.argument(
                                    "async_int_d",
                                    Integer.class),
                            TestUtils.argument(
                                    "async_int_c",
                                    Integer.class),
                            TestUtils.argument(
                                    "i_int_a",
                                    Integer.class),
                            TestUtils.argument(
                                    "i_int_b",
                                    Integer.class)
                    ));

            assertTrue(Sets.difference(deps1, deps2).isEmpty());
            assertTrue(Sets.difference(deps2, deps1).isEmpty());
        }

        { // Checks when the argument dependencies have no common arguments
            Expression exp1 = TestUtils.asyncExpression(
                    "$async_int_f > 1.0",
                    Boolean.class,
                    session);
            Expression exp2 = TestUtils.asyncExpression(
                    "$async_int_g > 1.0",
                    Boolean.class,
                    session);

            {
                Set<Argument> diff = new HashSet<>();
                Sets.difference(
                        exp1.info().getDependentArguments(),
                        exp2.info().getDependentArguments()).copyInto(diff);
                assertTrue(
                        Sets.difference(
                                diff,
                                exp1.info().getDependentArguments()).isEmpty());
            }

            {
                Set<Argument> diff = new HashSet<>();
                Sets.difference(
                        exp2.info().getDependentArguments(),
                        exp1.info().getDependentArguments()).copyInto(diff);
                assertTrue(
                        Sets.difference(
                                diff,
                                exp2.info().getDependentArguments()).isEmpty());
            }
        }

        { // Checks when the same argument appears multiple times
            Expression exp1 = TestUtils.expression(
                    "($i_int_a + $i_int_a + $i_int_a)"
                            + " / ($e_int_e + $e_int_e) < 1.0",
                    Boolean.class,
                    session);
            assertEquals(3, exp1.info().getDependentArguments().size());
        }

        { // Checks when the same argument appears multiple times
            Expression exp1 = TestUtils.asyncExpression(
                    "($i_int_a + $i_int_a + $i_int_a)"
                            + " / ($e_int_e + $e_int_e) < 1.0",
                    Boolean.class,
                    session);
            assertEquals(3, exp1.info().getDependentArguments().size());
        }
    }

    @Test
    public void evaluateArgumentMethodCallExpression()
            throws ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);
        String expText = "(Integer) $sync_map.values().stream()"
                + ".distinct().findAny().get()";
        {
            Expression exp1 = TestUtils.asyncExpression(
                    expText,
                    Integer.class,
                    session);
            assertEquals(100, exp1.evalAsync(session, executor).get());
        }

        {
            Expression exp1 = TestUtils.expression(
                    expText,
                    Integer.class,
                    session);
            assertEquals(100, exp1.eval(session));
        }
    }

    @Test
    public void evaluateMultipleArgumentTypeExpression()
            throws ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        {
            Expression exp1 = TestUtils.asyncExpression(
                    "$i_int_a > 10 || ($i_string_b == \"sb\" && $i_double_a < 0.4)",
                    Boolean.class,
                    session);
            assertEquals(true, exp1.evalAsync(session, executor).get());
        }

        {
            Expression exp1 = TestUtils.expression(
                    "$i_int_a > 10 || ($i_string_b == \"sb\" && $i_double_a > 0.4)",
                    Boolean.class,
                    session);
            assertEquals(false, exp1.eval(session));
        }
    }

    @Test
    public void evaluateSameArgumentMultipleTimes()
            throws ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        {
            Expression exp1 = TestUtils.asyncExpression(
                    "((double)($i_int_a + $i_int_a + $i_int_a))"
                            + " / ($e_int_e + $e_int_e) <= 0.09",
                    Boolean.class,
                    session);
            assertEquals(false, exp1.evalAsync(session, executor).get());
        }

        {
            Expression exp1 = TestUtils.expression(
                    "((double)($i_int_a + $i_int_a + $i_int_a))"
                            + " / ($e_int_e + $e_int_e) <= 0.1",
                    Boolean.class,
                    session);
            assertEquals(true, exp1.eval(session));
        }
    }
}
