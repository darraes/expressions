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
        Expression expression = new Expression("1 + 3", int.class);
        assertEquals("1 + 3", expression.getExpressionText());
        assertEquals(int.class, expression.getExpressionType());
    }

    @Test
    public void evaluateStaticMethod()
            throws CompilationException, EvaluationException {
        Expression expression = new Expression(
                "Math.max(3, 10)",
                int.class);

        assertEquals(10, expression.eval(null));
    }

    @Test
    public void evaluateInstanceMethod()
            throws CompilationException, EvaluationException {
        Expression expression = new Expression(
                "\" trim_me \".trim()",
                String.class);

        assertEquals("trim_me", expression.eval(null));
    }

    @Test
    public void evaluateConstructor()
            throws CompilationException, EvaluationException {
        Expression expression = new Expression(
                "(new String(\" trim_me \")).trim()",
                String.class);

        assertEquals("trim_me", expression.eval(null));
    }

    @Test
    public void evaluateInputArgument()
            throws CompilationException, EvaluationException {
        EvalSession session = createTestSession();

        Expression expression = new Expression(
                "((Integer)session.registry().value(\"a\", session))"
                        + " + ((Integer)session.registry().value(\"b\", session))",
                int.class);

        assertEquals(9, expression.eval(session));
    }

    @Test
    public void evaluateRecursiveArguments()
            throws CompilationException, EvaluationException {
        EvalSession session = createTestSession();

        Expression expression = new Expression(
                "Math.sqrt(((Integer)session.registry().value(\"c\", session)))",
                int.class);

        assertEquals(3.0, expression.eval(session));
    }

    @Test
    public void handleExceptions() throws CompilationException {
        EvalSession session = createTestSession();

        Expression expression = new Expression(
                "session.registry().value(\"d\", session)",
                int.class);

        try {
            expression.eval(session);
            fail();
        } catch (EvaluationException e) {

        }
    }

    @Test
    public void evaluateSimpleAsyncExpression()
            throws CompilationException, ExecutionException, InterruptedException, ClassNotFoundException {
        EvalSession session = createTestSession();

        Executor executor = Executors.newFixedThreadPool(2);

        {

            Expression expression =
                    ExpressionFactory.create(
                            session.registry(),
                            new ExpressionConfig(
                                    "foo",
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
                                    "foo",
                                    "ExpressionTest.someAsyncMethod2($c)",
                                    Integer.class.getName(),
                                    new String[]{"com.airbnb.payments.featuresengine.ExpressionTest"}));

            expression.evalAsync(session, executor)
                    .thenAccept(res -> assertEquals(90, res)).get();
        }
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
