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

package com.megaease.easeagent.mock.plugin.api.junit;

import com.megaease.easeagent.mock.metrics.MockMetricUtils;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

public class ResetStatement extends Statement {
    static final Map<Redirect, ResourceConfig> OLD_CONFIG;

    static {
        Map<Redirect, ResourceConfig> oldConfig = new HashMap<>();
        for (Redirect redirect : Redirect.values()) {
            oldConfig.put(redirect, redirect.getConfig());
        }
        OLD_CONFIG = oldConfig;
    }

    private final Statement after;

    public ResetStatement(Statement after) {
        this.after = after;
    }

    @Override
    public void evaluate() throws Throwable {
        cleanAll();
        after.evaluate();
    }

    private void cleanAll() {
        EaseAgent.initializeContextSupplier.get().clear();
        MockMetricUtils.clearAll();
        resetRedirect();
    }

    private void resetRedirect() {
        for (Map.Entry<Redirect, ResourceConfig> entry : OLD_CONFIG.entrySet()) {
            if (entry.getKey().getConfig() == entry.getValue()) {
                continue;
            }
            AgentFieldReflectAccessor.setFieldValue(entry.getKey(), "config", entry.getValue());
        }
    }

}
