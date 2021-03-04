package com.megaease.easeagent.zipkin.http;

import brave.Tracing;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class HttpFilterLogInterceptor implements AgentInterceptor {

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        Long beginTime = ContextUtils.getBeginTime(context);
        TraceContext traceContext = Tracing.current().currentTraceContext().get();

        RequestInfo requestInfo = RequestInfo.builder()
                .service("")
                .system("")
                .hostName(HostAddress.localhost())
                .hostIpv4(HostAddress.localaddr().getHostAddress())
                .category("application")
                .url(httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI())
                .method(httpServletRequest.getMethod())
                .headers(ServletUtils.getHeaders(httpServletRequest))
                .beginTime(beginTime)
                .queries(ServletUtils.getQueries(httpServletRequest))
                .clientIP(ServletUtils.getRemoteHost(httpServletRequest))
                .requestTime(System.currentTimeMillis())
                .beginCpuTime(System.nanoTime())
                .traceId(traceContext.traceIdString())
                .spanId(traceContext.spanIdString())
                .parentSpanId(traceContext.parentIdString())
                .build();

        // TODO: 2021/3/3 send info


    }

}
