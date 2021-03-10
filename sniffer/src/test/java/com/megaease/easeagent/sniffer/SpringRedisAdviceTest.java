package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import org.junit.Test;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SpringRedisAdviceTest extends BaseSnifferTest {

    @Test
    public void success() throws Exception {
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder().addInterceptor(mock(AgentInterceptor.class));
        AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

        Definition.Default def = new GenSpringRedisAdvice().define(Definition.Default.EMPTY);
        String baseName = this.getClass().getName();
        ClassLoader loader = this.getClass().getClassLoader();
        MyRedisStringCommands commands = (MyRedisStringCommands) Classes.transform(baseName + "$MyRedisStringCommands")
                .with(def, new QualifiedBean("", chainInvoker), new QualifiedBean("agentInterceptorChainBuilder4SpringRedis", builder))
                .load(loader).get(0).newInstance();

        commands.test(new byte[]{0});

        this.verifyInvokeTimes(chainInvoker, 0);

    }


    static class MyRedisStringCommands implements RedisStringCommands {

        public byte[] test(byte[] key) {
            return new byte[0];
        }

        @Override
        public byte[] get(byte[] key) {
            return new byte[0];
        }

        @Override
        public byte[] getSet(byte[] key, byte[] value) {
            return new byte[0];
        }

        @Override
        public List<byte[]> mGet(byte[]... keys) {
            return null;
        }

        @Override
        public Boolean set(byte[] key, byte[] value) {
            return null;
        }

        @Override
        public Boolean set(byte[] key, byte[] value, Expiration expiration, SetOption option) {
            return null;
        }

        @Override
        public Boolean setNX(byte[] key, byte[] value) {
            return null;
        }

        @Override
        public Boolean setEx(byte[] key, long seconds, byte[] value) {
            return null;
        }

        @Override
        public Boolean pSetEx(byte[] key, long milliseconds, byte[] value) {
            return null;
        }

        @Override
        public Boolean mSet(Map<byte[], byte[]> tuple) {
            return null;
        }

        @Override
        public Boolean mSetNX(Map<byte[], byte[]> tuple) {
            return null;
        }

        @Override
        public Long incr(byte[] key) {
            return null;
        }

        @Override
        public Long incrBy(byte[] key, long value) {
            return null;
        }

        @Override
        public Double incrBy(byte[] key, double value) {
            return null;
        }

        @Override
        public Long decr(byte[] key) {
            return null;
        }

        @Override
        public Long decrBy(byte[] key, long value) {
            return null;
        }

        @Override
        public Long append(byte[] key, byte[] value) {
            return null;
        }

        @Override
        public byte[] getRange(byte[] key, long start, long end) {
            return new byte[0];
        }

        @Override
        public void setRange(byte[] key, byte[] value, long offset) {

        }

        @Override
        public Boolean getBit(byte[] key, long offset) {
            return null;
        }

        @Override
        public Boolean setBit(byte[] key, long offset, boolean value) {
            return null;
        }

        @Override
        public Long bitCount(byte[] key) {
            return null;
        }

        @Override
        public Long bitCount(byte[] key, long start, long end) {
            return null;
        }

        @Override
        public List<Long> bitField(byte[] key, BitFieldSubCommands subCommands) {
            return null;
        }

        @Override
        public Long bitOp(BitOperation op, byte[] destination, byte[]... keys) {
            return null;
        }

        @Override
        public Long bitPos(byte[] key, boolean bit, Range<Long> range) {
            return null;
        }

        @Override
        public Long strLen(byte[] key) {
            return null;
        }
    }
}
