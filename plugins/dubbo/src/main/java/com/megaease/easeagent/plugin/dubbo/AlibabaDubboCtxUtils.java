package com.megaease.easeagent.plugin.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.dubbo.config.DubboTraceConfig;
import com.megaease.easeagent.plugin.dubbo.interceptor.DubboBaseInterceptor;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba.AlibabaDubboAsyncTraceInterceptor;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba.AlibabaDubboClientRequest;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba.AlibabaDubboServerRequest;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.util.concurrent.Future;

import static com.alibaba.dubbo.common.Constants.*;
import static com.megaease.easeagent.plugin.dubbo.DubboTraceTags.RESULT;

public class AlibabaDubboCtxUtils {
	public static final String CLIENT_REQUEST_CONTEXT = AlibabaDubboCtxUtils.class.getName() + ".CLIENT_REQUEST_CONTEXT";
	private static final String SERVICE_REQUEST_CONTEXT = AlibabaDubboCtxUtils.class.getName() + ".SERVICE_REQUEST_CONTEXT";
	public static final String METRICS_SERVICE_NAME = AlibabaDubboCtxUtils.class.getName() + ".METRICS_SERVICE_NAME";
	public static final String BEGIN_TIME = AlibabaDubboCtxUtils.class.getName() + ".BEGIN_TIME";


	public static void initSpan(MethodInfo methodInfo, Context context) {
		Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
		Invocation invocation = (Invocation) methodInfo.getArgs()[1];

		URL requestUrl = invoker.getUrl();
		RpcContext rpcContext = RpcContext.getContext();
		String applicationName = requestUrl.getParameter(Constants.APPLICATION_KEY);
		boolean isConsumer = requestUrl.getParameter(SIDE_KEY, PROVIDER_SIDE).equals(CONSUMER_SIDE);
		if (isConsumer) {
			String groupName = requestUrl.getParameter(Constants.GROUP_KEY);
			String version = requestUrl.getParameter(Constants.VERSION_KEY);

			AlibabaDubboClientRequest alibabaDubboClientRequest = new AlibabaDubboClientRequest(invoker, invocation);
			RequestContext requestContext = context.clientRequest(alibabaDubboClientRequest);
			Span span = requestContext.span().start();
			span.kind(alibabaDubboClientRequest.kind());
			span.name(alibabaDubboClientRequest.name());
			span.remoteServiceName(ConfigConst.Namespace.DUBBO);
			span.remoteIpAndPort(rpcContext.getRemoteHost(), rpcContext.getRemotePort());

			String argsList = DubboBaseInterceptor.DUBBO_TRACE_CONFIG.argsCollectEnabled() ? JsonUtil.toJson(invocation.getArguments()) : null;
			span.tag(DubboTraceTags.CLIENT_APPLICATION.name, applicationName);
			span.tag(DubboTraceTags.GROUP.name, groupName);
			span.tag(DubboTraceTags.SERVICE.name, requestUrl.getPath());
			span.tag(DubboTraceTags.METHOD.name, method(invocation));
			span.tag(DubboTraceTags.SERVICE_VERSION.name, version);
			span.tag(DubboTraceTags.ARGS.name, argsList);

			context.put(CLIENT_REQUEST_CONTEXT, requestContext);
		} else {
			AlibabaDubboServerRequest alibabaDubboServerRequest = new AlibabaDubboServerRequest(invoker, invocation);
			RequestContext requestContext = context.serverReceive(alibabaDubboServerRequest);

			Span span = requestContext.span().start();
			span.kind(alibabaDubboServerRequest.kind());
			span.name(alibabaDubboServerRequest.name());
			span.remoteServiceName(ConfigConst.Namespace.DUBBO);
			span.remoteIpAndPort(rpcContext.getRemoteHost(), rpcContext.getRemotePort());
			span.tag(DubboTraceTags.SERVER_APPLICATION.name, applicationName);

			context.put(SERVICE_REQUEST_CONTEXT, requestContext);
		}
	}

