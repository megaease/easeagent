package com.megaease.easeagent.core.config;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.core.GlobalAgentHolder;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PluginPropertiesHttpHandler extends ConfigsUpdateAgentHttpHandler {
    public PluginPropertiesHttpHandler() {
        this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
    }

    @Override
    public String getPath() {
        return "/plugins/domains/:domain/namespaces/:namespace/:id/properties";
    }

    @Override
    public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
        try {
            String domain = ConfigUtils.requireNonEmpty(urlParams.get("domain"), "urlParams.domain must not be null and empty.");
            String namespace = ConfigUtils.requireNonEmpty(urlParams.get("namespace"), "urlParams.namespace must not be null and empty.");
            String id = ConfigUtils.requireNonEmpty(urlParams.get("id"), "urlParams.id must not be null and empty.");
            Map<String, String> changeConfig = new HashMap<>();
            for (Map.Entry<String, String> propertyEntry : config.entrySet()) {
                String property = ConfigUtils.buildPluginProperty(domain,
                    namespace,
                    id,
                    ConfigUtils.requireNonEmpty(propertyEntry.getKey(), "body.key must not be null and empty."));
                String value = Objects.requireNonNull(propertyEntry.getValue(), String.format("body.%s must not be null.", propertyEntry.getKey()));
                changeConfig.put(property, value);
            }
            this.mxBeanConfig.updateService2(changeConfig, version);
            return null;
        } catch (Exception e) {
            return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, e.getMessage());
        }
    }
}
