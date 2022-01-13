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

package com.megaease.easeagent.plugin.okhttp.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.okhttp.advice.OkHttpAdvice;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AdviceTo(value = OkHttpAdvice.class, qualifier = "enqueue")
public class OkHttpAsyncTracingInterceptor implements NonReentrantInterceptor {
    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        Object realCall = methodInfo.getInvoker();
        Request originalRequest = AgentFieldReflectAccessor.getFieldValue(realCall, "originalRequest");
        if (originalRequest == null) {
            return;
        }
        Request.Builder requestBuilder = originalRequest.newBuilder();
        InternalRequest request = new InternalRequest(originalRequest, requestBuilder);
        RequestContext requestContext = context.clientRequest(request);
        HttpUtils.handleReceive(requestContext.span().start(), request);
        context.put(OkHttpAsyncTracingInterceptor.class, requestContext);
        Callback callback = (Callback) methodInfo.getArgs()[0];
        InternalCallback internalCallback = new InternalCallback(callback, request.method(), requestContext);
        methodInfo.changeArg(0, internalCallback);
        AgentFieldReflectAccessor.setFieldValue(realCall, "originalRequest", requestBuilder.build());
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        RequestContext requestContext = context.remove(OkHttpAsyncTracingInterceptor.class);
        if (requestContext == null) {
            return;
        }
        try (Scope scope = requestContext.scope()) {
            if (methodInfo.isSuccess()) {
                return;
            }
            requestContext.span().error(methodInfo.getThrowable()).finish();
        }
    }

    public static class InternalCallback implements Callback {
        private final Callback delegate;
        private final String method;
        private final RequestContext requestContext;

        public InternalCallback(Callback delegate, String method, RequestContext requestContext) {
            this.delegate = delegate;
            this.method = method;
            this.requestContext = requestContext;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            this.delegate.onFailure(call, e);
            if (this.requestContext != null) {
                this.requestContext.span().abandon();
            }
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            this.delegate.onResponse(call, response);
            if (this.requestContext != null) {
                InternalResponse internalResponse = new InternalResponse(null, method, response);
                HttpUtils.save(requestContext.span(), internalResponse);
                requestContext.finish(internalResponse);
            }
        }
    }
}
