///*
// * Copyright (c) 2017, MegaEase
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.megaease.easeagent.zipkin;
//
//import brave.Tracing;
//import brave.handler.MutableSpan;
//import brave.handler.SpanHandler;
//import brave.propagation.TraceContext;
//import com.megaease.easeagent.config.Config;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
//import com.megaease.easeagent.plugin.MethodInfo;
//import com.megaease.easeagent.core.utils.ServletUtils;
//import com.megaease.easeagent.zipkin.http.HttpFilterTracingInterceptor;
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.web.filter.CharacterEncodingFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.*;
//
//public class HttpFilterTracingInterceptorTest extends BaseZipkinTest {
//
//    @Test
//    public void success() {
//        Config config = this.createConfig(HttpFilterTracingInterceptor.ENABLE_KEY, "true");
//        Map<String, String> spanInfoMap = new HashMap<>();
//        Tracing.newBuilder()
//                .currentTraceContext(currentTraceContext)
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        Map<String, String> tmpMap = new HashMap<>(span.tags());
//                        spanInfoMap.putAll(tmpMap);
//                        return super.end(context, span, cause);
//                    }
//                })
//                .build().tracer();
//
//        HttpFilterTracingInterceptor httpFilterTracingInterceptor = new HttpFilterTracingInterceptor(Tracing.current(), config);
//
//        CharacterEncodingFilter filter = mock(CharacterEncodingFilter.class);
//        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
//        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
//
//        when(httpServletRequest.getMethod()).thenReturn("GET");
//        when(httpServletRequest.getRequestURI()).thenReturn("/path/users/123/info");
//        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/path/users/123/info?page=200"));
//        when(httpServletResponse.getStatus()).thenReturn(200);
//
//        Map<String, Object> attrMap = new HashMap<>();
//        doAnswer(invocation -> {
//            String key = invocation.getArgumentAt(0, String.class);
//            Object value = invocation.getArgumentAt(1, Object.class);
//            attrMap.put(key, value);
//            return null;
//        }).when(httpServletRequest).setAttribute(any(String.class), any());
//        when(httpServletRequest.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));
//
//        httpServletRequest.setAttribute(ServletUtils.BEST_MATCHING_PATTERN_ATTRIBUTE, "/path/users/{userId}/info");
//
//
//        FilterChain filterChain = mock(FilterChain.class);
//        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
//        Map<Object, Object> context = new HashMap<>();
//
//        MethodInfo methodInfo = MethodInfo.builder()
//                .invoker(filter)
//                .method("doFilterInternal")
//                .args(args)
//                .retValue(null)
//                .throwable(null)
//                .build();
//
//        httpFilterTracingInterceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
//
//        // mock do something
//        // mock do something end
//
//        httpFilterTracingInterceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
//
//        Map<String, String> expectedMap = new HashMap<>();
//        expectedMap.put("http.path", "/path/users/123/info");
//        expectedMap.put("http.method", "GET");
//        expectedMap.put("http.route", "/path/users/{userId}/info");
//        Assert.assertEquals(expectedMap, spanInfoMap);
//    }
//
//    @Test
//    public void fail() {
//        Config config = this.createConfig(HttpFilterTracingInterceptor.ENABLE_KEY, "true");
//        Map<String, String> spanInfoMap = new HashMap<>();
//        Tracing.newBuilder()
//                .currentTraceContext(currentTraceContext)
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        Map<String, String> tmpMap = new HashMap<>(span.tags());
//                        tmpMap.put("error", span.error().toString());
//                        spanInfoMap.putAll(tmpMap);
//                        return super.end(context, span, cause);
//                    }
//                })
//                .build().tracer();
//
//        HttpFilterTracingInterceptor httpFilterTracingInterceptor = new HttpFilterTracingInterceptor(Tracing.current(), config);
//
//        CharacterEncodingFilter filter = mock(CharacterEncodingFilter.class);
//        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
//        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
//
//        when(httpServletRequest.getMethod()).thenReturn("GET");
//        when(httpServletRequest.getRequestURI()).thenReturn("/path/users/123/info");
//        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/path/users/123/info?page=200"));
//        when(httpServletResponse.getStatus()).thenReturn(400);
//
//        Map<String, Object> attrMap = new HashMap<>();
//        doAnswer(invocation -> {
//            String key = invocation.getArgumentAt(0, String.class);
//            Object value = invocation.getArgumentAt(1, Object.class);
//            attrMap.put(key, value);
//            return null;
//        }).when(httpServletRequest).setAttribute(any(String.class), any());
//        when(httpServletRequest.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));
//
//        httpServletRequest.setAttribute(ServletUtils.BEST_MATCHING_PATTERN_ATTRIBUTE, "/path/users/{userId}/info");
//
//
//        FilterChain filterChain = mock(FilterChain.class);
//        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
//        Map<Object, Object> context = new HashMap<>();
//
//        MethodInfo methodInfo = MethodInfo.builder()
//                .invoker(filter)
//                .method("doFilterInternal")
//                .args(args)
//                .retValue(null)
//                .throwable(null)
//                .build();
//
//        httpFilterTracingInterceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
//
//        // mock do something
//        // mock do something end
//        Exception exception = new Exception("test fail");
//        methodInfo.setThrowable(exception);
//
//        httpFilterTracingInterceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
//
//        Map<String, String> expectedMap = new HashMap<>();
//        expectedMap.put("http.path", "/path/users/123/info");
//        expectedMap.put("http.method", "GET");
//        expectedMap.put("http.route", "/path/users/{userId}/info");
//        expectedMap.put("http.status_code", "400");
//        expectedMap.put("error", Exception.class.getName() + ": test fail");
//        Assert.assertEquals(expectedMap, spanInfoMap);
//    }
//
//    @Test
//    public void disableTracing() {
//        Config config = this.createConfig(HttpFilterTracingInterceptor.ENABLE_KEY, "false");
//        Map<String, String> spanInfoMap = new HashMap<>();
//        Tracing.newBuilder()
//                .currentTraceContext(currentTraceContext)
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        Map<String, String> tmpMap = new HashMap<>(span.tags());
//                        spanInfoMap.putAll(tmpMap);
//                        return super.end(context, span, cause);
//                    }
//                })
//                .build().tracer();
//
//        HttpFilterTracingInterceptor httpFilterTracingInterceptor = new HttpFilterTracingInterceptor(Tracing.current(), config);
//
//        CharacterEncodingFilter filter = mock(CharacterEncodingFilter.class);
//        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
//        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
//
//        when(httpServletRequest.getMethod()).thenReturn("GET");
//        when(httpServletRequest.getRequestURI()).thenReturn("/path/users/123/info");
//        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("/path/users/123/info?page=200"));
//        when(httpServletResponse.getStatus()).thenReturn(200);
//
//        Map<String, Object> attrMap = new HashMap<>();
//        doAnswer(invocation -> {
//            String key = invocation.getArgumentAt(0, String.class);
//            Object value = invocation.getArgumentAt(1, Object.class);
//            attrMap.put(key, value);
//            return null;
//        }).when(httpServletRequest).setAttribute(any(String.class), any());
//        when(httpServletRequest.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));
//
//        httpServletRequest.setAttribute(ServletUtils.BEST_MATCHING_PATTERN_ATTRIBUTE, "/path/users/{userId}/info");
//
//        FilterChain filterChain = mock(FilterChain.class);
//        Object[] args = new Object[]{httpServletRequest, httpServletResponse, filterChain};
//        Map<Object, Object> context = new HashMap<>();
//
//        MethodInfo methodInfo = MethodInfo.builder()
//                .invoker(filter)
//                .method("doFilterInternal")
//                .args(args)
//                .retValue(null)
//                .throwable(null)
//                .build();
//
//        httpFilterTracingInterceptor.before(methodInfo, context, mock(AgentInterceptorChain.class));
//
//        httpFilterTracingInterceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
//
//        Assert.assertTrue(spanInfoMap.isEmpty());
//    }
//}
