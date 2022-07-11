/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.redis.interceptor.initialize;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class CompletableFutureWrapperTest {
    private String attach = "127.0.0.1:9090";


    private CompletableFutureWrapper<DynamicFieldAccessorObj> createOne() {
        DynamicFieldAccessorObj dynamicFieldAccessorObj = new DynamicFieldAccessorObj();
        dynamicFieldAccessorObj.setChannelWriter(new DynamicFieldAccessorObj());
        CompletableFuture<DynamicFieldAccessorObj> connectionFuture = CompletableFuture.completedFuture(dynamicFieldAccessorObj);
        return new CompletableFutureWrapper(connectionFuture, attach);
    }

    @Test
    public void isDone() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        assertTrue(completableFutureWrapper.isDone());

    }

    private void checkDynamicFieldAccessorObj(DynamicFieldAccessorObj dynamicFieldAccessorObj) {
        assertEquals(attach, dynamicFieldAccessorObj.getEaseAgent$$DynamicField$$Data());
        assertTrue(dynamicFieldAccessorObj.getChannelWriter() instanceof DynamicFieldAccessorObj);
        assertEquals(attach, ((DynamicFieldAccessorObj) dynamicFieldAccessorObj.getChannelWriter()).getEaseAgent$$DynamicField$$Data());
    }

    @Test
    public void get() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        checkDynamicFieldAccessorObj(completableFutureWrapper.get());

    }

    @Test
    public void get1() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        checkDynamicFieldAccessorObj(completableFutureWrapper.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void join() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        checkDynamicFieldAccessorObj(completableFutureWrapper.join());
    }

    @Test
    public void getNow() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        DynamicFieldAccessorObj dynamicFieldAccessorObj = new DynamicFieldAccessorObj();
        checkDynamicFieldAccessorObj(completableFutureWrapper.getNow(dynamicFieldAccessorObj));
    }

    @Test
    public void complete() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        DynamicFieldAccessorObj dynamicFieldAccessorObj = new DynamicFieldAccessorObj();
        dynamicFieldAccessorObj.setChannelWriter(new DynamicFieldAccessorObj());
        completableFutureWrapper.complete(dynamicFieldAccessorObj);
        checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
        assertNotSame(dynamicFieldAccessorObj, completableFutureWrapper.get());
    }

    @Test
    public void completeExceptionally() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        completableFutureWrapper.completeExceptionally(new RuntimeException("test error"));
        checkDynamicFieldAccessorObj(completableFutureWrapper.get());
    }

    @Test
    public void thenApply() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        CompletableFuture<Boolean> booleanCompletableFuture = completableFutureWrapper.thenApply(dynamicFieldAccessorObj -> {
            checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
            return true;
        });
        assertTrue(booleanCompletableFuture.get());
    }

    @Test
    public void thenApplyAsync() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        CompletableFuture<Boolean> booleanCompletableFuture = completableFutureWrapper.thenApplyAsync(dynamicFieldAccessorObj -> {
            checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
            return true;
        });
        assertTrue(booleanCompletableFuture.get());

    }

    @Test
    public void thenApplyAsync1() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        CompletableFuture<Boolean> booleanCompletableFuture = completableFutureWrapper.thenApplyAsync(dynamicFieldAccessorObj -> {
            checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
            return true;
        }, Runnable::run);
        assertTrue(booleanCompletableFuture.get());
    }

    class TestRunTwo {
        final CompletableFutureWrapper<DynamicFieldAccessorObj> one = createOne();
        final CompletableFutureWrapper<DynamicFieldAccessorObj> other = createOne();
        final AtomicBoolean ran = new AtomicBoolean(false);
        final AtomicReference<DynamicFieldAccessorObj> result = new AtomicReference<>();

        private void check() throws Exception {
            assertTrue(ran.get());
            if (result.get() != null) {
                checkDynamicFieldAccessorObj(result.get());
            }
        }

        protected void setRan() {
            this.ran.set(true);
        }

        public void consumer(TestRunTwoConsumer consumer) throws Exception {
            consumer.accept(this);
            check();
        }
    }

    interface TestRunTwoConsumer {
        void accept(TestRunTwo t) throws Exception;
    }


    @Test
    public void thenAccept() throws Exception {
        new TestRunTwo().consumer(t -> {
            t.one.thenAccept(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            });
        });
    }

    @Test
    public void thenAcceptAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.thenAcceptAsync(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            });
            c.get();
        });
    }

    @Test
    public void thenAcceptAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.thenAcceptAsync(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            }, Runnable::run);
            c.get();
        });

    }

    @Test
    public void thenRun() throws Exception {
        new TestRunTwo().consumer(t -> {
            t.one.thenRun(t::setRan);
        });
    }

    @Test
    public void thenRunAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.thenRunAsync(t::setRan);
            c.get();
        });
    }

    @Test
    public void thenRunAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.thenRunAsync(t::setRan, Runnable::run);
            c.get();
        });
    }

    @Test
    public void thenCombine() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.thenCombine(t.other, (dynamicFieldAccessorObj, u) -> {
                t.setRan();
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                return dynamicFieldAccessorObj;
            });
            checkDynamicFieldAccessorObj(c.get());
        });
    }

    @Test
    public void thenCombineAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.thenCombineAsync(t.other, (dynamicFieldAccessorObj, u) -> {
                t.setRan();
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                return dynamicFieldAccessorObj;
            });
            checkDynamicFieldAccessorObj(c.get());
        });
    }

    @Test
    public void thenCombineAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.thenCombineAsync(t.other, (dynamicFieldAccessorObj, u) -> {
                t.setRan();
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                return dynamicFieldAccessorObj;
            }, Runnable::run);
            checkDynamicFieldAccessorObj(c.get());
        });
    }

    @Test
    public void thenAcceptBoth() throws Exception {
        new TestRunTwo().consumer(t -> {
            t.one.thenAcceptBoth(t.other, (dynamicFieldAccessorObj, dynamicFieldAccessorObj2) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            });
        });
    }

    @Test
    public void thenAcceptBothAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.thenAcceptBothAsync(t.other, (dynamicFieldAccessorObj, dynamicFieldAccessorObj2) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            });
            c.get();
        });
    }

    @Test
    public void thenAcceptBothAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.thenAcceptBothAsync(t.other, (dynamicFieldAccessorObj, dynamicFieldAccessorObj2) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            }, Runnable::run);
            c.get();
        });
    }

    @Test
    public void runAfterBoth() throws Exception {
        new TestRunTwo().consumer(t -> {
            t.one.runAfterBoth(t.other, t::setRan);
        });
    }

    @Test
    public void runAfterBothAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.runAfterBothAsync(t.other, t::setRan);
            c.get();
        });
    }

    @Test
    public void runAfterBothAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.runAfterBothAsync(t.other, t::setRan, Runnable::run);
            c.get();
        });
    }

    @Test
    public void applyToEither() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.applyToEither(t.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    t.setRan();
                    return dynamicFieldAccessorObj;
                });
            t.result.set(c.get());
        });
    }

    @Test
    public void applyToEitherAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.applyToEitherAsync(t.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    t.setRan();
                    return dynamicFieldAccessorObj;
                });
            t.result.set(c.get());
        });
    }

    @Test
    public void applyToEitherAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.applyToEitherAsync(t.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    t.setRan();
                    return dynamicFieldAccessorObj;
                }, Runnable::run);
            t.result.set(c.get());
        });
    }

    @Test
    public void acceptEither() throws Exception {
        new TestRunTwo().consumer(t -> {
            t.one.acceptEither(t.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    t.setRan();
                });
        });
    }

    @Test
    public void acceptEitherAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.acceptEitherAsync(t.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    t.setRan();
                });

            c.get();
        });
    }

    @Test
    public void acceptEitherAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.acceptEitherAsync(t.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    t.setRan();
                }, Runnable::run);
            c.get();
        });
    }

    @Test
    public void runAfterEither() throws Exception {
        new TestRunTwo().consumer(t -> {
            t.one.runAfterEither(t.other,
                t::setRan);
        });
    }

    @Test
    public void runAfterEitherAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.runAfterEitherAsync(t.other,
                t::setRan);
            c.get();
        });

    }

    @Test
    public void runAfterEitherAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<Void> c = t.one.runAfterEitherAsync(t.other,
                t::setRan, Runnable::run);
            c.get();
        });
    }

    @Test
    public void thenCompose() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.thenCompose(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
                return t.other;
            });
            checkDynamicFieldAccessorObj(c.get());
        });
    }

    @Test
    public void thenComposeAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.thenComposeAsync(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
                return t.other;
            });
            checkDynamicFieldAccessorObj(c.get());
        });
    }

    @Test
    public void thenComposeAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> c = t.one.thenComposeAsync(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
                return t.other;
            }, Runnable::run);
            checkDynamicFieldAccessorObj(c.get());
        });

    }

    @Test
    public void whenComplete() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = t.one.whenComplete((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            });
        });
    }

    @Test
    public void whenCompleteAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = t.one.whenCompleteAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            });
            connectionFuture.get();
        });
    }

    @Test
    public void whenCompleteAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = t.one.whenCompleteAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
            }, Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void handle() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = t.one.handle((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
                return null;
            });
        });
    }

    @Test
    public void handleAsync() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = t.one.handleAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
                return null;
            });
            connectionFuture.get();
        });
    }

    @Test
    public void handleAsync1() throws Exception {
        new TestRunTwo().consumer(t -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = t.one.handleAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                t.setRan();
                return null;
            }, Runnable::run);
            connectionFuture.get();
        });

    }

    @Test
    public void toCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        CompletableFuture<DynamicFieldAccessorObj> c = completableFutureWrapper.toCompletableFuture();
        assertNull(c.get().getValue());
    }

    @Test
    public void exceptionally() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        CompletableFuture<DynamicFieldAccessorObj> a = completableFutureWrapper.exceptionally(throwable -> null);
        checkDynamicFieldAccessorObj(a.get());
    }

    @Test
    public void cancel() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        assertFalse(completableFutureWrapper.cancel(true));
    }

    @Test
    public void isCancelled() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        assertFalse(completableFutureWrapper.isCancelled());
    }

    @Test
    public void isCompletedExceptionally() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        assertFalse(completableFutureWrapper.isCompletedExceptionally());
    }

    @Test
    public void obtrudeValue() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        String value = "newValue";
        completableFutureWrapper.obtrudeValue(new DynamicFieldAccessorObj().setValue(value).setChannelWriter(new DynamicFieldAccessorObj()));
        DynamicFieldAccessorObj obj = completableFutureWrapper.get();
        checkDynamicFieldAccessorObj(obj);
        assertEquals(value, obj.getValue());
    }

    @Test
    public void obtrudeException() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        completableFutureWrapper.obtrudeException(new RuntimeException("error"));
        assertTrue(completableFutureWrapper.isCompletedExceptionally());
    }

    @Test
    public void getNumberOfDependents() throws ExecutionException, InterruptedException {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        assertEquals(0, completableFutureWrapper.getNumberOfDependents());
    }

    @Test
    public void testToString() {
        CompletableFutureWrapper<DynamicFieldAccessorObj> completableFutureWrapper = createOne();
        completableFutureWrapper.toString();
        assertTrue(completableFutureWrapper.isDone());
    }
}
