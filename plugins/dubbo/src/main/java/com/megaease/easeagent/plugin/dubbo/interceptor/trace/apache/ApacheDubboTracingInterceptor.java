package com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.ApacheDubboAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.DubboBaseInterceptor;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

@AdviceTo(value = ApacheDubboAdvice.class, plugin = DubboPlugin.class)
public class ApacheDubboTracingInterceptor extends DubboBaseInterceptor {


	@Override
	public String getType() {
		return Order.TRACING.getName();
	}

	@Override
	public int order() {
		return Order.TRACING.getOrder();
	}

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		ApacheDubboCtxUtils.initSpan(methodInfo, context, DUBBO_TRACE_CONFIG);
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		Result result = (Result) methodInfo.getRetValue();
		Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
		ApacheDubboCtxUtils.finishSpan(invoker.getUrl(), DUBBO_TRACE_CONFIG, context, result, methodInfo.getThrowable());
	}

}
