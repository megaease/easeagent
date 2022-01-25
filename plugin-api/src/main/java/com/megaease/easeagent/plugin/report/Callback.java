package com.megaease.easeagent.plugin.report;

import java.io.IOException;

@SuppressWarnings("unused")
public interface Callback<V> {
    V execute() throws IOException;

    default V enqueue() {
        return null;
    }
}
