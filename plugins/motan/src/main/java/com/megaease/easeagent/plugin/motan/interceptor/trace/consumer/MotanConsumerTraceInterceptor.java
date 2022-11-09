package com.megaease.easeagent.plugin.motan.interceptor.trace.consumer;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanPlugin;
import com.megaease.easeagent.plugin.motan.advice.MotanConsumerAdvice;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanBaseInterceptor;
import com.megaease.easeagent.plugin.motan.interceptor.MotanClassUtils;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.*;

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
		MotanCtxUtils.initConsumerSpan(context, url, request);
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		Response response = (Response) methodInfo.getRetValue();
		if (MotanClassUtils.DefaultResponseFutureTypeChecker.getTypeChecker().hasClassAndIsType(response)) {
			DefaultResponseFuture defaultResponseFuture = (DefaultResponseFuture) response;
			defaultResponseFuture.addListener(new TraceFutureListener(context.exportAsync()));
		} else {
			MotanCtxUtils.finishConsumerSpan(response, methodInfo.getThrowable(), context);
		}
	}

}
