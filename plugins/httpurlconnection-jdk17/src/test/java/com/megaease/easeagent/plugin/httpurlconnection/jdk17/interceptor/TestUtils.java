/*
 * Copyright (c) 2023, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.httpurlconnection.jdk17.interceptor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TestUtils {
    public static final String FORWARDED_NAME = "X-Forwarded-For";
    public static final String FORWARDED_VALUE = "testForwarded";
    public static final String RESPONSE_TAG_NAME = "X-EG-Test";
    public static final String RESPONSE_TAG_VALUE = "X-EG-Test-Value";

    @SneakyThrows
    static MethodInfo mockMethodInfo() {
        HttpURLConnection httpURLConnection = mockHttpURLConnection();
        return mockMethodInfo(httpURLConnection);
    }

    static MethodInfo mockMethodInfo(HttpURLConnection httpURLConnection) {
        MethodInfo methodInfo = MethodInfo.builder()
            .invoker(httpURLConnection).retValue(httpURLConnection)
            .build();
        return methodInfo;
    }

    @SneakyThrows
    static HttpURLConnection mockHttpURLConnection() {
        URL url = new URL("http://127.0.0.1:8080");
        Map<String, String> responseHeader = ImmutableMap.of(TestUtils.RESPONSE_TAG_NAME, TestUtils.RESPONSE_TAG_VALUE);
        return getConnection(url, "GET", null, responseHeader);
    }

    static HttpURLConnection getConnection(
        URL url, String method, Map<String, String> requestHeaders, Map<String, String> responseHeader) throws IOException {

        HttpURLConnection conn = new HttpURLConnectionTest(url, responseHeader);
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

    public static class HttpURLConnectionTest extends HttpURLConnection implements DynamicFieldAccessor {
        Map<String, String> responseHeader;
        Object easeAgent$$DynamicField$$Data;

        public HttpURLConnectionTest(URL u, Map<String, String> responseHeader) {
            super(u);
            this.responseHeader = responseHeader;
        }

        @Override
        public void setEaseAgent$$DynamicField$$Data(Object data) {
            this.easeAgent$$DynamicField$$Data = data;
        }

        @Override
        public Object getEaseAgent$$DynamicField$$Data() {
            return easeAgent$$DynamicField$$Data;
        }


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
    }
}
