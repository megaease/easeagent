package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.DubboPlugin;
import com.megaease.easeagent.plugin.dubbo.advice.AlibabaDubboAdvice;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboMetrics;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;

import java.util.concurrent.Future;


@AdviceTo(value = AlibabaDubboAdvice.class, plugin = DubboPlugin.class)
public class AlibabaDubboMetricsInterceptor implements Interceptor {
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

		String interfaceSignature = AlibabaDubboCtxUtils.interfaceSignature(invocation);
		Result retValue = (Result) methodInfo.getRetValue();
		boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
		if (isAsync) {
			Future<?> f = RpcContext.getContext().getFuture();
			if (f instanceof FutureAdapter) {
				ResponseFuture future = ((FutureAdapter<?>) f).getFuture();
				future.setCallback(new ResponseCallback() {
					@Override
					public void done(Object response) {
						boolean callResult = AlibabaDubboCtxUtils.checkCallResult(response, null);
						Long duration = ContextUtils.getDuration(context);
						DUBBO_METRICS.collect(interfaceSignature, duration, callResult);
					}

					@Override
					public void caught(Throwable exception) {
						boolean callResult = exception == null;
						Long duration = ContextUtils.getDuration(context);
						DUBBO_METRICS.collect(interfaceSignature, duration, callResult);
					}
				});
			}
		} else {
			Long duration = ContextUtils.getDuration(context);
			boolean callResult = AlibabaDubboCtxUtils.checkCallResult(retValue, methodInfo.getThrowable());
			DUBBO_METRICS.collect(interfaceSignature, duration, callResult);
		}
	}
}
