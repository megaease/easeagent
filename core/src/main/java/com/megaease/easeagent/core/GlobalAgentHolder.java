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
