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

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.junit.Assert.*;

public class ServletAccessLogServerInfoTest {

    private ServletAccessLogServerInfo loadMock() {
        ServletAccessLogServerInfo servletAccessLogServerInfo = new ServletAccessLogServerInfo();
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        servletAccessLogServerInfo.load(httpServletRequest, response);
        return servletAccessLogServerInfo;
    }

    @Test
    public void load() {
        ServletAccessLogServerInfo serverInfo = loadMock();
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(serverInfo, "request"));
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(serverInfo, "response"));
    }

    @Test
    public void getMethod() {
        assertEquals(TestConst.METHOD, loadMock().getMethod());
    }

    @Test
    public void getHeader() {
        assertEquals(TestConst.FORWARDED_VALUE, loadMock().getHeader(TestConst.FORWARDED_NAME));
    }

    @Test
    public void getRemoteAddr() {
        assertEquals(TestConst.REMOTE_ADDR, loadMock().getRemoteAddr());
    }

    @Test
    public void getRequestURI() {
        assertEquals(TestConst.URL, loadMock().getRequestURI());
    }

    @Test
    public void getResponseBufferSize() {
        assertEquals(TestConst.RESPONSE_BUFFER_SIZE, loadMock().getResponseBufferSize());
    }

    @Test
    public void getMatchURL() {
        assertEquals(TestConst.METHOD + " " + TestConst.ROUTE, loadMock().getMatchURL());
    }

    @Test
    public void findHeaders() {
        assertEquals(TestConst.FORWARDED_VALUE, loadMock().findHeaders().get(TestConst.FORWARDED_NAME));
    }

    @Test
    public void findQueries() {
        Map<String, String> queries = loadMock().findQueries();
        assertEquals("10", queries.get("q1"));
        assertEquals("testq", queries.get("q2"));
    }

    @Test
    public void getStatusCode() {
        assertEquals("200", loadMock().getStatusCode());
    }
}
