package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.PluginMetricReporter;
import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PluginMetricReporterImpl implements PluginMetricReporter {
    private final Configs configs;
    private final ConcurrentHashMap<String, KeySender> typeLoggers = new ConcurrentHashMap<>();
    private final AppenderManager appenderManager;

    public PluginMetricReporterImpl(Configs configs) {
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(new PluginMetricReporterImpl.InternalListener());
    }

    public static PluginMetricReporter create(Configs config) {
        return new PluginMetricReporterImpl(config);
    }

    @Override
    public PluginMetricReporter.Reporter reporter(Config config) {
        return new Reporter(config);
    }

    private class InternalListener implements ConfigChangeListener {
        @Override
        public void onChange(List<ChangeItem> list) {
            this.tryRefreshAppenders(list);
        }

        private void tryRefreshAppenders(List<ChangeItem> list) {
            if (Utils.isOutputPropertiesChange(list)) {
                appenderManager.refresh();
            }
        }
    }

    public class Reporter implements PluginMetricReporter.Reporter {
        private final Config config;

        public Reporter(Config config) {
            this.config = config;
        }

        public void report(String context) {
            KeySender sender = typeLoggers.computeIfAbsent(config.domain(), this::newKeyLogger);
            sender.send(context);
        }

        private KeySender newKeyLogger(String key) {
            return new KeySender(key, PluginMetricReporterImpl.this.appenderManager, Utils.extractMetricProps(config));
        }
    }
}
