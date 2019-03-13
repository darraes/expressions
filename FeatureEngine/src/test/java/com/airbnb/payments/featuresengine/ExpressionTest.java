package com.airbnb.payments.featuresengine;

import static org.junit.Assert.assertEquals;

import com.airbnb.payments.featuresengine.arguments.ArgumentProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.ExpressionArgument;
import com.airbnb.payments.featuresengine.arguments.SimpleArgument;
import com.airbnb.payments.featuresengine.expressions.Expression;
import org.codehaus.commons.compiler.CompileException;
import org.junit.Test;

public class ExpressionTest {

    @Test
    public void accessProperties() throws CompileException {
        Expression expression = new Expression("1 + 3");
        assertEquals("1 + 3", expression.getExpressionText());
    }

    @Test
    public void evaluateSimple() throws CompileException, EvaluationException {

        ArgumentProvider provider = new ArgumentProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        EvalSession session = new EvalSession();

        ArgumentRegistry registry = new ArgumentRegistry();
        registry.put(new SimpleArgument("a", Integer.class));
        registry.put(new SimpleArgument("b", Integer.class));

        Expression expression = new Expression(
                "((Integer)registry.value(\"a\", provider, session))"
                        + " + ((Integer)registry.value(\"b\", provider, session))");

        assertEquals(9, expression.eval(registry, provider, session));
    }

    @Test
    public void evaluateRecursive() throws CompileException, EvaluationException {

        ArgumentProvider provider = new ArgumentProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        EvalSession session = new EvalSession();

        ArgumentRegistry registry = new ArgumentRegistry();
        registry.put(new SimpleArgument("a", Integer.class));
        registry.put(new SimpleArgument("b", Integer.class));
        registry.put(new ExpressionArgument(
                "c", Integer.class,
                "((Integer)registry.value(\"a\", provider, session))"
                        + " + ((Integer)registry.value(\"b\", provider, session))"));

        Expression expression = new Expression(
                "Math.sqrt(((Integer)registry.value(\"c\", provider, session)))");

        assertEquals(3.0, expression.eval(registry, provider, session));
    }
}
