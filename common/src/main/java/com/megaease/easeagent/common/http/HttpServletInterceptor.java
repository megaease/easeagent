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

package com.megaease.easeagent.common.http;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.Consumer;

public abstract class HttpServletInterceptor implements AgentInterceptor {

    public abstract void internalBefore(MethodInfo methodInfo, Map<Object, Object> context,
                                        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    public abstract void internalAfter(MethodInfo methodInfo, Map<Object, Object> context,
                                       HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    public abstract String processedBeforeKey();

    public abstract String processedAfterKey();

    protected boolean markProcessedBefore(HttpServletRequest request) {
        String key = this.processedBeforeKey();
        Object attribute = request.getAttribute(key);
        if (attribute != null) {
            return true;
        }
        request.setAttribute(key, true);
        return false;
    }

    protected boolean markProcessedAfter(HttpServletRequest request) {
        String key = this.processedAfterKey();
        Object attribute = request.getAttribute(key);
        if (attribute != null) {
            return true;
        }
        request.setAttribute(key, true);
        return false;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        boolean markProcessedBefore = this.markProcessedBefore(httpServletRequest);
        if (markProcessedBefore) {
            chain.doBefore(methodInfo, context);
            return;
        }
        this.internalBefore(methodInfo, context, httpServletRequest, httpServletResponse);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        boolean markProcessedAfter = this.markProcessedAfter(httpServletRequest);
        if (markProcessedAfter) {
            return chain.doAfter(methodInfo, context);
        }
        if (httpServletRequest.isAsyncStarted()) {
            httpServletRequest.getAsyncContext().addListener(new InternalAsyncListener(
                    asyncEvent -> {
                        HttpServletRequest suppliedRequest = (HttpServletRequest) asyncEvent.getSuppliedRequest();
                        HttpServletResponse suppliedResponse = (HttpServletResponse) asyncEvent.getSuppliedResponse();
                        this.internalAfter(methodInfo, context, suppliedRequest, suppliedResponse);
                    }
                )
            );
        } else {
            this.internalAfter(methodInfo, context, httpServletRequest, httpServletResponse);
        }
        return chain.doAfter(methodInfo, context);
    }

    static class InternalAsyncListener implements AsyncListener {

        private final Consumer<AsyncEvent> consumer;

        public InternalAsyncListener(Consumer<AsyncEvent> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onComplete(AsyncEvent event) {
            this.consumer.accept(event);
        }

        @Override
        public void onTimeout(AsyncEvent event) {
        }

        @Override
        public void onError(AsyncEvent event) {
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            AsyncContext eventAsyncContext = event.getAsyncContext();
            if (eventAsyncContext != null) {
                eventAsyncContext.addListener(this, event.getSuppliedRequest(), event.getSuppliedResponse());
            }
        }
    }
}
