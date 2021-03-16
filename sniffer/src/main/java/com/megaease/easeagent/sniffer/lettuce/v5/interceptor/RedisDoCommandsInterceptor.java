package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.interceptor.*;
import io.lettuce.core.RedisURI;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class RedisDoCommandsInterceptor extends BaseRedisAgentInterceptor {

    private final AgentInterceptorChainInvoker chainInvoker;

    private final AgentInterceptorChain.Builder reactiveChainBuilder;

    public RedisDoCommandsInterceptor(AgentInterceptorChainInvoker chainInvoker, AgentInterceptorChain.Builder reactiveChainBuilder) {
        this.chainInvoker = chainInvoker;
        this.reactiveChainBuilder = reactiveChainBuilder;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        RedisURI redisURI = this.getRedisURIFromDynamicField(methodInfo.getInvoker());
        context.put(RedisURI.class, redisURI);
        super.before(methodInfo, context, chain);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object retValue = methodInfo.getRetValue();
//        RedisURI redisURI = this.getRedisURIFromDynamicField(methodInfo.getInvoker());
//        if (retValue instanceof RedisFuture) {
//            Object[] tmpArr = new Object[]{redisURI, methodInfo, context, chain};
//            this.setDataToDynamicField(retValue, tmpArr);
//            return super.after(methodInfo, context, chain);
//        }
        Object after = super.after(methodInfo, context, chain);
        if (retValue instanceof Flux) {
            Flux<?> ret = (Flux<?>) after;
            return new AgentFlux<>(ret, methodInfo, this.reactiveChainBuilder, chainInvoker, context, true);
        }
        if (retValue instanceof Mono) {
            Mono<?> ret = (Mono<?>) after;
            return new AgentMono<>(ret, methodInfo, this.reactiveChainBuilder, chainInvoker, context, true);
        }
        // return for sync
        return after;
    }
}
