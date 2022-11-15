package com.megaease.easeagent.plugin.motan.interceptor;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanTags;
import com.megaease.easeagent.plugin.motan.config.MotanPluginConfig;
import com.megaease.easeagent.plugin.motan.interceptor.trace.MotanBaseInterceptor;
import com.megaease.easeagent.plugin.motan.interceptor.trace.consumer.MotanConsumerRequest;
import com.megaease.easeagent.plugin.motan.interceptor.trace.provider.MotanProviderRequest;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.weibo.api.motan.common.URLParamType;
import com.weibo.api.motan.rpc.*;


public class MotanCtxUtils {

	public static final String CLIENT_REQUEST_CONTEXT = MotanCtxUtils.class.getName() + ".CLIENT_REQUEST_CONTEXT";
	public static final String SERVER_REQUEST_CONTEXT = MotanCtxUtils.class.getName() + ".SERVER_REQUEST_CONTEXT";
	public static final String METRICS_SERVICE_NAME = MotanCtxUtils.class.getName() + ".METRICS_SERVICE_NAME";

	public static String interfaceSignature(Request request) {
		return new StringBuilder(request.getInterfaceName())
				.append(".")
				.append(request.getMethodName())
				.append("(")
				.append(request.getParamtersDesc())
				.append(")")
				.toString();
	}

	public static String method(Request request) {
		return new StringBuilder(request.getMethodName())
				.append("(")
				.append(request.getParamtersDesc())
				.append(")")
				.toString();
	}

	public static String name(Request request) {
		String interfaceFullName = request.getInterfaceName();
		String interfaceName = interfaceFullName.substring(interfaceFullName.lastIndexOf(".") + 1);
		StringBuilder argsStringBuilder = new StringBuilder();
		Object[] arguments = request.getArguments();
		for (int i = 0; i < arguments.length; i++) {
			argsStringBuilder.append(arguments[i].getClass().getSimpleName());
			if (i != arguments.length - 1) {
				argsStringBuilder.append(",");
			}
		}
		return String.format("%s/%s(%s)", interfaceName, request.getMethodName(), argsStringBuilder);
	}

	public static void initProviderSpan(Context context, URL url, Request request) {
		MotanProviderRequest motanProviderRequest = new MotanProviderRequest(request);
		RequestContext requestContext = context.serverReceive(motanProviderRequest);
		Span span = requestContext.span().start();
		span.remoteServiceName(Type.MOTAN.getRemoteType());
		span.kind(motanProviderRequest.kind());
		span.name(motanProviderRequest.name());
		span.remoteIpAndPort(url.getHost(), url.getPort());
		context.put(SERVER_REQUEST_CONTEXT, requestContext);
	}

	public static void initConsumerSpan(Context context, URL url, Request request) {
		MotanConsumerRequest motanClientRequest = new MotanConsumerRequest(request);
		RequestContext requestContext = context.clientRequest(motanClientRequest);
		Span span = requestContext.span().start();
		span.remoteServiceName(Type.MOTAN.getRemoteType());
		span.kind(motanClientRequest.kind());
		span.name(motanClientRequest.name());
		span.tag(MotanTags.APPLICATION.name, url.getParameter(URLParamType.application.getName(),URLParamType.application.getValue()));
		span.tag(MotanTags.GROUP.name, url.getGroup());
		span.tag(MotanTags.MODULE.name, url.getParameter(URLParamType.module.getName(),URLParamType.module.getValue()));
		span.tag(MotanTags.SERVICE.name, request.getInterfaceName());
		span.tag(MotanTags.SERVICE_VERSION.name, url.getVersion());
		span.tag(MotanTags.METHOD.name, method(request));
		String args = null;
		if (MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG.argsCollectEnabled()) {
			args = JsonUtil.toJson(request.getArguments());
		}
		span.tag(MotanTags.ARGUMENTS.name, args);
		span.remoteIpAndPort(url.getHost(), url.getPort());
		context.put(CLIENT_REQUEST_CONTEXT, requestContext);
	}

	public static void finishConsumerSpan(Response response, Throwable throwable, Context context) {
		RequestContext requestContext = context.remove(CLIENT_REQUEST_CONTEXT);
		judgmentFinishSpan(response, throwable, requestContext);
	}

	public static void finishConsumerSpan(Future future, Context context) {
		RequestContext requestContext = context.remove(CLIENT_REQUEST_CONTEXT);
		if (future.getException() != null) {
			finishSpan(null, future.getException(), requestContext);
		} else {
			finishSpan(future.getValue(), null, requestContext);
		}
	}

	public static void finishProviderSpan(Response response, Throwable throwable, Context context) {
		RequestContext requestContext = context.remove(SERVER_REQUEST_CONTEXT);
		judgmentFinishSpan(response, throwable, requestContext);
	}

	private static void judgmentFinishSpan(Response response, Throwable throwable, RequestContext requestContext) {
		if (throwable != null) {
			finishSpan(null, throwable, requestContext);
		} else if (response.getException() != null) {
			finishSpan(null, response.getException(), requestContext);
		} else {
			finishSpan(response.getValue(), null, requestContext);
		}
	}

	private static void finishSpan(Object retValue, Throwable throwable, RequestContext requestContext) {
		if (requestContext == null) {
			return;
		}
		MotanPluginConfig motanPluginConfig = MotanBaseInterceptor.MOTAN_PLUGIN_CONFIG;
		try (Scope scope = requestContext.scope()) {
			Span span = requestContext.span();
			if (span.isNoop()) {
				return;
			}
			if (throwable != null) {
				span.error(throwable);
			}
			if (motanPluginConfig.resultCollectEnabled() && retValue != null) {
				span.tag(MotanTags.RESULT.name, JsonUtil.toJson(retValue));
			}
			span.finish();
		}
	}
}
