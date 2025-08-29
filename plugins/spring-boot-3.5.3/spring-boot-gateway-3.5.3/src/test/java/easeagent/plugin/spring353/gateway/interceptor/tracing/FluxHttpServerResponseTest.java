/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easeagent.plugin.spring353.gateway.interceptor.tracing;

import easeagent.plugin.spring353.gateway.TestServerWebExchangeUtils;
import org.junit.Test;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.junit.Assert.*;

public class FluxHttpServerResponseTest {


    @Test
    public void header() {
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        String key = "testKey";
        String value = "testValue";
        mockServerWebExchange.getResponse().getHeaders().add(key, value);
        FluxHttpServerResponse fluxHttpServerResponse = new FluxHttpServerResponse(mockServerWebExchange, null);
        assertEquals(value, fluxHttpServerResponse.header(key));

    }

    @Test
    public void method() {
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        FluxHttpServerResponse fluxHttpServerResponse = new FluxHttpServerResponse(mockServerWebExchange, null);
        assertEquals("GET", fluxHttpServerResponse.method());
    }

    @Test
    public void route() {
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        FluxHttpServerResponse fluxHttpServerResponse = new FluxHttpServerResponse(mockServerWebExchange, null);
        assertEquals("/test", fluxHttpServerResponse.route());
    }

    @Test
    public void statusCode() {
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        FluxHttpServerResponse fluxHttpServerResponse = new FluxHttpServerResponse(mockServerWebExchange, null);
        assertEquals(200, fluxHttpServerResponse.statusCode());

    }

    @Test
    public void maybeError() {
        MockServerWebExchange mockServerWebExchange = TestServerWebExchangeUtils.mockServerWebExchange();
        FluxHttpServerResponse fluxHttpServerResponse = new FluxHttpServerResponse(mockServerWebExchange, null);
        assertNull(fluxHttpServerResponse.maybeError());
        RuntimeException err = new RuntimeException();
        fluxHttpServerResponse = new FluxHttpServerResponse(mockServerWebExchange, err);
        assertSame(err, fluxHttpServerResponse.maybeError());
    }
}
