package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.config.ExpressionConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.airbnb.payments.featuresengine.expressions.ExpressionFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExpressionFactoryTest {
    private static EvalSession createTestSession() {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 2);
        provider.put("C", 3);
        provider.put("_d", 4);
        provider.put("big", 5);

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
                        "C",
                        Integer.class.getName()));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "_d",
                        Integer.class.getName()));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "big",
                        Integer.class.getName()));

        return new EvalSession(provider, registry, cache);
    }

    @Test
    public void expressionInfo() {
        String expressionText = "1 + 3";
        Expression expression = ExpressionFactory.create(
                new ArgumentRegistry(),
                new ExpressionConfig(expressionText, Integer.class.getName()));

        assertEquals(expressionText, expression.info().getExpression());
        assertEquals(expressionText, expression.info().getSrcExpression());
        assertFalse(expression.info().isAsync());
        assertEquals(Integer.class, expression.info().getReturnType());
        assertEquals(0, expression.info().getAccessedArguments().size());
        assertNotNull(expression.info().getID());
        assertArrayEquals(new String[0], expression.info().getImports());
    }

    @Test
    public void accessedArgumentsDetection() {
        // TODO Implement
    }

    @Test
    public void convertDoubleMatch() {
        EvalSession session = createTestSession();

        Expression expression = ExpressionFactory.create(
                session.registry(), new ExpressionConfig(
                        "$a + $b", Integer.class.getName()));
        assertEquals(
                "((java.lang.Integer)session.registry().value(\"a\", session))"
                        + " + ((java.lang.Integer)session.registry().value(\"b\", session))",
                expression.info().getExpression());


        assertEquals(3, expression.eval(session));
    }

    @Test
    public void convertMultiMatch() throws ClassNotFoundException {
        EvalSession session = createTestSession();

        Expression expression = ExpressionFactory.create(
                session.registry(),
                new ExpressionConfig(
                        "$a + $b - $C + $_d - $big",
                        Integer.class.getName()));


        assertEquals(-1, expression.eval(session));
    }

    @Test
    public void argumentNotFound() {
        EvalSession session = createTestSession();

        try {
            ExpressionFactory.create(
                    session.registry(), new ExpressionConfig(
                            "$a + $d", Integer.class.getName()));
            fail();
        } catch (Exception e) {

        }
    }
}
