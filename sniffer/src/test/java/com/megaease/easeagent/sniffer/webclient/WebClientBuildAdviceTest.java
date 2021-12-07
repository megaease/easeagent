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
//package com.megaease.easeagent.sniffer.webclient;
//
//import com.megaease.easeagent.core.Classes;
//import com.megaease.easeagent.core.Definition;
//import com.megaease.easeagent.core.QualifiedBean;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
//import com.megaease.easeagent.sniffer.BaseSnifferTest;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.client.reactive.ClientHttpConnector;
//import org.springframework.http.codec.ClientCodecConfigurer;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
//import org.springframework.web.reactive.function.client.ExchangeFunction;
//import org.springframework.web.reactive.function.client.ExchangeStrategies;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.util.UriBuilderFactory;
//
//import java.util.List;
//import java.util.Map;
//import java.util.function.Consumer;
//
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.spy;
//
//public class WebClientBuildAdviceTest extends BaseSnifferTest {
//
//    protected static List<Class<?>> classList;
//    protected static AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//
//    @Before
//    public void before() throws Exception {
//        System.out.println(org.springframework.web.reactive.function.client.WebClient.Builder.class.getName());
//        reset(chainInvoker);
//        if (classList != null) {
//            return;
//        }
//        Definition.Default def = new GenWebClientBuilderAdvice().define(Definition.Default.EMPTY);
//        ClassLoader loader = this.getClass().getClassLoader();
//        classList = Classes.transform(
//                this.getClass().getName() + "$MyBuilder"
//        )
//                .with(def,
//                        new QualifiedBean("supplier4WebClientBuild", this.mockSupplier()),
//                        new QualifiedBean("", chainInvoker))
//                .load(loader);
//
//    }
//
//    @Test
//    public void invoke() throws Exception {
//        MyBuilder builder = (MyBuilder) classList.get(0).newInstance();
//        builder.build();
//        this.verifyInvokeTimes(chainInvoker, 1);
//
//    }
//
//    static class MyBuilder implements WebClient.Builder {
//
//        @Override
//        public WebClient.Builder baseUrl(String baseUrl) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder defaultUriVariables(Map<String, ?> defaultUriVariables) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder uriBuilderFactory(UriBuilderFactory uriBuilderFactory) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder defaultHeader(String header, String... values) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder defaultHeaders(Consumer<HttpHeaders> headersConsumer) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder defaultCookie(String cookie, String... values) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder defaultCookies(Consumer<MultiValueMap<String, String>> cookiesConsumer) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder defaultRequest(Consumer<WebClient.RequestHeadersSpec<?>> defaultRequest) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder filter(ExchangeFilterFunction filter) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder filters(Consumer<List<ExchangeFilterFunction>> filtersConsumer) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder clientConnector(ClientHttpConnector connector) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder codecs(Consumer<ClientCodecConfigurer> configurer) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder exchangeStrategies(ExchangeStrategies strategies) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder exchangeStrategies(Consumer<ExchangeStrategies.Builder> configurer) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder exchangeFunction(ExchangeFunction exchangeFunction) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder apply(Consumer<WebClient.Builder> builderConsumer) {
//            return null;
//        }
//
//        @Override
//        public WebClient.Builder clone() {
//            return null;
//        }
//
//        @Override
//        public WebClient build() {
//            return null;
//        }
//    }
//
//}
