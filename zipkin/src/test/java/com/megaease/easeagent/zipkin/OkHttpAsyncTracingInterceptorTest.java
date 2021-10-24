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
import com.megaease.easeagent.zipkin.http.okhttp.OkHttpAsyncTracingInterceptor;
import okhttp3.*;
import okhttp3.internal.connection.RealCall;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class OkHttpAsyncTracingInterceptorTest extends BaseZipkinTest {
    @Test
    public void success() {
        Config config = this.createConfig(BaseClientTracingInterceptor.ENABLE_KEY, "true");
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();

        OkHttpAsyncTracingInterceptor interceptor = new OkHttpAsyncTracingInterceptor(Tracing.current(), config);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().method("GET", null).url("https://httpbin.org/get").build();
        RealCall realCall = new RealCall(client, request, false);
        Response response = new Response.Builder()
                .code(200)
                .request(request)
                .message("")
                .protocol(Protocol.HTTP_1_1)
                .build();

        Map<Object, Object> context = ContextUtils.createContext();
        String method = "enqueue";

        Callback callback = mock(Callback.class);

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(realCall)
                .method(method)
                .args(new Object[]{callback})
                .retValue(response)
                .throwable(null)
                .build();
        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));

        //mock do something

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(methodInfo.getArgs()[0] instanceof OkHttpAsyncTracingInterceptor.InternalCallback);

    }

}
