/*
 * Copyright (c) 2021, MegaEase
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

package easeagent.plugin.spring353.gateway.advice;

import com.megaease.easeagent.plugin.CodeVersion;
import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import easeagent.plugin.spring353.gateway.GatewayCons;

import java.util.Set;

public class AgentGlobalFilterAdvice implements Points {
    @Override
    public CodeVersion codeVersions() {
        return GatewayCons.VERSIONS;
    }

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasClassName("easeagent.plugin.spring.gateway.interceptor.initialize.AgentGlobalFilter")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.builder()
            .named("filter")
            .build().toSet();
    }
}
