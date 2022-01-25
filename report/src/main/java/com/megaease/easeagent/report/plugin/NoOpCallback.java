package com.megaease.easeagent.report.plugin;

import com.megaease.easeagent.plugin.report.Callback;

import java.io.IOException;

public class NoOpCallback<V> implements Callback<V> {
    @Override
    public V execute() throws IOException {
        return null;
    }
}
