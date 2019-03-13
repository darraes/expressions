package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.ArgumentProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.ExpressionArgument;
import com.airbnb.payments.featuresengine.arguments.SimpleArgument;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArgumentTest {
    @Test
    public void accessProperties() {
        {
            SimpleArgument arg1 = new SimpleArgument("a", Integer.class);

            assertFalse(arg1.fromExpression());
            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            SimpleArgument arg1 = new SimpleArgument("a", Integer.class, true);

            assertFalse(arg1.fromExpression());
            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            SimpleArgument arg1 = new SimpleArgument("a", Integer.class, false);

            assertFalse(arg1.fromExpression());
            assertFalse(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }
    }

    @Test
    public void expressionArgument() throws Throwable {
        ArgumentProvider provider = new ArgumentProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        EvalSession session = new EvalSession();

        ArgumentRegistry registry = new ArgumentRegistry();
        registry.put(new SimpleArgument("a", int.class));
        registry.put(new SimpleArgument("b", int.class));
        registry.put(new ExpressionArgument(
                "c", Integer.class,
                "((Integer)registry.value(\"a\", provider, session))"
                        + " + ((Integer)registry.value(\"b\", provider, session))"));
        registry.put(new ExpressionArgument(
                "d", Integer.class,
                "10*((Integer)registry.value(\"c\", provider, session))"));

        assertEquals(90, registry.value("d", provider, session));
    }
}
