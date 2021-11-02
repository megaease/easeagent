package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.trace.TraceContext;

public interface InitializeContext extends Context, TraceContext {
    void pushConfig(Config config);

    Config popConfig();
}
