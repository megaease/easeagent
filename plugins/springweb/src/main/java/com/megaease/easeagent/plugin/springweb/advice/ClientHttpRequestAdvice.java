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

package com.megaease.easeagent.plugin.springweb.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientHttpRequestAdvice implements Points {
    Set<String> VERSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DEFAULT_VERSION, "spring-boot_2_x")));


    @Override
    public Set<String> codeVersions() {
        return VERSIONS;
    }

    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder().hasInterface("org.springframework.http.client.ClientHttpRequest").notInterface()
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("execute")
                .returnType("org.springframework.http.client.ClientHttpResponse")
                .qualifier("default")
                .build())
            .build();
    }
}
