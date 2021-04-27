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

package com.megaease.easeagent.zipkin.http.reactive;

import com.megaease.easeagent.zipkin.http.AccessLogServerInfo;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

public class SpringGatewayAccessLogServerInfo implements AccessLogServerInfo {

    private ServerWebExchange exchange;

    public void load(ServerWebExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getMethod() {
        return exchange.getRequest().getMethodValue();
    }

    @Override
    public String getHeader(String key) {
        return exchange.getRequest().getHeaders().getFirst(key);
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress == null ? null : remoteAddress.getHostString();
    }

    @Override
    public String getRequestURI() {
        return exchange.getRequest().getURI().toString();
    }

    @Override
    public int getResponseBufferSize() {
        return 0;
    }

    @Override
    public String getMatchURL() {
        HttpMethod httpMethod = exchange.getRequest().getMethod();
        if (httpMethod == null) {
            return "";
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null && route.getUri() != null) {
            return httpMethod.name() + " " + route.getUri().toString();
        }
        return httpMethod.name() + " " + getRequestURI();
    }

    @Override
    public Map<String, String> findHeaders() {
        return exchange.getRequest().getHeaders().toSingleValueMap();
    }

    @Override
    public Map<String, String> findQueries() {
        return exchange.getRequest().getQueryParams().toSingleValueMap();
    }

    @Override
    public String getStatusCode() {
        HttpStatus rawStatusCode = exchange.getResponse().getStatusCode();
        return Optional.ofNullable(rawStatusCode).map(e -> e.value() + "").orElse("0");
    }
}
