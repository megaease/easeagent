package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.apache;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.ApacheDubboAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboBaseMetricsInterceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

@AdviceTo(value = ApacheDubboAdvice.class, plugin = DubboPlugin.class)
public class ApacheDubboMetricsInterceptor extends DubboBaseMetricsInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ContextUtils.setBeginTime(context);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
        Invocation invocation = (Invocation) methodInfo.getArgs()[1];
        if (invoker.getUrl().getPath().equals(ApacheDubboCtxUtils.METADATA_INTERFACE)) {
            return;
        }

        String service = ApacheDubboCtxUtils.interfaceSignature(invocation);
        context.put(ApacheDubboCtxUtils.METRICS_SERVICE_NAME, service);
        Result retValue = (Result) methodInfo.getRetValue();
        if (retValue instanceof AsyncRpcResult) {
            retValue.whenCompleteWithContext(new ApacheDubboMetricsAsyncCallback(context.exportAsync(), DUBBO_METRICS));
        } else {
            Long duration = ContextUtils.getDuration(context);
            boolean callResult = ApacheDubboCtxUtils.checkCallResult(retValue, methodInfo.getThrowable());
            DUBBO_METRICS.collect(service, duration, callResult);
        }
    }
}
