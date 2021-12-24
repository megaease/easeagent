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

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.MetricProps;
import com.megaease.easeagent.report.trace.TraceProps;

import java.util.Arrays;
import java.util.List;

public class Utils {
    public static boolean isOutputPropertiesChange(List<ChangeItem> list) {
        List<String> relatedNames = Arrays.asList(ConfigConst.Observability.OUTPUT_ENABLED
            , ConfigConst.Observability.OUTPUT_SERVERS
            , ConfigConst.Observability.OUTPUT_TIMEOUT
            , ConfigConst.Observability.OUTPUT_CERT
            , ConfigConst.Observability.OUTPUT_KEY
            , ConfigConst.Observability.OUTPUT_SECURITY_PROTOCOL
            , ConfigConst.Observability.OUTPUT_SSL_KEYSTORE_TYPE
            , ConfigConst.Observability.OUTPUT_TRUST_CERT
            , ConfigConst.Observability.OUTPUT_TRUST_CERT_TYPE
            , ConfigConst.Observability.OUTPUT_ENDPOINT_IDENTIFICATION_ALGORITHM
        );
        boolean hasChange = list.stream().map(ChangeItem::getFullName)
            .anyMatch(relatedNames::contains);
        return hasChange;
    }

    public static boolean isTraceOutputPropertiesChange(List<ChangeItem> list) {
        boolean hasChange = list.stream().map(ChangeItem::getFullName)
            .anyMatch(name -> name.startsWith(ConfigConst.Observability.TRACE_OUTPUT + ConfigConst.DELIMITER));
        return hasChange;
    }

    public static OutputProperties extractOutputProperties(Configs configs) {
        return OutputProperties.newDefault(configs);
    }

    public static TraceProps extractTraceProps(Configs configs) {
        return TraceProps.newDefault(configs);
    }

    public static MetricProps extractMetricProps(Configs configs, String key) {
        return MetricProps.newDefault(configs, key);
    }

    public static MetricProps extractMetricProps(Config config) {
        return MetricProps.newDefault(config);
    }


}
