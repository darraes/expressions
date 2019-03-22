package com.airbnb.payments.featuresengine.expressions;

import com.airbnb.payments.featuresengine.arguments.Argument;
import com.airbnb.payments.featuresengine.errors.CompilationException;
import com.airbnb.payments.featuresengine.core.EvalSession;
import com.airbnb.payments.featuresengine.errors.EvaluationException;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.IExpressionEvaluator;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Expression {
    // All metadata about the expression
    private ExpressionInfo info;
    // Actual expression evaluator
    private IExpressionEvaluator eval;

    private static String[] defaultImports = {
            "java.util.concurrent.CompletableFuture",
    };

    Expression(ExpressionInfo info) {
        this.info = info;

        try {
            this.eval = buildExpressionEvaluator(this.info);
        } catch (CompileException e) {
            throw new CompilationException(
                    e, "Failed compiling %s", this.info.getSourceExpression());
        }
    }

    /**
     * Getter.
     *
     * @return The original expression metadata
     */
    public final ExpressionInfo info() {
        return this.info;
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

        Argument[] asyncArgs = this.info.getAccessedArguments().stream()
                .filter(Argument::isAsync)
                .toArray(Argument[]::new);

        // All async arguments will be asynchronously fetched and cached in the session
        // before this expression runs.
        // Expression that have async arguments will rely on those arguments to be
        // already in the session.cache() and therefore they will load those arguments
        // normally.
        this.cacheAsyncArguments(asyncArgs, session, executor).thenAccept(
                (v) -> {
                    try {
                        Object res = this.eval.evaluate(
                                new Object[]{session, executor});
                        if (res instanceof CompletableFuture) {
                            // Handles when the expression itself is async
                            ((CompletableFuture<Object>)
                                    res)
                                    .thenApply(result::complete);
                        } else {
                            // Handles when the expression itself is not async, but
                            // it has at least one async argument
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
     * Asynchronously caches all arguments passed in on @arguments. The resulting
     * completable future will only be ready when all arguments are in teh cache.
     *
     * @param arguments Arguments to be fetched
     * @param session   Caller's evaluation session
     * @param executor  Executor to fetch the arguments on
     * @return Future to be fulfilled when all arguments are ready
     */
    private CompletableFuture<Void> cacheAsyncArguments(
            Argument[] arguments, EvalSession session, Executor executor) {
        CompletableFuture<Void> res = new CompletableFuture<>();
        if (arguments == null || arguments.length == 0) {
            res.complete(null);
            return res;
        }

        CompletableFuture.runAsync(
                () -> {
                    CompletableFuture[] futures = Arrays.stream(arguments)
                            .map((argument) -> argument.valueAsync(
                                    session,
                                    executor))
                            .toArray(CompletableFuture[]::new);

                    CompletableFuture.allOf(futures).thenAccept(res::complete);
                }, executor);

        return res;
    }

    /**
     * Builds a Janino ExpressionEvaluator to serve the given @expression evaluations
     */
    private static IExpressionEvaluator buildExpressionEvaluator(
            ExpressionInfo info)
            throws CompileException {
        IExpressionEvaluator eval = new ExpressionEvaluator();

        // All expressions will only feed of the arguments therefore all we need are
        // the argument registry, the argument provider and the evaluation session
        eval.setParameters(
                new String[]{"session", "executor"},
                new Class[]{EvalSession.class, Executor.class});

        //Merge all imports (default and user) and set them
        String[] allImports =
                new String[defaultImports.length + info.getDependencies().length];
        System.arraycopy(
                defaultImports, 0, allImports, 0, defaultImports.length);
        System.arraycopy(
                info.getDependencies(),
                0,
                allImports,
                defaultImports.length,
                info.getDependencies().length);
        eval.setDefaultImports(allImports);

        // TODO Check when the expression gets destructed the compilation doesn't leak

        try {
            eval.setExpressionType(info.getReturnType());
            eval.cook(info.getExpression());
        } catch (CompileException e) {
            if (info.isAsync()) {
                // Async expression can return CompletableFuture, when they are root
                // async expression, or they can return the actual final type, when
                // only their arguments are async and therefore will be asynchronously
                // fetched before evaluating the expression.
                // Eg.:
                // $exp1 = 'MyClass.doFooAsync($a)' -> return type = CompletableFuture
                // $exp2 = '$exp1 + 10' -> return type = Integer
                eval.setExpressionType(CompletableFuture.class);
                eval.cook(info.getExpression());
            }
        }

        return eval;
    }
}
