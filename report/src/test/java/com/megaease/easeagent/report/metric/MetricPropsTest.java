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
package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.config.PluginConfig;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class MetricPropsTest {
    @Test
    public void test_plugin_props() {
        String testNamespace = "test";
        // test console
        HashMap<String, String> globalConfig = new HashMap<>();
        globalConfig.put("interval", "30");
        globalConfig.put("topic", "application-meter");
        globalConfig.put("appendType", "console");

        HashMap<String, String> coverConfig = new HashMap<>();
        coverConfig.put("interval", "30");
        coverConfig.put("topic", "test-meter");
        coverConfig.put("appendType", "kafka");

        PluginConfig pluginConfig = PluginConfig.build("observability", "metric",
            globalConfig, testNamespace, coverConfig, null);

        Map<String, String> cfgMap = new HashMap<>();

        cfgMap.put("reporter.outputServer.bootstrapServer", "http://127.0.0.1:8080/report");
        cfgMap.put("plugin.observability.global.metric.enabled", "true");
        cfgMap.put("plugin.observability.global.metric.interval", "30");
        cfgMap.put("plugin.observability.global.metric.topic", "application-meter");
        cfgMap.put("plugin.observability.global.metric.appendType", CONSOLE_SENDER_NAME);

        cfgMap.put("plugin.observability." + testNamespace + ".metric.topic", "test-meter");
        cfgMap.put("plugin.observability." + testNamespace + ".metric.appendType", "kafka");
        cfgMap.put("plugin.observability." + testNamespace + ".metric.interval", "30");
        cfgMap.put("plugin.observability." + testNamespace + ".metric.enabled", "true");

        cfgMap.put("reporter.outputServer.appendType", "http");

        MetricProps props = MetricProps.newDefault(pluginConfig, new GlobalConfigs(cfgMap));
        Configs reportConfigs = props.asReportConfig();
        String prefix = props.getSenderPrefix();

        // test kafka sender / topic / interval
        Assert.assertEquals(METRIC_KAFKA_SENDER_NAME, reportConfigs.getString(join(prefix, APPEND_TYPE_KEY)));

        Assert.assertEquals("30",
            reportConfigs.getString(join(StringUtils.replaceSuffix(prefix, ASYNC_KEY), INTERVAL_KEY)));

        Assert.assertEquals("test-meter", reportConfigs.getString(join(prefix, TOPIC_KEY)));

        // test console
        cfgMap.remove("plugin.observability." + testNamespace + ".metric.appendType");
        props = MetricProps.newDefault(pluginConfig, new GlobalConfigs(cfgMap));
        reportConfigs = props.asReportConfig();
        Assert.assertEquals(CONSOLE_SENDER_NAME, reportConfigs.getString(join(prefix, APPEND_TYPE_KEY)));

        // test http
        cfgMap.remove("plugin.observability.global.metric.appendType");
        props = MetricProps.newDefault(pluginConfig, new GlobalConfigs(cfgMap));
        reportConfigs = props.asReportConfig();
        Assert.assertEquals(ZIPKIN_SENDER_NAME, reportConfigs.getString(join(prefix, APPEND_TYPE_KEY)));
    }
}
