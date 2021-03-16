package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;

import java.util.HashMap;
import java.util.Map;

/**
 * Place it at last order
 */
public class RedisClientCreateInterceptor extends BaseRedisAgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        RedisClient redisClient = (RedisClient) methodInfo.getInvoker();
        Object[] args = methodInfo.getArgs();
        RedisURI redisURI = this.getRedisURI(redisClient, args);
        if (redisURI != null) {
            HashMap<Object, Object> infoMap = new HashMap<>();
            infoMap.put(RedisURI.class, redisURI);
            ClientResources resources = redisClient.getResources();
            AgentDynamicFieldAccessor.setDynamicFieldValue(resources, infoMap);
            ClientOptions options = redisClient.getOptions();
            AgentDynamicFieldAccessor.setDynamicFieldValue(options, infoMap);
        }
        return chain.doAfter(methodInfo, context);
    }
}
