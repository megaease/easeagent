package com.megaease.easeagent.plugin.jdkhttpserver;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.annotation.Plugin;
import com.megaease.easeagent.plugin.api.config.Config;

@Plugin
public class JdkHttpServerPlugin implements AgentPlugin {
    @Override
    public String getName() {
        return "jdkhttpserver-trace";
    }

    @Override
    public String getDomain() {
        return "observability";
    }
}
