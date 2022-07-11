/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.api.middleware;

import com.megaease.easeagent.plugin.api.MockSystemEnv;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedirectTest {


    @Before
    public void before() {
        MockSystemEnv.set(MiddlewareConstants.ENV_REDIS, "{\"uris\":\"localhost:6379\"}");
        MockSystemEnv.set(MiddlewareConstants.ENV_ES, "{\"uris\":\"127.0.0.1:9200\"}");
        MockSystemEnv.set(MiddlewareConstants.ENV_KAFKA, "{\"uris\":\"127.0.0.1:9092\", \"tags\": {\"label.local\": \"shadow\"}}");
        MockSystemEnv.set(MiddlewareConstants.ENV_RABBITMQ, "{\"uris\":\"localhost:5672\"}");
        MockSystemEnv.set(MiddlewareConstants.ENV_DATABASE, "{\"uris\":\"jdbc:mysql://localhost:3306/db_demo?useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true\"}");
    }

    @Test
    public void hasConfig() {
        assertTrue(Redirect.REDIS.hasConfig());
        assertTrue(Redirect.ELASTICSEARCH.hasConfig());
        assertTrue(Redirect.KAFKA.hasConfig());
        assertTrue(Redirect.RABBITMQ.hasConfig());
        assertTrue(Redirect.DATABASE.hasConfig());
    }

    @Test
    public void getConfig() {
        assertEquals("localhost:6379", Redirect.REDIS.getConfig().getUris());
        assertEquals("127.0.0.1:9200", Redirect.ELASTICSEARCH.getConfig().getUris());
        assertEquals("127.0.0.1:9092", Redirect.KAFKA.getConfig().getUris());
        assertEquals("localhost:5672", Redirect.RABBITMQ.getConfig().getUris());
        assertEquals("jdbc:mysql://localhost:3306/db_demo?useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true", Redirect.DATABASE.getConfig().getUris());
    }

}
