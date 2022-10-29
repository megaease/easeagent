package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.apache;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.ApacheDubboAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboMetrics;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

import java.util.function.BiConsumer;

@AdviceTo(value = ApacheDubboAdvice.class, plugin = DubboPlugin.class)
public class ApacheDubboMetricsInterceptor implements Interceptor {
	private static volatile DubboMetrics DUBBO_METRICS;

	@Override
	public int order() {
		return Order.METRIC.getOrder();
	}

	@Override
	public String getType() {
		return Order.METRIC.getName();
	}

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		Tags tags = new Tags("application", ConfigConst.Namespace.DUBBO, "service");
		DUBBO_METRICS = ServiceMetricRegistry.getOrCreate(config, tags, DubboMetrics.DUBBO_METRICS_SUPPLIER);
	}

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

		String operationName = ApacheDubboCtxUtils.interfaceSignature(invocation);
		Result retValue = (Result) methodInfo.getRetValue();
		if (retValue instanceof AsyncRpcResult) {
			retValue.whenCompleteWithContext(new BiConsumer<Result, Throwable>() {
				@Override
				public void accept(Result result, Throwable throwable) {
					Long duration = ContextUtils.getDuration(context);
					boolean callResult = ApacheDubboCtxUtils.checkCallResult(result, throwable);
					DUBBO_METRICS.collect(operationName, duration, callResult);
				}
			});
		} else {
			Long duration = ContextUtils.getDuration(context);
			boolean callResult = ApacheDubboCtxUtils.checkCallResult(retValue, methodInfo.getThrowable());
			DUBBO_METRICS.collect(ApacheDubboCtxUtils.interfaceSignature(invocation), duration, callResult);
		}
	}
}
