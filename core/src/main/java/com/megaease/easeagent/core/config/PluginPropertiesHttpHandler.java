/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
