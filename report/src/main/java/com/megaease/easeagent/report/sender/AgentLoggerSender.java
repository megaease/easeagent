package com.megaease.easeagent.report.sender;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.Sender;

import java.io.IOException;
import java.util.Map;

/**
 * Send span data to agent log
 * It will be printed to console, when the logger configuration to append to console
 */
@AutoService(Sender.class)
public class AgentLoggerSender implements Sender {
    public static final String SENDER_NAME = ReportConfigConst.CONSOLE_SENDER_NAME;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentLoggerSender.class);

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        // ignored
    }

    @Override
    public Callback<Void> send(byte[] encodedData) {
        return new ConsoleCallback(encodedData);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // ignored
    }

    @Override
    public void close() throws IOException {
        // ignored
    }

    static class ConsoleCallback implements Callback<Void> {
        private final byte[] msg;

        ConsoleCallback(byte[] msg) {
            this.msg = msg;
        }

        @Override
        public Void execute() throws IOException {
            LOGGER.info("{}", new String(msg));
            return null;
        }

        @Override
        public Void enqueue() {
            LOGGER.debug("{}", new String(msg));
            return null;
        }
    }
}
