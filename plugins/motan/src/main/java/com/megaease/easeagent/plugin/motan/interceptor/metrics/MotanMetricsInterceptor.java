package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.advice.MotanConsumerAdvice;
import com.megaease.easeagent.plugin.motan.interceptor.MotanClassUtils;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.*;
import com.weibo.api.motan.transport.netty.NettyResponseFuture;

@AdviceTo(value = MotanConsumerAdvice.class, plugin = MotanPlugin.class)
public class MotanMetricsInterceptor extends MotanBaseMetricsInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ContextUtils.setBeginTime(context);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        URL url = ((AbstractReferer<?>) methodInfo.getInvoker()).getUrl();
        Request request = (Request) methodInfo.getArgs()[0];
        Throwable throwable = methodInfo.getThrowable();
        String interfaceSignature = MotanCtxUtils.interfaceSignature(request);
        Long duration = ContextUtils.getDuration(context);
        Response response = (Response) methodInfo.getRetValue();

        if (throwable != null) {
            motanMetric.collectMetric(interfaceSignature, duration, false);
        } else if (MotanClassUtils.NettyResponseFutureTypeChecker.getTypeChecker().hasClassAndIsType(response)){
            NettyResponseFuture nettyResponseFuture = (NettyResponseFuture) response;
            nettyResponseFuture.addListener(listener(interfaceSignature, duration));
        } else if (MotanClassUtils.DefaultResponseFutureTypeChecker.getTypeChecker().hasClassAndIsType(response)) {
            DefaultResponseFuture defaultResponseFuture = (DefaultResponseFuture) response;
            defaultResponseFuture.addListener(listener(interfaceSignature, duration));
        } else {
            boolean callResult = response != null && response.getException() == null;
            motanMetric.collectMetric(interfaceSignature, duration, callResult);
        }
    }

    private FutureListener listener(String endpoint, Long duration) {
        return new FutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                boolean callResult = future.getException() == null;
                motanMetric.collectMetric(endpoint, duration, callResult);
            }
        };
    }

}
