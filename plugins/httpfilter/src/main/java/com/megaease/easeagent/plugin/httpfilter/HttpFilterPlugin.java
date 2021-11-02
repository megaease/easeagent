package com.megaease.easeagent.plugin.httpfilter;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.annotation.Plugin;
import com.megaease.easeagent.plugin.api.config.Config;

@Plugin
public class HttpFilterPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return "httpfilter";
    }

    @Override
    public String getDomain() {
        return "observability";
    }
}
