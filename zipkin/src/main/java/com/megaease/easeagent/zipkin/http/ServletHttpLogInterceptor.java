package com.megaease.easeagent.zipkin.http;

import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.Consumer;

public class ServletHttpLogInterceptor extends HttpLogInterceptor {

    public ServletHttpLogInterceptor(AutoRefreshConfigItem<String> serviceName, Consumer<String> reportConsumer) {
        super(serviceName, reportConsumer);
    }

    @Override
    public AccessLogServerInfo serverInfo(MethodInfo methodInfo, Map<Object, Object> context) {
        ServletAccessLogServerInfo serverInfo = ContextUtils.getFromContext(context, ServletAccessLogServerInfo.class);
        if (serverInfo == null) {
            serverInfo = new ServletAccessLogServerInfo();
            context.put(ServletAccessLogServerInfo.class, serverInfo);
        }
        HttpServletRequest request = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse response = (HttpServletResponse) methodInfo.getArgs()[1];
        serverInfo.load(request, response);
        return serverInfo;
    }
}
