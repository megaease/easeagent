package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.ServletUtils;
import com.megaease.easeagent.metrics.servlet.HttpFilterMetricsInterceptor;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpFilterMetricsInterceptorTest {

    @Test
    public void success() {
        MetricRegistry metricRegistry = new MetricRegistry();

        HttpFilterMetricsInterceptor interceptor = new HttpFilterMetricsInterceptor(metricRegistry);

        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .counterType(MetricSubType.DEFAULT, Maps.newHashMap())
                .build();

        Filter filter = mock(Filter.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURI()).thenReturn("/path/users/123/info");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/path/users/123/info?page=200"));
        when(httpServletResponse.getStatus()).thenReturn(200);

        Map<String, Object> attrMap = new HashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgumentAt(0, String.class);
            Object value = invocation.getArgumentAt(1, Object.class);
            attrMap.put(key, value);
            return null;
        }).when(httpServletRequest).setAttribute(any(String.class), any());
        when(httpServletRequest.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));

        httpServletRequest.setAttribute(ServletUtils.BEST_MATCHING_PATTERN_ATTRIBUTE, "/path/users/{userId}/info");

        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
        Map<Object, Object> context = ContextUtils.createContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(filter)
                .method("doFilterInternal")
                .args(args)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        //mock do something end
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        String key = httpServletRequest.getMethod() + " " + ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest);
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT)).getCount());

    }

    @Test
    public void fail() {
        MetricRegistry metricRegistry = new MetricRegistry();

        HttpFilterMetricsInterceptor interceptor = new HttpFilterMetricsInterceptor(metricRegistry);

        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.ERROR, Maps.newHashMap())
                .counterType(MetricSubType.ERROR, Maps.newHashMap())
                .build();

        Filter filter = mock(Filter.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURI()).thenReturn("/path/users/123/info");
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/path/users/123/info?page=200"));
        when(httpServletResponse.getStatus()).thenReturn(400);

        Map<String, Object> attrMap = new HashMap<>();
        doAnswer(invocation -> {
            String key = invocation.getArgumentAt(0, String.class);
            Object value = invocation.getArgumentAt(1, Object.class);
            attrMap.put(key, value);
            return null;
        }).when(httpServletRequest).setAttribute(any(String.class), any());
        when(httpServletRequest.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));

        httpServletRequest.setAttribute(ServletUtils.BEST_MATCHING_PATTERN_ATTRIBUTE, "/path/users/{userId}/info");

        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
        Map<Object, Object> context = ContextUtils.createContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(filter)
                .method("doFilterInternal")
                .args(args)
                .build();

        interceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
        //mock do something
        //mock do something end
        ContextUtils.setEndTime(context);
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        String key = httpServletRequest.getMethod() + " " + ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest);
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR)).getCount());


    }
}
