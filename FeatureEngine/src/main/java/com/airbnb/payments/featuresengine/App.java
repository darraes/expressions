package com.airbnb.payments.featuresengine;

import java.lang.reflect.ParameterizedType;
import org.codehaus.janino.ExpressionEvaluator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static <T> Class<? extends Argument<T>> load(String fqcn, Class<T> type)
            throws ClassNotFoundException
    {
        Class<?> any = Class.forName(fqcn);
        System.out.println(any);
        for (Class<?> clz = any; clz != null; clz = clz.getSuperclass()) {
            for (Object ifc : clz.getGenericInterfaces()) {
                System.out.println(ifc);
                if (ifc instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) ifc;
                    if (Argument.class.equals(pType.getRawType())) {
                        if (!pType.getActualTypeArguments()[0].equals(type))
                            throw new ClassCastException("Class implements " + pType);
                        /* We've done the necessary checks to show that this is safe. */
                        @SuppressWarnings("unchecked")
                        Class<? extends Argument<T>> creator = (Class<? extends Argument<T>>) any;
                        return creator;
                    }
                }
            }
        }
        throw new ClassCastException(fqcn + " does not implement Argument<String>");
    }

    public static void main( String[] args )
    {
        try{
            System.out.println( "Hello World!" );
            System.out.println( Math.abs(-4) );
            ExpressionEvaluator ee = new ExpressionEvaluator();
            ee.setParameters(
                    new String[]{"a"},
                    new Class[]{int.class});
            ee.cook("(int) Math.sqrt(a + 6)");
            System.out.println(ee.evaluate(new Object[]{3}));
            Class<?> o = load("com.airbnb.payments.featuresengine.Argument", int.class);
            System.out.println(o);
        } catch (Exception e) {
            System.out.println( e );
        }

    }
}
