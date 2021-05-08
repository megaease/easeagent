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

package com.megaease.easeagent.zipkin;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.redis.CommonRedisTracingInterceptor;
import com.megaease.easeagent.zipkin.redis.JedisTracingInterceptor;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JedisTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Config config = this.createConfig(CommonRedisTracingInterceptor.ENABLE_KEY, "true");
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracer tracer = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        spanInfoMap.putAll(tmpMap);
                        Assert.assertNull(span.error());
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();

        ScopedSpan root = tracer.startScopedSpan("root");

        JedisTracingInterceptor interceptor = new JedisTracingInterceptor(Tracing.current(), config);

        Jedis jedis = mock(Jedis.class);
        ProtocolCommand cmd = mock(ProtocolCommand.class);
        when(cmd.getRaw()).thenReturn(SafeEncoder.encode("get"));
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(jedis)
                .method("get")
                .args(new Object[]{cmd})
                .build();
        Map<Object, Object> context = ContextUtils.createContext();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        root.finish();

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("redis.method", "get");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void error() {
        Config config = this.createConfig(CommonRedisTracingInterceptor.ENABLE_KEY, "true");
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracer tracer = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        spanInfoMap.putAll(tmpMap);
                        Assert.assertNotNull(span.error());
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();

        ScopedSpan root = tracer.startScopedSpan("root");

        JedisTracingInterceptor interceptor = new JedisTracingInterceptor(Tracing.current(), config);

        Jedis jedis = mock(Jedis.class);
        ProtocolCommand cmd = mock(ProtocolCommand.class);
        when(cmd.getRaw()).thenReturn(SafeEncoder.encode("get"));
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(jedis)
                .method("get")
                .throwable(new Exception("mock err"))
                .args(new Object[]{cmd})
                .build();
        Map<Object, Object> context = ContextUtils.createContext();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        root.finish();

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("redis.method", "get");
        Assert.assertEquals(expectedMap, spanInfoMap);


    }

    @Test
    public void disableTracing() {
        Config config = this.createConfig(CommonRedisTracingInterceptor.ENABLE_KEY, "false");
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracer tracer = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        spanInfoMap.putAll(tmpMap);
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();

        ScopedSpan root = tracer.startScopedSpan("root");

        JedisTracingInterceptor interceptor = new JedisTracingInterceptor(Tracing.current(), config);

        Jedis jedis = mock(Jedis.class);
        ProtocolCommand cmd = mock(ProtocolCommand.class);
        when(cmd.getRaw()).thenReturn(SafeEncoder.encode("get"));
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(jedis)
                .method("get")
                .args(new Object[]{cmd})
                .build();
        Map<Object, Object> context = ContextUtils.createContext();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        root.finish();

        Assert.assertTrue(spanInfoMap.isEmpty());
    }

}
