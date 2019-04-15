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
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class Expression {
    private static final Void VOID = null;
    // All metadata about the expression
    private ExpressionMetadata info;
    // Actual expression evaluator
    private IExpressionEvaluator eval;

    private static final String[] DEFAULT_IMPORTS = {
            "java.util.concurrent.CompletableFuture",
    };

    Expression(ExpressionMetadata info) {
        this.info = info;

        try {
            this.eval = buildExpressionEvaluator(this.info);
        } catch (CompileException e) {
            throw new CompilationException(
                    e, "Failed compiling %s", this.info.getSrcExpression());
        }
    }

    /**
     * Getter.
     *
     * @return The original expression metadata
     */
    public final ExpressionMetadata info() {
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
                            + " Expression (%s) was not",
                    this.info.getSrcExpression());
        }

        try {
            return this.eval.evaluate(new Object[]{session, null});
        } catch (Exception e) {
            throw cause(e, this.info().getSrcExpression());
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
                                    .thenApply(result::complete)
                                    .exceptionally((e) -> {
                                        result.completeExceptionally(
                                                cause(e, this.info.getSrcExpression()));
                                        return null;
                                    });
                            ;
                        } else {
                            // Handles when the expression itself is not async, but
                            // it has at least one async argument
                            result.complete(res);
                        }
                    } catch (Exception e) {
                        result.completeExceptionally(
                                cause(e, this.info.getSrcExpression()));
                    }
                })
                .exceptionally((e) -> {
                    result.completeExceptionally(
                            cause(e, this.info.getSrcExpression()));
                    return null;
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
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (arguments == null || arguments.length == 0) {
            result.complete(null);
            return result;
        }

        CompletableFuture.runAsync(
                () -> {
                    try {
                        // TODO Use DisjointSets to optimize this logic.

                        if (this.info.hasCommonDependencies()) {
                            // Arguments have dependencies in common.
                            // To avoid racing when loading the same arguments in the
                            // same session, we load the async arguments serially when
                            // they share at least one dependency.
                            //
                            // As an example, one of the issues of loading the async
                            // args in parallel is if different arguments depend on a
                            // common nested argument. In this case, if both fetches
                            // start before the other finishes, they will fetch the
                            // dependant argument independently instead of leveraging
                            // the cached result of one another and possibly can get
                            // different values causing unattended consequences.
                            CompletableFuture<Object> fetchAllTask = null;
                            for (Argument arg : arguments) {
                                if (fetchAllTask == null) {
                                    // First argument, start the chain
                                    fetchAllTask = arg.valueAsync(session, executor);
                                } else {
                                    // Nesting all other arguments in the chain
                                    fetchAllTask = fetchAllTask.thenCompose(
                                            (r) -> arg.valueAsync(session, executor));
                                }
                            }

                            fetchAllTask
                                    .thenAccept((r) -> result.complete(VOID))
                                    .exceptionally((e) -> {
                                        result.completeExceptionally(
                                                cause(e, this.info.getSrcExpression()));
                                        return VOID;
                                    });
                        } else {
                            // Arguments have no dependencies in common and therefore
                            // they can be load in parallel
                            CompletableFuture[] futures = Arrays.stream(arguments)
                                    .map((argument) -> argument.valueAsync(
                                            session,
                                            executor))
                                    .toArray(CompletableFuture[]::new);

                            CompletableFuture.allOf(futures)
                                    .thenAccept(result::complete)
                                    .exceptionally((e) -> {
                                        result.completeExceptionally(
                                                cause(e, this.info().getSrcExpression()));
                                        return null;
                                    });
                        }
                    } catch (Exception e) {
                        result.completeExceptionally(
                                cause(e, this.info().getSrcExpression()));
                    }
                }, executor);

        return result;
    }

    /**
     * Builds a Janino ExpressionEvaluator to serve the given @expression evaluations
     */
    private static IExpressionEvaluator buildExpressionEvaluator(
            ExpressionMetadata info)
            throws CompileException {
        IExpressionEvaluator eval = new ExpressionEvaluator();

        // All expressions will only feed of the arguments therefore all we need are
        // the argument registry, the argument provider and the evaluation session
        eval.setParameters(
                new String[]{"session", "executor"},
                new Class[]{EvalSession.class, Executor.class});

        //Merge all imports (default and user) and set them
        String[] allImports =
                new String[DEFAULT_IMPORTS.length + info.getImports().length];
        System.arraycopy(
                DEFAULT_IMPORTS, 0, allImports, 0, DEFAULT_IMPORTS.length);
        System.arraycopy(
                info.getImports(),
                0,
                allImports,
                DEFAULT_IMPORTS.length,
                info.getImports().length);
        eval.setDefaultImports(allImports);

        // TODO Check when the expression gets destructed the compilation doesn't leak

        try {
            eval.setExpressionType(info.getReturnType());
            eval.cook(info.getExpression());
        } catch (CompileException e) {
            if (info.isAsync()) {
                try {
                    // Async expression can return CompletableFuture, when they are root
                    // async expression, or they can return the actual final type, when
                    // only their arguments are async and therefore will be asynchronously
                    // fetched before evaluating the expression.
                    // Eg.:
                    // $exp1 = 'MyClass.doFooAsync($a)' -> return type = CompletableFuture
                    // $exp2 = '$exp1 + 10' -> return type = Integer
                    eval.setExpressionType(CompletableFuture.class);
                    eval.cook(info.getExpression());
                } catch (CompileException innerEx) {
                    throw e;
                }
            } else {
                throw e;
            }
        }

        return eval;
    }

    /**
     * The async nature together with the dynamic compilation makes that the actual
     * exception are wrapper inside layers of other exceptions.
     * This method will unwrap that and return the actual root error.
     *
     * @param e Exception to be searched
     * @return Root error
     */
    private static EvaluationException cause(Throwable e, String context) {
        if (e instanceof EvaluationException) {
            return ((EvaluationException) e);
        }

        if (e.getCause() instanceof EvaluationException) {
            return (EvaluationException) e.getCause();
        } else if (e.getCause() instanceof InvocationTargetException ||
                e.getCause() instanceof CompletionException ||
                e.getCause() instanceof ExecutionException) {
            return new EvaluationException(
                    e.getCause(),
                    "Error evaluation (%s): %s",
                    context,
                    e.getCause().getMessage());
        }

        return new EvaluationException(
                e,
                "Error evaluation (%s): %s",
                context,
                e.getMessage());
    }
}
