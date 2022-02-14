/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.redis.interceptor.initialize;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.junit.Test;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class CommonRedisClientInterceptorTest {

    @Test
    public void doAfter() throws SocketException {
        CommonRedisClientInterceptor commonRedisClientInterceptor = new CommonRedisClientInterceptor();
        RedisClusterClient redisClusterClient = RedisClusterClient.create(RedisURI.create("127.0.0.1", 111));
        MethodInfo methodInfo = MethodInfo.builder().invoker(redisClusterClient).retValue(null).throwable(new RuntimeException()).build();
        commonRedisClientInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(methodInfo.getRetValue());

        CompletableFuture completableFuture = CompletableFuture.completedFuture("testCompletableFuture");
        methodInfo = MethodInfo.builder().invoker(redisClusterClient).retValue(completableFuture).build();
        commonRedisClientInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertTrue(methodInfo.getRetValue() instanceof CompletableFutureWrapper);

        RedisClient redisClient = RedisClient.create(RedisURI.create("127.0.0.1", 111));
        DatagramSocket s = new DatagramSocket(0);
        ConnectionFuture connectionFuture = ConnectionFuture.completed(new InetSocketAddress(s.getLocalPort()), "test");
        methodInfo = MethodInfo.builder().invoker(redisClient).retValue(connectionFuture).build();
        commonRedisClientInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertTrue(methodInfo.getRetValue() instanceof ConnectionFutureWrapper);
    }

    @Test
    public void processRedisClient() throws SocketException {
        CommonRedisClientInterceptor commonRedisClientInterceptor = new CommonRedisClientInterceptor();
        RedisClient redisClient = RedisClient.create(RedisURI.create("127.0.0.1", 111));
        DatagramSocket s = new DatagramSocket(0);
        ConnectionFuture connectionFuture = ConnectionFuture.completed(new InetSocketAddress(s.getLocalPort()), "test");
        MethodInfo methodInfo = MethodInfo.builder().invoker(redisClient).retValue(connectionFuture).build();
        commonRedisClientInterceptor.processRedisClient(methodInfo, EaseAgent.getContext());
        assertTrue(methodInfo.getRetValue() instanceof ConnectionFutureWrapper);
    }

    @Test
    public void processRedisClusterClient() {
        CommonRedisClientInterceptor commonRedisClientInterceptor = new CommonRedisClientInterceptor();
        RedisClusterClient redisClusterClient = RedisClusterClient.create(RedisURI.create("127.0.0.1", 111));
        CompletableFuture completableFuture = CompletableFuture.completedFuture("testCompletableFuture");
        MethodInfo methodInfo = MethodInfo.builder().invoker(redisClusterClient).retValue(completableFuture).build();
        commonRedisClientInterceptor.processRedisClusterClient(methodInfo, EaseAgent.getContext());
        assertTrue(methodInfo.getRetValue() instanceof CompletableFutureWrapper);
    }

    @Test
    public void order() {
        CommonRedisClientInterceptor commonRedisClientInterceptor = new CommonRedisClientInterceptor();
        assertTrue(commonRedisClientInterceptor.order() < Order.TRACING.getOrder());
    }

    @Test
    public void getType() {
        CommonRedisClientInterceptor commonRedisClientInterceptor = new CommonRedisClientInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, commonRedisClientInterceptor.getType());
    }
}
