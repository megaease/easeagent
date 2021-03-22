package com.megaease.easeagent.zipkin.redis;


import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class JedisTracingInterceptor extends CommonRedisTracingInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Jedis invoker = (Jedis) methodInfo.getInvoker();
        String name = invoker.getClass().getSimpleName() + "." + methodInfo.getMethod();
        String cmd = methodInfo.getMethod();
        this.startTracing(name, null, cmd, context);
        chain.doBefore(methodInfo, context);
    }
}
