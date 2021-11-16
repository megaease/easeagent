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

public class JedisAdvice implements Points {
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasSuperClass("redis.clients.jedis.BinaryJedis")
            .build();
    }

    private IClassMatcher named(String name) {
        return ClassMatcher.builder().hasClassName(name).isInterface()
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        IClassMatcher overriddenFrom = named("redis.clients.jedis.commands.JedisCommands")
            .or(named("redis.clients.jedis.commands.AdvancedJedisCommands"))
            .or(named("redis.clients.jedis.commands.BasicCommands"))
            .or(named("redis.clients.jedis.commands.ClusterCommands"))
            .or(named("redis.clients.jedis.commands.ModuleCommands"))
            .or(named("redis.clients.jedis.commands.MultiKeyCommands"))
            .or(named("redis.clients.jedis.commands.ScriptingCommands"))
            .or(named("redis.clients.jedis.commands.SentinelCommands"))
            .or(named("redis.clients.jedis.commands.BinaryJedisCommands"))
            .or(named("redis.clients.jedis.commands.MultiKeyBinaryCommands"))
            .or(named("redis.clients.jedis.commands.AdvancedBinaryJedisCommands"))
            .or(named("redis.clients.jedis.commands.BinaryScriptingCommands"));
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("set").isPublic().build())
            .match(MethodMatcher.builder().named("exists").isPublic().build())
            .build();
//        return MethodMatcher.multiBuilder()
//            .match(MethodMatcher.builder().isOverriddenFrom(overriddenFrom).build())
//            .build();
    }
}
