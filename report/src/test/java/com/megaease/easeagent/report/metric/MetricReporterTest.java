package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.config.PluginConfig;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.report.DefaultAgentReport;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class MetricReporterTest {
    @Test
    public void global_config_test() {
        Map<String, String> cfgMap = new HashMap<>();
        String testNamespace = "test";

        cfgMap.put("reporter.outputServer.bootstrapServer", "http://127.0.0.1:8080/report");
        cfgMap.put("plugin.observability.global.metric.enabled", "true");
        cfgMap.put("plugin.observability.global.metric.interval", "30");
        cfgMap.put("plugin.observability.global.metric.topic", "application-meter");
        cfgMap.put("plugin.observability.global.metric.appendType", "kafka");
        cfgMap.put("plugin.observability.access.metric.topic", "application-log");

        Configs config = new GlobalConfigs(cfgMap);

        DefaultAgentReport agentReport = (DefaultAgentReport)DefaultAgentReport.create(config);
        MetricReporter metricReporter = agentReport.metricReporter();

        // --- generate plugin config
        HashMap<String, String> globalConfig = new HashMap<>();
        globalConfig.put("interval", "30");
        globalConfig.put("topic", "application-meter");
        globalConfig.put("appendType", "kafka");

        HashMap<String, String> coverConfig = new HashMap<>();
        coverConfig.put("interval", "30");
        coverConfig.put("topic", "test-meter");
        coverConfig.put("appendType", "console");

        PluginConfig pluginConfig = PluginConfig.build("observability", "metric",
            globalConfig, testNamespace, coverConfig, null);

        Reporter reporter = metricReporter.reporter(pluginConfig);
        MetricReporterImpl.DefaultMetricReporter dReporter = (MetricReporterImpl.DefaultMetricReporter) reporter;
        String prefix = dReporter.getMetricProps().getSenderPrefix();
        Config cfg = dReporter.getReporterConfig();
        Assert.assertEquals("console", cfg.getString(join(prefix, NAME_KEY)));
        Assert.assertEquals("test-meter", cfg.getString(join(prefix, TOPIC_KEY)));

        // change topic
        Map<String, String> changes = new HashMap<>();
        changes.put("plugin.observability.global.metric.topic", "tom");
        changes.put(join(join("plugin.observability", testNamespace), "metric.topic"), "john");
        coverConfig.put(TOPIC_KEY, "john");
        PluginConfig npCfg = PluginConfig.build("observability", "metric",
            globalConfig, testNamespace, coverConfig, null);

        config.updateConfigs(changes);
        dReporter.onChange(pluginConfig, npCfg);
        pluginConfig = npCfg;

        Assert.assertEquals("john", cfg.getString(join(prefix, TOPIC_KEY)));

        // change sender
        changes.put(join(join("plugin.observability", testNamespace), "metric.appendType"), "http");
        coverConfig.put(NAME_KEY, "http");
        npCfg = PluginConfig.build("observability", "metric",
            globalConfig, testNamespace, coverConfig, null);
        config.updateConfigs(changes);
        dReporter.onChange(pluginConfig, npCfg);
        SenderWithEncoder sender = dReporter.getSender();
        Assert.assertEquals("http", sender.name());
    }
}
