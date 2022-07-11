/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.megaease.easeagent.plugin.kafka.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class KafkaConsumerAdvice implements Points {
    //return def.type(named("org.apache.kafka.clients.consumer.KafkaConsumer")
    //                .or(hasSuperType(named("org.apache.kafka.clients.consumer.MockConsumer")))
    //        )
    //                .transform(objConstruct(isConstructor().and(takesArguments(3))
    //                                .and(takesArgument(0, named("org.apache.kafka.clients.consumer.ConsumerConfig")))
    //                        , AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
    //                .transform(doPoll((named("poll")
    //                                .and(takesArguments(1)))
    //                                .and(takesArgument(0, named("java.time.Duration")))
    //                        )
    //                )
    //                .end()
    //                ;
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasClassName("org.apache.kafka.clients.consumer.KafkaConsumer")
            .build().or(ClassMatcher.builder().hasSuperClass("org.apache.kafka.clients.consumer.MockConsumer")
                .build());

    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("<init>")
                .argsLength(3)
                .arg(0, "org.apache.kafka.clients.consumer.ConsumerConfig")
                .qualifier("constructor")
                .build())
            .match(MethodMatcher.builder().named("poll")
                .argsLength(1)
                .arg(0, "java.time.Duration")
                .qualifier("poll")
                .build())
            .build();
    }

    @Override
    public boolean isAddDynamicField() {
        return true;
    }
}
