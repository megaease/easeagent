/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.redis.interceptor.initialize;

import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompletableFutureWrapper<T> extends CompletableFuture<T> {

    private final CompletableFuture<T> source;
    private final Object attach;
    private volatile boolean processed;

    public CompletableFutureWrapper(CompletableFuture<T> source, Object attach) {
        this.source = source;
        this.attach = attach;
    }

    private T processResult(T t) {
        return processResult(t, null, this.attach);
    }

    private T processResult(T t, Throwable throwable) {
        return processResult(t, throwable, this.attach);
    }

    private void processException(Throwable throwable) {
        processResult(null, throwable, this.attach);
    }

    private T processResult(T t, Throwable throwable, Object dynamicFieldValue) {
        if (this.processed) {
            return t;
        }
        AgentDynamicFieldAccessor.setDynamicFieldValue(t, dynamicFieldValue);
        Object channelWriter = AgentFieldReflectAccessor.getFieldValue(t, "channelWriter");
        if (channelWriter != null) {
            AgentDynamicFieldAccessor.setDynamicFieldValue(channelWriter, dynamicFieldValue);
        }
        this.processed = true;
        return t;
    }

    @Override
    public boolean isDone() {
        return source.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        T t = source.get();
        return processResult(t);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            T t = source.get(timeout, unit);
            return this.processResult(t);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            this.processException(e);
            throw e;
        }
    }

    @Override
    public T join() {
        T t = source.join();
        return processResult(t);
    }

    @Override
    public T getNow(T valueIfAbsent) {
        T t = source.getNow(valueIfAbsent);
        return processResult(t);
    }

    @Override
    public boolean complete(T value) {
        boolean b = source.complete(value);
        this.processResult(value);
        return b;
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        boolean b = source.completeExceptionally(ex);
        this.processException(ex);
        return b;
    }

    @Override
    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return source.thenApply(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return source.thenApplyAsync(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return source.thenApplyAsync(t -> fn.apply(processResult(t)), executor);
    }

    @Override
    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return source.thenAccept(t -> action.accept(processResult(t)));
    }

    @Override
    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return source.thenAcceptAsync(t -> action.accept(processResult(t)));
    }

    @Override
    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return source.thenAcceptAsync(t -> action.accept(processResult(t)), executor);
    }

    @Override
    public CompletableFuture<Void> thenRun(Runnable action) {

        return source.thenRun(() -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return source.thenRunAsync(() -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return source.thenRunAsync(() -> {
            processResult(null);
            action.run();
        }, executor);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return source.thenCombine(other, (BiFunction<T, U, V>) (t, u) -> fn.apply(processResult(t), u));
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return source.thenCombineAsync(other, (BiFunction<T, U, V>) (t, u) -> fn.apply(processResult(t), u));
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return source.thenCombineAsync(other, (BiFunction<T, U, V>) (t, u) -> fn.apply(processResult(t), u), executor);
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return source.thenAcceptBoth(other, (BiConsumer<T, U>) (t, u) -> action.accept(processResult(t), u));
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return source.thenAcceptBothAsync(other, (BiConsumer<T, U>) (t, u) -> action.accept(processResult(t), u));
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return source.thenAcceptBothAsync(other, (BiConsumer<T, U>) (t, u) -> action.accept(processResult(t), u), executor);
    }

    @Override
    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return source.runAfterBoth(other, () -> {
            action.run();
            processResult(null);
        });
    }

    @Override
    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return source.runAfterBothAsync(other, () -> {
            action.run();
            processResult(null);
        });
    }

    @Override
    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return source.runAfterBothAsync(other, () -> {
            action.run();
            processResult(null);
        }, executor);
    }

    @Override
    public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {

        return source.applyToEither(other, t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return source.applyToEitherAsync(other, t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return source.applyToEitherAsync(other, t -> fn.apply(processResult(t)), executor);
    }

    @Override
    public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return source.acceptEither(other, t -> action.accept(processResult(t)));
    }

    @Override
    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return source.acceptEitherAsync(other, t -> action.accept(processResult(t)));
    }

    @Override
    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return source.acceptEitherAsync(other, t -> action.accept(processResult(t)), executor);
    }

    @Override
    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return source.runAfterEither(other, () -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {

        return source.runAfterEitherAsync(other, () -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {

        return source.runAfterEitherAsync(other, () -> {
            processResult(null);
            action.run();
        }, executor);
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return source.thenCompose(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return source.thenComposeAsync(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return source.thenComposeAsync(t -> fn.apply(processResult(t)), executor);
    }

    @Override
    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return source.whenComplete((t, throwable) -> action.accept(processResult(t, throwable), throwable));
    }

    @Override
    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return source.whenCompleteAsync((t, throwable) -> action.accept(processResult(t, throwable), throwable));
    }

    @Override
    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return source.whenCompleteAsync((t, throwable) -> action.accept(processResult(t, throwable), throwable), executor);
    }

    @Override
    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return source.handle((t, throwable) -> fn.apply(processResult(t, throwable), throwable));
    }

    @Override
    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {

        return source.handleAsync((t, throwable) -> fn.apply(processResult(t, throwable), throwable));
    }

    @Override
    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {

        return source.handleAsync((t, throwable) -> fn.apply(processResult(t, throwable), throwable), executor);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return source.toCompletableFuture();
    }

    @Override
    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return source.exceptionally(throwable -> {
            processException(throwable);
            return fn.apply(throwable);
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancel = source.cancel(mayInterruptIfRunning);
        this.processException(null);
        return cancel;
    }

    @Override
    public boolean isCancelled() {
        return source.isCancelled();
    }

    @Override
    public boolean isCompletedExceptionally() {
        return source.isCompletedExceptionally();
    }

    @Override
    public void obtrudeValue(T value) {
        source.obtrudeValue(value);
        this.processResult(value);
    }

    @Override
    public void obtrudeException(Throwable ex) {
        source.obtrudeException(ex);
        this.processException(ex);
    }

    @Override
    public int getNumberOfDependents() {
        return source.getNumberOfDependents();
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
