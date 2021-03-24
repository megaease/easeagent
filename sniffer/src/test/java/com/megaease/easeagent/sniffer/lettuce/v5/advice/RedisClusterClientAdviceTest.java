package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.sniffer.BaseSnifferTest;

public class RedisClusterClientAdviceTest extends BaseSnifferTest {

//    static List<Class<?>> classList;
//
//    static AgentInterceptorChainInvoker chainInvoker;
//    static AgentInterceptorChain.Builder builder4RedisClientConnectAsync;
//    static AgentInterceptor interceptor4ConnectASync;
//
//    @Before
//    public void before() {
//        if (classList == null) {
//            interceptor4ConnectASync = mock(AgentInterceptor.class);
//            builder4RedisClientConnectAsync = new DefaultAgentInterceptorChain.Builder().addInterceptor(interceptor4ConnectASync);
//            chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//            Definition.Default def = new GenRedisClusterClientAdvice().define(Definition.Default.EMPTY);
//            ClassLoader loader = this.getClass().getClassLoader();
//            classList = Classes.transform(this.getClass().getName() + "$MyRedisClusterClient")
//                    .with(def,
//                            new QualifiedBean("builder4RedisClientConnectAsync", builder4RedisClientConnectAsync),
//                            new QualifiedBean("", chainInvoker)
//                    )
//                    .load(loader);
//        }
//    }
//
//    @Test
//    public void connect() throws Exception {
//        MyRedisClusterClient myRedisClient = (MyRedisClusterClient) classList.get(0).newInstance();
//        myRedisClient.connectClusterAsync();
//        this.verifyInterceptorTimes(interceptor4ConnectASync, 1, false);
//
//    }
//
//    static class MyRedisClusterClient extends RedisClusterClient {
//
//        private CompletableFuture<StatefulRedisClusterConnection> connectClusterAsync() throws ExecutionException, InterruptedException {
//            CompletableFuture<StatefulRedisClusterConnection> future = mock(CompletableFuture.class);
//            when(future.get()).thenReturn(mock(StatefulRedisClusterConnection.class));
//            return future;
//        }
//
//    }

}
