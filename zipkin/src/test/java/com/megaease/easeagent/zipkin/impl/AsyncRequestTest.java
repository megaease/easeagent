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

package com.megaease.easeagent.zipkin.impl;

import com.megaease.easeagent.plugin.api.trace.Span;
import org.junit.Test;

import static org.junit.Assert.*;

public class AsyncRequestTest {

    @Test
    public void kind() {
        AsyncRequest asyncRequest = new AsyncRequest(new RequestMock());
        assertNull(asyncRequest.kind());
        asyncRequest = new AsyncRequest(new RequestMock().setKind(Span.Kind.PRODUCER));
        assertEquals(Span.Kind.PRODUCER, asyncRequest.kind());
    }

    @Test
    public void name() {
        AsyncRequest asyncRequest = new AsyncRequest(new RequestMock());
        assertNull(asyncRequest.name());
        String name = "testName";
        asyncRequest = new AsyncRequest(new RequestMock().setName(name));
        assertEquals(name, asyncRequest.name());
    }

    @Test
    public void header() {
        AsyncRequest asyncRequest = new AsyncRequest(new RequestMock());
        String name = "testName";
        assertNull(asyncRequest.header(name));
        RequestMock requestMock = new RequestMock();
        asyncRequest = new AsyncRequest(requestMock);
        String value = "value";
        requestMock.setHeader(name, value);
        assertEquals(value, asyncRequest.header(name));
        assertEquals(value, requestMock.header(name));
    }

    @Test
    public void cacheScope() {
        AsyncRequest asyncRequest = new AsyncRequest(new RequestMock());
        assertFalse(asyncRequest.cacheScope());
        asyncRequest = new AsyncRequest(new RequestMock().setCacheScope(true));
        assertTrue(asyncRequest.cacheScope());
    }

    @Test
    public void setHeader() {
        header();
    }

    @Test
    public void getHeaders() {
        AsyncRequest asyncRequest = new AsyncRequest(new RequestMock());
        String name = "testName";
        assertNull(asyncRequest.header(name));
        RequestMock requestMock = new RequestMock();
        asyncRequest = new AsyncRequest(requestMock);
        String value = "value";
        asyncRequest.setHeader(name, value);
        assertEquals(1, asyncRequest.getHeaders().size());
        assertEquals(value, asyncRequest.getHeaders().get(name));
    }
}
