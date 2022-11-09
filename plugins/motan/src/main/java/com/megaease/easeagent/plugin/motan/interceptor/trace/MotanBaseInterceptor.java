package com.megaease.easeagent.plugin.motan.interceptor.trace;

import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.motan.config.MotanPluginConfig;

public abstract class MotanBaseInterceptor implements Interceptor {

    public static volatile MotanPluginConfig MOTAN_PLUGIN_CONFIG = null;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        MOTAN_PLUGIN_CONFIG = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, ConfigConst.Namespace.MOTAN, this.getType(), MotanPluginConfig.SUPPLIER);
    }
}
