/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package com.megaease.easeagent.plugin.elasticsearch.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import java.util.Set;

import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.hasSuperType;
import static com.megaease.easeagent.plugin.tools.matcher.ClassMatcherUtils.name;

public class SpringElasticsearchAdvice implements Points {
    //return def.type(
    //                hasSuperType(named("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
    //                    .or(hasSuperType(named("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties")))
    //                    .or(named("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
    //                    .or(named("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"))
    //            )
    //            .transform(setProperty(nameStartsWith("set")))
    //            .end();
    @Override
    public IClassMatcher getClassMatcher() {
        return name("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties")
            .or(hasSuperType("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"))
            .or(name("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
            .or(name("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"));
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(
                MethodMatcher
                    .builder()
                    .nameStartWith("set")
                    .build()
            )
            .build();
    }
}
