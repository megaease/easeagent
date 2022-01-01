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
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.springweb.WebClientPlugin;
import com.megaease.easeagent.plugin.springweb.advice.WebClientFilterAdvice;
import com.megaease.easeagent.plugin.springweb.reactor.AgentMono;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Collection;

@AdviceTo(value = WebClientFilterAdvice.class, plugin = WebClientPlugin.class)
public class WebClientFilterTracingInterceptor implements NonReentrantInterceptor {
    static Logger log = EaseAgent.getLogger(WebClientFilterTracingInterceptor.class);
    private static final Object PROGRESS_CONTEXT = new Object();

    @Override
    public void init(IPluginConfig config, int index) {
    }

    public Object getProgressKey() {
        return PROGRESS_CONTEXT;
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpRequest request = getRequest(methodInfo);
        RequestContext requestContext = context.clientRequest(request);
        Span span = requestContext.span();
        HttpUtils.handleReceive(span.start(), request);
        context.put(getProgressKey(), requestContext);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        RequestContext pCtx = context.get(getProgressKey());
        try (Scope scope = pCtx.scope()) {
            @SuppressWarnings("unchecked")
            Mono<ClientResponse> mono = (Mono<ClientResponse>) methodInfo.getRetValue();
            methodInfo.setRetValue(new AgentMono(mono, methodInfo, pCtx));

            if (!methodInfo.isSuccess()) {
                Span span = pCtx.span();
                span.error(methodInfo.getThrowable());
                span.finish();
            }
        }
    }

    protected HttpRequest getRequest(MethodInfo methodInfo) {
        ClientRequest clientRequest = (ClientRequest) methodInfo.getArgs()[0];
        ClientRequest.Builder builder = ClientRequest.from(clientRequest);
        WebClientRequest request = new WebClientRequest(clientRequest, builder);
        Object[] args = methodInfo.getArgs();
        args[0] = builder.build();
        methodInfo.setArgs(args);
        return request;
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

    public static class WebClientResponse implements HttpResponse {
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
        public String header(String name) {
            if (response == null) {
                return null;
            }
            return getFirstHeaderValue(response.headers(), name);
        }
    }

    private static String getFirstHeaderValue(ClientResponse.Headers headers, String name) {
        Collection<String> values = headers.header(name);
        if (values.isEmpty()) {
            return null;
        }
        return values.iterator().next();
    }
}
