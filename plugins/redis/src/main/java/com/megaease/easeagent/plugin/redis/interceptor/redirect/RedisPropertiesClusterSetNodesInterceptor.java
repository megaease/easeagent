package com.megaease.easeagent.plugin.redis.interceptor.redirect;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.RedisPropertiesClusterAdvice;

@AdviceTo(value = RedisPropertiesClusterAdvice.class, plugin = RedisRedirectPlugin.class)
public class RedisPropertiesClusterSetNodesInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_REDIS);
        if (cnf == null) {
            return;
        }
        methodInfo.changeArg(0, cnf.getUris());
    }

    @Override
    public String getName() {
        return Order.REDIRECT.getName();
    }
}
