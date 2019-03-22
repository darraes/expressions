package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.IScriptEvaluator;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Expression {
    // All metadata about the expression
    private ExpressionInfo info;
    // Actual expression evaluator
    private IScriptEvaluator eval;

    private static String[] defaultImports = {
            "java.util.concurrent.CompletableFuture",
    };

    Expression(ExpressionInfo info) {
        this.info = info;

        try {
            this.eval = buildExpressionEvaluator(
                    this.info.getExpression(),
                    this.info.getReturnType(),
                    info.getDependencies());
        } catch (CompileException e) {
            throw new CompilationException(
                    e, "Failed compiling %s", this.info.getSourceExpression());
        }
    }

    /**
     * Getter.
     *
     * @return The original expression text before compiling it
     */
    public final String getExpression() {
        return this.info.getExpression();
    }

    /**
     * @return The class type this expression evaluates to
     */
    public Class<?> getReturnType() {
        return this.info.getReturnType();
    }

    /**
     * Evaluates the final value of the expression and returns that value.
     * <p>
     * All arguments must be registered int the [@session.registry()] object and all
     * user inputted arguments must be available on the [@session.provider()].
     * <p>
     * [@session] will record all events and be used as cache to prevent
     * re-computations.
     * <p>
     *
     * @param session Session of the individual request
     * @return Result of the expression computation
     */
    public final Object eval(EvalSession session) {
        if (this.info.isAsync()) {
            throw new EvaluationException(
                    "Async expressions must be computed using evalAsync()."
                            + " Expression %s",
                    this.info.getSourceExpression());
        }

        try {
            return this.eval.evaluate(new Object[]{session, null});
        } catch (Exception e) {
            if (e.getCause() instanceof EvaluationException) {
                throw (EvaluationException) e.getCause();
            }

            throw new EvaluationException(
                    e,
                    "Error evaluation expression %s",
                    this.info.getSourceExpression());
        }

    }

    /**
     * Evaluates the final value of the expression and returns that value.
     * <p>
     * All arguments must be registered int the [@session.registry()] object and all
     * user inputted arguments must be available on the [@session.provider()].
     * <p>
     * [@session] will record all events and be used as cache to prevent
     * re-computations.
     * <p>
     *
     * @param session  Session of the individual request
     * @param executor Executor to run the fetching on
     * @return Result of the expression computation
     */
    @SuppressWarnings(value = "unchecked")
    public final CompletableFuture<Object> evalAsync(
            EvalSession session, Executor executor) {
        CompletableFuture<Object> result = new CompletableFuture<>();

        String[] asyncArgs = this.info.getAccessedArguments().stream()
                .filter(Argument::isAsync)
                .map(Argument::getName)
                .toArray(String[]::new);

        this.cacheAsyncArguments(asyncArgs, session, executor).thenAccept(
                (v) -> {
                    try {
                        Object res = this.eval.evaluate(
                                new Object[]{session, executor});
                        if (res instanceof CompletableFuture) {
                            ((CompletableFuture<Object>)
                                    res)
                                    .thenApply(result::complete);
                        } else {
                            result.complete(res);
                        }
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof EvaluationException) {
                            result.completeExceptionally(e.getCause());
                        } else {
                            result.completeExceptionally(
                                    new EvaluationException(
                                            e,
                                            "Error evaluation expression %s",
                                            this.info.getSourceExpression()));
                        }
                    }
                });
        return result;
    }

    /**
     * Builds a Janino ExpressionEvaluator to serve the given @expression evaluations
     */
    private static IScriptEvaluator buildExpressionEvaluator(
            String expression, Class<?> type, String[] imports)
            throws CompileException {
        return prepareEvaluator(new ExpressionEvaluator(), expression, type, imports);
    }

    /**
     * Adds parameters, imports and any other configuration necessary to run the
     * Janino evaluator
     */
    private static IScriptEvaluator prepareEvaluator(
            IScriptEvaluator eval,
            String expression,
            Class<?> type,
            String[] imports) throws CompileException {
        // All expressions will only feed of the arguments therefore all we need are
        // the argument registry, the argument provider and the evaluation session
        eval.setParameters(
                new String[]{"session", "executor"},
                new Class[]{EvalSession.class, Executor.class});

        //Merge all imports (default and user) and set them
        String[] allImports = new String[defaultImports.length + imports.length];
        System.arraycopy(
                defaultImports, 0, allImports, 0, defaultImports.length);
        System.arraycopy(
                imports, 0, allImports, defaultImports.length, imports.length);
        eval.setDefaultImports(allImports);

        // TODO Check when the expression gets destructed the compilation doesn't leak
        // Leave the expression already compiled for faster performance on evaluation
        eval.cook(expression);
        return eval;
    }


    /**
     * Asynchronously caches all arguments passed in on @arguments. The resulting
     * completable future will only be ready when all arguments are in teh cache.
     *
     * @param arguments Arguments to be fetched
     * @param session   Caller's evaluation session
     * @param executor  Executor to fetch the arguments on
     * @return Future to be fulfilled when all arguments are ready
     */
    private CompletableFuture<Void> cacheAsyncArguments(
            String[] arguments, EvalSession session, Executor executor) {
        CompletableFuture<Void> res = new CompletableFuture<>();
        if (arguments == null || arguments.length == 0) {
            res.complete(null);
            return res;
        }

        CompletableFuture.runAsync(
                () -> {
                    CompletableFuture[] futures = Arrays.stream(arguments)
                            .map((argument) -> session.registry().valueAsync(
                                    argument,
                                    session,
                                    executor))
                            .toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(futures).thenAccept(res::complete);
                }, executor);

        return res;
    }

}
