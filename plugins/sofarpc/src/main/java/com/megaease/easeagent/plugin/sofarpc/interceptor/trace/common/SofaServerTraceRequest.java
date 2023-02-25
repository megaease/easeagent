package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common;

import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.filter.ProviderInvoker;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;

import java.net.InetSocketAddress;

public class SofaServerTraceRequest implements Request {

	private final ProviderInvoker<?> providerInvoker;
	private final SofaRequest sofaRequest;

	public SofaServerTraceRequest(ProviderInvoker<?> providerInvoker, SofaRequest sofaRequest) {
		this.providerInvoker = providerInvoker;
		this.sofaRequest = sofaRequest;
	}

	@Override
	public Span.Kind kind() {
		return Span.Kind.SERVER;
	}

	@Override
	public String header(String name) {
		return (String) this.sofaRequest.getRequestProp(name);
	}

	@Override
	public String name() {
		return SofaRpcCtxUtils.name(this.sofaRequest);
	}

	@Override
	public boolean cacheScope() {
		return false;
	}

	@Override
	public void setHeader(String name, String value) {
		this.sofaRequest.addRequestProp(name,value);
	}

	public String appName() {
		return this.providerInvoker.getConfig().getAppName();
	}

	public String remoteHost() {
		InetSocketAddress remoteAddress = RpcInternalContext.getContext().getRemoteAddress();
		return remoteAddress != null ? remoteAddress.getHostString() : null;
	}

	public int remotePort() {
		InetSocketAddress remoteAddress = RpcInternalContext.getContext().getRemoteAddress();
		return remoteAddress != null ? remoteAddress.getPort() : 0;
	}

}
