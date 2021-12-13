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

package com.megaease.easeagent.plugin.servicename;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfig;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import static com.megaease.easeagent.plugin.servicename.Const.DEFAULT_PROPAGATE_HEAD;
import static com.megaease.easeagent.plugin.servicename.Const.PROPAGATE_HEAD_CONFIG;

public class ServiceNameConfig implements AutoRefreshConfig {
    public static final AutoRefreshConfigSupplier<ServiceNameConfig> SUPPLIER = ServiceNameConfig::new;

    private volatile String propagateHead = DEFAULT_PROPAGATE_HEAD;

    public String getPropagateHead() {
        return propagateHead;
    }

    @Override
    public void onChange(Config oldConfig, Config newConfig) {
        String propagateHead = newConfig.getString(PROPAGATE_HEAD_CONFIG);
        if (StringUtils.isEmpty(propagateHead) || StringUtils.isEmpty(propagateHead.trim())) {
            return;
        }
        this.propagateHead = propagateHead.trim();
    }
}
