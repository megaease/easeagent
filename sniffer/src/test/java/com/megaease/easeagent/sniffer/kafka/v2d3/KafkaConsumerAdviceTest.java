package com.megaease.easeagent.sniffer.kafka.v2d3;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.*;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.megaease.easeagent.sniffer.kafka.v2d3.advice.GenKafkaConsumerAdvice;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

public class KafkaConsumerAdviceTest extends BaseSnifferTest {

    static List<Class<?>> classList;
    AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

    @Before
    public void before() {
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenKafkaConsumerAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                this.getClass().getName() + "$MyConsumer"
        )
                .with(def, new QualifiedBean("", chainInvoker),
                        new QualifiedBean("supplier4KafkaConsumerConstructor", (Supplier<AgentInterceptorChain.Builder>) () -> new DefaultAgentInterceptorChain.Builder().addInterceptor(new MockAgentInterceptor())),
                        new QualifiedBean("supplier4KafkaConsumerDoPoll", (Supplier<AgentInterceptorChain.Builder>) () -> new DefaultAgentInterceptorChain.Builder().addInterceptor(new MockAgentInterceptor()))
                )
                .load(loader);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void invoke() throws Exception {
        MyConsumer<String, String> consumer = (MyConsumer<String, String>) classList.get(0).newInstance();
        reset(chainInvoker);
        ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
        Assert.assertNotNull(consumerRecords);
        this.verifyInvokeTimes(this.chainInvoker, 1);
    }

    static class MyConsumer<K, V> extends MockConsumer<K, V> {

        public MyConsumer() {
            this(OffsetResetStrategy.EARLIEST);
        }

        public MyConsumer(OffsetResetStrategy offsetResetStrategy) {
            super(offsetResetStrategy);
        }

        @Override
        public synchronized ConsumerRecords<K, V> poll(Duration timeout) {
            Map<TopicPartition, List<ConsumerRecord<K, V>>> map = new HashMap<>();
            return new ConsumerRecords<>(map);
        }

    }
}
