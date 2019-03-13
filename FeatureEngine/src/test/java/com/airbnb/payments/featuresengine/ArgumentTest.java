package com.airbnb.payments.featuresengine;

import static org.junit.Assert.assertEquals;

import com.airbnb.payments.featuresengine.arguments.ArgumentProvider;
import com.airbnb.payments.featuresengine.arguments.ArgumentRegistry;
import com.airbnb.payments.featuresengine.arguments.ExpressionArgument;
import com.airbnb.payments.featuresengine.arguments.SimpleArgument;
import org.codehaus.commons.compiler.CompileException;
import org.junit.Test;

public class ArgumentTest {
    @Test
    public void expressionArgument() throws CompileException {
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
        registry.put(new ExpressionArgument(
                "d", Integer.class,
                "10*((Integer)registry.value(\"c\", provider, session))"));

        assertEquals(90, registry.value("d", provider, session));
    }
}
