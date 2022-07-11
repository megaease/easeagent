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

package com.megaease.easeagent.plugin.springweb.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

public class FeignClientAdvice implements Points {

    //return def.type(hasSuperType(named("feign.Client")))
    //                .transform(execute(named("execute").and(takesArguments(2)
    //                        .and(takesArgument(0, named("feign.Request")))
    //                        .and(takesArgument(1, named("feign.Request$Options")))
    //                )))
    //                .end();
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasInterface("feign.Client")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("execute")
                .isPublic()
                .argsLength(2)
                .arg(0, "feign.Request")
                .arg(1, "feign.Request$Options")
                .qualifier("default")
                .build())
            .build();
    }
}
