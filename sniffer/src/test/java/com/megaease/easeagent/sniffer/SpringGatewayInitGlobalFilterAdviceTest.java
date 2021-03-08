package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import org.junit.Test;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SpringGatewayInitGlobalFilterAdviceTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testInvoke() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        Definition.Default def = new GenSpringGatewayInitGlobalFilterAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        List<GlobalFilter> list = new ArrayList<>();
        GatewayAutoConfiguration instance = (GatewayAutoConfiguration) Classes.transform("org.springframework.cloud.gateway.config.GatewayAutoConfiguration")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4Gateway", builder))
                .load(loader).get(0).newInstance();

        instance.filteringWebHandler(list);

        verify(chainInvoker, times(1))
                .doBefore(any(AgentInterceptorChain.Builder.class), any(), any(String.class),
                        any(Object[].class),
                        any(Map.class));
        verify(chainInvoker, times(1))
                .doAfter(any(), any(String.class),
                        any(Object[].class),
                        any(Object.class), any(Exception.class),
                        any(Map.class));
    }
}
