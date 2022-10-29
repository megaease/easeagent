package com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.config.DubboTraceConfig;
import org.apache.dubbo.rpc.Result;

import java.util.function.BiConsumer;

public class DubboAsyncCallback implements BiConsumer<Result, Throwable> {
	private final Context context;

	private final boolean isConsumer;

	private final Throwable throwable;

	private final DubboTraceConfig dubboTraceConfig;

	public DubboAsyncCallback(boolean isConsumer, DubboTraceConfig dubboTraceConfig, Context context, Throwable throwable) {
		this.context = context;
		this.dubboTraceConfig = dubboTraceConfig;
		this.isConsumer = isConsumer;
		this.throwable = throwable;
	}

	@Override
	public void accept(Result result, Throwable throwable) {
		ApacheDubboCtxUtils.doFinishSpan(isConsumer, dubboTraceConfig, context, result, this.throwable != null ? this.throwable : throwable);
	}
}
