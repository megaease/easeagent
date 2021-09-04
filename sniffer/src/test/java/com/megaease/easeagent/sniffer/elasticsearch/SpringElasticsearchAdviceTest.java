/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.sniffer.elasticsearch;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.megaease.easeagent.sniffer.elasticsearch.advice.GenSpringElasticsearchAdvice;
import org.junit.Test;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties;

import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SpringElasticsearchAdviceTest extends BaseSnifferTest {

    @Test
    public void testInvoke() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        Supplier<AgentInterceptorChain.Builder> supplier = () -> builder;
        Definition.Default def = new GenSpringElasticsearchAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        MyClientConfig myClientConfig = (MyClientConfig) Classes.transform(baseName + "$MyClientConfig")
            .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("supplier4SpringElasticsearchSetProperty", supplier))
            .load(loader).get(0).newInstance();

        myClientConfig.setPassword("sss");

        this.verifyInvokeTimes(chainInvoker, 1);

    }

    static class MyClientConfig extends ElasticsearchRestClientProperties {
        @Override
        public void setPassword(String password) {
            super.setPassword(password);
        }
    }
}
