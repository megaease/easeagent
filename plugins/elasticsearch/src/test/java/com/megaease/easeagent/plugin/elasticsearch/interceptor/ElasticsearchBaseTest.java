/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.zipkin.ReportSpan;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.junit.Assert;
import org.junit.Before;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ElasticsearchBaseTest {

    protected Request request;
    protected String body;
    protected Response successResponse;
    protected Response failResponse;
    protected String errMsg;
    protected String index = "index-1";
    protected IPluginConfig config;
    protected ResponseListener responseListener ;

    @Before
    public void before() {
        Context context = EaseAgent.getContext();
        ContextUtils.setBeginTime(context);

        ReportMock.cleanLastSpan();
        request = new Request("GET", "/" + index + "/_search");
        body = "mock body";
        HttpEntity httpEntity = new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON);
        request.setEntity(httpEntity);

        errMsg = "mock exception";

        {
            successResponse = mock(Response.class);
            StatusLine statusLine = mock(StatusLine.class);
            when(statusLine.getStatusCode()).thenReturn(200);
            when(successResponse.getStatusLine()).thenReturn(statusLine);
        }

        {
            failResponse = mock(Response.class);
            StatusLine statusLine = mock(StatusLine.class);
            when(statusLine.getStatusCode()).thenReturn(500);
            when(failResponse.getStatusLine()).thenReturn(statusLine);
        }

        config = mock(IPluginConfig.class);
        when(config.namespace()).thenReturn("es");

        responseListener = mock(ResponseListener.class);

    }

    protected void assertTrace(boolean success, String error) {
        ReportSpan mockSpan = ReportMock.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals("index-1", mockSpan.tag("es.index"));
        assertEquals("GET /index-1/_search", mockSpan.tag("es.operation"));
        assertEquals(body, mockSpan.tag("es.body"));
        if (success) {
            assertNull(mockSpan.tag("error"));
        }
        if (!success) {
            assertNotNull(mockSpan.tag("error"));
            assertEquals(error, mockSpan.tag("error"));
        }
        assertNull(mockSpan.parentId());
    }

    protected void assertMetric(NameFactory nameFactory, MetricRegistry metricRegistry, boolean success) {
        Map<String, Metric> metrics = metricRegistry.getMetrics();
        Assert.assertFalse(metrics.isEmpty());
        Assert.assertNotNull(metrics.get(nameFactory.timerName(this.index, MetricSubType.DEFAULT)));
        Assert.assertNotNull(metrics.get(nameFactory.meterName(this.index, MetricSubType.DEFAULT)));
        Assert.assertNotNull(metrics.get(nameFactory.counterName(this.index, MetricSubType.DEFAULT)));
        Assert.assertNotNull(metrics.get(nameFactory.gaugeName(this.index, MetricSubType.DEFAULT)));
        if (success) {
            Assert.assertNull(metrics.get(nameFactory.meterName(this.index, MetricSubType.ERROR)));
            Assert.assertNull(metrics.get(nameFactory.counterName(this.index, MetricSubType.ERROR)));
        } else {
            Assert.assertNotNull(metrics.get(nameFactory.meterName(this.index, MetricSubType.ERROR)));
            Assert.assertNotNull(metrics.get(nameFactory.counterName(this.index, MetricSubType.ERROR)));
        }
        metricRegistry.remove(nameFactory.timerName(this.index, MetricSubType.DEFAULT));
        metricRegistry.remove(nameFactory.meterName(this.index, MetricSubType.DEFAULT));
        metricRegistry.remove(nameFactory.counterName(this.index, MetricSubType.DEFAULT));
        metricRegistry.remove(nameFactory.gaugeName(this.index, MetricSubType.DEFAULT));

        metricRegistry.remove(nameFactory.meterName(this.index, MetricSubType.ERROR));
        metricRegistry.remove(nameFactory.counterName(this.index, MetricSubType.ERROR));
    }
}
