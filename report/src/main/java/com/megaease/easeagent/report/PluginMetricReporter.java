package com.megaease.easeagent.report;

import com.megaease.easeagent.plugin.api.config.Config;

public interface PluginMetricReporter {
    Reporter reporter(Config config);

    interface Reporter {
        void report(String context);
    }
}
