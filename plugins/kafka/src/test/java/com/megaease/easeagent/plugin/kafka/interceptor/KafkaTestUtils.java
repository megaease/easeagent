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

package com.megaease.easeagent.plugin.kafka.interceptor;

import com.megaease.easeagent.mock.utils.MockSystemEnv;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

public class KafkaTestUtils {
    public static void mockRedirect(Runnable r) {
        ResourceConfig oldConfig = Redirect.KAFKA.getConfig();
        try {
            MockSystemEnv.set(MiddlewareConstants.ENV_KAFKA, String.format("{\"uris\":\"%s\"}", TestConst.REDIRECT_URIS));
            AgentFieldReflectAccessor.setFieldValue(Redirect.KAFKA, "config", ResourceConfig.getResourceConfig(Redirect.KAFKA.getEnv(), Redirect.KAFKA.isNeedParse()));
            r.run();
        } finally {
            AgentFieldReflectAccessor.setFieldValue(Redirect.KAFKA, "config", oldConfig);
        }
    }
}
