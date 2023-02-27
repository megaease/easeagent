package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.AlibabaDubboResponseFutureAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboBaseMetricsInterceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;

@AdviceTo(value = AlibabaDubboResponseFutureAdvice.class,plugin = DubboPlugin.class)
public class AlibabaDubboAsyncMetricsInterceptor extends DubboBaseMetricsInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResponseCallback responseCallback = (ResponseCallback) methodInfo.getArgs()[0];
        AsyncContext asyncContext = context.exportAsync();
        AlibabaDubboMetricsCallback alibabaDubboMetricsCallback = new AlibabaDubboMetricsCallback(responseCallback, asyncContext);
        methodInfo.changeArg(0, alibabaDubboMetricsCallback);
    }
}
