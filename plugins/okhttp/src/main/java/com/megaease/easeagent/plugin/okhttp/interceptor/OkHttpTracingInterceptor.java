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
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.okhttp.advice.OkHttpAdvice;
import com.megaease.easeagent.plugin.tools.trace.BaseHttpClientTracingInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import okhttp3.Request;
import okhttp3.Response;

@AdviceTo(value = OkHttpAdvice.class, qualifier = "execute")
public class OkHttpTracingInterceptor extends BaseHttpClientTracingInterceptor {
    public static Object REQUEST_BUILDER_KEY = new Object();
    public static Object METHOD_KEY = new Object();

    @Override
    public Object getProgressKey() {
        return OkHttpTracingInterceptor.class;
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        super.doBefore(methodInfo, context);
        Object realCall = methodInfo.getInvoker();
        Request.Builder requestBuilder = context.remove(REQUEST_BUILDER_KEY);
        AgentFieldReflectAccessor.setFieldValue(realCall, "originalRequest", requestBuilder.build());
    }

    @Override
    protected HttpRequest getRequest(MethodInfo methodInfo, Context context) {
        Object realCall = methodInfo.getInvoker();
        Request originalRequest = AgentFieldReflectAccessor.getFieldValue(realCall, "originalRequest");
        if (originalRequest == null) {
            return null;
        }
        context.put(METHOD_KEY, originalRequest.method());
        Request.Builder requestBuilder = originalRequest.newBuilder();
        context.put(REQUEST_BUILDER_KEY, requestBuilder);
        return new InternalRequest(originalRequest, requestBuilder);
    }

    @Override
    protected HttpResponse getResponse(MethodInfo methodInfo, Context context) {
        return new InternalResponse(methodInfo.getThrowable(), context.remove(METHOD_KEY), (Response) methodInfo.getRetValue());
    }
}
