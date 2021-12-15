///*
// * Copyright (c) 2017, MegaEase
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.megaease.easeagent.sniffer.kafka.v2d3;
//
//import com.megaease.easeagent.core.Classes;
//import com.megaease.easeagent.core.Definition;
//import com.megaease.easeagent.core.QualifiedBean;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
//import com.megaease.easeagent.sniffer.BaseSnifferTest;
//import com.megaease.easeagent.sniffer.kafka.v2d3.advice.GenKafkaProducerAdvice;
//import org.apache.kafka.clients.producer.Callback;
//import org.apache.kafka.clients.producer.MockProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//import java.util.concurrent.Future;
//
//import static org.mockito.Mockito.*;
//
//public class KafkaProducerAdviceTest extends BaseSnifferTest {
//    static List<Class<?>> classList;
//    static AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());
//
//    @Before
//    public void before() {
//        if (classList != null) {
//            return;
//        }
//        Definition.Default def = new GenKafkaProducerAdvice().define(Definition.Default.EMPTY);
//        ClassLoader loader = this.getClass().getClassLoader();
//        classList = Classes.transform(
//                this.getClass().getName() + "$MyKafkaProducer"
//        )
//                .with(def, new QualifiedBean("", chainInvoker),
//                        new QualifiedBean("supplier4KafkaProducerConstructor", mockSupplier()),
//                        new QualifiedBean("supplier4KafkaProducerDoSend", mockSupplier())
//                )
//                .load(loader);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void invoke() throws Exception {
//        MyKafkaProducer<String, String> producer = (MyKafkaProducer<String, String>) classList.get(0)
//                .newInstance();
//        reset(chainInvoker);
//        producer.doSend(mock(ProducerRecord.class), (metadata, exception) -> System.out.println(metadata));
//        this.verifyInvokeTimes(chainInvoker, 1);
//    }
//
//    static class MyKafkaProducer<K, V> extends MockProducer<K, V> {
//
//        private Future<V> doSend(@SuppressWarnings("unused") ProducerRecord<K, V> record,
//                                 @SuppressWarnings("unused") Callback callback) {
//            return null;
//        }
//    }
//
//}
