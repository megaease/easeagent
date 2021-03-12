package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.sniffer.BaseSnifferTest;

public class AbstractRedisClientAdviceTest extends BaseSnifferTest {

//    @Test
//    public void success() throws Exception {
//        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
//        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//
//        Definition.Default def = new GenAbstractRedisClientAdvice().define(Definition.Default.EMPTY);
//        ClassLoader loader = this.getClass().getClassLoader();
//
//        ClientResources clientResources = mock(ClientResources.class);
//
//        Class<?> aClass = Classes.transform("io.lettuce.core.RedisClient")
//                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4LettuceRedisClient", builder))
//                .load(loader).get(0);
//
//        this.verifyInvokeTimes(chainInvoker, 1);
//    }
//
//    static class MyRedisClient extends AbstractRedisClient {
//
//        /**
//         * Create a new instance with client resources.
//         *
//         * @param clientResources the client resources. If {@code null}, the client will create a new dedicated instance of client
//         *                        resources and keep track of them.
//         */
//        public MyRedisClient(ClientResources clientResources) {
//            super(clientResources);
//        }
//    }
}
