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

package com.megaease.easeagent.plugin.redis.interceptor.metric;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.tools.metrics.RedisMetric;

public abstract class CommonRedisMetricInterceptor implements NonReentrantInterceptor {
    private static volatile RedisMetric REDIS_METRIC = null;
    private static final Object ENTER = new Object();
    private static final Object START = new Object();

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        Tags tags = new Tags("application", "cache-redis", "signature");
        RedirectProcessor.setTagsIfRedirected(Redirect.REDIS, tags);
        REDIS_METRIC = ServiceMetricRegistry.getOrCreate(config, tags, RedisMetric.REDIS_METRIC_SUPPLIER);
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        context.put(START, System.currentTimeMillis());
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        String key = this.getKey(methodInfo, context);
        long start = context.remove(START);
        REDIS_METRIC.collect(key, System.currentTimeMillis() - start, methodInfo.isSuccess());
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public Object getEnterKey(MethodInfo methodInfo, Context context) {
        return ENTER;
    }

    public abstract String getKey(MethodInfo methodInfo, Context context);

}
