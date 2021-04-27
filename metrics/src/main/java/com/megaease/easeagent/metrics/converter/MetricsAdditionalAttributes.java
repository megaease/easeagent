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

package com.megaease.easeagent.metrics.converter;

import com.megaease.easeagent.common.AdditionalAttributes;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.config.ConfigUtils;

import java.util.Map;
import java.util.function.Supplier;

public class MetricsAdditionalAttributes implements Supplier<Map<String, Object>> {

    private volatile Map<String, Object> additionalAttributes;

    public MetricsAdditionalAttributes(Config config) {
        ConfigUtils.bindProp(ConfigConst.SERVICE_NAME, config, Config::getString, v -> {
            this.additionalAttributes = new AdditionalAttributes(v).getAdditionalAttributes();
        });
    }

    @Override
    public Map<String, Object> get() {
        return additionalAttributes;
    }
}
