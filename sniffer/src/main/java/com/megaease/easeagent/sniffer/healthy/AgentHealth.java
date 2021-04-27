/*
 *   Copyright (c) 2017, MegaEase
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.megaease.easeagent.sniffer.healthy;

import com.megaease.easeagent.httpserver.AgentHttpHandler;
import com.megaease.easeagent.httpserver.AgentHttpServer;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import lombok.Data;

import java.util.Map;

@Data
public class AgentHealth {
    private boolean readinessEnabled;
    private boolean alive = true;
    private boolean ready;

    public static final AgentHealth instance = new AgentHealth();

    public static class HealthAgentHttpHandler extends AgentHttpHandler {

        @Override
        public String getPath() {
            return "/health";
        }

        @Override
        public NanoHTTPD.Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, AgentHttpServer.JSON_TYPE, null);
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
        public NanoHTTPD.Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            if (instance.isReadinessEnabled()) {
                if (instance.isReady()) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, AgentHttpServer.JSON_TYPE, null);
                }
                return NanoHTTPD.newFixedLengthResponse(Status.SERVICE_UNAVAILABLE, AgentHttpServer.JSON_TYPE, null);
            }
            return super.process(uriResource, urlParams, session);
        }
    }

    enum Status implements NanoHTTPD.Response.IStatus {

        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        ;
        private final int requestStatus;

        private final String description;

        Status(int requestStatus, String description) {
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
