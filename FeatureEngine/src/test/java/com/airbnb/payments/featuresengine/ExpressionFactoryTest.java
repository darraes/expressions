package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentFactory;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.HashMapInputProvider;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.config.ArgumentConfig;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.airbnb.payments.featuresengine.expressions.ExpressionFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExpressionFactoryTest {
    private static EvalSession createTestSession() throws CompilationException {
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
                        Integer.class.getName(),
                        true,
                        false));
        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "b",
                        Integer.class.getName(),
                        true,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "C",
                        Integer.class.getName(),
                        true,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "_d",
                        Integer.class.getName(),
                        true,
                        false));

        ArgumentFactory.create(
                registry,
                new ArgumentConfig(
                        "big",
                        Integer.class.getName(),
                        true,
                        false));

        return new EvalSession(provider, registry, cache);
    }

    @Test
    public void convertDoubleMatch() {
        EvalSession session = createTestSession();

        String expressionText = ExpressionFactory.process(
                session.registry(), "$a + $b", false);
        assertEquals(
                "((java.lang.Integer)session.registry().value(\"a\", session))"
                        + " + ((java.lang.Integer)session.registry().value(\"b\", session))",
                expressionText);

        Expression expression = new Expression(
                expressionText,
                int.class);

        assertEquals(3, expression.eval(session));
    }

    @Test
    public void convertMultiMatch() {
        EvalSession session = createTestSession();

        String expressionText = ExpressionFactory.process(
                session.registry(),
                "$a + $b - $C + $_d - $big",
                false);

        Expression expression = new Expression(
                expressionText,
                int.class);

        assertEquals(-1, expression.eval(session));
    }

    @Test
    public void argumentNotFound() {
        EvalSession session = createTestSession();

        try{
            ExpressionFactory.process(
                    session.registry(), "$a + $d", false);
            fail();
        } catch (Exception e) {

        }
    }
}
