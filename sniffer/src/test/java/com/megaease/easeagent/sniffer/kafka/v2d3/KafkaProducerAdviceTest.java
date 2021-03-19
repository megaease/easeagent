package com.megaease.easeagent.sniffer.kafka.v2d3;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.megaease.easeagent.sniffer.kafka.v2d3.advice.GenKafkaProducerAdvice;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

public class KafkaProducerAdviceTest extends BaseSnifferTest {
    static List<Class<?>> classList;
    static AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
    static AgentInterceptorChain.BuilderFactory builderFactory = mock(DefaultAgentInterceptorChain.BuilderFactory.class);

    @Before
    public void before() {
        this.initBuilderFactory(builderFactory);
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenKafkaProducerAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                this.getClass().getName() + "$MyKafkaProducer"
        )
                .with(def, new QualifiedBean("", chainInvoker),
                        new QualifiedBean("", builderFactory),
                        new QualifiedBean("", this.tracing()),
                        new QualifiedBean("", new MetricRegistry())
                )
                .load(loader);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void invoke() throws Exception {
        MyKafkaProducer<String, String> producer = (MyKafkaProducer<String, String>) classList.get(0)
                .newInstance();
        reset(chainInvoker);
        producer.doSend(mock(ProducerRecord.class), (metadata, exception) -> System.out.println(metadata));
        this.verifyInvokeTimes(chainInvoker, 1);
    }

    static class MyKafkaProducer<K, V> extends MockProducer<K, V> {

        private Future<V> doSend(@SuppressWarnings("unused") ProducerRecord<K, V> record,
                                 @SuppressWarnings("unused") Callback callback) {
            return null;
        }
    }

}
