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
import io.lettuce.core.ConnectionFuture;

import javax.annotation.Nonnull;
import java.net.SocketAddress;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectionFutureWrapper<T> implements ConnectionFuture<T> {

    private final ConnectionFuture<T> source;
    private final Object attach;
    private volatile boolean processed;

    public ConnectionFutureWrapper(ConnectionFuture<T> source, Object attach) {
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
        if (this.processed || t == null) {
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
    public boolean cancel(boolean mayInterruptIfRunning) {
        return source.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return source.isCancelled();
    }

    @Override
    public boolean isDone() {
        return source.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        T t = source.get();
        return this.processResult(t);
    }


    @Override
    public T get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            T t = source.get(timeout, unit);
            return this.processResult(t);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            this.processException(e);
            throw e;
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return source.getRemoteAddress();
    }

    @Override
    public T join() {
        T t = source.join();
        return this.processResult(t);
    }

    @Override
    public <U> ConnectionFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return source.thenApply(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> ConnectionFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return source.thenApplyAsync(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> ConnectionFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return source.thenApplyAsync(t -> fn.apply(processResult(t)), executor);
    }

    @Override
    public ConnectionFuture<Void> thenAccept(Consumer<? super T> action) {
        return source.thenAccept(t -> action.accept(processResult(t)));
    }

    @Override
    public ConnectionFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return source.thenAcceptAsync(t -> action.accept(processResult(t)));
    }

    @Override
    public ConnectionFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return source.thenAcceptAsync(t -> action.accept(processResult(t)), executor);
    }

    @Override
    public ConnectionFuture<Void> thenRun(Runnable action) {
        return source.thenRun(() -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public ConnectionFuture<Void> thenRunAsync(Runnable action) {
        return source.thenRunAsync(() -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public ConnectionFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return source.thenRunAsync(() -> {
            processResult(null);
            action.run();
        }, executor);
    }

    @Override
    public <U, V> ConnectionFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return source.thenCombine(other, (BiFunction<T, U, V>) (t, u) -> fn.apply(processResult(t), u));
    }

    @Override
    public <U, V> ConnectionFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return source.thenCombineAsync(other, (BiFunction<T, U, V>) (t, u) -> fn.apply(processResult(t), u));
    }

    @Override
    public <U, V> ConnectionFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return source.thenCombineAsync(other, (BiFunction<T, U, V>) (t, u) -> fn.apply(processResult(t), u), executor);
    }

    @Override
    public <U> ConnectionFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return source.thenAcceptBoth(other, (BiConsumer<T, U>) (t, u) -> action.accept(processResult(t), u));
    }

    @Override
    public <U> ConnectionFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return source.thenAcceptBothAsync(other, (BiConsumer<T, U>) (t, u) -> action.accept(processResult(t), u));
    }

    @Override
    public <U> ConnectionFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return source.thenAcceptBothAsync(other, (BiConsumer<T, U>) (t, u) -> action.accept(processResult(t), u), executor);
    }

    @Override
    public ConnectionFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return source.runAfterBoth(other, () -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public ConnectionFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return source.runAfterBothAsync(other, () -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public ConnectionFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return source.runAfterBothAsync(other, () -> {
            processResult(null);
            action.run();
        }, executor);
    }

    @Override
    public <U> ConnectionFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return source.applyToEither(other, t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> ConnectionFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return source.applyToEitherAsync(other, t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> ConnectionFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return source.applyToEitherAsync(other, t -> fn.apply(processResult(t)), executor);
    }

    @Override
    public ConnectionFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return source.acceptEither(other, t -> action.accept(processResult(t)));
    }

    @Override
    public ConnectionFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return source.acceptEitherAsync(other, t -> action.accept(processResult(t)));
    }

    @Override
    public ConnectionFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return source.acceptEitherAsync(other, t -> action.accept(processResult(t)), executor);
    }

    @Override
    public ConnectionFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return source.runAfterEither(other, () -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public ConnectionFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return source.runAfterEitherAsync(other, () -> {
            processResult(null);
            action.run();
        });
    }

    @Override
    public ConnectionFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return source.runAfterEitherAsync(other, () -> {
            processResult(null);
            action.run();
        }, executor);
    }

    @Override
    public <U> ConnectionFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return source.thenCompose(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> ConnectionFuture<U> thenCompose(BiFunction<? super T, ? super Throwable, ? extends CompletionStage<U>> fn) {
        return source.thenCompose((t, throwable) -> fn.apply(processResult(t), throwable));
    }

    @Override
    public <U> ConnectionFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return source.thenComposeAsync(t -> fn.apply(processResult(t)));
    }

    @Override
    public <U> ConnectionFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return source.thenComposeAsync(t -> fn.apply(processResult(t)), executor);
    }

    @Override
    public ConnectionFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return source.exceptionally(throwable -> {
            processException(throwable);
            return fn.apply(throwable);
        });
    }


    @Override
    public ConnectionFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return source.whenComplete((t, throwable) -> action.accept(processResult(t, throwable), throwable));
    }

    @Override
    public ConnectionFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return source.whenCompleteAsync((t, throwable) -> action.accept(processResult(t, throwable), throwable));
    }

    @Override
    public ConnectionFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return source.whenCompleteAsync((t, throwable) -> action.accept(processResult(t, throwable), throwable), executor);
    }

    @Override
    public <U> ConnectionFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return source.handle((t, throwable) -> fn.apply(processResult(t, throwable), throwable));
    }

    @Override
    public <U> ConnectionFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return source.handleAsync((t, throwable) -> fn.apply(processResult(t, throwable), throwable));
    }

    @Override
    public <U> ConnectionFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return source.handleAsync((t, throwable) -> fn.apply(processResult(t, throwable), throwable), executor);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return source.toCompletableFuture();
    }
}
