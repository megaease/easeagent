/*
 * Copyright (c) 2022, MegaEase
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

import com.megaease.easeagent.config.ConfigManagerMXBean;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.core.GlobalAgentHolder;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class PluginPropertyHttpHandler extends AgentHttpHandler {
    ConfigManagerMXBean mxBeanConfig;

    public PluginPropertyHttpHandler() {
        this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
        methods = Collections.singleton(
            com.megaease.easeagent.httpserver.nanohttpd.protocols.http.request.Method.GET);
    }

    @Override
    public String getPath() {
        return "/plugins/domains/:domain/namespaces/:namespace/:id/properties/:property/:value/:version";
    }

    @SneakyThrows
    @Override
    public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        String version = urlParams.get("version");
        if (version == null) {
            return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
        }
        try {
            String property = ConfigUtils.buildPluginProperty(
                ConfigUtils.requireNonEmpty(urlParams.get("domain"), "urlParams.domain must not be null and empty."),
                ConfigUtils.requireNonEmpty(urlParams.get("namespace"), "urlParams.namespace must not be null and empty."),
                ConfigUtils.requireNonEmpty(urlParams.get("id"), "urlParams.id must not be null and empty."),
                ConfigUtils.requireNonEmpty(urlParams.get("property"), "urlParams.property must not be null and empty."));
            String value = Objects.requireNonNull(urlParams.get("value"), "urlParams.value must not be null.");
            this.mxBeanConfig.updateService2(Collections.singletonMap(property, value), version);
            return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
        } catch (Exception e) {
            return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, e.getMessage());
        }

    }
}
