package com.megaease.easeagent.zipkin.http.reactive;

import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.zipkin.http.AccessLogServerInfo;
import com.megaease.easeagent.zipkin.http.HttpLogInterceptor;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.function.Consumer;

public class SpringGatewayLogInterceptor extends HttpLogInterceptor {

    public SpringGatewayLogInterceptor(AutoRefreshConfigItem<String> serviceName, Consumer<String> reportConsumer) {
        super(serviceName, reportConsumer);
    }

    @Override
    public AccessLogServerInfo serverInfo(MethodInfo methodInfo, Map<Object, Object> context) {
        ServerWebExchange exchange = (ServerWebExchange) methodInfo.getArgs()[0];
        SpringGatewayAccessLogServerInfo serverInfo = new SpringGatewayAccessLogServerInfo();
        serverInfo.load(exchange);
        return serverInfo;
    }


}
