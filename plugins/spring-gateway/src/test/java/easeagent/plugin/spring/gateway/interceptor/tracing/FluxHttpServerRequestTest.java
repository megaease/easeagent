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

package easeagent.plugin.spring.gateway.interceptor.tracing;

import com.megaease.easeagent.plugin.api.trace.Span;
import easeagent.plugin.spring.gateway.TestServerWebExchangeUtils;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class FluxHttpServerRequestTest {

    @Test
    public void kind() {
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder().build());
        assertEquals(Span.Kind.SERVER, fluxHttpServerRequest.kind());
    }

    @Test
    public void header() {
        String key = "testKey";
        String value = "testValue";
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder()
            .header(key, value).build());
        assertEquals(value, fluxHttpServerRequest.header(key));
    }

    @Test
    public void cacheScope() {
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder().build());
        assertFalse(fluxHttpServerRequest.cacheScope());
    }

    @Test
    public void setHeader() {
        String key = "testKey";
        String value = "testValue";
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder().build());
        fluxHttpServerRequest.setHeader(key, value);
        assertNull(fluxHttpServerRequest.header(key));
    }

    @Test
    public void method() {
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder().build());
        assertEquals("GET", fluxHttpServerRequest.method());
    }

    @Test
    public void path() {
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder().build());
        assertEquals("/test", fluxHttpServerRequest.path());
    }

    @Test
    public void route() {
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder().build());
        assertEquals(null, fluxHttpServerRequest.route());
    }

    @Test
    public void getRemoteAddr() {

        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder()
            .remoteAddress(new InetSocketAddress("192.168.0.12", 8080)).build());
        assertEquals("192.168.0.12", fluxHttpServerRequest.getRemoteAddr());
    }

    @Test
    public void getRemotePort() {
        FluxHttpServerRequest fluxHttpServerRequest = new FluxHttpServerRequest(TestServerWebExchangeUtils.builder()
            .remoteAddress(new InetSocketAddress("192.168.0.12", 8080)).build());
        assertEquals(8080, fluxHttpServerRequest.getRemotePort());

    }
}
