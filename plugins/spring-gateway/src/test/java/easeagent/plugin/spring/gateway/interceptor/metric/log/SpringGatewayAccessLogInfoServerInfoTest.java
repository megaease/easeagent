/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package easeagent.plugin.spring.gateway.interceptor.metric.log;

import easeagent.plugin.spring.gateway.TestServerWebExchangeUtils;
import easeagent.plugin.spring.gateway.interceptor.metric.MockRouteBuilder;
import org.junit.Test;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.*;

public class SpringGatewayAccessLogInfoServerInfoTest {

    @Test
    public void load() {
        getMethod();
    }

    @Test
    public void getMethod() {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.mockServerWebExchange());
        assertEquals("GET", springGatewayAccessLogServerInfo.getMethod());
    }

    @Test
    public void getHeader() {
        String key = "testKey";
        String value = "testValue";
        MockServerHttpRequest.BaseBuilder<?> baseBuilder = TestServerWebExchangeUtils.builder().header(key, value);
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.build(baseBuilder));
        assertEquals(value, springGatewayAccessLogServerInfo.getHeader(key));
    }

    @Test
    public void getRemoteAddr() {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.mockServerWebExchange());
        assertEquals("192.168.0.12", springGatewayAccessLogServerInfo.getRemoteAddr());
    }

    @Test
    public void getRequestURI() {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.mockServerWebExchange());
        assertEquals("http://192.168.0.12:8080/test?a=b", springGatewayAccessLogServerInfo.getRequestURI());
    }

    @Test
    public void getResponseBufferSize() {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        assertEquals(0, springGatewayAccessLogServerInfo.getResponseBufferSize());
    }


    @Test
    public void getMatchURL() throws URISyntaxException {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        MockServerWebExchange exchange = TestServerWebExchangeUtils.mockServerWebExchange();
        springGatewayAccessLogServerInfo.load(exchange);
        assertEquals("GET http://192.168.0.12:8080/test?a=b", springGatewayAccessLogServerInfo.getMatchURL());
        String url = "http://192.168.0.12:8080/";
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, new MockRouteBuilder().uri(new URI(url)).id("t").build());
        assertEquals("GET " + url, springGatewayAccessLogServerInfo.getMatchURL());
    }

    @Test
    public void findHeaders() {
        String key = "testKey";
        String value = "testValue";
        MockServerHttpRequest.BaseBuilder<?> baseBuilder = TestServerWebExchangeUtils.builder().header(key, value);
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.build(baseBuilder));
        Map<String, String> headers = springGatewayAccessLogServerInfo.findHeaders();
        assertEquals(value, headers.get(key));
    }

    @Test
    public void findQueries() {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.mockServerWebExchange());
        Map<String, String> queries = springGatewayAccessLogServerInfo.findQueries();
        assertEquals(1, queries.size());
        assertEquals("b", queries.get("a"));
    }

    @Test
    public void getStatusCode() {
        SpringGatewayAccessLogServerInfo springGatewayAccessLogServerInfo = new SpringGatewayAccessLogServerInfo();
        springGatewayAccessLogServerInfo.load(TestServerWebExchangeUtils.mockServerWebExchange());
        assertEquals("200", springGatewayAccessLogServerInfo.getStatusCode());
    }

}
