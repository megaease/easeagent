package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.Category;
import com.megaease.easeagent.report.DataItem;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.Processor;
import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetricProcessor implements Processor, ConfigChangeListener {
    private final Configs configs;
    private final ConcurrentHashMap<String, TypeSender> typeLoggers = new ConcurrentHashMap<>();
    private final AppenderManager appenderManager;

    public MetricProcessor(Configs configs) {
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(this);
    }


    @Override
    public boolean support(DataItem item) {
        return Category.Metric == item.getCategory();
    }

    @Override
    public void process(DataItem item) {
        TypeSender sender = typeLoggers.computeIfAbsent(item.getType(), this::newTypeLogger);
        sender.send(item.getContent());
    }

    private TypeSender newTypeLogger(String type) {
        return new TypeSender(type, this.appenderManager, new MetricPropsImpl(configs, type));
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        this.tryRefreshAppenders(list);
    }

    private void tryRefreshAppenders(List<ChangeItem> list) {
        if (Utils.isOutputPropertiesChange(list)) {
            this.appenderManager.refresh();
        }
    }
}
