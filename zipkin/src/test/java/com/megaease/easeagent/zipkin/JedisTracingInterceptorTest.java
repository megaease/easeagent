package com.megaease.easeagent.zipkin;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
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

        JedisTracingInterceptor interceptor = new JedisTracingInterceptor();

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

}