package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Map;

/**
 * put redisURI into result like {@link RedisCommands}，{@link RedisAsyncCommands}，{@link RedisReactiveCommands}
 */
public class StatefulRedisConnectionInterceptor extends BaseRedisAgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return null;
        }
        RedisURI redisURI = getRedisURIFromDynamicField(methodInfo.getInvoker());
        setRedisURIToDynamicField(methodInfo.getRetValue(), redisURI);
        return chain.doAfter(methodInfo, context);
    }
}
