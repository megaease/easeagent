package com.megaease.easeagent.zipkin;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.MethodInfo;
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
        KafkaMessageListenerTracingInterceptor interceptor = new KafkaMessageListenerTracingInterceptor(Tracing.current());

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

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("kafka.topic", "topic");
        expectedMap.put("kafka.key", "key");
        expectedMap.put("kafka.broker", "localhost:9092");
        Assert.assertEquals(expectedMap, spanInfoMap);
        root.finish();
    }
}
