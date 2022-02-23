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
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.httpservlet.AccessPlugin;
import com.megaease.easeagent.plugin.httpservlet.utils.ServletUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.tools.metrics.AccessLogServerInfo;
import com.megaease.easeagent.plugin.tools.metrics.RequestInfo;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class ServletHttpLogInterceptorTest {

    @Test
    public void init() {
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        AccessPlugin accessPlugin = new AccessPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(accessPlugin.getDomain(), accessPlugin.getNamespace(), servletHttpLogInterceptor.getType());
        servletHttpLogInterceptor.init(iPluginConfig, "", "", "");
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(servletHttpLogInterceptor, "reportConsumer"));
    }

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

    public void verify(RequestInfo requestInfo, long startTime) {
        assertEquals(EaseAgent.getConfig("system"), requestInfo.getSystem());
        assertEquals(EaseAgent.getConfig("name"), requestInfo.getService());
        assertEquals(HostAddress.localhost(), requestInfo.getHostName());
        assertEquals(HostAddress.getHostIpv4(), requestInfo.getHostIpv4());
        assertEquals(TestConst.METHOD + " " + TestConst.URL, requestInfo.getUrl());
        assertEquals(TestConst.METHOD, requestInfo.getMethod());
        assertEquals(TestConst.FORWARDED_VALUE, requestInfo.getHeaders().get(TestConst.FORWARDED_NAME));
        assertEquals(startTime, requestInfo.getBeginTime());
        assertEquals("10", requestInfo.getQueries().get("q1"));
        assertEquals("testq", requestInfo.getQueries().get("q2"));
        assertEquals(TestConst.FORWARDED_VALUE, requestInfo.getClientIP());
        assertTrue(requestInfo.getBeginCpuTime() > 0);
    }

    @Test
    public void getAfterMark() {
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        assertNotNull(servletHttpLogInterceptor.getAfterMark());
    }

    private RequestInfo getRequestInfo(LastJsonReporter lastJsonReporter) {
        String result = JsonUtil.toJson(lastJsonReporter.flushAndOnlyOne());
        assertNotNull(result);
        return JsonUtil.toObject(result, RequestInfo.TYPE_REFERENCE);
    }

    @Test
    public void internalAfter() throws JsonProcessingException {
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();
        ServletHttpLogInterceptor servletHttpLogInterceptor = new ServletHttpLogInterceptor();
        AccessPlugin accessPlugin = new AccessPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(accessPlugin.getDomain(), accessPlugin.getNamespace(), servletHttpLogInterceptor.getType());
        servletHttpLogInterceptor.init(iPluginConfig, "", "", "");

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();
        servletHttpLogInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        Object requestInfoO = httpServletRequest.getAttribute(RequestInfo.class.getName());
        assertNotNull(requestInfoO);
        assertTrue(requestInfoO instanceof RequestInfo);
        RequestInfo requestInfo = (RequestInfo) requestInfoO;
        long start = (long) httpServletRequest.getAttribute(ServletUtils.START_TIME);
        verify(requestInfo, start);
        LastJsonReporter lastJsonReporter = ReportMock.lastMetricJsonReporter(stringObjectMap -> {
            Object type = stringObjectMap.get("type");
            return type instanceof String && "access-log".equals(type);
        });
        servletHttpLogInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        RequestInfo info = getRequestInfo(lastJsonReporter);
        verify(info, start);
        assertEquals("200", info.getStatusCode());

        lastJsonReporter.clean();
        httpServletRequest = TestServletUtils.buildMockRequest();
        response = TestServletUtils.buildMockResponse();
        methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).throwable(new RuntimeException("test error")).build();
        servletHttpLogInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        servletHttpLogInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        info = getRequestInfo(lastJsonReporter);
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
