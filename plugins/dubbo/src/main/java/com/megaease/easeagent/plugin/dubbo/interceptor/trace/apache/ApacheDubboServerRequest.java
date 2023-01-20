package com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache;

import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

public class ApacheDubboServerRequest implements Request {

	private final Invoker<?> invoker;
	private final Invocation invocation;

	public ApacheDubboServerRequest(Invoker<?> invoker, Invocation invocation) {
		this.invoker = invoker;
		this.invocation = invocation;
	}

	@Override
	public Span.Kind kind() {
		return Span.Kind.SERVER;
	}

	@Override
	public String header(String name) {
		return invocation.getAttachments().get(name.toUpperCase());
	}

	@Override
	public String name() {
		return ApacheDubboCtxUtils.name(invocation);
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
