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
//import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Supplier;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.spy;
//
//public class SpringGatewayInitGlobalFilterAdviceTest extends BaseSnifferTest {
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testInvoke() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
//        Definition.Default def = new GenSpringGatewayInitGlobalFilterAdvice().define(Definition.Default.EMPTY);
//        ClassLoader loader = this.getClass().getClassLoader();
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//        List<GlobalFilter> list = new ArrayList<>();
//        GatewayAutoConfiguration instance = (GatewayAutoConfiguration) Classes.transform("org.springframework.cloud.gateway.config.GatewayAutoConfiguration")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4Gateway", supplier))
//                .load(loader).get(0).newInstance();
//
//        instance.filteringWebHandler(list);
//
//        this.verifyInvokeTimes(chainInvoker, 1);
//
//    }
//}
