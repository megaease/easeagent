package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

abstract class BaseAgentInterceptor implements AgentInterceptor {
    protected RedisURI getRedisURI(RedisClient redisClient, Object[] args) {
        RedisURI redisURI = null;
        for (Object arg : args) {
            if (arg instanceof RedisURI) {
                redisURI = (RedisURI) arg;
                break;
            }
        }
        if (redisURI == null) {
            redisURI = AgentFieldAccessor.getFieldValue(redisClient, "redisURI");
        }
        return redisURI;
    }
}
