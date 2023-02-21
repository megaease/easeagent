package com.megaease.easeagent.plugin.sofarpc;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.ConsumerInvoker;
import com.alipay.sofa.rpc.filter.ProviderInvoker;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetricsBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common.SofaClientTraceRequest;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common.SofaServerTraceRequest;
import com.megaease.easeagent.plugin.utils.SystemClock;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

public class SofaRpcCtxUtils {
	private static final Logger LOG = EaseAgent.getLogger(SofaRpcCtxUtils.class);
	public static final String METRICS_KEY_NAME = SofaRpcCtxUtils.class.getName() + ".METRICS_KEY_NAME";
	private static final String METRICS_IS_ASYNC = SofaRpcCtxUtils.class.getName() + ".METRICS_IS_ASYNC";
	private static final String TRACE_IS_ASYNC = SofaRpcCtxUtils.class.getName() + ".TRACE_IS_ASYNC";
	private static final String BEGIN_TIME = SofaRpcCtxUtils.class.getName() + ".BEGIN_TIME";

	public static final String CLIENT_REQUEST_CONTEXT_KEY = SofaRpcCtxUtils.class.getName() + ".CLIENT_REQUEST_CONTEXT";
	public static final String SERVER_REQUEST_CONTEXT_KEY = SofaRpcCtxUtils.class.getName() + ".SERVER_REQUEST_CONTEXT";

	/**
	 * SOFARPC span name, e.g. TestService/test(String)
	 *
	 * @param sofaRequest sofa rpc request
	 * @return
	 */
	public static String name(SofaRequest sofaRequest) {
		StringBuilder operationName = new StringBuilder();
		operationName.append(sofaRequest.getMethod().getDeclaringClass().getSimpleName());
		operationName.append("/").append(sofaRequest.getMethod().getName()).append("(");
		for (Class<?> parameterType : sofaRequest.getMethod().getParameterTypes()) {
			operationName.append(parameterType.getSimpleName()).append(",");
		}

		if (sofaRequest.getMethod().getParameterTypes().length > 0) {
			operationName.deleteCharAt(operationName.length() - 1);
		}

		operationName.append(")");

		return operationName.toString();
	}

	/**
	 * Format method name. e.g. test(String)
	 *
	 * @param sofaRequest
	 * @return method name
	 */
	public static String method(SofaRequest sofaRequest) {
		StringBuilder methodName = new StringBuilder();
		methodName.append(sofaRequest.getMethod().getName())
				.append("(");
		for (Class<?> parameterType : sofaRequest.getMethod().getParameterTypes()) {
			methodName.append(parameterType.getSimpleName()).append(",");
		}

		if (sofaRequest.getMethod().getParameterTypes().length > 0) {
			methodName.deleteCharAt(methodName.length() - 1);
		}
		methodName.append(")");
		return methodName.toString();
	}

	/**
	 * sofarpc interface signature, e.g. com.test.TestService/test(java.lang.String)
	 *
	 * @param sofaRequest
	 * @return
	 */
	public static String methodSignature(SofaRequest sofaRequest) {
		StringBuilder operationName = new StringBuilder();
		operationName.append(sofaRequest.getMethod().getDeclaringClass().getName());
		operationName.append("/").append(sofaRequest.getMethod().getName()).append("(");
		for (Class<?> parameterType : sofaRequest.getMethod().getParameterTypes()) {
			operationName.append(parameterType.getSimpleName()).append(",");
		}

		if (sofaRequest.getMethod().getParameterTypes().length > 0) {
			operationName.deleteCharAt(operationName.length() - 1);
		}

		operationName.append(")");

		return operationName.toString();
	}

	//-------------------Sofa Rpc trace operate method------------

