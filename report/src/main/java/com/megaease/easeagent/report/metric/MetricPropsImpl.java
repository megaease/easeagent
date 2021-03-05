package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.Configs;

public class MetricPropsImpl implements MetricProps {
    private volatile boolean enabled = false;
    private volatile String appendType;
    private volatile String topic;

    public MetricPropsImpl(Configs configs, String type) {
        // todo init props by configs
        configs.addChangeListener(list -> {
            // todo check and update props
        });
    }

    @Override
    public String getAppendType() {
        return this.appendType;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
