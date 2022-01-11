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

public enum Redirect {
    REDIS(Const.ENV_REDIS, true),
    ELASTICSEARCH(Const.ENV_ES, true),
    KAFKA(Const.ENV_KAFKA, true),
    RABBITMQ(Const.ENV_RABBITMQ, true),
    DATABASE(Const.ENV_DATABASE, false);

    private final String env;
    private final ResourceConfig config;

    Redirect(String env, boolean needParse) {
        this.env = env;
        this.config = ResourceConfig.getResourceConfig(env, needParse);
    }

    public String getEnv() {
        return env;
    }

    public boolean hasConfig() {
        return config != null;
    }

    public ResourceConfig getConfig() {
        return config;
    }

}
