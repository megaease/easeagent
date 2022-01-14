/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.mongodb.points;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

/**
 * Brave-instrumentation-mongodb usage: https://github.com/openzipkin/brave/tree/master/instrumentation/mongodb
 * <pre>
 * CommandListener listener = MongoDBTracing.create(Tracing.current())
 *         .commandListener();
 * MongoClientSettings settings = MongoClientSettings.builder()
 *         .addCommandListener(listener)
 *         .build();
 * MongoClient client = MongoClients.create(settings);
 *     </pre>
 */
public class MongoDBClientConstructPoints implements Points {
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasClassName("com.mongodb.client.internal.MongoClientImpl")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.builder().isConstruct().build().toSet();
    }
}
