package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.*;
import org.codehaus.commons.compiler.CompileException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArgumentTest {
    @Test
    public void accessProperties() throws CompileException {
        {
            Argument arg1 = new SimpleArgument("a", Integer.class);

            assertFalse(arg1.fromExpression());
            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new SimpleArgument("a", Integer.class, true);

            assertFalse(arg1.fromExpression());
            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new SimpleArgument("a", Integer.class, false);

            assertFalse(arg1.fromExpression());
            assertFalse(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new ExpressionArgument("a", Integer.class, "3 + 7");

            assertTrue(arg1.fromExpression());
            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new ExpressionArgument(
                    "a", Integer.class, "3 + 7", false);

            assertTrue(arg1.fromExpression());
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

        {
            ArgumentRegistry registry = new ArgumentRegistry();
            // Using class 'int' to test the boxed type checking
            registry.put(new SimpleArgument("a", Integer.class));
            registry.put(new SimpleArgument("b", Integer.class));
            registry.put(new ExpressionArgument(
                    "c", Integer.class,
                    "((Integer)registry.value(\"a\", provider, session))"
                            + " + ((Integer)registry.value(\"b\", provider, session))"));
            registry.put(new ExpressionArgument(
                    "d", Integer.class,
                    "10 * ((Integer)registry.value(\"c\", provider, session))"));

            assertEquals(90, registry.value("d", provider, session));
        }

        { // Using class 'int' to test the boxed type checking
            ArgumentRegistry registry = new ArgumentRegistry();

            registry.put(new SimpleArgument("a", int.class));
            registry.put(new SimpleArgument("b", int.class));
            registry.put(new ExpressionArgument(
                    "c", Integer.class,
                    "((Integer)registry.value(\"a\", provider, session))"
                            + " + ((Integer)registry.value(\"b\", provider, session))"));
            registry.put(new ExpressionArgument(
                    "d", Integer.class,
                    "10 * ((Integer)registry.value(\"c\", provider, session))"));

            assertEquals(90, registry.value("d", provider, session));
        }
    }
}
