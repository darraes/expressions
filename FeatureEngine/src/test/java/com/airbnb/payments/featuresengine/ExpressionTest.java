package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.arguments.InputArgument;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
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
        {
            // Argument doesn't exist
            try {
                TestUtils.expression("$dont_exists", Integer.class);
                fail();
            } catch (CompilationException e) {
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
    }

    @Test
    public void evaluateSimpleAsyncExpression()
            throws ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        {
            Expression expression = TestUtils.asyncExpression(
                    "TestUtils.asyncPow(10, 2)", Integer.class, session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(100, res)).get();
        }

        {
            Expression expression = TestUtils.asyncExpression(
                    "(new TestUtils()).asyncMap(\"key_1\", 100)",
                    Map.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept((res) -> {
                        assertEquals(100, ((Map) res).get("key_1"));
                    }).get();
        }

        {
            Expression expression = TestUtils.expression(
                    "(new TestUtils()).asyncMap(\"key_1\", 100)",
                    CompletableFuture.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept((res) -> {
                        assertEquals(100, ((Map) res).get("key_1"));
                    }).get();
        }

        {
            Expression expression = TestUtils.asyncExpression(
                    "Math.pow(10, 2)",
                    Double.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(100.0, res)).get();
        }

        {
            Expression expression = TestUtils.asyncExpression(
                    "new String(\"test\")",
                    String.class,
                    session);
            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals("test", res)).get();
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
    }

    @Test
    public void evaluateExceptionOnSyncEvalForAsyncExpression() {
        // TODO implement
        // Making sure only the async methods can serve async expression
    }

    @Test
    public void evaluateArgumentMethodCallExpression() {
        // TODO implement
        // Calling a method on an argument
    }

    @Test
    public void evaluateMultipleArgumentTypeExpression() {
        // TODO implement
        // Same expression playing around multiple types
    }

    @Test
    public void evaluateSameArgumentMultipleTimes() {
        // TODO implement
        // Same argument appearing several times on the same expression
    }
}
