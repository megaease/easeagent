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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.httpservlet.AccessPlugin;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class ServletHttpLogInterceptorTest {

    @Test
    public void serverInfo() {
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        AccessLogServerInfo accessLogServerInfo = servletHttpLogInterceptor.serverInfo(httpServletRequest, response);
        assertSame(accessLogServerInfo, servletHttpLogInterceptor.serverInfo(httpServletRequest, response));
    }

    @Test
    public void doBefore() throws JsonProcessingException {
        internalAfter();
    }

    public void verify(AccessLogInfo accessLogInfo, long startTime) {
        assertEquals(EaseAgent.getConfig("system"), accessLogInfo.getSystem());
        assertEquals(EaseAgent.getConfig("name"), accessLogInfo.getService());
        assertEquals(HostAddress.localhost(), accessLogInfo.getHostName());
        assertEquals(HostAddress.getHostIpv4(), accessLogInfo.getHostIpv4());
        assertEquals(TestConst.METHOD + " " + TestConst.URL, accessLogInfo.getUrl());
        assertEquals(TestConst.METHOD, accessLogInfo.getMethod());
        assertEquals(TestConst.FORWARDED_VALUE, accessLogInfo.getHeaders().get(TestConst.FORWARDED_NAME));
        assertEquals(startTime, accessLogInfo.getBeginTime());
        assertEquals("10", accessLogInfo.getQueries().get("q1"));
        assertEquals("testq", accessLogInfo.getQueries().get("q2"));
        assertEquals(TestConst.FORWARDED_VALUE, accessLogInfo.getClientIP());
        assertTrue(accessLogInfo.getBeginCpuTime() > 0);
    }

    @Test
    public void getAfterMark() {
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        assertNotNull(servletHttpLogInterceptor.getAfterMark());
    }

    private AccessLogInfo getRequestInfo(LastJsonReporter lastJsonReporter) {
        String result = JsonUtil.toJson(lastJsonReporter.getLastOnlyOne());
        assertNotNull(result);
        return JsonUtil.toObject(result, AccessLogInfo.TYPE_REFERENCE);
    }

    @Test
    public void internalAfter() throws JsonProcessingException {
        EaseAgent.agentReport = MockReport.getAgentReport();
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        AccessPlugin accessPlugin = new AccessPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(accessPlugin.getDomain(), accessPlugin.getNamespace(), servletHttpLogInterceptor.getType());
        servletHttpLogInterceptor.init(iPluginConfig, "", "", "");

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();
        servletHttpLogInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        Object requestInfoO = httpServletRequest.getAttribute(AccessLogInfo.class.getName());
        assertNotNull(requestInfoO);
        assertTrue(requestInfoO instanceof AccessLogInfo);
        AccessLogInfo accessLogInfo = (AccessLogInfo) requestInfoO;
        long start = (long) httpServletRequest.getAttribute(ServletUtils.START_TIME);
        verify(accessLogInfo, start);
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(stringObjectMap -> {
            Object type = stringObjectMap.get("type");
            return type instanceof String && "access-log".equals(type);
        });
        servletHttpLogInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        // AccessLogInfo info = getRequestInfo(lastJsonReporter);
        AccessLogInfo info = MockEaseAgent.getLastLog();
        verify(info, start);
        assertEquals("200", info.getStatusCode());

        lastJsonReporter.clean();
        httpServletRequest = TestServletUtils.buildMockRequest();
        response = TestServletUtils.buildMockResponse();
        methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).throwable(new RuntimeException("test error")).build();
        servletHttpLogInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        servletHttpLogInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        // info = getRequestInfo(lastJsonReporter);
        info = MockEaseAgent.getLastLog();
        start = (long) httpServletRequest.getAttribute(ServletUtils.START_TIME);
        verify(info, start);
        assertEquals("500", info.getStatusCode());
    }

    @Test
    public void getType() {
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        assertEquals(Order.METRIC.getName(), servletHttpLogInterceptor.getType());
    }
}
