package com.megaease.easeagent.plugin.mongodb.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.mongodb.MongoDBRedirectPlugin;
import com.megaease.easeagent.plugin.mongodb.points.MongoDBRedirectPoints;

@AdviceTo(value = MongoDBRedirectPoints.class, plugin = MongoDBRedirectPlugin.class)
public class MongoDbRedirectInterceptor implements Interceptor {

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        Interceptor.super.init(config, className, methodName, methodDescriptor);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.MONGODB.getConfig();
        if (cnf == null) {
            return;
        }
        methodInfo.changeArg(0, cnf.getFirstUri());
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }


}
