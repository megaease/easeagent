package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class RedisClientAdviceTest extends BaseSnifferTest {

    @Test
    public void success() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

        Definition.Default def = new GenRedisClientAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();

        RedisClient redisClient = (RedisClient) Classes.transform(this.getClass().getName() + "$MyRedisClient")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4LettuceRedisClient", builder))
                .load(loader).get(0).newInstance();

        redisClient.connect();

        this.verifyInvokeTimes(chainInvoker, 1);
    }

    static class MyRedisClient extends RedisClient {

        @Override
        public StatefulRedisConnection<String, String> connect() {
            return mock(StatefulRedisConnection.class);
        }
    }
}
