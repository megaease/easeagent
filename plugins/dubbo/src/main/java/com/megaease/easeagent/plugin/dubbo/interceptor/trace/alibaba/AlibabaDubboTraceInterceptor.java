package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.AlibabaDubboAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.DubboBaseInterceptor;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;

@AdviceTo(value = AlibabaDubboAdvice.class, plugin = DubboPlugin.class)
public class AlibabaDubboTraceInterceptor extends DubboBaseInterceptor {


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
		AlibabaDubboCtxUtils.initSpan(methodInfo, context);
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		AlibabaDubboCtxUtils.finishSpan(context, methodInfo);
	}

}
