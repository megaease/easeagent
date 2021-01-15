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

 package com.megaease.easeagent.common;

import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Transformation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class HttpServletService implements Transformation {

    static final String HTTP_SERVLET = "javax.servlet.http.HttpServlet";
    static final String HTTP_SERVLET_REQUEST = "javax.servlet.http.HttpServletRequest";
    static final String HTTP_SERVLET_RESPONSE = "javax.servlet.http.HttpServletResponse";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named(HTTP_SERVLET)))
                  .transform(service(
                          takesArguments(2)
                                  .and(takesArgument(0, named(HTTP_SERVLET_REQUEST)))
                                  .and(takesArgument(1, named(HTTP_SERVLET_RESPONSE))))
                  ).end();

    }

    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

}
