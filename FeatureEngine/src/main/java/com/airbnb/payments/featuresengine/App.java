package com.airbnb.payments.featuresengine;

import org.codehaus.janino.ExpressionEvaluator;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try{
            System.out.println( "Hello World!" );
            System.out.println( Math.abs(-4) );
            ExpressionEvaluator ee = new ExpressionEvaluator();
            ee.cook("Math.sqrt(3 + 6)");
            System.out.println(ee.evaluate(null));
        } catch (Exception e) {
            System.out.println( "Exception" );
        }

    }
}
