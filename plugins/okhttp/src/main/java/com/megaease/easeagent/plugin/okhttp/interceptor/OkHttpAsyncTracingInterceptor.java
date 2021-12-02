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

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;
import com.megaease.easeagent.plugin.okhttp.advice.OkHttpAdvice;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AdviceTo(value = OkHttpAdvice.class, qualifier = "enqueue")
public class OkHttpAsyncTracingInterceptor implements FirstEnterInterceptor {
    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        Object realCall = methodInfo.getInvoker();
        Request originalRequest = AgentFieldReflectAccessor.getFieldValue(realCall, "originalRequest");
        if (originalRequest == null) {
            return;
        }
        Request.Builder requestBuilder = originalRequest.newBuilder();
        InternalRequest request = new InternalRequest(originalRequest, requestBuilder);
        ProgressContext progressContext = context.nextProgress(request);
        HttpUtils.handleReceive(progressContext.span().start(), request);
        context.put(OkHttpAsyncTracingInterceptor.class, progressContext);
        Callback callback = (Callback) methodInfo.getArgs()[0];
        InternalCallback internalCallback = new InternalCallback(callback, request.method(), progressContext);
        methodInfo.changeArg(0, internalCallback);
        AgentFieldReflectAccessor.setFieldValue(realCall, "originalRequest", requestBuilder.build());
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ProgressContext progressContext = context.remove(OkHttpAsyncTracingInterceptor.class);
        if (progressContext == null) {
            return;
        }
        try (Scope scope = progressContext.scope()) {
            if (methodInfo.isSuccess()) {
                return;
            }
            progressContext.span().error(methodInfo.getThrowable()).finish();
        }
    }

    public static class InternalCallback implements Callback {
        private final Callback delegate;
        private final String method;
        private final ProgressContext progressContext;

        public InternalCallback(Callback delegate, String method, ProgressContext progressContext) {
            this.delegate = delegate;
            this.method = method;
            this.progressContext = progressContext;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            this.delegate.onFailure(call, e);
            if (this.progressContext != null) {
                this.progressContext.span().abandon();
            }
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            this.delegate.onResponse(call, response);
            if (this.progressContext != null) {
                InternalResponse internalResponse = new InternalResponse(null, method, response);
                HttpUtils.save(progressContext.span(), internalResponse);
                progressContext.finish(internalResponse);
            }
        }
    }
}
