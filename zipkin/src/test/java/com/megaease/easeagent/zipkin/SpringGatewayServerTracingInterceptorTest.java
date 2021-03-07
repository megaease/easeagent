package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.flux.SpringGatewayServerTracingInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringGatewayServerTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracing.newBuilder()
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

        SpringGatewayServerTracingInterceptor interceptor = new SpringGatewayServerTracingInterceptor(Tracing.current());

        Map<String, Object> attrMap = new HashMap<>();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(exchange.getAttributes()).thenReturn(attrMap);
        when(exchange.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));

        HttpHeaders httpHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(httpHeaders);
        when(request.getPath()).thenReturn(RequestPath.parse(URI.create("http://httpbin.org/anything"), ""));
        when(request.getURI()).thenReturn(URI.create("http://httpbin.org/anything"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getMethodValue()).thenReturn(HttpMethod.GET.name());

        when(response.getRawStatusCode()).thenReturn(200);

        Object[] args = new Object[]{exchange};
        Map<Object, Object> context = ContextUtils.createContext();

        interceptor.before(null, null, args, context);
        // mock do something
        // mock do something end
        interceptor.after(null, "doFilterInternal", args, null, null, context);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.path", "/anything");
        expectedMap.put("http.method", "GET");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void fail() {
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracing.newBuilder()
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

        SpringGatewayServerTracingInterceptor interceptor = new SpringGatewayServerTracingInterceptor(Tracing.current());

        Map<String, Object> attrMap = new HashMap<>();
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(exchange.getAttributes()).thenReturn(attrMap);
        when(exchange.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));

        HttpHeaders httpHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(httpHeaders);
        when(request.getPath()).thenReturn(RequestPath.parse(URI.create("http://httpbin.org/anything"), ""));
        when(request.getURI()).thenReturn(URI.create("http://httpbin.org/anything"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getMethodValue()).thenReturn(HttpMethod.GET.name());

        when(response.getRawStatusCode()).thenReturn(400);

        Object[] args = new Object[]{exchange};
        Map<Object, Object> context = ContextUtils.createContext();

        interceptor.before(null, null, args, context);
        // mock do something
        // mock do something end
        interceptor.after(null, "doFilterInternal", args, null, null, context);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.path", "/anything");
        expectedMap.put("http.method", "GET");
        expectedMap.put("error", "400");
        expectedMap.put("http.status_code", "400");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }
}
