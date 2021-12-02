/*
 * Copyright (c) 2021, MegaEase
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

package easeagent.plugin.spring.gateway.interceptor.tracing;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;
import easeagent.plugin.spring.gateway.advice.HttpHeadersFilterAdvice;
import easeagent.plugin.spring.gateway.interceptor.GatewayCons;
import com.megaease.easeagent.plugin.tools.trace.HttpUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@AdviceTo(value = HttpHeadersFilterAdvice.class)
public class HttpHeadersFilterTracingInterceptor implements FirstEnterInterceptor {
    // org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter
    static final String CLIENT_HEADER_ATTR = HttpHeadersFilterTracingInterceptor.class.getName() + ".Headers";

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[1];
        HttpHeaders retHttpHeaders = (HttpHeaders) methodInfo.getRetValue();
        ProgressContext pCtx = exchange.getAttribute(GatewayCons.SPAN_KEY);
        if (pCtx == null) {
            return;
        }
        FluxHttpServerRequest request = new HeaderFilterRequest(exchange.getRequest());

        ProgressContext pnCtx = pCtx.getContext().nextProgress(request);
        pnCtx.span().start();
        exchange.getAttributes().put(GatewayCons.CHILD_SPAN_KEY, pnCtx);
        Map<String, String> map = getHeadersFromExchange(exchange);
        map.putAll(retHttpHeaders.toSingleValueMap());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(map);
        methodInfo.setRetValue(httpHeaders);

        Consumer<ServerWebExchange> consumer = serverWebExchange -> {
            ProgressContext p = serverWebExchange.getAttribute(GatewayCons.CHILD_SPAN_KEY);
            if (p == null) {
                return;
            }
            FluxHttpServerResponse response = new FluxHttpServerResponse(serverWebExchange, null);
            p.finish(response);
            HttpUtils.finish(p.span(), response);
        };
        exchange.getAttributes().put(GatewayCons.CLIENT_RECEIVE_CALLBACK_KEY, consumer);
    }


    private Map<String, String> getHeadersFromExchange(ServerWebExchange exchange) {
        Map<String, String> headers = exchange.getAttribute(CLIENT_HEADER_ATTR);
        if (headers == null) {
            headers = new HashMap<>();
            exchange.getAttributes().put(CLIENT_HEADER_ATTR, headers);
        }
        return headers;
    }

    static class HeaderFilterRequest extends FluxHttpServerRequest {
        public HeaderFilterRequest(ServerHttpRequest request) {
            super(request);
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.CLIENT;
        }
    }
}
