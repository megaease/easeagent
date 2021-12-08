/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.jdbc.common;

import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.utils.common.HostAddress;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;

import java.util.Map;
import java.util.function.Consumer;

public class MD5ReportConsumer  implements Consumer<Map<String, String>> {
    private final Config config;
    private static Reporter reporter;

    public MD5ReportConsumer(Config config) {
        this.config = config;
        this.reporter = EaseAgent.metricReporter(config);
    }

    @Override
    public void accept(Map<String, String> map) {
        if (!this.config.enabled()) {
            return;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            MD5DictionaryItem item = MD5DictionaryItem.builder()
                .timestamp(System.currentTimeMillis())
                .category("application")
                .hostName(HostAddress.localhost())
                .hostIpv4(HostAddress.getHostIpv4())
                .gid("")
                .system(EaseAgent.configFactory.getConfig("system"))
                .service(EaseAgent.configFactory.getConfig("name"))
                .tags("")
                .type("md5-dictionary")
                .id("")
                .md5(entry.getKey())
                .sql(entry.getValue())
                .build();
            String json = JsonUtil.toJson(item);
            this.reporter.report(json);
        }
    }
}
