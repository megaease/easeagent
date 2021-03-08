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
    private final ConcurrentHashMap<String, TypeSender> typeLoggers = new ConcurrentHashMap<>();
    private final AppenderManager appenderManager;

    public MetricReport(Configs configs) {
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(new InternalListener());
    }

    public void report(MetricItem item) {
        TypeSender sender = typeLoggers.computeIfAbsent(item.getType(), this::newTypeLogger);
        sender.send(item.getContent());
    }

    private TypeSender newTypeLogger(String type) {
        return new TypeSender(type, this.appenderManager, new MetricPropsImpl(configs, type));
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
