package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class RestTemplateTracingInterceptorTest extends BaseZipkinTest {

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

        RestTemplateTracingInterceptor interceptor = new RestTemplateTracingInterceptor(Tracing.current());
        MyRequest request = spy(MyRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);

        Object[] args = new Object[]{request.getHeaders()};
        Map<Object, Object> context = ContextUtils.createContext();
        String method = "executeInternal";

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(request)
                .method(method)
                .args(args)
                .retValue(response)
                .throwable(null)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "http://google.com");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void fail() throws IOException {
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

        RestTemplateTracingInterceptor interceptor = new RestTemplateTracingInterceptor(Tracing.current());
        MyRequest request = spy(MyRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getRawStatusCode()).thenReturn(400);

        Object[] args = new Object[]{request.getHeaders()};
        Map<Object, Object> context = ContextUtils.createContext();
        String method = "executeInternal";

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(request)
                .method(method)
                .args(args)
                .retValue(response)
                .throwable(null)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "http://google.com");
        expectedMap.put("http.status_code", "400");
        expectedMap.put("error", "400");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    static class MyRequest extends AbstractClientHttpRequest {

        @Override
        protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
            return null;
        }

        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
            return null;
        }

        @Override
        public String getMethodValue() {
            return "GET";
        }

        @Override
        public URI getURI() {
            return URI.create("http://google.com");
        }
    }
}
