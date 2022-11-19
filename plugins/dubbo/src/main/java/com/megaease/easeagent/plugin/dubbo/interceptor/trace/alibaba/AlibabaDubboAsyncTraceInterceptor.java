package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.AlibabaDubboResponseFutureAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.DubboBaseInterceptor;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;

@AdviceTo(value = AlibabaDubboResponseFutureAdvice.class,plugin = DubboPlugin.class)
public class AlibabaDubboAsyncTraceInterceptor extends DubboBaseInterceptor {
    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResponseCallback responseCallback = (ResponseCallback) methodInfo.getArgs()[0];
        AsyncContext asyncContext = context.exportAsync();
        RequestContext requestContext = context.get(AlibabaDubboCtxUtils.CLIENT_REQUEST_CONTEXT);
        try(Scope scope = requestContext.scope()) {
            methodInfo.changeArg(0, new AlibabaDubboTraceCallback(responseCallback, asyncContext));
        }
    }
}
