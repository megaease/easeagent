package com.megaease.easeagent.sniffer.jedis.v3;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class JedisAdviceTest extends BaseSnifferTest {

    static List<Class<?>> classList;

    static AgentInterceptorChainInvoker chainInvoker;
    static AgentInterceptorChain.Builder builder4Jedis;

    @Before
    public void before() {
        if (classList == null) {
            builder4Jedis = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
            chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
            Supplier<AgentInterceptorChain.Builder> supplier = () -> builder4Jedis;
            Definition.Default def = new GenJedisAdvice().define(Definition.Default.EMPTY);
            ClassLoader loader = this.getClass().getClassLoader();
            classList = Classes.transform(this.getClass().getName() + "$MyJedis")
                    .with(def,
                            new QualifiedBean("supplier4Jedis", supplier),
                            new QualifiedBean("", chainInvoker)
                    )
                    .load(loader);
        }
    }

    @Test
    public void invokeSuccess() throws Exception {
        MyJedis myJedis = (MyJedis) classList.get(0).newInstance();
        myJedis.get("key");
        this.verifyInvokeTimes(chainInvoker, 1);

        myJedis.getTest("key");
        this.verifyInvokeTimes(chainInvoker, 1);

        myJedis.get(new byte[0]);
        this.verifyInvokeTimes(chainInvoker, 2);

    }

    static class MyJedis extends Jedis {

        public String getTest(String key) {
            return "ok";
        }

        @Override
        public String get(String key) {
            return "ok";
        }

        @Override
        public byte[] get(byte[] key) {
            return new byte[0];
        }
    }

}
