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
import com.megaease.easeagent.zipkin.http.httpclient5.HttpClient5AsyncTracingInterceptor;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.spi.http.HttpContext;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpClient5AsyncTracingInterceptorTest extends BaseZipkinTest {
    @SneakyThrows
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

        HttpClient5AsyncTracingInterceptor interceptor = new HttpClient5AsyncTracingInterceptor(Tracing.current(), config);

        HttpHost target = HttpHost.create("https://httpbin.org");

        HttpAsyncClient httpclient = mock(HttpAsyncClient.class);

        HttpResponse response = mock(HttpResponse.class);
        when(response.getCode()).thenReturn(200);


        final SimpleHttpRequest request = SimpleRequestBuilder.get()
                .setHttpHost(target)
                .setPath("/get")
                .build();

        Object[] args = new Object[]{SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                mock(HandlerFactory.class),
                mock(HttpContext.class),
                mock(FutureCallback.class)};
        Map<Object, Object> context = ContextUtils.createContext();
        String method = "execute";

        MethodInfo methodInfo = MethodInfo.builder().invoker(httpclient).method(method).args(args).retValue(response).throwable(null).build();
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        HttpClient5AsyncTracingInterceptor.InternalFutureCallback internalFutureCallback =
                (HttpClient5AsyncTracingInterceptor.InternalFutureCallback) methodInfo.getArgs()[4];

        internalFutureCallback.completed(response);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "https://httpbin.org/get");

        Assert.assertEquals(expectedMap, spanInfoMap);
    }

}
