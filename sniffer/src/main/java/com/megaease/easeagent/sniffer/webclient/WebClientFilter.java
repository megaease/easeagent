package com.megaease.easeagent.sniffer.webclient;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.AgentMono;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import lombok.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.Map;

public class WebClientFilter implements ExchangeFilterFunction {


    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;

    public WebClientFilter(AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest clientRequest, @NonNull ExchangeFunction exchangeFunction) {
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(this)
                .method("filter")
                .args(new Object[]{clientRequest, exchangeFunction})
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        chainInvoker.doBefore(this.chainBuilder, methodInfo, context);
        clientRequest = (ClientRequest) methodInfo.getArgs()[0];
        exchangeFunction = (ExchangeFunction) methodInfo.getArgs()[1];
        try {
            Mono<ClientResponse> mono = exchangeFunction.exchange(clientRequest);
            return new AgentMono<>(mono, methodInfo, chainBuilder, chainInvoker, context, false);
        } catch (Exception exception) {
            methodInfo.setThrowable(exception);
            chainInvoker.doAfter(this.chainBuilder, methodInfo, context);
            throw exception;
        }
    }
}
