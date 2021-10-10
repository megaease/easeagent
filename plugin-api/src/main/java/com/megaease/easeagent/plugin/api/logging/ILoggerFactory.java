package com.megaease.easeagent.plugin.api.logging;

public interface ILoggerFactory {
    /**
     * Returns a logger that logs to the Java agent log output.
     * @return A log where messages can be written to the Java agent log file or console.
     */
    public Logger getLogger(String name);
}
