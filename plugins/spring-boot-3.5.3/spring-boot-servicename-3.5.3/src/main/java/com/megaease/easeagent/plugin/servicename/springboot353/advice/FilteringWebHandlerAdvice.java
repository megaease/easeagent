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

package com.megaease.easeagent.plugin.servicename.springboot353.advice;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import com.megaease.easeagent.plugin.servicename.springboot353.Const;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

// Spring Cloud Gateway
public class FilteringWebHandlerAdvice implements Points {//FilteringWebHandlerInterceptor
    //            .type(named(FilteringWebHandler))
    //            .transform(filteringWebHandlerHandle(named("handle").and(takesArguments(1))
    //                .and(takesArgument(0, named("org.springframework.web.server.ServerWebExchange")))
    //            ))

    @Override
    public CodeVersion codeVersions() {
        return Const.VERSIONS;
    }


    @Override
    public IClassMatcher getClassMatcher() {
        return name(Const.FilteringWebHandler);
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("handle")
                .argsLength(1)
                .arg(0, "org.springframework.web.server.ServerWebExchange")
                .qualifier("servicename")
                .build())
            .build();
    }
}
