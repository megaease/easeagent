package com.megaease.easeagent.plugin.redis.interceptor.redirect;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.RedisPropertiesAdvice;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

@AdviceTo(value = RedisPropertiesAdvice.class, plugin = RedisRedirectPlugin.class)
public class RedisPropertiesSetPropertyInterceptor implements Interceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_REDIS);
        if (cnf == null) {
            return;
        }
        String method = methodInfo.getMethod();
        ResourceConfig.HostAndPort hostAndPort = cnf.getFirstHostAndPort();
        String host = hostAndPort.getHost();
        Integer port = hostAndPort.getPort();
        if (method.equals("setHost") && host != null) {
            methodInfo.changeArg(0, host);
        } else if (method.equals("setPort") && port != null) {
            methodInfo.changeArg(0, port);
        } else if (method.equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            methodInfo.changeArg(0, cnf.getPassword());
        }
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }
}
