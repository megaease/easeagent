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

package com.megaease.easeagent.plugin.tomcat.interceptor;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.tomcat.interceptor.HttpServerRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class HttpServerRequestTest {
    public HttpServerRequest build() {
        return new HttpServerRequest(TestServletUtils.buildMockRequest());
    }

    @Test
    public void kind() {
        assertEquals(Span.Kind.SERVER, build().kind());
    }

    @Test
    public void method() {
        assertEquals(TestConst.METHOD, build().method());
    }

    @Test
    public void path() {
        assertEquals(TestConst.URL, build().path());
    }

    @Test
    public void route() {
        assertEquals(TestConst.ROUTE, build().route());
    }

    @Test
    public void getRemoteAddr() {
        assertEquals(TestConst.REMOTE_ADDR, build().getRemoteAddr());
    }

    @Test
    public void getRemotePort() {
        assertEquals(TestConst.REMOTE_PORT, build().getRemotePort());
    }

    @Test
    public void header() {
        assertEquals(TestConst.FORWARDED_VALUE, build().header(TestConst.FORWARDED_NAME));
    }

    @Test
    public void cacheScope() {
        assertEquals(false, build().cacheScope());
    }
}
