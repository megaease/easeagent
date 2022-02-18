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
        Assert.assertEquals(CONSOLE_SENDER_NAME, reportConfigs.getString(METRIC_SENDER_NAME));
        Assert.assertEquals("30", reportConfigs.getString(METRIC_ASYNC_INTERVAL));
        Assert.assertEquals("test-meter", reportConfigs.getString(METRIC_SENDER_TOPIC));

        // test kafka
        globalConfig.put("observability.outputServer.bootstrapServer", "127.0.0.1:9092");
        pluginConfig = PluginConfig.build("observability", "metric",
            globalConfig, "test", coverConfig, null);
        props = MetricProps.newDefault(pluginConfig, new GlobalConfigs(globalConfig));
        reportConfigs = props.asReportConfig();
        Assert.assertEquals(METRIC_KAFKA_SENDER_NAME, reportConfigs.getString(METRIC_SENDER_NAME));
    }
}
