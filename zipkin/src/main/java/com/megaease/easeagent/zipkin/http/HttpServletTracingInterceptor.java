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

package com.megaease.easeagent.zipkin.http;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HttpServletTracingInterceptor implements AgentInterceptor {

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        TraceContext.Extractor<HttpServletRequest> extractor = Tracing.current().propagation().extractor((request, key) -> {
            final String header = request.getHeader(key);
            return header != null ? header
                    : request.getHeader(key.toLowerCase());
        });
        Span span = Tracing.currentTracer().nextSpan(extractor.extract(httpServletRequest)).start();
        Tracer.SpanInScope spanInScope = Tracing.currentTracer().withSpanInScope(span);
        context.put(Span.class, span);
        context.put(Tracer.SpanInScope.class, spanInScope);
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) args[1];
        Span span = (Span) context.get(Span.class);
        span.name("http_recv")
                .kind(Span.Kind.SERVER)
                .tag("component", "web")
                .tag("span.kind", "server")
                .tag("http.url", httpServletRequest.getRequestURL().toString())
                .tag("http.method", httpServletRequest.getMethod())
                .tag("http.status_code", String.valueOf(httpServletResponse.getStatus()))
                .tag("peer.hostname", httpServletRequest.getRemoteHost())
                .tag("peer.ipv4", httpServletRequest.getRemoteAddr())
                .tag("peer.port", String.valueOf(httpServletRequest.getRemotePort()))
                .tag("has.error", String.valueOf(httpServletResponse.getStatus() >= 400))
                .tag("remote.address", httpServletRequest.getRemoteAddr())
                .finish();
    }
}
