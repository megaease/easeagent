//package com.megaease.easeagent.sniffer.lettuce.v5.advice;
//
//import com.megaease.easeagent.core.Classes;
//import com.megaease.easeagent.core.Definition;
//import com.megaease.easeagent.core.DynamicFieldAccessor;
//import io.lettuce.core.ConnectionFuture;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.net.SocketAddress;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.function.BiConsumer;
//import java.util.function.BiFunction;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//public class ConnectionFutureAdviceTest {
//
//    @Test
//    public void success() throws Exception {
//        Definition.Default def = new GenConnectionFutureAdvice().define(Definition.Default.EMPTY);
//        ClassLoader loader = this.getClass().getClassLoader();
//        List<Class<?>> classList = Classes.transform(this.getClass().getName() + "$MyConnectionFuture")
//                .with(def)
//                .load(loader);
//
//        Object o = classList.get(0).newInstance();
//        Assert.assertTrue(o instanceof DynamicFieldAccessor);
//    }
//
//    static class MyConnectionFuture<T> implements ConnectionFuture<T> {
//
//        @Override
//        public boolean cancel(boolean mayInterruptIfRunning) {
//            return false;
//        }
//
//        @Override
//        public boolean isCancelled() {
//            return false;
//        }
//
//        @Override
//        public boolean isDone() {
//            return false;
//        }
//
//        @Override
//        public T get() throws InterruptedException, ExecutionException {
//            return null;
//        }
//
//        @Override
//        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
//            return null;
//        }
//
//        @Override
//        public SocketAddress getRemoteAddress() {
//            return null;
//        }
//
//        @Override
//        public T join() {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenApply(Function<? super T, ? extends U> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> thenAccept(Consumer<? super T> action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> thenRun(Runnable action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> thenRunAsync(Runnable action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> thenRunAsync(Runnable action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public <U, V> ConnectionFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
//            return null;
//        }
//
//        @Override
//        public <U, V> ConnectionFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
//            return null;
//        }
//
//        @Override
//        public <U, V> ConnectionFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenCompose(BiFunction<? super T, ? super Throwable, ? extends CompletionStage<U>> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
//            return null;
//        }
//
//        @Override
//        public ConnectionFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
//            return null;
//        }
//
//        @Override
//        public <U> ConnectionFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
//            return null;
//        }
//
//        @Override
//        public CompletableFuture<T> toCompletableFuture() {
//            return null;
//        }
//    }
//}
