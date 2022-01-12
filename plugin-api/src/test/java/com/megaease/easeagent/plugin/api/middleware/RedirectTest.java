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

package com.megaease.easeagent.plugin.api.middleware;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedirectTest {


    @Test
    public void getEnv() {
        assertEquals(MiddlewareConstants.ENV_REDIS, Redirect.REDIS.getEnv());
        assertEquals(MiddlewareConstants.ENV_ES, Redirect.ELASTICSEARCH.getEnv());
        assertEquals(MiddlewareConstants.ENV_KAFKA, Redirect.KAFKA.getEnv());
        assertEquals(MiddlewareConstants.ENV_RABBITMQ, Redirect.RABBITMQ.getEnv());
        assertEquals(MiddlewareConstants.ENV_DATABASE, Redirect.DATABASE.getEnv());
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
        assertEquals("jdbc:mysql://localhost:3306/db_demo?useUnicode=true", Redirect.DATABASE.getConfig().getUris());
    }

}
