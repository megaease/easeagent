/*
 * Copyright (c) 2021, MegaEase
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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class MetricPropsTest {
    @Test
    public void test_plugin_props() {
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
            globalConfig, "test", coverConfig, null);

        MetricProps props = MetricProps.newDefault(pluginConfig, new GlobalConfigs(globalConfig));
        Configs reportConfigs = props.asReportConfig();
        String prefix = props.getSenderPrefix();

        Assert.assertEquals(CONSOLE_SENDER_NAME, reportConfigs.getString(join(prefix, NAME_KEY)));
        Assert.assertEquals("30", reportConfigs.getString(join(prefix, INTERVAL_KEY)));
        Assert.assertEquals("test-meter", reportConfigs.getString(join(prefix, TOPIC_KEY)));

        // test kafka
        globalConfig.put("observability.outputServer.bootstrapServer", "127.0.0.1:9092");
        pluginConfig = PluginConfig.build("observability", "metric",
            globalConfig, "test", coverConfig, null);

        props = MetricProps.newDefault(pluginConfig, new GlobalConfigs(globalConfig));
        reportConfigs = props.asReportConfig();
        Assert.assertEquals(METRIC_KAFKA_SENDER_NAME, reportConfigs.getString(join(prefix, NAME_KEY)));
    }
}
