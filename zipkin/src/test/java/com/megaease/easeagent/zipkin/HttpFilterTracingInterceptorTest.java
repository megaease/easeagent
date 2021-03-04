package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.core.utils.ServletUtils;
import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpFilterTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        spanInfoMap.putAll(tmpMap);
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();

        HttpFilterTracingInterceptor httpFilterTracingInterceptor = new HttpFilterTracingInterceptor(Tracing.current());

        CharacterEncodingFilter filter = mock(CharacterEncodingFilter.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

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


        FilterChain filterChain = mock(FilterChain.class);
        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
        Map<Object, Object> context = new HashMap<>();
        httpFilterTracingInterceptor.before(filter, "doFilterInternal", args, context);

        // mock do something
        // mock do something end

        httpFilterTracingInterceptor.after(filter, "doFilterInternal", args, null, null, context);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.path", "/path/users/123/info");
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.route", "/path/users/{userId}/info");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void fail() {
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        tmpMap.put("error", span.error().toString());
                        spanInfoMap.putAll(tmpMap);
                        return super.end(context, span, cause);
                    }
                })
                .build().tracer();

        HttpFilterTracingInterceptor httpFilterTracingInterceptor = new HttpFilterTracingInterceptor(Tracing.current());

        CharacterEncodingFilter filter = mock(CharacterEncodingFilter.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

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


        FilterChain filterChain = mock(FilterChain.class);
        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
        Map<Object, Object> context = new HashMap<>();
        httpFilterTracingInterceptor.before(filter, "doFilterInternal", args, context);

        // mock do something
        // mock do something end
        Exception exception = new Exception("test fail");

        httpFilterTracingInterceptor.after(filter, "doFilterInternal", args, null, exception, context);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("http.path", "/path/users/123/info");
        expectedMap.put("http.method", "GET");
        expectedMap.put("http.route", "/path/users/{userId}/info");
        expectedMap.put("http.status_code", "400");
        expectedMap.put("error", Exception.class.getName() + ": test fail");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }
}
