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

package com.megaease.easeagent.plugin.redis.interceptor;

import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.protocol.RedisCommand;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

public class RedisClientUtils {
    private static final String REDIS_URI = "redisURI";
    private static final String REDIS_URIS = "initialUris";

    public static RedisURI getRedisURI(RedisClient redisClient, Object[] args) {
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

    public static Iterable<RedisURI> getRedisURIs(RedisClusterClient redisClusterClient) {
        return AgentFieldReflectAccessor.getFieldValue(redisClusterClient, REDIS_URIS);
    }

    public static String toURI(RedisURI redisURI) {
        try {
            return URLDecoder.decode(redisURI.toURI().toString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return redisURI.toURI().toString();
        }
    }

    public static String cmd(Object arg0) {
        String cmd;
        if (arg0 instanceof RedisCommand) {
            RedisCommand<?, ?, ?> redisCommand = (RedisCommand<?, ?, ?>) arg0;
            cmd = redisCommand.getType().name();
        } else if (arg0 instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<RedisCommand<?, ?, ?>> redisCommands = (Collection<RedisCommand<?, ?, ?>>) arg0;
            cmd = "[" + String.join(",", redisCommands.stream().map(input -> input.getType().name()).collect(Collectors.toList())) + "]";
        } else {
            cmd = null;
        }
        return cmd;
    }
}
