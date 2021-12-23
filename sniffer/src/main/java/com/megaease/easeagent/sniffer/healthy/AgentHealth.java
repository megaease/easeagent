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

import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
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
        public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String)null);
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
            if (instance.isReadinessEnabled()) {
                if (instance.isReady()) {
                    return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
                }

                return Response.newFixedLengthResponse(HStatus.SERVICE_UNAVAILABLE,
                    AgentHttpServer.JSON_TYPE, (String) null);
            }
            return super.process(uriResource, urlParams, session);
        }
    }

    enum HStatus implements IStatus {

        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        ;
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
