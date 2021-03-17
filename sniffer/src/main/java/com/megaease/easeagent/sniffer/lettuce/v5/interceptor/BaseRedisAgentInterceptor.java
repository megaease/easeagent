package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

abstract class BaseRedisAgentInterceptor implements AgentInterceptor {
    private static final String REDIS_URI = "redisURI";
    private static final String REDIS_URIS = "initialUris";

    protected RedisURI getRedisURI(RedisClient redisClient, Object[] args) {
        if (args == null) {
            return AgentFieldAccessor.getFieldValue(redisClient, REDIS_URI);
        }
        RedisURI redisURI = null;
        for (Object arg : args) {
            if (arg instanceof RedisURI) {
                redisURI = (RedisURI) arg;
                break;
            }
        }
        if (redisURI == null) {
            redisURI = AgentFieldAccessor.getFieldValue(redisClient, REDIS_URI);
        }
        return redisURI;
    }

    protected Iterable<RedisURI> getRedisURIs(RedisClusterClient redisClusterClient) {
        return AgentFieldAccessor.getFieldValue(redisClusterClient, REDIS_URIS);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getDataFromDynamicField(Object target) {
        if (target instanceof DynamicFieldAccessor) {
            return (T) ((DynamicFieldAccessor) target).getEaseAgent$$DynamicField$$Data();
        }
        return null;
    }

    protected void setDataToDynamicField(Object target, Object data) {
        if (target instanceof DynamicFieldAccessor) {
            ((DynamicFieldAccessor) target).setEaseAgent$$DynamicField$$Data(data);
        }
    }
}
