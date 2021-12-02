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
//import org.junit.Test;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.client.AbstractClientHttpRequest;
//import org.springframework.http.client.ClientHttpResponse;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.URI;
//import java.util.function.Supplier;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//
//public class RestTemplateAdviceTest extends BaseSnifferTest {
//
//    @Test
//    public void testInvoke() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        Definition.Default def = new GenRestTemplateAdvice().define(Definition.Default.EMPTY);
//        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//        String baseName = this.getClass().getName();
//        ClassLoader loader = this.getClass().getClassLoader();
//        MyRequest request = (MyRequest) Classes.transform(baseName + "$MyRequest")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4RestTemplate", supplier))
//                .load(loader).get(0).newInstance();
//
//        HttpHeaders headers = new HttpHeaders();
//        request.executeInternal(headers);
//
//        this.verifyInvokeTimes(chainInvoker, 1);
//
//    }
//
//    public static class MyRequest extends AbstractClientHttpRequest {
//
//        @Override
//        protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
//            return null;
//        }
//
//        @Override
//        protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
//            return null;
//        }
//
//        @Override
//        public String getMethodValue() {
//            return null;
//        }
//
//        @Override
//        public URI getURI() {
//            return null;
//        }
//    }
//}
