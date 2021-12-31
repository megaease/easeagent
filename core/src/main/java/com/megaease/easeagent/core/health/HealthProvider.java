/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.core.health;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.plugin.IProvider;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandlerProvider;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.megaease.easeagent.plugin.BeanProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.megaease.easeagent.plugin.api.health.AgentHealth;

public class HealthProvider implements AgentHttpHandlerProvider, ConfigAware, IProvider, BeanProvider {
    private static final String EASEAGENT_HEALTH_READINESS_ENABLED = "easeagent.health.readiness.enabled";

    private Config config;

    @Override
    public List<AgentHttpHandler> getAgentHttpHandlers() {
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new HealthAgentHttpHandler());
        list.add(new LivenessAgentHttpHandler());
        list.add(new ReadinessAgentHttpHandler());
        return list;
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() {
        AgentHealth.setReadinessEnabled(this.config.getBoolean(EASEAGENT_HEALTH_READINESS_ENABLED));
    }


    public static class HealthAgentHttpHandler extends AgentHttpHandler {

        @Override
        public String getPath() {
            return "/health";
        }

        @Override
        public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
        }
    }

    public static class LivenessAgentHttpHandler extends HealthAgentHttpHandler {

        @Override
        public String getPath() {
            return "/health/liveness";
        }

    }

    public static class ReadinessAgentHttpHandler extends HealthAgentHttpHandler {

        @Override
        public String getPath() {
            return "/health/readiness";
        }

        @Override
        public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            if (AgentHealth.INSTANCE.isReadinessEnabled()) {
                if (AgentHealth.INSTANCE.isReady()) {
                    return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
                }

                return Response.newFixedLengthResponse(HStatus.SERVICE_UNAVAILABLE,
                    AgentHttpServer.JSON_TYPE, (String) null);
            }
            return super.process(uriResource, urlParams, session);
        }
    }

    enum HStatus implements IStatus {

        SERVICE_UNAVAILABLE(503, "Service Unavailable"),;
        private final int requestStatus;

        private final String description;

        HStatus(int requestStatus, String description) {
            this.requestStatus = requestStatus;
            this.description = description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getRequestStatus() {
            return requestStatus;
        }
    }
}
