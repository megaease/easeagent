package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import org.junit.Test;
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
    public void testInvoke() {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        Definition.Default def = new GenSpringGatewayHttpHeadersFilterAdvice().define(Definition.Default.EMPTY);
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
        ClassLoader loader = this.getClass().getClassLoader();
        Classes.transform("org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4GatewayHeaders", builder))
                .load(loader);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(exchange.getRequest()).thenReturn(request);
        HttpHeaders httpHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(httpHeaders);

        List<HttpHeadersFilter> list = new ArrayList<>();
        HttpHeadersFilter.filterRequest(list, exchange);

        verify(chainInvoker, times(1))
                .doBefore(any(AgentInterceptorChain.Builder.class), any(), any(String.class),
                        any(Object[].class),
                        any(Map.class));

        verify(chainInvoker, times(1))
                .doAfter(any(), any(String.class),
                        any(Object[].class),
                        any(), any(Throwable.class),
                        any(Map.class));
    }
}
