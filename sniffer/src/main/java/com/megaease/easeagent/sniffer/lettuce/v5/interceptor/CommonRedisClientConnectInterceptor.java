package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StatefulRedisConnectionImpl;

import java.util.Map;

/**
 * put redisURI into result like {@link StatefulRedisConnectionImpl}，{@link ConnectionFuture}
 */
public class CommonRedisClientConnectInterceptor extends BaseRedisAgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return null;
        }
        RedisURI redisURI = this.getRedisURI((RedisClient) methodInfo.getInvoker(), methodInfo.getArgs());
        if (redisURI != null) {
            this.setRedisURIToDynamicField(methodInfo.getRetValue(), redisURI);
        }
        Object ret = chain.doAfter(methodInfo, context);
        if (ret instanceof ConnectionFuture) {
            ConnectionFuture<?> future = (ConnectionFuture<?>) ret;
            return new ConnectionFutureWrapper<>(future, redisURI);
        }
        return ret;
    }
}
