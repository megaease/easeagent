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

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

public class KafkaProducerConfigAdvice implements Points {
    public static final String CONFIG_NAME = "org.apache.kafka.clients.producer.ProducerConfig";

    //        return def.type(named("org.apache.kafka.clients.producer.ProducerConfig")
    //                .or(hasSuperType(named("org.apache.kafka.clients.producer.ProducerConfig")))
    //            )
    //            .transform(objConstruct(isConstructor())
    //            )
    //            .end()
    //            ;
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
