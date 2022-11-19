package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;

public class AlibabaDubboServerRequest implements Request {

	private final Invoker<?> invoker;
	private final Invocation invocation;

	public AlibabaDubboServerRequest(Invoker<?> invoker, Invocation invocation) {
		this.invoker = invoker;
		this.invocation = invocation;
	}

	@Override
	public Span.Kind kind() {
		return Span.Kind.SERVER;
	}

	@Override
	public String header(String name) {
		return invocation.getAttachments().get(name);
	}

	@Override
	public String name() {
		return AlibabaDubboCtxUtils.name(invocation);
	}

	@Override
	public boolean cacheScope() {
		return false;
	}

	@Override
	public void setHeader(String name, String value) {
		invocation.getAttachments().put(name, value);
	}
}
