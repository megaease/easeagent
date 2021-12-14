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
//package com.megaease.easeagent.sniffer;
//
//import com.megaease.easeagent.core.Classes;
//import com.megaease.easeagent.core.Definition;
//import com.megaease.easeagent.core.QualifiedBean;
//import com.megaease.easeagent.core.interceptor.AgentInterceptor;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
//import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
//import com.megaease.easeagent.sniffer.httpclient.advice.GenHttpClientAdvice;
//import org.apache.http.HttpHost;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.conn.ClientConnectionManager;
//import org.apache.http.params.HttpParams;
//import org.apache.http.protocol.HttpContext;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.function.Supplier;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//
//public class HttpClientAdviceTest extends BaseSnifferTest {
//
//    @Test
//    public void testInvoke() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
//        Definition.Default def = new GenHttpClientAdvice().define(Definition.Default.EMPTY);
//        String baseName = this.getClass().getName();
//        ClassLoader loader = this.getClass().getClassLoader();
//        MyClient client = (MyClient) Classes.transform(baseName + "$MyClient")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4HttpClient", supplier))
//                .load(loader).get(0).newInstance();
//
//
//        HttpHost target = HttpHost.create("https://httpbin.org/get");
//        HttpGet httpGet = new HttpGet("https://httpbin.org/get");
//
//        client.doExecute(target, httpGet, null);
//
//        this.verifyInvokeTimes(chainInvoker, 1);
//
//    }
//
//    static class MyClient implements HttpClient {
//        public CloseableHttpResponse doExecute(HttpHost target, HttpRequest request,
//                                               HttpContext context) {
//            return null;
//        }
//
//        @Override
//        public HttpParams getParams() {
//            return null;
//        }
//
//        @Override
//        public ClientConnectionManager getConnectionManager() {
//            return null;
//        }
//
//        @Override
//        public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
//            return null;
//        }
//    }
//}
