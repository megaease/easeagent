package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import io.lettuce.core.ConnectionFuture;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class RedisClientAdviceTest extends BaseSnifferTest {

    private static List<Class<?>> classList;

    AgentInterceptorChainInvoker chainInvoker;
    AgentInterceptorChain.Builder builder4RedisClientConnectSync;
    AgentInterceptorChain.Builder builder4RedisClientConnectASync;
    AgentInterceptor interceptor4ConnectSync;
    AgentInterceptor interceptor4ConnectASync;

    @Before
    public void before() {
        interceptor4ConnectSync = mock(AgentInterceptor.class);
        interceptor4ConnectASync = mock(AgentInterceptor.class);
        builder4RedisClientConnectSync = new DefaultAgentInterceptorChain.Builder().addInterceptor(interceptor4ConnectSync);
        builder4RedisClientConnectASync = new DefaultAgentInterceptorChain.Builder().addInterceptor(interceptor4ConnectASync);
        chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

        if (classList == null) {
            Definition.Default def = new GenRedisClientAdvice().define(Definition.Default.EMPTY);
            ClassLoader loader = this.getClass().getClassLoader();
            classList = Classes.transform(this.getClass().getName() + "$MyRedisClient")
                    .with(def,
                            new QualifiedBean("builder4RedisClientConnectSync", builder4RedisClientConnectSync),
                            new QualifiedBean("builder4RedisClientConnectASync", builder4RedisClientConnectASync),
                            new QualifiedBean("", chainInvoker)
                    )
                    .load(loader);
        }
    }

    @Test
    public void connect() throws Exception {
        MyRedisClient myRedisClient = (MyRedisClient) classList.get(0).newInstance();

        myRedisClient.connect();
        this.verifyInterceptorTimes(interceptor4ConnectSync, 1, false);

        myRedisClient.connectAsync(mock(RedisCodec.class), mock(RedisURI.class));
        this.verifyInterceptorTimes(interceptor4ConnectASync, 1, false);

    }

    static class MyRedisClient extends RedisClient {

        @Override
        public StatefulRedisConnection<String, String> connect() {
            return mock(StatefulRedisConnection.class);
        }

        @Override
        public <K, V> ConnectionFuture<StatefulRedisConnection<K, V>> connectAsync(RedisCodec<K, V> codec, RedisURI redisURI) {
            return mock(ConnectionFuture.class);
        }
    }

}
