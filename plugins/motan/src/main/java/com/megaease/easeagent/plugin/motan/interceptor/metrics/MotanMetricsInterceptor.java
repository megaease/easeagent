package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.advice.MotanConsumerAdvice;
import com.megaease.easeagent.plugin.motan.interceptor.MotanClassUtils;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.DefaultResponseFuture;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;

@AdviceTo(value = MotanConsumerAdvice.class, plugin = MotanPlugin.class)
public class MotanMetricsInterceptor extends MotanBaseMetricsInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ContextUtils.setBeginTime(context);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Request request = (Request) methodInfo.getArgs()[0];
        Response response = (Response) methodInfo.getRetValue();
        Throwable throwable = methodInfo.getThrowable();
        String interfaceSignature = MotanCtxUtils.interfaceSignature(request);
        context.put(MotanCtxUtils.METRICS_SERVICE_NAME,interfaceSignature);

        if (MotanClassUtils.DefaultResponseFutureTypeChecker.getTypeChecker().hasClassAndIsType(response)) {
            DefaultResponseFuture defaultResponseFuture = (DefaultResponseFuture) response;
            defaultResponseFuture.addListener(new MetricsFutureListener(motanMetric,context.exportAsync()));
        } else {
            Long duration = ContextUtils.getDuration(context);
            boolean callResult = throwable == null && response != null && response.getException() == null;
            motanMetric.collectMetric(interfaceSignature, duration, callResult);
        }
    }
}
