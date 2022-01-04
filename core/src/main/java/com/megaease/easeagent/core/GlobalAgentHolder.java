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
package com.megaease.easeagent.core;

import com.megaease.easeagent.core.config.WrappedConfigManager;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.report.AgentReport;

public class GlobalAgentHolder {
    private static WrappedConfigManager wrappedConfigManager;
    private static AgentHttpServer agentHttpServer;
    private static AgentReport agentReport;

    private GlobalAgentHolder() {}

    public static void setWrappedConfigManager(WrappedConfigManager config) {
        wrappedConfigManager = config;
    }

    public static WrappedConfigManager getWrappedConfigManager() {
        return wrappedConfigManager;
    }

    public static void setAgentHttpServer(AgentHttpServer server) {
        agentHttpServer = server;
    }

    public static AgentHttpServer getAgentHttpServer() {
        return agentHttpServer;
    }

    public static void setAgentReport(AgentReport report) {
        agentReport = report;
    }

    public static AgentReport getAgentReport() {
        return agentReport;
    }
}
