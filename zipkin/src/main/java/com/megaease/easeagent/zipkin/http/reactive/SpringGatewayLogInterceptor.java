package com.megaease.easeagent.zipkin.http.reactive;

import brave.Span;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.AccessLogServerInfo;
import com.megaease.easeagent.zipkin.http.HttpLog;
import com.megaease.easeagent.zipkin.http.RequestInfo;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.function.Consumer;

public class SpringGatewayLogInterceptor implements AgentInterceptor {

    private final Consumer<String> reportConsumer;

    protected final AutoRefreshConfigItem<String> serviceName;

    private final HttpLog httpLog = new HttpLog();

    public SpringGatewayLogInterceptor(AutoRefreshConfigItem<String> serviceName, Consumer<String> reportConsumer) {
        this.serviceName = serviceName;
        this.reportConsumer = reportConsumer;
    }

    public AccessLogServerInfo serverInfo(ServerWebExchange exchange) {
        SpringGatewayAccessLogServerInfo serverInfo = new SpringGatewayAccessLogServerInfo();
        serverInfo.load(exchange);
        return serverInfo;
    }

    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        Long beginTime = ContextUtils.getBeginTime(context);
        Span span = (Span) context.get(ContextCons.SPAN);
        RequestInfo requestInfo = this.httpLog.prepare(this.serviceName.getValue(), beginTime, span, serverInfo);
        exchange.getAttributes().put(RequestInfo.class.getName(), requestInfo);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        Long beginTime = ContextUtils.getBeginTime(context);
        AccessLogServerInfo serverInfo = this.serverInfo(exchange);
        RequestInfo requestInfo = exchange.getAttribute(RequestInfo.class.getName());
        if (requestInfo == null) {
            return chain.doAfter(methodInfo, context);
        }
        String logString = this.httpLog.getLogString(requestInfo, methodInfo.isSuccess(), beginTime, serverInfo);
        reportConsumer.accept(logString);
        return chain.doAfter(methodInfo, context);
    }
}
