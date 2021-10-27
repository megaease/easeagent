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
//import com.megaease.easeagent.sniffer.httpclient.advice.GenHttpClient5Advice;
//import org.apache.hc.client5.http.classic.HttpClient;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
//import org.apache.hc.core5.http.*;
//import org.apache.hc.core5.http.io.HttpClientResponseHandler;
//import org.apache.hc.core5.http.protocol.HttpContext;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.function.Supplier;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//
//public class HttpClient5AdviceTest extends BaseSnifferTest {
//
//    @Test
//    public void testInvoke() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
//        Definition.Default def = new GenHttpClient5Advice().define(Definition.Default.EMPTY);
//        String baseName = this.getClass().getName();
//        ClassLoader loader = this.getClass().getClassLoader();
//        MyClient client = (MyClient) Classes.transform(baseName + "$MyClient")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4HttpClient5", supplier))
//                .load(loader).get(0).newInstance();
//
//
//        HttpHost target = HttpHost.create("https://httpbin.org");
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
//        public HttpResponse execute(ClassicHttpRequest request) throws IOException {
//            return null;
//        }
//
//        @Override
//        public HttpResponse execute(ClassicHttpRequest request, HttpContext context) throws IOException {
//            return null;
//        }
//
//        @Override
//        public ClassicHttpResponse execute(HttpHost target, ClassicHttpRequest request) throws IOException {
//            return null;
//        }
//
//        @Override
//        public HttpResponse execute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(HttpHost target, ClassicHttpRequest request, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
//            return null;
//        }
//
//        @Override
//        public <T> T execute(HttpHost target, ClassicHttpRequest request, HttpContext context, HttpClientResponseHandler<? extends T> responseHandler) throws IOException {
//            return null;
//        }
//    }
//}
