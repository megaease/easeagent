package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.ServletUtils;
import com.megaease.easeagent.zipkin.servlet.HttpFilterTracingInterceptor;
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

        HttpFilterTracingInterceptor httpFilterTracingInterceptor = new HttpFilterTracingInterceptor();


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
        System.out.println(spanInfoMap);
    }
}
