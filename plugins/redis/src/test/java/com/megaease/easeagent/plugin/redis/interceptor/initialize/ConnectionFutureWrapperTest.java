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

import io.lettuce.core.ConnectionFuture;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public class ConnectionFutureWrapperTest {
    private String attach;
    private InetSocketAddress socketAddress;

    @Before
    public void before() throws SocketException {
        DatagramSocket s = new DatagramSocket(0);
        attach = "127.0.0.1:" + s.getLocalPort();
        socketAddress = new InetSocketAddress("127.0.0.1", s.getLocalPort());

    }

    private ConnectionFutureWrapper<DynamicFieldAccessorObj> createOne() {
        DynamicFieldAccessorObj dynamicFieldAccessorObj = new DynamicFieldAccessorObj();
        dynamicFieldAccessorObj.setChannelWriter(new DynamicFieldAccessorObj());
        ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = ConnectionFuture.completed(socketAddress, dynamicFieldAccessorObj);
        return new ConnectionFutureWrapper(connectionFuture, attach);
    }

    @Test
    public void cancel() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        assertFalse(connectionFutureWrapper.cancel(true));
    }

    @Test
    public void isCancelled() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        assertFalse(connectionFutureWrapper.isCancelled());

    }

    @Test
    public void isDone() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        assertTrue(connectionFutureWrapper.isDone());

    }

    private void checkDynamicFieldAccessorObj(DynamicFieldAccessorObj dynamicFieldAccessorObj) {
        assertEquals(attach, dynamicFieldAccessorObj.getEaseAgent$$DynamicField$$Data());
        assertTrue(dynamicFieldAccessorObj.getChannelWriter() instanceof DynamicFieldAccessorObj);
        assertEquals(attach, ((DynamicFieldAccessorObj) dynamicFieldAccessorObj.getChannelWriter()).getEaseAgent$$DynamicField$$Data());
    }

    @Test
    public void get() throws ExecutionException, InterruptedException {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        checkDynamicFieldAccessorObj(connectionFutureWrapper.get());
    }

    @Test
    public void get1() throws InterruptedException, ExecutionException, TimeoutException {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        checkDynamicFieldAccessorObj(connectionFutureWrapper.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void getRemoteAddress() throws SocketException {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        assertEquals(socketAddress, connectionFutureWrapper.getRemoteAddress());
    }

    @Test
    public void join() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        checkDynamicFieldAccessorObj(connectionFutureWrapper.join());
    }

    @Test
    public void thenApply() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        connectionFutureWrapper.thenApply(dynamicFieldAccessorObj -> {
            checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
            return null;
        });
    }

    @Test
    public void thenApplyAsync() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        connectionFutureWrapper.thenApplyAsync(dynamicFieldAccessorObj -> {
            checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
            return null;
        });
    }

    @Test
    public void thenApplyAsync1() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        connectionFutureWrapper.thenApplyAsync(dynamicFieldAccessorObj -> {
            checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
            return null;
        }, command -> {
        });
    }

    @Test
    public void thenAccept() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        connectionFutureWrapper.thenAccept(this::checkDynamicFieldAccessorObj);
    }

    @Test
    public void thenAcceptAsync() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        connectionFutureWrapper.thenAcceptAsync(this::checkDynamicFieldAccessorObj);
    }

    @Test
    public void thenAcceptAsync1() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        connectionFutureWrapper.thenAcceptAsync(this::checkDynamicFieldAccessorObj, command -> {
        });
    }

    @Test
    public void thenRun() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        AtomicBoolean ran = new AtomicBoolean(false);
        connectionFutureWrapper.thenRun(() -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    public void thenRunAsync() throws ExecutionException, InterruptedException {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        AtomicBoolean ran = new AtomicBoolean(false);
        ConnectionFuture connectionFuture = connectionFutureWrapper.thenRunAsync(() -> ran.set(true));
        connectionFuture.get();
        assertTrue(ran.get());

    }

    @Test
    public void thenRunAsync1() {
        ConnectionFutureWrapper<DynamicFieldAccessorObj> connectionFutureWrapper = createOne();
        AtomicBoolean ran = new AtomicBoolean(false);
        connectionFutureWrapper.thenRunAsync(() -> ran.set(true), Runnable::run);
        assertTrue(ran.get());
    }

    class TestRunTwo {
        final ConnectionFutureWrapper<DynamicFieldAccessorObj> one = createOne();
        final ConnectionFutureWrapper<DynamicFieldAccessorObj> other = createOne();
        final AtomicBoolean ran = new AtomicBoolean(false);
        final AtomicReference<DynamicFieldAccessorObj> result = new AtomicReference<>();

        public void check() throws Exception {
            assertTrue(ran.get());
            if (result.get() != null) {
                checkDynamicFieldAccessorObj(result.get());
            }
        }
    }

    interface TestRunTwoConsumer {
        void accept(TestRunTwo t) throws Exception;
    }

    public void consumerTestRunTwo(TestRunTwoConsumer consumer) throws Exception {
        TestRunTwo testRunTwo = new TestRunTwo();
        consumer.accept(testRunTwo);
        testRunTwo.check();
    }

    @Test
    public void thenCombine() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.thenCombine(testRunTwo.other, (dynamicFieldAccessorObj, u) -> {
                testRunTwo.ran.set(true);
                return dynamicFieldAccessorObj;
            });
            testRunTwo.result.set(connectionFuture.get());
        });
    }

    @Test
    public void thenCombineAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.thenCombineAsync(testRunTwo.other, (dynamicFieldAccessorObj, u) -> {
                testRunTwo.ran.set(true);
                return dynamicFieldAccessorObj;
            });
            testRunTwo.result.set(connectionFuture.get());
        });
    }

    @Test
    public void thenCombineAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.thenCombineAsync(testRunTwo.other, (dynamicFieldAccessorObj, u) -> {
                testRunTwo.ran.set(true);
                return dynamicFieldAccessorObj;
            }, Runnable::run);
            testRunTwo.result.set(connectionFuture.get());
        });
    }

    @Test
    public void thenAcceptBoth() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            testRunTwo.one.thenAcceptBoth(testRunTwo.other, (dynamicFieldAccessorObj, dynamicFieldAccessorObj2) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
            });
        });
    }

    @Test
    public void thenAcceptBothAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.thenAcceptBothAsync(testRunTwo.other, (dynamicFieldAccessorObj, dynamicFieldAccessorObj2) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
            });
            connectionFuture.get();
        });
    }

    @Test
    public void thenAcceptBothAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.thenAcceptBothAsync(testRunTwo.other, (dynamicFieldAccessorObj, dynamicFieldAccessorObj2) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
            }, Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void runAfterBoth() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            testRunTwo.one.runAfterBoth(testRunTwo.other, () -> testRunTwo.ran.set(true));
        });
    }

    @Test
    public void runAfterBothAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.runAfterBothAsync(testRunTwo.other, () -> testRunTwo.ran.set(true));
            connectionFuture.get();
        });
    }

    @Test
    public void runAfterBothAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.runAfterBothAsync(testRunTwo.other,
                () -> testRunTwo.ran.set(true), Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void applyToEither() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.applyToEither(testRunTwo.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    testRunTwo.ran.set(true);
                    return dynamicFieldAccessorObj;
                });
            testRunTwo.result.set(connectionFuture.get());
        });
    }

    @Test
    public void applyToEitherAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.applyToEitherAsync(testRunTwo.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    testRunTwo.ran.set(true);
                    return dynamicFieldAccessorObj;
                });
            testRunTwo.result.set(connectionFuture.get());
        });
    }

    @Test
    public void applyToEitherAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.applyToEitherAsync(testRunTwo.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    testRunTwo.ran.set(true);
                    return dynamicFieldAccessorObj;
                }, Runnable::run);
            testRunTwo.result.set(connectionFuture.get());
        });
    }

    @Test
    public void acceptEither() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            testRunTwo.one.acceptEither(testRunTwo.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    testRunTwo.ran.set(true);
                });
        });

    }

    @Test
    public void acceptEitherAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.acceptEitherAsync(testRunTwo.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    testRunTwo.ran.set(true);
                });
            connectionFuture.get();
        });
    }

    @Test
    public void acceptEitherAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.acceptEitherAsync(testRunTwo.other,
                dynamicFieldAccessorObj -> {
                    checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                    testRunTwo.ran.set(true);
                }, Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void runAfterEither() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            testRunTwo.one.runAfterEither(testRunTwo.other,
                () -> testRunTwo.ran.set(true));
        });
    }

    @Test
    public void runAfterEitherAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.runAfterEitherAsync(testRunTwo.other,
                () -> testRunTwo.ran.set(true));
            connectionFuture.get();
        });
    }

    @Test
    public void runAfterEitherAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<Void> connectionFuture = testRunTwo.one.runAfterEitherAsync(testRunTwo.other,
                () -> testRunTwo.ran.set(true), Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void thenCompose() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            testRunTwo.one.thenCompose(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                return testRunTwo.other;
            });
        });
    }

    @Test
    public void thenCompose1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            testRunTwo.one.thenCompose((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                assertNull(throwable);
                return testRunTwo.other;
            });
        });

    }

    @Test
    public void thenComposeAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.thenComposeAsync(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                return testRunTwo.other;
            });
            connectionFuture.get();
        });
    }

    @Test
    public void thenComposeAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.thenComposeAsync(dynamicFieldAccessorObj -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                return testRunTwo.other;
            }, Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void exceptionally() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.exceptionally(dynamicFieldAccessorObj -> {
                return null;
            });
            testRunTwo.ran.set(true);
        });
    }

    @Test
    public void whenComplete() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.whenComplete((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
            });
        });
    }

    @Test
    public void whenCompleteAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.whenCompleteAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
            });
            connectionFuture.get();
        });
    }

    @Test
    public void whenCompleteAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.whenCompleteAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
            }, Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void handle() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.handle((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                return null;
            });
        });
    }

    @Test
    public void handleAsync() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.handleAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                return null;
            });
            connectionFuture.get();
        });
    }

    @Test
    public void handleAsync1() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            ConnectionFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.handleAsync((dynamicFieldAccessorObj, throwable) -> {
                checkDynamicFieldAccessorObj(dynamicFieldAccessorObj);
                testRunTwo.ran.set(true);
                return null;
            }, Runnable::run);
            connectionFuture.get();
        });
    }

    @Test
    public void toCompletableFuture() throws Exception {
        consumerTestRunTwo(testRunTwo -> {
            CompletableFuture<DynamicFieldAccessorObj> connectionFuture = testRunTwo.one.toCompletableFuture();
            testRunTwo.ran.set(true);
            connectionFuture.get();
        });
    }
}
