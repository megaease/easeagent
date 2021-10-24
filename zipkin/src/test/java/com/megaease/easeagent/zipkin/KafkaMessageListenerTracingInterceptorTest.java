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
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.kafka.spring.KafkaMessageListenerTracingInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class KafkaMessageListenerTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void invokeSuccess() {
        Config config = this.createConfig(KafkaMessageListenerTracingInterceptor.ENABLE_KEY, "true");
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
        KafkaMessageListenerTracingInterceptor interceptor = new KafkaMessageListenerTracingInterceptor(Tracing.current(), config);

        Headers headers = new RecordHeaders();
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>("topic", 1, 1, System.currentTimeMillis(), TimestampType.CREATE_TIME, 11L, "key".getBytes(StandardCharsets.UTF_8).length, "value".getBytes(StandardCharsets.UTF_8).length, "key", "value", headers);

        MethodInfo methodInfo = MethodInfo.builder()
                .method("onMessage")
                .args(new Object[]{consumerRecord})
                .build();

        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.MQ_URI, "localhost:9092");
        interceptor.before(methodInfo, context, this.mockChain());

        interceptor.after(methodInfo, context, this.mockChain());
        root.finish();

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("kafka.topic", "topic");
        expectedMap.put("kafka.key", "key");
        expectedMap.put("kafka.broker", "localhost:9092");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void disableTracing() {
        Config config = this.createConfig(KafkaMessageListenerTracingInterceptor.ENABLE_KEY, "false");
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
        KafkaMessageListenerTracingInterceptor interceptor = new KafkaMessageListenerTracingInterceptor(Tracing.current(), config);

        Headers headers = new RecordHeaders();
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>("topic", 1, 1, System.currentTimeMillis(), TimestampType.CREATE_TIME, 11L, "key".getBytes(StandardCharsets.UTF_8).length, "value".getBytes(StandardCharsets.UTF_8).length, "key", "value", headers);

        MethodInfo methodInfo = MethodInfo.builder()
                .method("onMessage")
                .args(new Object[]{consumerRecord})
                .build();

        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.MQ_URI, "localhost:9092");
        interceptor.before(methodInfo, context, this.mockChain());

        interceptor.after(methodInfo, context, this.mockChain());
        root.finish();

        Assert.assertTrue(spanInfoMap.isEmpty());
    }
}
