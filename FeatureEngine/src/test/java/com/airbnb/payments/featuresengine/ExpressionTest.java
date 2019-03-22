package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.errors.EvaluationException;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.airbnb.payments.featuresengine.expressions.ExpressionFactory;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class ExpressionTest {
    @Test
    public void accessProperties() throws CompilationException {
        ArgumentRegistry registry = new ArgumentRegistry();
        Expression expression = ExpressionFactory.create(
                registry,
                new ExpressionConfig("1 + 3", Integer.class.getName()));
        assertEquals("1 + 3", expression.getExpression());
        assertEquals(Integer.class, expression.getReturnType());
    }

    @Test
    public void evaluateStaticMethod()
            throws CompilationException, EvaluationException {
        ArgumentRegistry registry = new ArgumentRegistry();
        Expression expression = ExpressionFactory.create(
                registry,
                new ExpressionConfig(
                        "Math.max(3, 10)",
                        Integer.class.getName()));

        assertEquals(10, expression.eval(null));
    }

    @Test
    public void evaluateInstanceMethod()
            throws CompilationException, EvaluationException {
        ArgumentRegistry registry = new ArgumentRegistry();
        Expression expression = ExpressionFactory.create(
                registry,
                new ExpressionConfig(
                        "\" trim_me \".trim()", String.class.getName()));

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
        EvalSession session = createTestSession();

        Expression expression = ExpressionFactory.create(
                session.registry(),
                new ExpressionConfig(
                        "$a + $b",
                        Integer.class.getName()));

        assertEquals(9, expression.eval(session));
    }

    @Test
    public void evaluateRecursiveArguments()
            throws CompilationException, EvaluationException {
        EvalSession session = createTestSession();

        Expression expression = ExpressionFactory.create(
                session.registry(),
                new ExpressionConfig(
                        "Math.sqrt($c)",
                        Integer.class.getName()));

        assertEquals(3.0, expression.eval(session));
    }

    @Test
    public void handleExceptions() throws CompilationException {
        EvalSession session = createTestSession();
        try {
            ExpressionFactory.create(
                    session.registry(),
                    new ExpressionConfig(
                            "$d",
                            Integer.class.getName()));
            fail();
        } catch (CompilationException e) {

        }
    }

    @Test
    public void evaluateSimpleAsyncExpression()
            throws CompilationException, ExecutionException, InterruptedException {
        EvalSession session = createTestSession();

        Executor executor = Executors.newFixedThreadPool(2);

        {

            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    "ExpressionTest.someAsyncMethod($c)",
                                    Integer.class.getName(),
                                    new String[]{"com.airbnb.payments.featuresengine.ExpressionTest"}));

            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(19, res)).get();
        }

        {

            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    "ExpressionTest.someAsyncMethod2($c)",
                                    Integer.class.getName(),
                                    new String[]{"com.airbnb.payments.featuresengine.ExpressionTest"}));

            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(90, res)).get();
        }
    }

    @Test
    public void evaluateArgumentMethodCallExpression() {
        // TODO implement
    }

    @Test
    public void evaluateMultipleArgumentTypeExpression() {
        // TODO implement
    }

    private static EvalSession createTestSession() throws CompilationException {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        ArgumentRegistry registry = new ArgumentRegistry();

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
                        "((Integer)session.registry().value(\"a\", session))"
                                + " + ((Integer)session.registry().value(\"b\", session))"));

        return new EvalSession(provider, registry, cache);
    }

    public static CompletableFuture<Integer> someAsyncMethod(int x) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        CompletableFuture.runAsync(
                () -> {
                    result.complete(10 + x);
                });
        return result;
    }

    public static CompletableFuture<Integer> someAsyncMethod2(int x) {
        return CompletableFuture.supplyAsync(() -> 10 * x);
    }
}
