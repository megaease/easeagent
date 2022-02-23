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

package com.megaease.easeagent.plugin.mongodb;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterId;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.connection.ServerId;
import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoBaseTest {

    protected int requestId = 10000;
    protected String dbName = "db_demo";
    protected String cmdName = "insert";
    protected String collection = "mock-collection";
    protected ServerAddress serverAddress;
    protected ClusterId clusterId;
    protected ServerId serverId;
    protected ConnectionDescription connectionDescription;
    protected CommandStartedEvent startedEvent;
    protected AutoRefreshPluginConfigImpl config;
    protected String errMsg = "mock-err";


    @Before
    public void before() {
        EaseAgent.initializeContextSupplier.get().clear();
        config = new AutoRefreshPluginConfigImpl();
        IPluginConfig iPluginConfig = mock(IPluginConfig.class);
        when(iPluginConfig.enabled()).thenReturn(true);
        when(iPluginConfig.namespace()).thenReturn("mongodb");
        when(iPluginConfig.domain()).thenReturn("observability");
        config.onChange(null, iPluginConfig);
        Context context = EaseAgent.getContext();
        ContextUtils.setBeginTime(context);
        MockEaseAgent.cleanLastSpan();
        clusterId = new ClusterId("local-cluster");
        serverAddress = new ServerAddress("127.0.0.1", 2020);
        serverId = new ServerId(clusterId, serverAddress);
        this.connectionDescription = new ConnectionDescription(serverId);
        Map<String, Object> map = new HashMap<>();
        map.put("collection", collection);
        BsonDocument bsonDocument = BsonDocument.parse(JsonUtil.toJson(map));
        this.startedEvent = new CommandStartedEvent(this.requestId, this.connectionDescription, this.dbName, this.cmdName, bsonDocument);
    }

    protected void assertTrace(boolean success, String error) {
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals("mongodb-" + this.dbName, mockSpan.remoteServiceName());
        assertEquals(this.cmdName, mockSpan.tag("mongodb.command"));
        assertEquals("mongodb", mockSpan.tag("component.type"));
        assertEquals(this.collection, mockSpan.tag("mongodb.collection"));
        assertEquals(this.clusterId.getValue(), mockSpan.tag("mongodb.cluster_id"));
        assertEquals(this.serverAddress.getHost(), mockSpan.remoteEndpoint().ipv4());
        assertEquals(this.serverAddress.getPort(), mockSpan.remoteEndpoint().port());
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
        Assert.assertNotNull(metrics.get(nameFactory.timerName(this.cmdName, MetricSubType.DEFAULT)));
        Assert.assertNotNull(metrics.get(nameFactory.meterName(this.cmdName, MetricSubType.DEFAULT)));
        Assert.assertNotNull(metrics.get(nameFactory.counterName(this.cmdName, MetricSubType.DEFAULT)));
        Assert.assertNotNull(metrics.get(nameFactory.gaugeName(this.cmdName, MetricSubType.DEFAULT)));
        if (success) {
            Assert.assertNull(metrics.get(nameFactory.meterName(this.cmdName, MetricSubType.ERROR)));
            Assert.assertNull(metrics.get(nameFactory.counterName(this.cmdName, MetricSubType.ERROR)));
        } else {
            Assert.assertNotNull(metrics.get(nameFactory.meterName(this.cmdName, MetricSubType.ERROR)));
            Assert.assertNotNull(metrics.get(nameFactory.counterName(this.cmdName, MetricSubType.ERROR)));
        }
        metricRegistry.remove(nameFactory.timerName(this.cmdName, MetricSubType.DEFAULT));
        metricRegistry.remove(nameFactory.meterName(this.cmdName, MetricSubType.DEFAULT));
        metricRegistry.remove(nameFactory.counterName(this.cmdName, MetricSubType.DEFAULT));
        metricRegistry.remove(nameFactory.gaugeName(this.cmdName, MetricSubType.DEFAULT));

        metricRegistry.remove(nameFactory.meterName(this.cmdName, MetricSubType.ERROR));
        metricRegistry.remove(nameFactory.counterName(this.cmdName, MetricSubType.ERROR));
    }

}
