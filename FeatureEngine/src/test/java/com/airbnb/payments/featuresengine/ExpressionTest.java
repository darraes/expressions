package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.expressions.Expression;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExpressionTest {

    private static EvalSession createTestSession() throws CompilationException {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        ArgumentRegistry registry = new ArgumentRegistry();

        ArgumentFactory.create(registry, "a", Integer.class, true);
        ArgumentFactory.create(registry, "b", Integer.class, true);

        ArgumentFactory.create(registry,
                "c",
                Integer.class,
                "((Integer)session.registry().value(\"a\", session))"
                        + " + ((Integer)session.registry().value(\"b\", session))",
                true);

        return new EvalSession(provider, registry, cache);
    }

    @Test
    public void accessProperties() throws CompilationException {
        Expression expression = new Expression("1 + 3", int.class);
        assertEquals("1 + 3", expression.getExpressionText());
        assertEquals(int.class, expression.getExpressionType());
    }

    @Test
    public void evaluateStaticMethod()
            throws CompilationException, EvaluationException {
        // TODO
    }

    @Test
    public void evaluateInstanceMethod()
            throws CompilationException, EvaluationException {
        // TODO
    }

    @Test
    public void evaluateConstructor()
            throws CompilationException, EvaluationException {
        // TODO
    }

    @Test
    public void evaluateWriteExpression()
            throws CompilationException, EvaluationException {
        // TODO
    }

    @Test
    public void evaluateInputArgument()
            throws CompilationException, EvaluationException {
        EvalSession session = createTestSession();

        Expression expression = new Expression(
                "((Integer)session.registry().value(\"a\", session))"
                        + " + ((Integer)session.registry().value(\"b\", session))", int.class);

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
    public void handleExceptions() throws CompilationException, EvaluationException {
        EvalSession session = createTestSession();

        Expression expression = new Expression(
                "Math.sqrt(((Integer)session.registry().value(\"d\", session)))",
                int.class);

        try {
            var res = expression.eval(session);
            fail();
        } catch (EvaluationException e) {

        }
    }
}
