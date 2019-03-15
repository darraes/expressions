package com.airbnb.payments.featuresengine;

import static org.junit.Assert.assertEquals;

import com.airbnb.payments.featuresengine.arguments.HashMapArgumentProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.expressions.NamedExpression;
import com.airbnb.payments.featuresengine.arguments.SimpleArgument;
import com.airbnb.payments.featuresengine.expressions.Expression;
import org.codehaus.commons.compiler.CompileException;
import org.junit.Test;

public class ExpressionTest {

    @Test
    public void accessProperties() throws CompileException {
        Expression expression = new Expression("1 + 3", int.class);
        assertEquals("1 + 3", expression.getExpressionText());
        assertEquals(int.class, expression.getExpressionType());
    }

    @Test
    public void evaluateSimple() throws CompileException, EvaluationException {
        ICache cache = new HashMapCache();

        HashMapArgumentProvider provider = new HashMapArgumentProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        ArgumentRegistry registry = new ArgumentRegistry();
        registry.put(new SimpleArgument("a", Integer.class));
        registry.put(new SimpleArgument("b", Integer.class));

        EvalSession session = new EvalSession(provider, registry, cache);

        Expression expression = new Expression(
                "((Integer)session.registry().value(\"a\", session))"
                        + " + ((Integer)session.registry().value(\"b\", session))", int.class);

        assertEquals(9, expression.eval(session));
    }

    @Test
    public void evaluateRecursive() throws CompileException, EvaluationException {
        ICache cache = new HashMapCache();

        HashMapArgumentProvider provider = new HashMapArgumentProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        ArgumentRegistry registry = new ArgumentRegistry();
        registry.put(new SimpleArgument("a", Integer.class));
        registry.put(new SimpleArgument("b", Integer.class));
        registry.put(new NamedExpression(
                "c", Integer.class,
                "((Integer)session.registry().value(\"a\", session))"
                        + " + ((Integer)session.registry().value(\"b\", session))"));

        EvalSession session = new EvalSession(provider, registry, cache);

        Expression expression = new Expression(
                "Math.sqrt(((Integer)session.registry().value(\"c\", session)))",
                int.class);

        assertEquals(3.0, expression.eval(session));
    }
}
