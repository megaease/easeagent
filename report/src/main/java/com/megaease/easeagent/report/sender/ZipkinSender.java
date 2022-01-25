package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.Sender;

import java.io.IOException;
import java.util.Map;

public class ZipkinSender implements Sender {
    public static final String SENDER_NAME = ReportConfigConst.ZIPKIN_SENDER_NAME;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        // delay
    }

    @Override
    public Callback<Void> send(byte[] encodedData) {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // delay
    }

    @Override
    public void close() throws IOException {
        // delay
    }
}
