package com.megaease.easeagent.sniffer.kafka.v2d3.advice;

import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;

@Injection.Provider(Provider.class)
public abstract class KafkaConsumerRecordAdvice implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(named("org.apache.kafka.clients.consumer.ConsumerRecord"))
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);

    static class ObjConstruct extends AbstractAdvice {

        public ObjConstruct() {
            super(null, null);
        }

        @Advice.OnMethodExit
        public void exit() {
        }
    }
}
