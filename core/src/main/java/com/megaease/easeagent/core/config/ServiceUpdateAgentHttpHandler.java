package com.megaease.easeagent.core.config;

import com.megaease.easeagent.config.CompatibilityConversion;
import com.megaease.easeagent.config.ConfigManagerMXBean;
import com.megaease.easeagent.core.Bootstrap;
import com.megaease.easeagent.core.GlobalAgentHolder;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;

import java.util.Map;

public class ServiceUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {
    public ServiceUpdateAgentHttpHandler() {
        this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
    }

    @Override
    public String getPath() {
        return "/config-service";
    }

    @Override
    public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
        this.mxBeanConfig.updateService2(CompatibilityConversion.transform(config), version);
        return null;
    }
}
