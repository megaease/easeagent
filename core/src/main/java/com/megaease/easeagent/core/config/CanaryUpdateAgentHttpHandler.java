package com.megaease.easeagent.core.config;

import com.megaease.easeagent.core.GlobalAgentHolder;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;

import java.util.Map;

public class CanaryUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
    public CanaryUpdateAgentHttpHandler() {
        this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
    }

    @Override
    public String getPath() {
        return "/config-canary";
    }

    @Override
    public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
        this.mxBeanConfig.updateCanary2(config, version);
        return null;
    }
}
