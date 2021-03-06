package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
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
        AgentInterceptor agentInterceptor = mock(AgentInterceptor.class);
        Definition.Default def = new GenSpringGatewayInitGlobalFilterAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        List<GlobalFilter> list = new ArrayList<>();
        GatewayAutoConfiguration instance = (GatewayAutoConfiguration) Classes.transform("org.springframework.cloud.gateway.config.GatewayAutoConfiguration")
                .with(def, new QualifiedBean("agentInterceptor4Gateway", agentInterceptor))
                .load(loader).get(0).newInstance();

        instance.filteringWebHandler(list);

        verify(agentInterceptor, times(1))
                .before(any(), any(String.class),
                        any(Object[].class),
                        any(Map.class));
        verify(agentInterceptor, times(1))
                .after(any(), any(String.class),
                        any(Object[].class),
                        any(Object.class), any(Exception.class),
                        any(Map.class));
    }
}
