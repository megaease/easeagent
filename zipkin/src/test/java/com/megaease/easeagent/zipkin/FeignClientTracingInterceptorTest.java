package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.FeignClientTracingInterceptor;
import feign.Client;
import feign.Request;
import feign.Response;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class FeignClientTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        String url = "http://google.com";
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

        FeignClientTracingInterceptor interceptor = new FeignClientTracingInterceptor(Tracing.current());

        Map<String, Collection<String>> headers = new HashMap<>();
        Request request = Request.create(Request.HttpMethod.GET, url, headers, "ok".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, null);
        Request.Options options = new Request.Options();
        Response response = Response.builder()
                .status(200)
                .request(request)
                .build();
        Client client = mock(Client.class);
        Object[] args = new Object[]{request, options};
        Map<Object, Object> context = ContextUtils.createContext();
        String method = "execute";
        interceptor.before(client, method, args, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(request, method, args, response, null, context, mock(AgentInterceptorChain.class));

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "http://google.com");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void fail() {
        String url = "http://google.com";
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

        FeignClientTracingInterceptor interceptor = new FeignClientTracingInterceptor(Tracing.current());

        Map<String, Collection<String>> headers = new HashMap<>();
        Request request = Request.create(Request.HttpMethod.GET, url, headers, "ok".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, null);
        Request.Options options = new Request.Options();
        Response response = Response.builder()
                .status(400)
                .request(request)
                .build();
        Client client = mock(Client.class);
        Object[] args = new Object[]{request, options};
        Map<Object, Object> context = ContextUtils.createContext();
        String method = "execute";
        interceptor.before(client, method, args, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(request, method, args, response, null, context, mock(AgentInterceptorChain.class));

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "http://google.com");
        expectedMap.put("http.status_code", "400");
        expectedMap.put("error", "400");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }
}
