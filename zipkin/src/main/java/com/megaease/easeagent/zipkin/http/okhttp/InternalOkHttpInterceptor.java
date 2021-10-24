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

package com.megaease.easeagent.zipkin.http.okhttp;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class InternalOkHttpInterceptor implements Interceptor {

    private final AgentInterceptorChain.Builder chainBuilder;

    private final AgentInterceptorChainInvoker chainInvoker;

    protected static final String REQUEST_BUILDER_KEY = OkHttpClientProceedInterceptor.class.getName() + ".newRequestBuilder";

    public InternalOkHttpInterceptor(AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        MethodInfo methodInfo = MethodInfo.builder()
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        this.chainInvoker.doBefore(chainBuilder, methodInfo, context);
        Request.Builder requestBuilder = ContextUtils.getFromContext(context, REQUEST_BUILDER_KEY);
        Response response;
        try {
            response = chain.proceed(requestBuilder.build());
            methodInfo.setRetValue(response);
        } finally {
            response = (Response) this.chainInvoker.doAfter(chainBuilder, methodInfo, context);
        }
        return response;
    }
}
