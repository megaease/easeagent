package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.google.common.base.Joiner;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.protocol.RedisCommand;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisChannelWriterInterceptor extends BaseRedisAgentInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object data = this.getDataFromDynamicField(methodInfo.getInvoker());
        if (data == null) {
            super.before(methodInfo, context, chain);
            return;
        }
        String cmd;
        if (methodInfo.getArgs()[0] instanceof RedisCommand) {
            RedisCommand<?, ?, ?> redisCommand = (RedisCommand<?, ?, ?>) methodInfo.getArgs()[0];
            cmd = redisCommand.getType().name();
        } else {
            Collection<RedisCommand<?, ?, ?>> redisCommands = (Collection<RedisCommand<?, ?, ?>>) methodInfo.getArgs()[0];
            cmd = "[" + Joiner.on(",").join(redisCommands.stream().map(input -> input.getType().name()).collect(Collectors.toList())) + "]";
        }
        context.put(ContextCons.CACHE_CMD, cmd);
        context.put(ContextCons.CACHE_URI, data);
        super.before(methodInfo, context, chain);
    }
}
