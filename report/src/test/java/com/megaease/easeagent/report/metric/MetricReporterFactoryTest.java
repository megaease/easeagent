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
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.metric.MetricReporterFactory;
import com.megaease.easeagent.report.DefaultAgentReport;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class MetricReporterFactoryTest {
    @Test
    public void global_config_test() {
        Map<String, String> cfgMap = new HashMap<>();
        String testNamespace = "test";

        cfgMap.put("reporter.outputServer.bootstrapServer", "http://127.0.0.1:8080/report");
        cfgMap.put("plugin.observability.global.metric.enabled", "true");
        cfgMap.put("plugin.observability.global.metric.interval", "30");
        cfgMap.put("plugin.observability.global.metric.topic", "application-meter");
        cfgMap.put("plugin.observability.global.metric.appendType", "kafka");

        cfgMap.put("plugin.observability." + testNamespace + ".metric.topic", "test-meter");
        cfgMap.put("plugin.observability." + testNamespace + ".metric.appendType", "console");
        cfgMap.put("plugin.observability." + testNamespace + ".metric.interval", "30");
        cfgMap.put("plugin.observability." + testNamespace + ".metric.enabled", "true");

        cfgMap.put("reporter.outputServer.appendType", "http");

        Configs config = new GlobalConfigs(cfgMap);

        DefaultAgentReport agentReport = (DefaultAgentReport)DefaultAgentReport.create(config);
        MetricReporterFactory metricReporterFactory = agentReport.metricReporter();

        // --- generate plugin config
        HashMap<String, String> globalConfig = new HashMap<>();
        globalConfig.put("interval", "30");
        globalConfig.put("topic", "application-meter");
        globalConfig.put("appendType", "kafka");

        HashMap<String, String> coverConfig = new HashMap<>();
        coverConfig.put("interval", "30");
        coverConfig.put("topic", "xxx-meter");
        coverConfig.put("appendType", "console");

        PluginConfig pluginConfig = PluginConfig.build("observability", "metric",
            globalConfig, testNamespace, coverConfig, null);

        Reporter reporter = metricReporterFactory.reporter(pluginConfig);
        MetricReporterFactoryImpl.DefaultMetricReporter dReporter = (MetricReporterFactoryImpl.DefaultMetricReporter) reporter;
        String prefix = dReporter.getMetricProps().getSenderPrefix();

        Config cfg = dReporter.getMetricConfig();
        Assert.assertEquals("console", cfg.getString(join(prefix, APPEND_TYPE_KEY)));
        Assert.assertEquals("test-meter", cfg.getString(join(prefix, TOPIC_KEY)));

        // change topic
        Map<String, String> changes = new HashMap<>();
        changes.put("plugin.observability.global.metric.topic", "tom");
        changes.put(join(join("plugin.observability", testNamespace), "metric.topic"), "john");

        config.updateConfigs(changes);
        // dReporter.onChange(pluginConfig, npCfg);
        Assert.assertEquals("john", cfg.getString(join(prefix, TOPIC_KEY)));

        // change sender
        changes.put(join(join("plugin.observability", testNamespace), "metric.appendType"), "http");
        config.updateConfigs(changes);
        SenderWithEncoder sender = dReporter.getSender();
        Assert.assertEquals("http", sender.name());
    }
}
