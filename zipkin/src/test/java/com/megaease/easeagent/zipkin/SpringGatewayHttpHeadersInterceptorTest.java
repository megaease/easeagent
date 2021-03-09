package com.megaease.easeagent.zipkin;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.flux.GatewayCons;
import com.megaease.easeagent.zipkin.http.flux.SpringGatewayHttpHeadersInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringGatewayHttpHeadersInterceptorTest extends BaseZipkinTest {
    @Test
    public void success() {
        Tracer tracer = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build().tracer();
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(Tracing.current()));
        ScopedSpan root = tracer.startScopedSpan("root");

        Map<String, Object> attrMap = new HashMap<>();
        attrMap.put(GatewayCons.TRACE_CONTEXT_ATTR, currentTraceContext.get());

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attrMap);
        when(exchange.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));

        HttpHeaders httpHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(httpHeaders);


        Map<Object, Object> context = ContextUtils.createContext();
        AgentInterceptorChainInvoker.getInstance().doBefore(builder, null, null, new Object[]{null, exchange}, context);
        Object ret = AgentInterceptorChainInvoker.getInstance().doAfter(null, null, new Object[]{null, exchange}, httpHeaders, null, context);
        root.finish();
        HttpHeaders retValue = (HttpHeaders) ret;
        Assert.assertNotNull(retValue);
    }
}
