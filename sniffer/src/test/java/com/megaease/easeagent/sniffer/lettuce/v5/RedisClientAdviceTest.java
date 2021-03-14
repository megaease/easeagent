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
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class RedisClientAdviceTest extends BaseSnifferTest {

//    private static List<Class<?>> classList;
//
//    AgentInterceptorChain.Builder builder4RedisClientCreate;
//    AgentInterceptorChain.Builder builder4RedisClientConnectSync;
//    AgentInterceptorChainInvoker chainInvoker;
//    AgentInterceptor interceptor4Create;
//    AgentInterceptor interceptor4ConnectSync;
//
//    @Before
//    public void before() {
//        interceptor4Create = mock(AgentInterceptor.class);
//        interceptor4ConnectSync = mock(AgentInterceptor.class);
//        builder4RedisClientCreate = new DefaultAgentInterceptorChain.Builder().addInterceptor(interceptor4Create);
//        builder4RedisClientConnectSync = new DefaultAgentInterceptorChain.Builder().addInterceptor(interceptor4ConnectSync);
//        chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//
//        if (classList == null) {
//            Definition.Default def = new GenRedisClientAdvice().define(Definition.Default.EMPTY);
//            ClassLoader loader = this.getClass().getClassLoader();
//            classList = Classes.transform("io.lettuce.core.RedisClient")
//                    .with(def, new QualifiedBean("builder4RedisClientCreate", builder4RedisClientCreate),
//                            new QualifiedBean("builder4RedisClientConnectSync", builder4RedisClientConnectSync),
//                            new QualifiedBean("", chainInvoker)
//                    )
//                    .load(loader);
//        }
//    }
//
//    @Test
//    public void createSuccess() throws Exception {
//        RedisClient.create();
//        this.verifyInterceptorTimes(interceptor4Create, 1, false);
//
//    }

}
