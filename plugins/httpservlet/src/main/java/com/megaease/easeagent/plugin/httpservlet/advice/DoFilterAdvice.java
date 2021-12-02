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

package com.megaease.easeagent.plugin.httpservlet.advice;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.Set;

public class DoFilterAdvice implements Points {
    private static final String FILTER_NAME = "javax.servlet.Filter";
    private static final String HTTP_SERVLET_NAME = "javax.servlet.http.HttpServlet";
    static final String SERVLET_REQUEST = "javax.servlet.ServletRequest";
    static final String SERVLET_RESPONSE = "javax.servlet.ServletResponse";

    //        return def.type(
    //                hasSuperType(namedOneOf(FILTER_NAME, HTTP_SERVLET_NAME)))
    //                .transform(doFilterOrService(
    //                        namedOneOf("doFilter", "service")
    //                                .and(takesArgument(0, named(SERVLET_REQUEST)))
    //                                .and(takesArgument(1, named(SERVLET_RESPONSE)))
    //                        )
    //                ).end();
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasInterface(FILTER_NAME)
            .build().or(ClassMatcher.builder()
                .hasSuperClass(HTTP_SERVLET_NAME)
                .build());
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
        return MethodMatcher.multiBuilder()
            .match(MethodMatcher.builder().named("doFilter")
                .isPublic()
                .argsLength(3)
                .arg(0, SERVLET_REQUEST)
                .arg(1, SERVLET_RESPONSE)
                .returnType("void")
                .qualifier("default")
                .build())
            .match(MethodMatcher.builder().named("service")
                .arg(0, SERVLET_REQUEST)
                .arg(1, SERVLET_RESPONSE)
                .qualifier("default")
                .build())
            .build();
    }
}