	public static void finishSpan(Context context, MethodInfo methodInfo) {
		Result result = (Result) methodInfo.getRetValue();
		Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
		Invocation invocation = (Invocation) methodInfo.getArgs()[1];
		Throwable throwable = methodInfo.getThrowable();

        /**
         * requestContext.scope() will be closed in
         * {@link AlibabaDubboAsyncTraceInterceptor#before(MethodInfo, Context)}
         */
        boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        Future<?> f = RpcContext.getContext().getFuture();
        if (isAsync && f instanceof FutureAdapter) {
            return;
        }

		boolean isConsumer = invoker.getUrl().getParameter(SIDE_KEY, PROVIDER_SIDE).equals(CONSUMER_SIDE);
        RequestContext requestContext = requestContext(isConsumer,context);
        if (requestContext == null) {
            return;
        }
        try (Scope scope = requestContext.scope()){
            Span span = requestContext.span();
            doFinishSpan(span, result, throwable);
        }
	}

    public static void finishSpan(Context context, Result result, Throwable throwable) {
        RequestContext requestContext = context.remove(CLIENT_REQUEST_CONTEXT);
        if (requestContext == null) {
            return;
        }
        try (Scope scope = requestContext.scope()){
            Span span = requestContext.span();
            doFinishSpan(span, result, throwable);
        }
    }


	private static void doFinishSpan(Span span, Result result, Throwable throwable) {
        if (span == null || span.isNoop()) {
            return;
        }
        DubboTraceConfig config = DubboBaseInterceptor.DUBBO_TRACE_CONFIG;
        if (throwable != null) {
            span.error(throwable);
        }
        if (result != null) {
            if (result.getException() != null) {
                span.error(result.getException());
            } else {
                if (config.resultCollectEnabled() && result.getValue() != null) {
                    span.tag(RESULT.name, config.resultCollectEnabled() ? JsonUtil.toJson(result.getValue()) : null);
                }
            }
        }
        span.finish();
	}

    private static RequestContext requestContext(boolean isConsumer, Context context) {
        RequestContext requestContext;
        if (isConsumer) {
            requestContext = context.remove(CLIENT_REQUEST_CONTEXT);
        } else {
            requestContext = context.remove(SERVICE_REQUEST_CONTEXT);
        }
        return requestContext;
    }


    /**
	 * Format method name. e.g. test(String)
	 *
	 * @param invocation
	 * @return method name
	 */
	public static String method(Invocation invocation) {
		StringBuilder methodName = new StringBuilder();
		methodName
				.append(invocation.getMethodName())
				.append("(");
		for (Class<?> classes : invocation.getParameterTypes()) {
			methodName.append(classes.getSimpleName()).append(",");
		}

		if (invocation.getParameterTypes().length > 0) {
			methodName.delete(methodName.length() - 1, methodName.length());
		}
		methodName.append(")");
		return methodName.toString();
	}

	/**
	 * dubbo span name, e.g. TestService/test(String)
	 *
	 * @param invocation
	 * @return
	 */
	public static String name(Invocation invocation) {
		final URL url = invocation.getInvoker().getUrl();
		final String interfaceClassSimpleName = url.getPath().substring(url.getPath().lastIndexOf(".") + 1);
		StringBuilder argsStringBuilder = new StringBuilder();
		final Class<?>[] parameterTypes = invocation.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			argsStringBuilder.append(parameterTypes[i].getSimpleName());
			if (i != parameterTypes.length - 1) {
				argsStringBuilder.append(",");
			}
		}
		return String.format("%s/%s(%s)", interfaceClassSimpleName, invocation.getMethodName(), argsStringBuilder);
	}

	/**
	 * check response result is success
	 *
	 * @param response  dubbo response result
	 * @param throwable dubbo exception
	 * @return if success the return true, otherwise return false.
	 */
	public static boolean checkCallResult(Object response, Throwable throwable) {
		if (response == null || throwable != null) {
			return false;
		}
		if (response instanceof Result) {
			Result rpcRet = (Result) response;
			return rpcRet.getValue() != null;
		}
		return false;
	}


	/**
	 * Format interface signature. e.g. com.magaease.easeagent.service.DubboService.test(String)
	 *
	 * @return interface signature strings.
	 */
	public static String interfaceSignature(Invocation invocation) {
		StringBuilder operationName = new StringBuilder();
		URL requestURL = invocation.getInvoker().getUrl();
		operationName.append(requestURL.getPath());
		operationName.append(".").append(invocation.getMethodName()).append("(");
		for (Class<?> classes : invocation.getParameterTypes()) {
			operationName.append(classes.getSimpleName()).append(",");
		}

		if (invocation.getParameterTypes().length > 0) {
			operationName.delete(operationName.length() - 1, operationName.length());
		}
		operationName.append(")");

		return operationName.toString();
	}
}
