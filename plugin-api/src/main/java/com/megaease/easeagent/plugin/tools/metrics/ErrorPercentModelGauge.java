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

package com.megaease.easeagent.plugin.tools.metrics;

import com.megaease.easeagent.plugin.utils.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;

public class ErrorPercentModelGauge implements GaugeMetricModel {
    private BigDecimal m1ErrorPercent;
    private BigDecimal m5ErrorPercent;
    private BigDecimal m15ErrorPercent;

    public ErrorPercentModelGauge(BigDecimal m1ErrorPercent, BigDecimal m5ErrorPercent, BigDecimal m15ErrorPercent) {
        this.m1ErrorPercent = m1ErrorPercent;
        this.m5ErrorPercent = m5ErrorPercent;
        this.m15ErrorPercent = m15ErrorPercent;
    }

    @Override
    public Map<String, Object> toHashMap() {
        return ImmutableMap.<String, Object>builder()
            .put("m1errpct", m1ErrorPercent)
            .put("m5errpct", m5ErrorPercent)
            .put("m15errpct", m15ErrorPercent)
            .build();
    }
}
