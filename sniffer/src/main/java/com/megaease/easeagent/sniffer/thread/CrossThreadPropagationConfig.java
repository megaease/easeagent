package com.megaease.easeagent.sniffer.thread;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.config.ConfigUtils;

public class CrossThreadPropagationConfig {
    private volatile String[] canaryHeaders = new String[0];

    public CrossThreadPropagationConfig(Config config) {
        ConfigUtils.bindProp(ConfigConst.CANARY_HEADERS, config, Config::getString, v -> this.canaryHeaders = v.split(","));
    }

    public String[] getCanaryHeaders() {
        return canaryHeaders;
    }
}
