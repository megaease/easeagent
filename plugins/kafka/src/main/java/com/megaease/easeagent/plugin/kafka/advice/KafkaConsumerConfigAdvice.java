package com.megaease.easeagent.plugin.kafka.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

public class KafkaConsumerConfigAdvice implements Points {
    public static final String CONFIG_NAME = "org.apache.kafka.clients.consumer.ConsumerConfig";
    //return def.type(named("org.apache.kafka.clients.consumer.ConsumerConfig")
    //                        .or(hasSuperType(named("org.apache.kafka.clients.consumer.ConsumerConfig")))
    //                )
    //                .transform(objConstruct(isConstructor())
    //                )
    //                .end()
    //                ;

    @Override
    public IClassMatcher getClassMatcher() {
        return name(CONFIG_NAME)
            .or(ClassMatcher.builder().hasSuperClass(CONFIG_NAME).build());
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("<init>")
                .qualifier("constructor")
                .build())
            .build();
    }
}
