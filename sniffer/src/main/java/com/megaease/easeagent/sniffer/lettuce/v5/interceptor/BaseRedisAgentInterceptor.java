package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

abstract class BaseRedisAgentInterceptor implements AgentInterceptor {
    private static final String REDIS_URI = "redisURI";

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

    protected RedisURI getRedisURIFromDynamicField(Object target) {
        if (target instanceof DynamicFieldAccessor) {
            return (RedisURI) ((DynamicFieldAccessor) target).getEaseAgent$$DynamicField$$Data();
        }
        return null;
    }

    protected void setRedisURIToDynamicField(Object target, RedisURI redisURI) {
        if (target instanceof DynamicFieldAccessor) {
            ((DynamicFieldAccessor) target).setEaseAgent$$DynamicField$$Data(redisURI);
        }
    }

    protected Object getDataFromDynamicField(Object target) {
        if (target instanceof DynamicFieldAccessor) {
            return ((DynamicFieldAccessor) target).getEaseAgent$$DynamicField$$Data();
        }
        return null;
    }

    protected void setDataToDynamicField(Object target, Object data) {
        if (target instanceof DynamicFieldAccessor) {
            ((DynamicFieldAccessor) target).setEaseAgent$$DynamicField$$Data(data);
        }
    }
}
