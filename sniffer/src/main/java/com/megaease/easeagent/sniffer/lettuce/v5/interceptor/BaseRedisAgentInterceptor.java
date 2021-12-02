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

package com.megaease.easeagent.sniffer.lettuce.v5.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

abstract class BaseRedisAgentInterceptor implements AgentInterceptor {
    private static final String REDIS_URI = "redisURI";
    private static final String REDIS_URIS = "initialUris";

    protected RedisURI getRedisURI(RedisClient redisClient, Object[] args) {
        if (args == null) {
            return AgentFieldReflectAccessor.getFieldValue(redisClient, REDIS_URI);
        }
        RedisURI redisURI = null;
        for (Object arg : args) {
            if (arg instanceof RedisURI) {
                redisURI = (RedisURI) arg;
                break;
            }
        }
        if (redisURI == null) {
            redisURI = AgentFieldReflectAccessor.getFieldValue(redisClient, REDIS_URI);
        }
        return redisURI;
    }

    protected Iterable<RedisURI> getRedisURIs(RedisClusterClient redisClusterClient) {
        return AgentFieldReflectAccessor.getFieldValue(redisClusterClient, REDIS_URIS);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getDataFromDynamicField(Object target) {
        return AgentDynamicFieldAccessor.getDynamicFieldValue(target);
    }

    protected void setDataToDynamicField(Object target, Object data) {
        AgentDynamicFieldAccessor.setDynamicFieldValue(target, data);
    }
}
