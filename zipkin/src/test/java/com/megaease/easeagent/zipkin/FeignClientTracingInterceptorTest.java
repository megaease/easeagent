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
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.BaseClientTracingInterceptor;
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
        Config config = this.createConfig(BaseClientTracingInterceptor.ENABLE_KEY, "true");
        String url = "https://google.com";
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

        FeignClientTracingInterceptor interceptor = new FeignClientTracingInterceptor(Tracing.current(), config);

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

        MethodInfo methodInfo = MethodInfo.builder().invoker(client).method(method).args(args).retValue(response).throwable(null).build();
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "https://google.com");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void fail() {
        Config config = this.createConfig(BaseClientTracingInterceptor.ENABLE_KEY, "true");
        String url = "https://google.com";
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

        FeignClientTracingInterceptor interceptor = new FeignClientTracingInterceptor(Tracing.current(), config);

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

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(client)
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
        String url = "https://google.com";
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

        FeignClientTracingInterceptor interceptor = new FeignClientTracingInterceptor(Tracing.current(), config);

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

        MethodInfo methodInfo = MethodInfo.builder().invoker(client).method(method).args(args).retValue(response).throwable(null).build();
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(spanInfoMap.isEmpty());
    }

}