	/**
	 * Start client span
	 * @param context
	 * @param sofaRequest
	 * @param consumerInvoker
	 */
	public static void startClientSpan(Context context, SofaRequest sofaRequest, ConsumerInvoker consumerInvoker) {
		SofaClientTraceRequest sofaClientTraceRequest = new SofaClientTraceRequest(consumerInvoker, sofaRequest);
		RequestContext requestContext = context.clientRequest(sofaClientTraceRequest);

		Span span = requestContext.span().start();
		span.kind(sofaClientTraceRequest.kind());
		span.name(sofaClientTraceRequest.name());
		span.remoteServiceName(ConfigConst.Namespace.SOFARPC);
		span.remoteIpAndPort(sofaClientTraceRequest.remoteHost(), sofaClientTraceRequest.remotePort());
		if (SofaRpcTraceBaseInterceptor.SOFA_RPC_TRACE_CONFIG.argsCollectEnabled()) {
			span.tag(SofaRpcTags.ARGS.name, JsonUtil.toJson(sofaRequest.getMethodArgs()));
		}
		span.tag(SofaRpcTags.CLIENT_APPLICATION.name, sofaClientTraceRequest.appName());
		span.tag(SofaRpcTags.SERVICE_UNIQUE_ID.name, sofaClientTraceRequest.uniqueId());
		span.tag(SofaRpcTags.SERVICE.name, sofaClientTraceRequest.service());
		span.tag(SofaRpcTags.METHOD.name, sofaClientTraceRequest.method());

		context.put(TRACE_IS_ASYNC, SofaRpcCtxUtils.isAsync(sofaRequest.getInvokeType()));
		context.put(CLIENT_REQUEST_CONTEXT_KEY, requestContext);
	}

	/**
	 * Start server span
	 * @param context interceptor context
	 * @param providerInvoker sofa rpc provider invoker
	 * @param sofaRequest sofa request
	 */
	public static void startServerSpan(Context context, ProviderInvoker<?> providerInvoker, SofaRequest sofaRequest) {
		SofaServerTraceRequest sofaServerTraceRequest = new SofaServerTraceRequest(providerInvoker, sofaRequest);
		RequestContext requestContext = context.serverReceive(sofaServerTraceRequest);

		Span span = requestContext.span().start();
		span.kind(sofaServerTraceRequest.kind());
		span.name(sofaServerTraceRequest.name());
		span.remoteServiceName(ConfigConst.Namespace.SOFARPC);
		span.tag(SofaRpcTags.SERVER_APPLICATION.name, sofaServerTraceRequest.appName());
		span.remoteIpAndPort(sofaServerTraceRequest.remoteHost(), sofaServerTraceRequest.remotePort());
		context.put(SERVER_REQUEST_CONTEXT_KEY, requestContext);
	}

	/**
	 * Finish server span
	 * @param context interceptor context
	 * @param sofaResponse sofa response
	 * @param throwable rpc server exception
	 */
	public static void finishServerSpan(Context context, SofaResponse sofaResponse, Throwable throwable) {
		finishSpan(SERVER_REQUEST_CONTEXT_KEY, context, sofaResponse, throwable);
	}

	/**
	 * Sync finish client span
	 * @param context interceptor context
	 * @param sofaResponse sofa response
	 * @param throwable rpc client call exception
	 */
	public static void finishClientSpan(Context context, SofaResponse sofaResponse, Throwable throwable) {
		if (throwable != null) {
			SofaRpcCtxUtils.finishSpan(CLIENT_REQUEST_CONTEXT_KEY, context, null, throwable);
			return;
		}

		boolean isAsync = context.remove(TRACE_IS_ASYNC);
		if (isAsync) {
			return;
		}

		SofaRpcCtxUtils.finishSpan(CLIENT_REQUEST_CONTEXT_KEY, context, sofaResponse, null);
	}

	/**
	 * Async finish client span
	 * @param asyncContext interceptor async context
	 * @param result rpc call return value
	 */
	public static void asyncFinishClientSpan(AsyncContext asyncContext, Object result) {
		try (Cleaner ignored = asyncContext.importToCurrent()) {
			Context context = EaseAgent.getContext();
			RequestContext requestContext = context.remove(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);

			try (Scope ignoredScope = requestContext.scope()) {
				Span span = requestContext.span();
				if (result instanceof Throwable) {
					span.error((Throwable) result);
				} else if (SofaRpcTraceBaseInterceptor.SOFA_RPC_TRACE_CONFIG.resultCollectEnabled()) {
					span.tag(SofaRpcTags.RESULT.name, JsonUtil.toJson(result));
				}
				span.finish();
			}
		}
	}

