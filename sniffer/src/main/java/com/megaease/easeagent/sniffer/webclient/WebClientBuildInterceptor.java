package com.megaease.easeagent.sniffer.webclient;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class WebClientBuildInterceptor implements AgentInterceptor {

    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;

    public WebClientBuildInterceptor(AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        WebClient.Builder builder = (WebClient.Builder) methodInfo.getInvoker();
        builder.filter(new WebClientFilter(chainBuilder, chainInvoker));
        chain.doBefore(methodInfo, context);
    }
}
