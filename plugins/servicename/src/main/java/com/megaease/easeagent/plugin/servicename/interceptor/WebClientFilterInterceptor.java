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

package com.megaease.easeagent.plugin.servicename.interceptor;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.servicename.ReflectionTool;
import com.megaease.easeagent.plugin.servicename.advice.WebClientFilterAdvice;
import org.springframework.util.MultiValueMap;

import java.net.URI;

@AdviceTo(value = WebClientFilterAdvice.class, qualifier = "servicename")
public class WebClientFilterInterceptor  extends BaseServiceNameInterceptor  {
    private static final Logger LOGGER = EaseAgent.getLogger(WebClientFilterInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        String method = methodInfo.getMethod();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("enter method [{}]", method);
            }
            Object request = methodInfo.getArgs()[0];
            URI uri = (URI) ReflectionTool.invokeMethod(request, "url");
            String host = uri.getHost();
            if (ReflectionTool.hasText(host)) {
                Object fakeHeaders = ReflectionTool.invokeMethod(request, "headers");//org.springframework.http.HttpHeaders
                @SuppressWarnings("unchecked")
                MultiValueMap<String, String> headers = (MultiValueMap<String, String>) ReflectionTool.extractField(fakeHeaders, "headers");
                headers.add(config.getPropagateHead(), host);
                context.injectForwardedHeaders(headers::add);
            }
        } catch (Throwable e) {
            LOGGER.warn("intercept method [{}] failure", method, e);
        }
    }
}
