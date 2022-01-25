package com.megaease.easeagent.report;

import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.report.encoder.span.GlobalExtrasSupplier;

public class GlobalExtractor implements GlobalExtrasSupplier {
    static GlobalExtractor instance;
    final AutoRefreshConfigItem<String> serviceName;
    final AutoRefreshConfigItem<String> systemName;

    public static GlobalExtractor getInstance(Config configs) {
        if (instance == null) {
            synchronized (GlobalExtractor.class) {
                if (instance != null) {
                    return instance;
                }
                instance = new GlobalExtractor(configs);
            }
        }
        return instance;
    }

    private GlobalExtractor(Config configs) {
        serviceName = new AutoRefreshConfigItem<>(configs, ConfigConst.SERVICE_NAME, Config::getString);
        systemName = new AutoRefreshConfigItem<>(configs, ConfigConst.SYSTEM_NAME, Config::getString);
    }

    @Override
    public String service() {
        return serviceName.getValue();
    }

    @Override
    public String system() {
        return systemName.getValue();
    }
}
