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

import com.megaease.easeagent.mock.metrics.MockMetricUtils;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.mock.utils.TagVerifier;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetric;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.httpservlet.HttpServletPlugin;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@MockEaseAgent
public class DoFilterMetricInterceptorTest {

    @Test
    public void init() {
        DoFilterMetricInterceptor doFilterMetricInterceptor = new DoFilterMetricInterceptor();
        HttpServletPlugin httpServletPlugin = new HttpServletPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(httpServletPlugin.getDomain(), httpServletPlugin.getNamespace(), doFilterMetricInterceptor.getType());
        doFilterMetricInterceptor.init(iPluginConfig, "", "", "");
        assertNotNull(AgentFieldReflectAccessor.getFieldValue(doFilterMetricInterceptor, "SERVER_METRIC"));

    }

    @Test
    public void getAfterMark() {
        DoFilterMetricInterceptor doFilterMetricInterceptor = new DoFilterMetricInterceptor();
        assertNotNull(doFilterMetricInterceptor.getAfterMark());

    }

    public Map<String, Object> getMetric(LastJsonReporter lastJsonReporter) throws InterruptedException {
        List<Map<String, Object>> mapList = lastJsonReporter.waitOne(3, TimeUnit.SECONDS);
        assertNotNull(mapList);
        assertEquals(1, mapList.size());
        return mapList.get(0);
    }

    @Test
    public void internalAfter() throws InterruptedException {
        MockHttpServletRequest httpServletRequest = TestServletUtils.buildMockRequest();
        HttpServletResponse response = TestServletUtils.buildMockResponse();

        DoFilterMetricInterceptor doFilterMetricInterceptor = new DoFilterMetricInterceptor();
        HttpServletPlugin httpServletPlugin = new HttpServletPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(httpServletPlugin.getDomain(), httpServletPlugin.getNamespace(), doFilterMetricInterceptor.getType());
        int interval = iPluginConfig.getInt("interval");
        assertEquals(1, interval);
        doFilterMetricInterceptor.init(iPluginConfig, "", "", "");

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).build();
        doFilterMetricInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "http-request")
            .add("url", TestConst.METHOD + " " + TestConst.ROUTE);
        LastJsonReporter lastJsonReporter = ReportMock.lastMetricJsonReporter(tagVerifier::verifyAnd);

        doFilterMetricInterceptor.doAfter(methodInfo, EaseAgent.getContext());
        Map<String, Object> metric = getMetric(lastJsonReporter);

        assertEquals(1, (int) metric.get("cnt"));
        assertEquals(0, (int) metric.get("errcnt"));

        MockMetricUtils.clear(Objects.requireNonNull(AgentFieldReflectAccessor.<ServiceMetric>getFieldValue(doFilterMetricInterceptor, "SERVER_METRIC")));
        lastJsonReporter.clean();
        httpServletRequest = TestServletUtils.buildMockRequest();
        response = TestServletUtils.buildMockResponse();
        methodInfo = MethodInfo.builder().args(new Object[]{httpServletRequest, response}).throwable(new RuntimeException("test error")).build();
        doFilterMetricInterceptor.doBefore(methodInfo, EaseAgent.getContext());
        doFilterMetricInterceptor.doAfter(methodInfo, EaseAgent.getContext());

        metric = getMetric(lastJsonReporter);
        assertEquals(1, (int) metric.get("cnt"));
        assertEquals(1, (int) metric.get("errcnt"));
    }

    @Test
    public void getType() {
        DoFilterMetricInterceptor doFilterMetricInterceptor = new DoFilterMetricInterceptor();
        assertEquals(Order.METRIC.getName(), doFilterMetricInterceptor.getType());

    }
}
