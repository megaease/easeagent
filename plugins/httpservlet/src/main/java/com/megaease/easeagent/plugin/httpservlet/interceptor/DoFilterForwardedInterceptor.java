/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.httpservlet.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.httpservlet.ForwardedPlugin;
import com.megaease.easeagent.plugin.httpservlet.advice.DoFilterAdvice;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.tools.trace.HttpRequest;

import javax.servlet.http.HttpServletRequest;

@AdviceTo(value = DoFilterAdvice.class, qualifier = "default", plugin = ForwardedPlugin.class)
public class DoFilterForwardedInterceptor implements NonReentrantInterceptor {
    private static final Object FORWARDED_KEY = new Object();

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpRequest httpRequest = new HttpServerRequest(httpServletRequest);
        Scope scope = context.importForwardedHeaders(httpRequest);
        context.put(FORWARDED_KEY, scope);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Scope scope = context.remove(FORWARDED_KEY);
        if (scope != null) {
            scope.close();
        }
    }

    @Override
    public String getType() {
        return ConfigConst.PluginID.FORWARDED;
    }
}

