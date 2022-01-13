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

package com.megaease.easeagent.plugin.elasticsearch.interceptor.redirect;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.elasticsearch.ElasticsearchRedirectPlugin;
import com.megaease.easeagent.plugin.elasticsearch.advice.SpringElasticsearchAdvice;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import java.util.ArrayList;
import java.util.List;

@AdviceTo(value = SpringElasticsearchAdvice.class, plugin = ElasticsearchRedirectPlugin.class)
public class SpringElasticsearchInterceptor implements NonReentrantInterceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(SpringElasticsearchInterceptor.class);

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.ELASTICSEARCH.getConfig();
        if (cnf == null) {
            return;
        }
        String method = methodInfo.getMethod();
        List<String> uris = this.formatUris(cnf.getUriList());
        if (method.equals("setUsername") && StringUtils.isNotEmpty(cnf.getUserName())) {
            LOGGER.info("Redirect Elasticsearch Username: {} to {}", methodInfo.getArgs()[0], cnf.getUserName());
            methodInfo.changeArg(0, cnf.getUserName());
        } else if (method.equals("setPassword") && StringUtils.isNotEmpty(cnf.getPassword())) {
            LOGGER.info("Redirect Elasticsearch Password: *** to ***");
            methodInfo.changeArg(0, cnf.getPassword());
        } else if (method.equals("setEndpoints") || method.equals("setUris")) {
            LOGGER.info("Redirect Elasticsearch uris: {} to {}", methodInfo.getArgs()[0], cnf.getUris());
            methodInfo.changeArg(0, uris);
            RedirectProcessor.redirected(Redirect.ELASTICSEARCH, cnf.getUris());
        }

    }

    private List<String> formatUris(List<String> uriList) {
        List<String> list = new ArrayList<>();
        for (String uri : uriList) {
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                list.add(uri);
            } else {
                list.add("http://" + uri);
            }
        }
        return list;
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }

}
