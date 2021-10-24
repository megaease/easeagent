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

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
import com.megaease.easeagent.zipkin.http.RestTemplateTracingInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class RestTemplateTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Config config = this.createConfig(BaseClientTracingInterceptor.ENABLE_KEY, "true");
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

        RestTemplateTracingInterceptor interceptor = new RestTemplateTracingInterceptor(Tracing.current(), config);
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
        expectedMap.put("http.path", "https://google.com");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void fail() throws IOException {
        Config config = this.createConfig(BaseClientTracingInterceptor.ENABLE_KEY, "true");
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

        RestTemplateTracingInterceptor interceptor = new RestTemplateTracingInterceptor(Tracing.current(), config);
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
        expectedMap.put("http.path", "https://google.com");
        expectedMap.put("http.status_code", "400");
        expectedMap.put("error", "400");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void disableTracing() {
        Config config = this.createConfig(BaseClientTracingInterceptor.ENABLE_KEY, "false");
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

        RestTemplateTracingInterceptor interceptor = new RestTemplateTracingInterceptor(Tracing.current(), config);
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

        Assert.assertTrue(spanInfoMap.isEmpty());
    }

    static class MyRequest extends AbstractClientHttpRequest {

        @Override
        @Nonnull
        protected OutputStream getBodyInternal(@Nonnull HttpHeaders headers) {
            return mock(OutputStream.class);
        }

        @Override
        @Nonnull
        protected ClientHttpResponse executeInternal(@Nonnull HttpHeaders headers) {
            return mock(ClientHttpResponse.class);
        }

        @Override
        @Nonnull
        public String getMethodValue() {
            return "GET";
        }

        @Override
        @Nonnull
        public URI getURI() {
            return URI.create("https://google.com");
        }
    }
}
