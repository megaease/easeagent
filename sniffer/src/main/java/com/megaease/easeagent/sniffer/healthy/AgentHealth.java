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
import com.megaease.easeagent.httpserver.HttpResponse;
import com.sun.net.httpserver.HttpExchange;
import lombok.Data;

@Data
public class AgentHealth {
    private boolean readinessEnabled;
    private boolean alive = true;
    private boolean ready;

    public static class HealthAgentHttpHandler extends AgentHttpHandler {

        protected final AgentHealth agentHealth;

        public HealthAgentHttpHandler(AgentHealth agentHealth) {
            this.agentHealth = agentHealth;
        }

        @Override
        public String getPath() {
            return "/health";
        }

        @Override
        public HttpResponse process(HttpExchange exchange) {
            return HttpResponse.builder().statusCode(200).build();
        }
    }

    public static class LivenessAgentHttpHandler extends HealthAgentHttpHandler {

        public LivenessAgentHttpHandler(AgentHealth agentHealth) {
            super(agentHealth);
        }

        @Override
        public String getPath() {
            return "/health/liveness";
        }

    }

    public static class ReadinessAgentHttpHandler extends HealthAgentHttpHandler {

        public ReadinessAgentHttpHandler(AgentHealth agentHealth) {
            super(agentHealth);
        }

        @Override
        public String getPath() {
            return "/health/readiness";
        }

        @Override
        public HttpResponse process(HttpExchange exchange) {
            if (this.agentHealth.isReadinessEnabled()) {
                if (this.agentHealth.isReady()) {
                    return HttpResponse.builder().statusCode(200).build();
                }
                return HttpResponse.builder().statusCode(500).build();
            }
            return super.process(exchange);
        }
    }

}
