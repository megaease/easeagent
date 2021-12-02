package com.megaease.easeagent.plugin.kafka.interceptor.redirect;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConfigProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;

import java.util.Map;
import java.util.Properties;

public class KafkaAbstractConfigConstructInterceptor implements FirstEnterInterceptor {

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_KAFKA);
        if (cnf == null) {
            return;
        }
        if (methodInfo.getArgs()[0] instanceof Properties) {
            Properties properties = (Properties) methodInfo.getArgs()[0];
            properties.put("bootstrap.servers", cnf.getUris());
            methodInfo.changeArg(0, properties);
        } else if (methodInfo.getArgs()[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) methodInfo.getArgs()[0];
            map.put("bootstrap.servers", cnf.getUris());
            methodInfo.changeArg(0, map);
        }
    }


    @Override
    public String getName() {
        return Order.REDIRECT.getName();
    }
}
