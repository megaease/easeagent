package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class RedisClusterClientAdviceTest extends BaseSnifferTest {

    static List<Class<?>> classList;

    static AgentInterceptorChainInvoker chainInvoker;
    static AgentInterceptor interceptor4ConnectASync;

    @Before
    public void before() {
        if (classList == null) {
            interceptor4ConnectASync = mock(AgentInterceptor.class);
            chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
            Definition.Default def = new GenRedisClusterClientAdvice().define(Definition.Default.EMPTY);
            ClassLoader loader = this.getClass().getClassLoader();
            classList = Classes.transform(this.getClass().getName() + "$MyRedisClient")
                    .with(def,
                            new QualifiedBean("supplier4RedisClusterConnectAsync", mockSupplier()),
                            new QualifiedBean("", chainInvoker)
                    )
                    .load(loader);
        }
    }

    @Test
    public void connect() throws Exception {
        MyRedisClient myRedisClient = (MyRedisClient) classList.get(0).newInstance();
        myRedisClient.connectClusterAsync(StringCodec.UTF8);
        this.verifyInvokeTimes(chainInvoker, 1);
    }

    static class MyRedisClient extends RedisClusterClient {

//        public <K, V> ConnectionFuture<StatefulRedisConnection<K, V>> _connectClusterAsync() throws ExecutionException, InterruptedException {
//            ConnectionFuture<StatefulRedisConnection<K, V>> connectionFuture = mock(ConnectionFuture.class);
//            when(connectionFuture.get()).thenReturn(mock(StatefulRedisConnection.class));
//            return connectionFuture;
//        }

        private  <K, V> CompletableFuture<StatefulRedisClusterConnection<K, V>> connectClusterAsync(RedisCodec<K, V> codec) {
            return mock(CompletableFuture.class);
        }
    }

}
