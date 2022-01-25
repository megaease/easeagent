package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.report.plugin.NoOpCallback;

import java.io.IOException;
import java.util.Map;

public class NoOpSender implements Sender {
    public static final NoOpSender INSTANCE = new NoOpSender();

    @Override
    public String name() {
        return "noop";
    }

    @Override
    public void init(Config config) {
        // ignored
    }

    @Override
    public Callback<Void> send(byte[] encodedData) {
        return new NoOpCallback<>();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // ignored
    }

    @Override
    public void close() throws IOException {
        // ignored
    }
}
