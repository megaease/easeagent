package com.megaease.easeagent.plugin.motan.interceptor.trace.provider;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.advice.MotanProviderAdvice;
import com.megaease.easeagent.plugin.motan.interceptor.MotanBaseInterceptor;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.Provider;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;

@AdviceTo(value = MotanProviderAdvice.class, plugin = MotanPlugin.class)
public class MotanProviderTraceInterceptor extends MotanBaseInterceptor {

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Request request = (Request) methodInfo.getArgs()[0];
        Provider<?> provider = (Provider<?>) methodInfo.getArgs()[1];
        MotanCtxUtils.initProviderSpan(context, provider.getUrl(), request);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Response response = (Response) methodInfo.getRetValue();
        Throwable throwable = methodInfo.getThrowable();
        MotanCtxUtils.finishProviderSpan(response, throwable, context);
    }

}
