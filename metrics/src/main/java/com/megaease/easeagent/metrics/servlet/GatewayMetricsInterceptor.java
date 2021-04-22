package com.megaease.easeagent.metrics.servlet;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

public class GatewayMetricsInterceptor extends AbstractServerMetric implements AgentInterceptor {

    public GatewayMetricsInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        String key = getKey(exchange);
        HttpStatus statusCode = exchange.getResponse().getStatusCode();
        int code = 0;
        if (statusCode != null) {
            code = statusCode.value();
        }
        this.collectMetric(key, code, methodInfo.getThrowable(), context);
        return chain.doAfter(methodInfo, context);
    }

    public static String getKey(ServerWebExchange exchange) {
        HttpMethod httpMethod = exchange.getRequest().getMethod();
        if (httpMethod == null) {
            return "";
        }
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String key;
        if (route != null && route.getUri() != null) {
            key = httpMethod.name() + " " + route.getUri().toString();
        } else {
            key = httpMethod.name() + " " + exchange.getRequest().getURI();
        }
        return key;
    }

}
