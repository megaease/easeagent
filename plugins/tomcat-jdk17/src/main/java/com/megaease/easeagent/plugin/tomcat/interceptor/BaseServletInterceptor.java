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

package com.megaease.easeagent.plugin.tomcat.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.tomcat.utils.InternalAsyncListener;
import com.megaease.easeagent.plugin.tomcat.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class BaseServletInterceptor implements NonReentrantInterceptor {

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        ServletUtils.startTime(httpServletRequest);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        final long start = ServletUtils.startTime(httpServletRequest);
        if (ServletUtils.markProcessed(httpServletRequest, getAfterMark())) {
            return;
        }
        String httpRoute = ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest);
        final String key = httpServletRequest.getMethod() + " " + httpRoute;
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        if (methodInfo.getThrowable() != null) {
            internalAfter(methodInfo.getThrowable(), key, httpServletRequest, httpServletResponse, start);
        } else if (httpServletRequest.isAsyncStarted()) {
            httpServletRequest.getAsyncContext().addListener(new InternalAsyncListener(
                    asyncEvent -> {
                        HttpServletResponse suppliedResponse = (HttpServletResponse) asyncEvent.getSuppliedResponse();
                        internalAfter(asyncEvent.getThrowable(), key, httpServletRequest, suppliedResponse, start);
                    }

                )
            );
        } else {
            internalAfter(null, key, httpServletRequest, httpServletResponse, start);
        }
    }

    protected abstract String getAfterMark();

    abstract void internalAfter(Throwable throwable, String key, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long start);
}
