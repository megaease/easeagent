package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.util.Map;

/**
 * Place it at last order
 */
public class RedisClientConnectInterceptor extends BaseAgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
//        RedisClient redisClient = (RedisClient) methodInfo.getInvoker();
//        Object[] args = methodInfo.getArgs();
//        RedisURI redisURI = this.getRedisURI(redisClient, args);
//        if (redisURI != null) {
//            AgentDynamicFieldAccessor.addSharedValue("RedisURI", redisURI);
//        }
//
//        ClientResources resources = redisClient.getResources();
//        AgentDynamicFieldAccessor.initDynamicFieldValue(resources);
//        ClientOptions options = redisClient.getOptions();
//        AgentDynamicFieldAccessor.initDynamicFieldValue(options);

        return chain.doAfter(methodInfo, context);
    }

}
