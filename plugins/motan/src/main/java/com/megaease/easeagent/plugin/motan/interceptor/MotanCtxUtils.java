package com.megaease.easeagent.plugin.motan.interceptor;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.motan.MotanTags;
import com.megaease.easeagent.plugin.motan.config.MotanPluginConfig;
import com.megaease.easeagent.plugin.motan.interceptor.trace.consumer.MotanConsumerRequest;
import com.megaease.easeagent.plugin.motan.interceptor.trace.provider.MotanProviderRequest;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.common.URLParamType;
import com.weibo.api.motan.rpc.*;


public class MotanCtxUtils {

    public static final String CLIENT_REQUEST_CONTEXT = MotanCtxUtils.class.getName() + ".CLIENT_REQUEST_CONTEXT";
    public static final String SERVER_REQUEST_CONTEXT = MotanCtxUtils.class.getName() + ".SERVER_REQUEST_CONTEXT";


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
        String interfaceName = interfaceFullName.substring(interfaceFullName.lastIndexOf(".")+1);
        StringBuilder argsStringBuilder = new StringBuilder();
        Object[] arguments = request.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            argsStringBuilder.append(arguments[i].getClass().getSimpleName());
            if (i != arguments.length - 1) {
                argsStringBuilder.append(",");
            }
        }
        return String.format("%s/%s(%s)",interfaceName,request.getMethodName(),argsStringBuilder);
    }

    public static String endpoint(URL url, Request request) {
        StringBuilder endpointBuilder = new StringBuilder();
        endpointBuilder.append(url.getProtocol())
            .append("://")
            .append(url.getHost())
            .append(":")
            .append(url.getPort())
            .append("/")
            .append(interfaceSignature(request));
        return endpointBuilder.toString();
    }

    public static void initSpan(Context context, URL url, Request request, MotanPluginConfig motanPluginConfig) {
        boolean isClient = url.getParameter(URLParamType.nodeType.name()).equals(MotanConstants.NODE_TYPE_REFERER);
        if (isClient) {
            MotanConsumerRequest motanClientRequest = new MotanConsumerRequest(request);
            RequestContext requestContext = context.clientRequest(motanClientRequest);
            Span span = requestContext.span().start();
            span.remoteServiceName(Type.MOTAN.getRemoteType());
            span.kind(motanClientRequest.kind());
            span.name(motanClientRequest.name());
            span.tag(MotanTags.APPLICATION.name, url.getApplication());
            span.tag(MotanTags.GROUP.name, url.getGroup());
            span.tag(MotanTags.MODULE.name, url.getModule());
            span.tag(MotanTags.SERVICE.name, request.getInterfaceName());
            span.tag(MotanTags.SERVICE_VERSION.name, url.getVersion());
            span.tag(MotanTags.METHOD.name, method(request));
            String args = motanPluginConfig.argsCollectEnabled() ? JsonUtil.toJson(request.getArguments()) : null;
            span.tag(MotanTags.ARGUMENTS.name, args);
            span.remoteIpAndPort(url.getHost(), url.getPort());
            context.put(CLIENT_REQUEST_CONTEXT, requestContext);
        } else {
            MotanProviderRequest motanProviderRequest = new MotanProviderRequest(request);
            RequestContext requestContext = context.serverReceive(motanProviderRequest);
            Span span = requestContext.span().start();
            span.remoteServiceName(Type.MOTAN.getRemoteType());
            span.kind(motanProviderRequest.kind());
            span.name(motanProviderRequest.name());
            span.remoteIpAndPort(url.getHost(), url.getPort());
            context.put(SERVER_REQUEST_CONTEXT, requestContext);
        }
    }

    public static void finishSpan(MethodInfo methodInfo, URL url, Context context, MotanPluginConfig motanPluginConfig) {
        RequestContext requestContext;
        boolean isClient = url.getParameter(URLParamType.nodeType.name()).equals(MotanConstants.NODE_TYPE_REFERER);
        if (isClient) {
            requestContext = context.remove(CLIENT_REQUEST_CONTEXT);
        } else {
            requestContext = context.remove(SERVER_REQUEST_CONTEXT);
        }

        if (requestContext == null) {
            return;
        }

        try {
            Span span = requestContext.span();
            Response response = (Response) methodInfo.getRetValue();
            if (response instanceof DefaultResponseFuture) {
                DefaultResponseFuture defaultResponseFuture = (DefaultResponseFuture) response;
                defaultResponseFuture.addListener(listener(span,motanPluginConfig));
            } else {
                if (response != null) {
                    if (response.getException() != null) {
                        span.error(response.getException());
                    } else {
                        String result = motanPluginConfig.argsCollectEnabled() ? JsonUtil.toJson(response.getValue()) : null;
                        span.tag(MotanTags.RESULT.name, result);
                    }
                }
                if (methodInfo.getThrowable() != null) {
                    span.error(methodInfo.getThrowable());
                }
                span.finish();
            }
        } finally {
            requestContext.scope().close();
        }
    }

    private static FutureListener listener(Span span, MotanPluginConfig motanPluginConfig) {
        return new FutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                if (future.getException() != null) {
                    span.error(future.getException());
                } else {
                    String result = motanPluginConfig.argsCollectEnabled() ? JsonUtil.toJson(future.getValue()) : null;
                    span.tag(MotanTags.RESULT.name, result);
                }
                span.finish();
            }
        };
    }
}
