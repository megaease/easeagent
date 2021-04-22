package com.megaease.easeagent.metrics.servlet;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HttpFilterMetricsInterceptor extends AbstractServerMetric implements AgentInterceptor {

    public HttpFilterMetricsInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
        String httpRoute = ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest);
        String key = httpServletRequest.getMethod() + " " + httpRoute;
        this.collectMetric(key, httpServletResponse.getStatus(), methodInfo.getThrowable(), context);
        return chain.doAfter(methodInfo, context);
    }
}
