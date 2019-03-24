package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
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
    public void evaluateAllTypes() {
        // TODO Implement
        {
            Expression expression = TestUtils.expression(
                    "(char) 65", Character.class);
            assertEquals('A', expression.eval(null));
        }
    }


    @Test
    public void evaluateStaticMethod()
            throws CompilationException, EvaluationException {
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
    public void evaluateInstanceMethod()
            throws CompilationException, EvaluationException {
        ArgumentRegistry registry = new ArgumentRegistry();
        Expression expression = ExpressionFactory.create(
                registry,
                new ExpressionConfig(
                        "\"  trim_me  \".trim()", String.class.getName()));

        assertEquals("trim_me", expression.eval(null));
    }

    @Test
    public void evaluateConstructor()
            throws CompilationException, EvaluationException {
        ArgumentRegistry registry = new ArgumentRegistry();
        Expression expression = ExpressionFactory.create(
                registry,
                new ExpressionConfig(
                        "(new String(\" trim_me \")).trim()",
                        String.class.getName()));

        assertEquals("trim_me", expression.eval(null));
    }

    @Test
    public void evaluateInputArgument()
            throws CompilationException, EvaluationException {
        EvalSession session = TestUtils.testSession();

        Expression expression = ExpressionFactory.create(
                session.registry(),
                new ExpressionConfig(
                        "$i_int_a + $i_int_b",
                        Integer.class.getName()));

        assertEquals(9, expression.eval(session));
    }

    @Test
    public void evaluateRecursiveArguments()
            throws CompilationException, EvaluationException {
        EvalSession session = TestUtils.testSession();

        Expression expression = ExpressionFactory.create(
                session.registry(),
                new ExpressionConfig(
                        "Math.sqrt($e_int_c)",
                        Double.class.getName()));

        assertEquals(3.0, expression.eval(session));
    }

    @Test
    public void handleExceptions() throws CompilationException {
        {
            // Argument doesn't exist
            try {
                ExpressionFactory.create(
                        new ArgumentRegistry(),
                        new ExpressionConfig(
                                "$dont_exists",
                                Integer.class.getName()));
                fail();
            } catch (CompilationException e) {
            } catch (Exception e) {
                fail();
            }
        }

        {
            // Type mismatch
            try {
                ExpressionFactory.create(
                        new ArgumentRegistry(),
                        new ExpressionConfig(
                                "new String(\"test\")",
                                Integer.class.getName()));
                fail();
            } catch (CompilationException e) {
            } catch (Exception e) {
                fail();
            }

            try {
                ExpressionFactory.create(
                        new ArgumentRegistry(),
                        new ExpressionConfig(
                                "1L",
                                String.class.getName()));
                fail();
            } catch (CompilationException e) {
            } catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    public void evaluateSimpleAsyncExpression()
            throws CompilationException, ExecutionException, InterruptedException {
        EvalSession session = TestUtils.testSession();
        Executor executor = Executors.newFixedThreadPool(2);

        {
            String expressionText = "TestUtils.asyncPow(10, 2)";
            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    expressionText,
                                    Integer.class.getName(),
                                    true,
                                    new String[]{TestUtils.class.getName()}));

            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(100, res)).get();
        }

        {
            String expressionText = "(new TestUtils()).asyncMap(\"key_1\", 100)";
            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    expressionText,
                                    Map.class.getName(),
                                    true,
                                    new String[]{TestUtils.class.getName()}));

            expression.evalAsync(session, executor)
                    .thenAccept((res) -> {
                        assertEquals(100, ((Map) res).get("key_1"));
                    }).get();
        }

        {
            String expressionText = "(new TestUtils()).asyncMap(\"key_1\", 100)";
            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    expressionText,
                                    CompletableFuture.class.getName(),
                                    false,
                                    new String[]{TestUtils.class.getName()}));

            expression.evalAsync(session, executor)
                    .thenAccept((res) -> {
                        assertEquals(100, ((Map) res).get("key_1"));
                    }).get();
        }

        {
            String expressionText = "Math.pow(10, 2)";
            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    expressionText,
                                    Double.class.getName(),
                                    true,
                                    new String[]{TestUtils.class.getName()}));

            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(100.0, res)).get();
        }

        {
            String expressionText = "new String(\"test\")";
            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    expressionText,
                                    String.class.getName(),
                                    true,
                                    new String[]{TestUtils.class.getName()}));

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
