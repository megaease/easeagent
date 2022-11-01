package com.megaease.easeagent.plugin.motan.interceptor.trace.consumer;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.advice.MotanConsumerAdvice;
import com.megaease.easeagent.plugin.motan.interceptor.MotanBaseInterceptor;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.AbstractReferer;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.URL;

@AdviceTo(value = MotanConsumerAdvice.class, plugin = MotanPlugin.class)
public class MotanConsumerTraceInterceptor extends MotanBaseInterceptor {

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        URL url = ((AbstractReferer<?>) methodInfo.getInvoker()).getUrl();
        Request request = (Request) methodInfo.getArgs()[0];
        MotanCtxUtils.initSpan(context, url, request, MOTAN_PLUGIN_CONFIG);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        URL url = ((AbstractReferer<?>) methodInfo.getInvoker()).getUrl();
        MotanCtxUtils.finishSpan(methodInfo, url, context, MOTAN_PLUGIN_CONFIG);
    }


}
