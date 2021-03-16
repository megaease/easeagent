package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.Tracing;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class RedisCommandsAdviceTest extends BaseSnifferTest {

    @Test
    public void success() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

        Definition.Default def = new GenRedisCommandsAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        List<Class<?>> classList = Classes.transform(
                "com.megaease.easeagent.sniffer.lettuce.v5.advice.MyRedisReactiveCommands"
        )
                .with(def,
                        new QualifiedBean("builder4LettuceDoCommand", builder),
                        new QualifiedBean("", chainInvoker))
                .load(loader);

//        RedisCommands<String, String> redisCommands = (RedisCommands<String, String>) classList.get(0).newInstance();
//        redisCommands.get("key");
//        this.verifyInvokeTimes(chainInvoker, 1);

        StatefulRedisConnection<String, String> connection = mock(StatefulRedisConnection.class);
        ClientResources clientResources = mock(ClientResources.class);
        when(connection.getResources()).thenReturn(clientResources);
        when(clientResources.tracing()).thenReturn(mock(Tracing.class));

        MyRedisReactiveCommands<String, String> reactiveCommands = (MyRedisReactiveCommands<String, String>) classList.get(0).getConstructor(StatefulConnection.class, RedisCodec.class).newInstance(connection, StringCodec.UTF8);
        reactiveCommands.get("key");
        reactiveCommands.mget("key", "123");

        this.verifyInvokeTimes(chainInvoker, 2);
    }

}
