package com.megaease.easeagent.plugin.dubbo;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.dubbo.config.DubboTraceConfig;
import com.megaease.easeagent.plugin.dubbo.interceptor.DubboBaseInterceptor;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache.ApacheDubboClientRequest;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache.ApacheDubboServerRequest;
import com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache.ApacheDubboTraceCallback;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.*;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER_SIDE;
import static org.apache.dubbo.common.constants.CommonConstants.SIDE_KEY;

public class ApacheDubboCtxUtils {
    private static final String CLIENT_REQUEST_CONTEXT = ApacheDubboCtxUtils.class.getName() + ".CLIENT_REQUEST_CONTEXT";
    private static final String SERVICE_REQUEST_CONTEXT = ApacheDubboCtxUtils.class.getName() + ".SERVICE_REQUEST_CONTEXT";
    public static final String METRICS_SERVICE_NAME = ApacheDubboCtxUtils.class.getName() + ".METRICS_SERVICE_NAME";
    //dubbo get metadata interface
    public static final String METADATA_INTERFACE = "org.apache.dubbo.metadata.MetadataService";

    private static final Logger log = LoggerFactory.getLogger(ApacheDubboCtxUtils.class);

    /**
     * init span
     *
     * @param methodInfo
     * @param context
     */
    public static void initSpan(MethodInfo methodInfo, Context context) {
        Invoker<?> invoker = (Invoker<?>) methodInfo.getArgs()[0];
        Invocation invocation = (Invocation) methodInfo.getArgs()[1];
        URL requestUrl = invoker.getUrl();

        if (requestUrl.getServiceInterface().equals(METADATA_INTERFACE)) {
            return;
        }

        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = requestUrl.getParameter(SIDE_KEY).equals(CONSUMER_SIDE);
        String applicationName = requestUrl.getParameter(CommonConstants.APPLICATION_KEY);
        if (isConsumer) {
            String groupName = requestUrl.getParameter(CommonConstants.GROUP_KEY);
            String version = requestUrl.getParameter(CommonConstants.VERSION_KEY);

            ApacheDubboClientRequest apacheDubboClientRequest = new ApacheDubboClientRequest(invoker, invocation);
            RequestContext requestContext = context.clientRequest(apacheDubboClientRequest);
            Span span = requestContext.span().start();
            span.kind(apacheDubboClientRequest.kind());
            span.name(apacheDubboClientRequest.name());
            span.remoteServiceName(CommonConstants.DUBBO);
            span.remoteIpAndPort(requestUrl.getIp(), requestUrl.getPort());

            String argsList = DubboBaseInterceptor.DUBBO_TRACE_CONFIG.argsCollectEnabled() ? JsonUtil.toJson(invocation.getArguments()) : null;
            span.tag(DubboTags.CLIENT_APPLICATION.name, applicationName);
            span.tag(DubboTags.GROUP.name, groupName);
            span.tag(DubboTags.SERVICE.name, requestUrl.getPath());
            span.tag(DubboTags.METHOD.name, method(invocation));
            span.tag(DubboTags.SERVICE_VERSION.name, version);
            span.tag(DubboTags.ARGS.name, argsList);

            context.put(CLIENT_REQUEST_CONTEXT, requestContext);
        } else {
            ApacheDubboServerRequest apacheDubboServerRequest = new ApacheDubboServerRequest(invoker, invocation);
            RequestContext requestContext = context.serverReceive(apacheDubboServerRequest);

            Span span = requestContext.span().start();
            span.kind(apacheDubboServerRequest.kind());
            span.name(apacheDubboServerRequest.name());
            span.remoteServiceName(CommonConstants.DUBBO);
            span.remoteIpAndPort(rpcContext.getRemoteHost(), rpcContext.getRemotePort());
            span.tag(DubboTags.SERVER_APPLICATION.name, applicationName);

            context.put(SERVICE_REQUEST_CONTEXT, requestContext);
        }
    }

    public static void finishSpan(URL url, Context context, Result result, Throwable throwable) {
        boolean isConsumer = url.getParameter(SIDE_KEY).equals(CONSUMER_SIDE);
        RequestContext requestContext = requestContext(isConsumer, context);
        if (requestContext == null) {
            return;
        }

        Span span = requestContext.span();
        if (result instanceof AsyncRpcResult) {
            result.whenCompleteWithContext(new ApacheDubboTraceCallback(span));
        } else {
            doFinishSpan(span, result, throwable);
        }
    }

    public static void doFinishSpan(Span span, Result result, Throwable throwable) {
        if (span == null || span.isNoop()) {
            return;
        }

        DubboTraceConfig dubboTraceConfig = DubboBaseInterceptor.DUBBO_TRACE_CONFIG;
        if (throwable != null) {
            span.error(throwable);
        }
        if (result != null) {
            if (result.getException() != null) {
                span.error(result.getException());
            } else {
                if (dubboTraceConfig.resultCollectEnabled() && result.getValue() != null) {
                    span.tag(DubboTags.RESULT.name, JsonUtil.toJson(result.getValue()));
                }
            }
        }
        span.finish();
    }

    public static RequestContext requestContext(boolean isConsumer, Context context) {
        RequestContext requestContext;
        if (isConsumer) {
            requestContext = context.remove(CLIENT_REQUEST_CONTEXT);
        } else {
            requestContext = context.remove(SERVICE_REQUEST_CONTEXT);
        }
        return requestContext;
    }

    /**
     * check response result is success
     *
     * @param result    dubbo response result
     * @param throwable exception
     * @return if success the return true, otherwise return false.
     */
    public static boolean checkCallResult(Result result, Throwable throwable) {
        return result != null && result.getException() == null && throwable == null;
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

    /**
     * Format method name. e.g. test(String)
     *
     * @param invocation
     * @return method name
     */
    public static String method(Invocation invocation) {
        StringBuilder methodName = new StringBuilder();
        methodName.append(invocation.getMethodName())
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
        URL url = invocation.getInvoker().getUrl();
        String interfaceClassSimpleName = url.getPath().substring(url.getPath().lastIndexOf(".") + 1);
        StringBuilder argsStringBuilder = new StringBuilder();
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            argsStringBuilder.append(parameterTypes[0].getSimpleName());
            if (i != parameterTypes.length - 1) {
                argsStringBuilder.append(",");
            }
        }
        return String.format("%s/%s(%s)", interfaceClassSimpleName, invocation.getMethodName(), argsStringBuilder);
    }

}
