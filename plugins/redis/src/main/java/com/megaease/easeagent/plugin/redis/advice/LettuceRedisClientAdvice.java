/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.plugin.redis.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.MethodMatcherUtils.constructor;

public class LettuceRedisClientAdvice implements Points {
    //return def.type(hasSuperType(named("io.lettuce.core.RedisClient"))
    //                .or(named("io.lettuce.core.RedisClient"))
    //        )
    //                .transform(connectAsync((named("connectStandaloneAsync")
    //                                .or(named("connectPubSubAsync"))
    //                                .or(named("connectSentinelAsync"))).and(isPrivate())
    //                        )
    //                )
    //                .transform(objConstruct(isConstructor()))
    //                .end()
    //                ;
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasSuperClass("io.lettuce.core.RedisClient")
            .build().or(ClassMatcher.builder().hasClassName("io.lettuce.core.RedisClient")
                .build());
    }

    private IMethodMatcher named(String name) {
        return MethodMatcher.builder().named(name).isPrivate().build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(named("connectStandaloneAsync")
                .or(named("connectPubSubAsync"))
                .or(named("connectSentinelAsync")))
            .match(constructor())
            .build();
    }
}
