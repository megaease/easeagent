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
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.ChainBuilderFactory;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.webclient.WebClientTracingInterceptor;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class WebClientTracingInterceptorTest extends BaseZipkinTest {

    @SneakyThrows
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

        ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://www.xxx.com/users/1")).build();
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK).build();

        ArrayList<ClientResponse> results = new ArrayList<>();
        results.add(clientResponse);
        MethodInfo methodInfo = MethodInfo.builder()
                .method("filter")
                .args(new Object[]{clientRequest})
                .retValue(results)
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        WebClientTracingInterceptor interceptor = new WebClientTracingInterceptor(Tracing.current());
        AgentInterceptorChain interceptorChain = mock(AgentInterceptorChain.class);
        interceptor.before(methodInfo, context, interceptorChain);

        AgentInterceptorChain.Builder builder = ChainBuilderFactory.DEFAULT.createBuilder();
        builder.addInterceptor(interceptor);

        interceptor.after(methodInfo, context, interceptorChain);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.path", "/users/1");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }
}
