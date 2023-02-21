package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common;

import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.filter.ConsumerInvoker;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;

public class SofaClientTraceRequest implements Request {
	private final ConsumerInvoker consumerInvoker;
	private final SofaRequest sofaRequest;

	public SofaClientTraceRequest(ConsumerInvoker consumerInvoker, SofaRequest sofaRequest) {
		this.consumerInvoker = consumerInvoker;
		this.sofaRequest = sofaRequest;
	}

	@Override
	public Span.Kind kind() {
		return Span.Kind.CLIENT;
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

	public String service() {
		return this.sofaRequest.getInterfaceName();
	}

	public String method() {
		return SofaRpcCtxUtils.method(this.sofaRequest);
	}

	public String uniqueId() {
		return consumerInvoker.getConfig().getUniqueId();
	}

	public String appName() {
		return consumerInvoker.getConfig().getAppName();
	}


	public String remoteHost() {
		return RpcInternalContext.getContext().getProviderInfo().getHost();
	}

	public int remotePort() {
		return RpcInternalContext.getContext().getProviderInfo().getPort();
	}
}
