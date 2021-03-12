package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Place it at last order
 */
public class RedisClientCreateInterceptor implements AgentInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        RedisClient redisClient = (RedisClient) methodInfo.getInvoker();
        Object[] args = methodInfo.getArgs();
        RedisURI redisURI = this.getRedisURI(redisClient, args);
        if (redisURI != null) {
            AgentDynamicFieldAccessor.addSharedValue("RedisURI", redisURI);
        }

        ClientResources resources = redisClient.getResources();
        AgentDynamicFieldAccessor.initDynamicFieldValue(resources);
        ClientOptions options = redisClient.getOptions();
        AgentDynamicFieldAccessor.initDynamicFieldValue(options);

        return chain.doAfter(methodInfo, context);
    }

    private RedisURI getRedisURI(RedisClient redisClient, Object[] args) {
        RedisURI redisURI = null;
        for (Object arg : args) {
            if (arg instanceof RedisURI) {
                redisURI = (RedisURI) arg;
                break;
            }
        }
        if (redisURI == null) {
            Field redisURIField = AgentFieldAccessor.getFieldFromClass(RedisClient.class, "redisURI");
            redisURI = AgentFieldAccessor.getFieldValue(redisURIField, redisClient);
        }
        return redisURI;
    }
}