	/**
	 * Finish span
	 * @param requestContextKey get the key of request context
	 * @param context interceptor context
	 * @param sofaResponse sofa rpc response
	 * @param throwable rpc call exception
	 */
	private static void finishSpan(String requestContextKey, Context context, SofaResponse sofaResponse, Throwable throwable) {
		RequestContext requestContext = context.remove(requestContextKey);
		try (Scope ignored = requestContext.scope()) {
			Span span = requestContext.span();
			try {
				if (!CLIENT_REQUEST_CONTEXT_KEY.equals(requestContextKey)) {
					return;
				}
				if (throwable != null) {
					span.error(throwable);
				} else if (sofaResponse != null) {
					if (sofaResponse.isError() || sofaResponse.getAppResponse() instanceof Throwable) {
						span.error((Throwable) sofaResponse.getAppResponse());
					} else if (SofaRpcTraceBaseInterceptor.SOFA_RPC_TRACE_CONFIG.resultCollectEnabled()) {
						span.tag(SofaRpcTags.RESULT.name, JsonUtil.toJson(sofaResponse.getAppResponse()));
					}
				}
			} finally {
				span.finish();
			}
		}
	}

	//-------------------Sofa Rpc metrics operate method------------

	/**
	 * Start collect metrics
	 * @param context interceptor context
	 * @param sofaRequest sofa rpc request
	 */
	public static void startCollectMetrics(Context context, SofaRequest sofaRequest) {
		String methodSignature = methodSignature(sofaRequest);
		context.put(BEGIN_TIME, SystemClock.now());
		context.put(METRICS_IS_ASYNC, isAsync(sofaRequest.getInvokeType()));
		context.put(METRICS_KEY_NAME, methodSignature);
	}

	/**
	 * Finish collect metrics
	 * @param context interceptor context
	 * @param sofaResponse sofa rpc response
	 * @param throwable call exception
	 */
	public static void finishCollectMetrics(Context context, SofaResponse sofaResponse, Throwable throwable) {
		if (throwable != null) {
			collectMetrics(context, sofaResponse, throwable);
			return;
		}

		boolean isAsync = context.remove(METRICS_IS_ASYNC);
		if (isAsync) {
			return;
		}

		collectMetrics(context, sofaResponse, null);
	}

	private static void collectMetrics(Context context, SofaResponse sofaResponse, Throwable throwable) {
		long duration = ContextUtils.getDuration(context, BEGIN_TIME);
		String methodSignature = context.remove(METRICS_KEY_NAME);
		if (methodSignature == null) {
			LOG.error("method signature is null");
			return;
		}
		boolean callResult = sofaResponse != null
				&& !sofaResponse.isError()
				&& !(sofaResponse.getAppResponse() instanceof Throwable)
				&& throwable == null;
		SofaRpcMetricsBaseInterceptor.SOFARPC_METRICS.collect(methodSignature, duration, callResult);
	}

	/**
	 * Async finish collect metrics
	 * @param asyncContext interceptor async context
	 * @param result sofa rpc call return value
	 */
	public static void asyncFinishCollectMetrics(AsyncContext asyncContext, Object result) {
		try (Cleaner ignored = asyncContext.importToCurrent()) {
			String methodSignature = EaseAgent.getContext().remove(METRICS_KEY_NAME);
			if (methodSignature == null) {
				LOG.error("method signature is null");
				return;
			}

			Long duration = ContextUtils.getDuration(EaseAgent.getContext(), BEGIN_TIME);
			boolean callResult = result != null && !(result instanceof Throwable);
			SofaRpcMetricsBaseInterceptor.SOFARPC_METRICS.collect(methodSignature, duration, callResult);
		}
	}

	/**
	 * Check if the invoke type is async call
	 * @param invokeType sofa rpc invoke type
	 * @return returns true if the invoke type is future or callback, otherwise it returns false.
	 */
	private static boolean isAsync(String invokeType) {
		return RpcConstants.INVOKER_TYPE_CALLBACK.equals(invokeType) || RpcConstants.INVOKER_TYPE_FUTURE.equals(invokeType);
	}
}
