package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StatefulRedisConnectionImpl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * put redisURI into result like {@link StatefulRedisConnectionImpl}，{@link ConnectionFuture}
 */
public class CommonRedisClientConnectInterceptor extends BaseRedisAgentInterceptor {

    private final AgentInterceptorChainInvoker chainInvoker;
    private final AgentInterceptorChain.Builder chainBuilder4Future;


    public CommonRedisClientConnectInterceptor(AgentInterceptorChainInvoker chainInvoker, AgentInterceptorChain.Builder chainBuilder4Future) {
        this.chainBuilder4Future = chainBuilder4Future;
        this.chainInvoker = chainInvoker;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return null;
        }
        RedisURI redisURI = this.getRedisURI((RedisClient) methodInfo.getInvoker(), methodInfo.getArgs());
        this.setRedisURIToDynamicField(methodInfo.getRetValue(), redisURI);
        Object ret = chain.doAfter(methodInfo, context);
        if (ret instanceof ConnectionFuture) {
            ConnectionFuture<?> future = (ConnectionFuture<?>) ret;
            return new ConnectionFutureWrapper<>(future, methodInfo, chainBuilder4Future, chainInvoker, context, true);
        }
        if (ret instanceof CompletableFuture) {
            CompletableFuture<?> future = (CompletableFuture<?>) ret;
            return new CompletableFutureWrapper<>(future, methodInfo, chainBuilder4Future, chainInvoker, context, true);
        }
        return ret;
    }
}
