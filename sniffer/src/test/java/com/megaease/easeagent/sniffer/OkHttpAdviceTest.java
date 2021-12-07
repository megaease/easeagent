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
//import com.megaease.easeagent.sniffer.http.okhttp.GenOkHttpAdvice;
//import okhttp3.*;
//import okio.Timeout;
//import org.jetbrains.annotations.NotNull;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.function.Supplier;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//
//public class OkHttpAdviceTest extends BaseSnifferTest {
//
//    @Test
//    public void testInvoke() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
//        Definition.Default def = new GenOkHttpAdvice().define(Definition.Default.EMPTY);
//        ClassLoader loader = this.getClass().getClassLoader();
//        String baseName = this.getClass().getName();
//        MyCall myCall = (MyCall) Classes.transform(baseName + "$MyCall")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4OkHttp", supplier), new QualifiedBean("supplier4OkHttpAsync", supplier))
//                .load(loader).get(0).newInstance();
//
//        myCall.execute();
//
//        this.verifyInvokeTimes(chainInvoker, 1);
//
//    }
//
//    static class MyCall implements Call {
//        private Request request = new Request.Builder().url("https://httpbin.org/get").build();
//
//        @Override
//        public void cancel() {
//
//        }
//
//        @NotNull
//        @Override
//        public Call clone() {
//            return mock(this.getClass());
//        }
//
//        @Override
//        public void enqueue(@NotNull Callback callback) {
//
//        }
//
//        @NotNull
//        @Override
//        public Response execute() throws IOException {
//            return new Response.Builder()
//                    .code(200)
//                    .request(request)
//                    .message("")
//                    .protocol(Protocol.HTTP_1_1)
//                    .build();
//        }
//
//        @Override
//        public boolean isCanceled() {
//            return false;
//        }
//
//        @Override
//        public boolean isExecuted() {
//            return false;
//        }
//
//        @NotNull
//        @Override
//        public Request request() {
//            return request;
//        }
//
//        @NotNull
//        @Override
//        public Timeout timeout() {
//            return Timeout.NONE;
//        }
//    }
//}
