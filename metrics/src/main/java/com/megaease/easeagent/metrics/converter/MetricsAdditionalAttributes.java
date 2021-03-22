package com.megaease.easeagent.metrics.converter;

import com.megaease.easeagent.common.AdditionalAttributes;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.config.ConfigUtils;

import java.util.Map;
import java.util.function.Supplier;

public class MetricsAdditionalAttributes implements Supplier<Map<String, Object>> {

    private volatile Map<String, Object> additionalAttributes;

    public MetricsAdditionalAttributes(Config config) {
        ConfigUtils.bindProp(ConfigConst.SERVICE_NAME, config, Config::getString, v -> {
            this.additionalAttributes = new AdditionalAttributes(v).getAdditionalAttributes();
        });
    }

    @Override
    public Map<String, Object> get() {
        return additionalAttributes;
    }
}
