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

package com.megaease.easeagent.plugin.springweb.interceptor.tracing;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.springweb.advice.WebClientFilterAdvice;
import com.megaease.easeagent.plugin.utils.FirstEnterInterceptor;
import com.megaease.easeagent.plugin.utils.trace.HttpRequest;
import com.megaease.easeagent.plugin.utils.trace.HttpResponse;
import com.megaease.easeagent.plugin.utils.trace.HttpUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@AdviceTo(value = WebClientFilterAdvice.class, qualifier = "default")
public class WebClientFilterTracingInterceptor implements FirstEnterInterceptor {
    private static final Object PROGRESS_CONTEXT = new Object();

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ClientRequest clientRequest = (ClientRequest) methodInfo.getArgs()[0];
        ClientRequest.Builder builder = ClientRequest.from(clientRequest);
        WebClientRequest request = new WebClientRequest(clientRequest, builder);
        ProgressContext progressContext = context.nextProgress(request);
        HttpUtils.handleReceive(progressContext.span().start(), request);
        context.put(PROGRESS_CONTEXT, progressContext);
        Object[] args = methodInfo.getArgs();
        args[0] = builder.build();
        methodInfo.setArgs(args);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ProgressContext progressContext = context.get(PROGRESS_CONTEXT);
        if (progressContext == null) {
            return;
        }
        try (Scope scope = progressContext.scope()) {
            ClientResponse clientResponse = null;
            if (methodInfo.isSuccess()) {
                clientResponse = getClientResponse(methodInfo);
            }
            WebClientResponse webClientResponse = new WebClientResponse(methodInfo.getThrowable(), clientResponse);
            progressContext.finish(webClientResponse);
            HttpUtils.finish(progressContext.span(), webClientResponse);
        }
    }

    private ClientResponse getClientResponse(MethodInfo methodInfo) {
        Object retValue = methodInfo.getRetValue();
        if (retValue == null) {
            return null;
        }
        List<ClientResponse> list = (List<ClientResponse>) retValue;
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }


    static class WebClientRequest implements HttpRequest {

        private final ClientRequest clientRequest;

        private final ClientRequest.Builder builder;

        public WebClientRequest(ClientRequest clientRequest, ClientRequest.Builder builder) {
            this.clientRequest = clientRequest;
            this.builder = builder;
        }


        @Override
        public String method() {
            return clientRequest.method().name();
        }

        @Override
        public String path() {
            return clientRequest.url().getPath();
        }

        @Override
        public String route() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return clientRequest.url().toString();
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getRemoteHost() {
            return null;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.CLIENT;
        }

        @Override
        public String header(String name) {
            HttpHeaders headers = clientRequest.headers();
            return headers.getFirst(name);
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            builder.header(name, value);
        }

    }

    private static String getFirstHeaderValue(ClientResponse.Headers headers, String name) {
        Collection<String> values = headers.header(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }

    static class WebClientResponse implements HttpResponse {
        private final Throwable caught;
        private final ClientResponse response;

        public WebClientResponse(Throwable caught, ClientResponse response) {
            this.caught = caught;
            this.response = response;
        }

        @Override
        public String method() {
            return null;
        }

        @Override
        public String route() {
            return null;
        }

        @Override
        public int statusCode() {
            return response == null ? 0 : response.rawStatusCode();
        }

        @Override
        public Throwable maybeError() {
            return caught;
        }

        @Override
        public Set<String> keys() {
            return null;
        }

        @Override
        public String header(String name) {
            if (response == null) {
                return null;
            }
            return getFirstHeaderValue(response.headers(), name);
        }
    }
}
