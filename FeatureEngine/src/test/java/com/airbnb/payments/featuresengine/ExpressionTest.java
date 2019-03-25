package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.airbnb.payments.featuresengine.expressions.ExpressionFactory;
import org.junit.Test;

import java.util.Map;
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
        // TODO implement
    }

    @Test
    public void evaluateExceptionOnSyncEvalForAsyncExpression() {
        // TODO implement
    }

    @Test
    public void evaluateArgumentMethodCallExpression() {
        // TODO implement
    }

    @Test
    public void evaluateMultipleArgumentTypeExpression() {
        // TODO implement
    }

    @Test
    public void evaluateSameArgumentMultipleTimes() {
        // TODO implement
    }
}
