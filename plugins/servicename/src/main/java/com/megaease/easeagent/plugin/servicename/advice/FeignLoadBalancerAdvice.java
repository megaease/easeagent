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

package com.megaease.easeagent.plugin.servicename.advice;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import com.megaease.easeagent.plugin.servicename.Const;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

// OpenFeign
public class FeignLoadBalancerAdvice implements Points {
    @Override
    public CodeVersion codeVersions() {
        return Const.VERSIONS;
    }

    //.type(named(FeignLoadBalancer).or(named(RetryableFeignLoadBalancer)))
    //            .transform(feignLoadBalancerExecute(named("execute")
    //                .and(takesArguments(2))
    //                .and(takesArgument(0, named(FeignLoadBalancer + "$RibbonRequest")))
    //                .and(takesArgument(1, named("com.netflix.client.config.IClientConfig")))
    //            ))
    @Override
    public IClassMatcher getClassMatcher() {
        return name(Const.FeignLoadBalancer).or(name(Const.RetryableFeignLoadBalancer));
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("execute")
                .argsLength(2)
                .arg(0, Const.FeignLoadBalancer + "$RibbonRequest")
                .arg(1, "com.netflix.client.config.IClientConfig")
                .qualifier("servicename")
                .build())
            .build();
    }
}
