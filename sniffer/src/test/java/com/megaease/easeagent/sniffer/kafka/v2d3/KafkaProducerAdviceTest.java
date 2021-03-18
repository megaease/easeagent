package com.megaease.easeagent.sniffer.kafka.v2d3;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.megaease.easeagent.sniffer.kafka.v2d3.advice.GenKafkaProducerAdvice;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaDoSendInterceptor;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class KafkaProducerAdviceTest extends BaseSnifferTest {
    static List<Class<?>> classList;
    AgentInterceptor interceptor = mock(AgentInterceptor.class);
    AgentInterceptorChain.Builder builder4Logic = new DefaultAgentInterceptorChain.Builder()
            .addInterceptor(interceptor);
    AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
    AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
            .addInterceptor(new KafkaDoSendInterceptor(builder4Logic, chainInvoker));

    @Before
    public void before() {
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenKafkaProducerAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                this.getClass().getName() + "$MyKafkaProducer"
        )
                .with(def,
                        new QualifiedBean("builder4KafkaDoSend", builder),
                        new QualifiedBean("", chainInvoker))
                .load(loader);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void sendSuccess() throws Exception {
        MyKafkaProducer<String, String> producer = (MyKafkaProducer<String, String>) classList.get(0)
                .newInstance();

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
