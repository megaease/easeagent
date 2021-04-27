/*
 *   Copyright (c) 2017, MegaEase
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.megaease.easeagent.sniffer.health.advice;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.megaease.easeagent.sniffer.healthy.advice.GenSpringApplicationAdminMXBeanRegistrarAdvice;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.admin.SpringApplicationAdminMXBeanRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SpringApplicationAdminMXBeanRegistrarAdviceTest extends BaseSnifferTest {

    static List<Class<?>> classList;

    static AgentInterceptorChainInvoker chainInvoker;

    @Before
    public void before() {
        if (classList == null) {
            chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
            Definition.Default def = new GenSpringApplicationAdminMXBeanRegistrarAdvice().define(Definition.Default.EMPTY);
            ClassLoader loader = this.getClass().getClassLoader();
            classList = Classes.transform("org.springframework.boot.admin.SpringApplicationAdminMXBeanRegistrar")
                    .with(def,
                            new QualifiedBean("supplier4CreateRegistrar", this.mockSupplier()),
                            new QualifiedBean("supplier4OnApplicationEvent", this.mockSupplier()),
                            new QualifiedBean("", chainInvoker)
                    )
                    .load(loader);
        }
    }

    @SneakyThrows
    @Test
    public void invoke() {
        SpringApplicationAdminMXBeanRegistrar registrar = (SpringApplicationAdminMXBeanRegistrar) classList.get(0)
                .getDeclaredConstructor(String.class)
                .newInstance("agent.org.springframework.boot:type=Admin,name=AgentSpringApplication");
//        this.verifyAfterInvokeTimes(chainInvoker, 1);
//        reset(chainInvoker);
        registrar = spy(registrar);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        registrar.setApplicationContext(context);
        ApplicationEvent event = new ApplicationReadyEvent(mock(SpringApplication.class), new String[0], context);
        registrar.onApplicationEvent(event);
        this.verifyInvokeTimes(chainInvoker, 1);
    }
}
