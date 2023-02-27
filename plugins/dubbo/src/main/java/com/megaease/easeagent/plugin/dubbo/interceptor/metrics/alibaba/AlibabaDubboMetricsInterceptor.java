package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.AlibabaDubboAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboBaseMetricsInterceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.utils.SystemClock;

import java.util.concurrent.Future;


@AdviceTo(value = AlibabaDubboAdvice.class, plugin = DubboPlugin.class)
public class AlibabaDubboMetricsInterceptor extends DubboBaseMetricsInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		context.put(AlibabaDubboCtxUtils.BEGIN_TIME, SystemClock.now());
        Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
        Invocation invocation = (Invocation) methodInfo.getArgs()[1];
        boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        boolean isConsumer = invoker.getUrl().getParameter(Constants.SIDE_KEY, Constants.PROVIDER_SIDE).equals(Constants.CONSUMER_SIDE);
        if (isConsumer && isAsync) {
		    String interfaceSignature = AlibabaDubboCtxUtils.interfaceSignature(invocation);
            context.put(AlibabaDubboCtxUtils.METRICS_SERVICE_NAME, interfaceSignature);
        }
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
		Invocation invocation = (Invocation) methodInfo.getArgs()[1];

		boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        Future<?> f = RpcContext.getContext().getFuture();
		if (isAsync && f instanceof FutureAdapter) {
            return;
		}

		Long duration = ContextUtils.getDuration(context, AlibabaDubboCtxUtils.BEGIN_TIME);
		Result retValue = (Result) methodInfo.getRetValue();
		String interfaceSignature = AlibabaDubboCtxUtils.interfaceSignature(invocation);
        boolean callResult = AlibabaDubboCtxUtils.checkCallResult(retValue, methodInfo.getThrowable());
        DUBBO_METRICS.collect(interfaceSignature, duration, callResult);
	}
}
