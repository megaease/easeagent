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

package com.megaease.easeagent.report.util;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.MetricProps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class Utils {
    private Utils() {}

    public static boolean isOutputPropertiesChange(Map<String, String> changes) {
        List<String> relatedNames = Arrays.asList(
            OUTPUT_SERVER_V2
        );
        return changes.keySet().stream().anyMatch(relatedNames::contains);
    }

    public static OutputProperties extractOutputProperties(Config configs) {
        return OutputProperties.newDefault(configs);
    }

    public static MetricProps extractMetricProps(IPluginConfig config, Config reportConfig) {
        return MetricProps.newDefault(config, reportConfig);
    }
}
