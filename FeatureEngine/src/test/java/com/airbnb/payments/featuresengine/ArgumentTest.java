package com.airbnb.payments.featuresengine;

import com.airbnb.payments.featuresengine.arguments.*;
import com.airbnb.payments.featuresengine.cache.HashMapCache;
import com.airbnb.payments.featuresengine.cache.ICache;
import com.airbnb.payments.featuresengine.expressions.Expression;
import com.airbnb.payments.featuresengine.expressions.NamedExpression;
import org.codehaus.commons.compiler.CompileException;
import org.junit.Test;


import static org.junit.Assert.*;

public class ArgumentTest {
    @Test
    public void accessProperties() throws CompileException {
        {
            Argument arg1 = new InputArgument("a", Integer.class);

            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new InputArgument("a", Integer.class, true);

            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new InputArgument("a", Integer.class, false);

            assertFalse(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new NamedExpression("a", Integer.class, "3 + 7");

            assertTrue(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }

        {
            Argument arg1 = new NamedExpression(
                    "a", Integer.class, "3 + 7", false);

            assertFalse(arg1.isCacheable());
            assertEquals(Integer.class, arg1.getReturnType());
            assertEquals("a", arg1.getName());
        }
    }

    @Test
    public void expressionArgument() throws EvaluationException, CompileException {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        provider.put("a", 1);
        provider.put("b", 8);

        {
            ArgumentRegistry registry = new ArgumentRegistry();
            // Using class 'int' to test the boxed type checking
            registry.put(new InputArgument("a", Integer.class));
            registry.put(new InputArgument("b", Integer.class));
            registry.put(new NamedExpression(
                    "c", Integer.class,
                    "((Integer)session.registry().value(\"a\", session))"
                            + " + ((Integer)session.registry().value(\"b\", session))"));
            registry.put(new NamedExpression(
                    "d", Integer.class,
                    "10 * ((Integer)session.registry().value(\"c\", session))"));

            EvalSession session = new EvalSession(provider, registry, cache);

            assertTrue(registry.exists("a"));
            assertTrue(registry.exists("b"));
            assertTrue(registry.exists("c"));
            assertTrue(registry.exists("d"));
            assertFalse(registry.exists("e"));

            assertEquals(90, registry.value("d", session));
        }

        { // Using class 'int' to test the boxed type checking
            ArgumentRegistry registry = new ArgumentRegistry();

            registry.put(new InputArgument("a", int.class));
            registry.put(new InputArgument("b", int.class));
            registry.put(new NamedExpression(
                    "c", Integer.class,
                    "((Integer)session.registry().value(\"a\", session))"
                            + " + ((Integer)session.registry().value(\"b\", session))"));
            registry.put(new NamedExpression(
                    "d", Integer.class,
                    "10 * ((Integer)session.registry().value(\"c\", session))"));

            EvalSession session = new EvalSession(provider, registry, cache);

            assertTrue(registry.exists("a"));
            assertTrue(registry.exists("b"));
            assertTrue(registry.exists("c"));
            assertTrue(registry.exists("d"));
            assertFalse(registry.exists("e"));

            assertEquals(90, registry.value("d", session));
        }
    }

    @Test
    public void handleExceptions() throws CompileException, EvaluationException {
        ICache cache = new HashMapCache();

        HashMapInputProvider provider = new HashMapInputProvider();
        ArgumentRegistry registry = new ArgumentRegistry();

        registry.put(new NamedExpression(
                "c", Integer.class,
                "((Integer)session.registry().value(\"a\", session))"
                        + " + ((Integer)session.registry().value(\"b\", session))"));

        EvalSession session = new EvalSession(provider, registry, cache);

        try {
            var res = registry.value("c", session);
            assertTrue(false);
        } catch (EvaluationException e) {

        }
    }
}
