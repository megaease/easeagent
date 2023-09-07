/*
 * Copyright (c) 2023, Inspireso and/or its affiliates. All rights reserved.
 */

package com.megaease.easeagent.plugin.httpclient.interceptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;
import com.megaease.easeagent.plugin.tools.trace.HttpResponse;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class HttpURLConnectionGetResponseCodeInterceptorTest {

    @SneakyThrows
    @Test
    public void before() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = mockMethodInfo();

        HttpURLConnectionGetResponseCodeInterceptor httpURLConnectionWriteRequestsInterceptor = new HttpURLConnectionGetResponseCodeInterceptor();
        MockEaseAgent.cleanLastSpan();
        httpURLConnectionWriteRequestsInterceptor.before(methodInfo, context);
        httpURLConnectionWriteRequestsInterceptor.after(methodInfo, context);
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        assertNotNull(mockSpan);
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals(TestConst.RESPONSE_TAG_VALUE, mockSpan.tag(TestConst.RESPONSE_TAG_NAME));
        assertNull(mockSpan.parentId());

        Span span = context.nextSpan();
        try (Scope ignored = span.maybeScope()) {
            httpURLConnectionWriteRequestsInterceptor.doBefore(methodInfo, context);
            httpURLConnectionWriteRequestsInterceptor.doAfter(methodInfo, context);
            mockSpan = MockEaseAgent.getLastSpan();
            assertEquals(span.traceIdString(), mockSpan.traceId());
            assertEquals(span.spanIdString(), mockSpan.parentId());
            assertNotNull(mockSpan.id());
        }
        span.abandon();
    }

    @Test
    public void after() {
        before();
    }


    @SneakyThrows
    @Test
    public void getRequest() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = mockMethodInfo();

        HttpURLConnectionGetResponseCodeInterceptor httpClientDoExecuteInterceptor = new HttpURLConnectionGetResponseCodeInterceptor();
        HttpRequest request = httpClientDoExecuteInterceptor.getRequest(methodInfo, context);
        assertEquals(Span.Kind.CLIENT, request.kind());
        assertEquals("GET", request.method());
    }

    @SneakyThrows
    @Test
    public void getResponse() {
        Context context = EaseAgent.getContext();
        MethodInfo methodInfo = mockMethodInfo();

        HttpURLConnectionGetResponseCodeInterceptor httpClientDoExecuteInterceptor = new HttpURLConnectionGetResponseCodeInterceptor();

        HttpResponse httpResponse = httpClientDoExecuteInterceptor.getResponse(methodInfo, context);
        assertEquals(200, httpResponse.statusCode());
    }

    @SneakyThrows
    private MethodInfo mockMethodInfo() {
        URL url = new URL("http://127.0.0.1:8080");
        Map<String, String> responseHeader = ImmutableMap.of(TestConst.RESPONSE_TAG_NAME, TestConst.RESPONSE_TAG_VALUE);
        HttpURLConnection httpURLConnection = getConnection(url, "GET", null, responseHeader);
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(httpURLConnection).retValue(httpURLConnection)
            .build();
        return methodInfo;
    }

    private static HttpURLConnection getConnection(
        URL url, String method, Map<String, String> requestHeaders, Map<String, String> responseHeader) throws IOException {

        HttpURLConnection conn = new HttpURLConnection(url) {

            @Override
            public void connect() throws IOException {

            }

            @Override
            public void disconnect() {

            }

            @Override
            public boolean usingProxy() {
                return false;
            }

            @Override
            public int getResponseCode() throws IOException {
                return 200;
            }

            @Override
            public Map<String, List<String>> getHeaderFields() {
                Map<String, List<String>> fields = new HashMap<>();
                for (String key : responseHeader.keySet()) {
                    fields.put(key, Lists.newArrayList(responseHeader.get(key)));
                }
                return fields;
            }
        };
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty(HttpHeaders.HOST, url.getHost());
        if (requestHeaders != null) {
            for (String key : requestHeaders.keySet()) {
                conn.setRequestProperty(key, requestHeaders.get(key));
            }
        }

        return conn;
    }
}
