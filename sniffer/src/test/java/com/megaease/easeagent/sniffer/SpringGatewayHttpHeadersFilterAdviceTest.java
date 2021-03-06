package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.junit.Test;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SpringGatewayHttpHeadersFilterAdviceTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testInvoke() throws Exception {
        AgentInterceptor agentInterceptor = mock(AgentInterceptor.class);
        Definition.Default def = new GenSpringGatewayHttpHeadersFilterAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        List<GlobalFilter> list = new ArrayList<>();
        Classes.transform("org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter")
                .with(def, new QualifiedBean("agentInterceptor4GatewayHeaders", agentInterceptor))
                .load(loader);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(exchange.getRequest()).thenReturn(request);
        HttpHeaders httpHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(httpHeaders);

        HttpHeadersFilter.filterRequest(new ArrayList<>(), exchange);

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
