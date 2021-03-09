package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetricReport {
    private final Configs configs;
    private final ConcurrentHashMap<String, KeySender> typeLoggers = new ConcurrentHashMap<>();
    private final AppenderManager appenderManager;

    public MetricReport(Configs configs) {
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(new InternalListener());
    }

    public void report(MetricItem item) {
        KeySender sender = typeLoggers.computeIfAbsent(item.getKey(), this::newKeyLogger);
        sender.send(item.getContent());
    }

    private KeySender newKeyLogger(String key) {
        return new KeySender(key, this.appenderManager, Utils.extractMetricProps(configs,key));
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
}
