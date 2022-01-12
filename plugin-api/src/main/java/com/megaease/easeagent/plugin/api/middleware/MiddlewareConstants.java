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

public final class MiddlewareConstants {
    public static final String ENV_REDIS = "EASE_RESOURCE_REDIS";
    public static final String ENV_ES = "EASE_RESOURCE_ELASTICSEARCH";
    public static final String ENV_KAFKA = "EASE_RESOURCE_KAFKA";
    public static final String ENV_RABBITMQ = "EASE_RESOURCE_RABBITMQ";
    public static final String ENV_DATABASE = "EASE_RESOURCE_DATABASE";


    public static final String TYPE_TAG_NAME = "component.type";
    public static final String TYPE_REDIS = "redis";
    public static final String TYPE_ES = "elasticsearch";
    public static final String TYPE_KAFKA = "kafka";
    public static final String TYPE_RABBITMQ = "rabbitmq";
    public static final String TYPE_DATABASE = "database";
}
