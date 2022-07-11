/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.tools.metrics;

import com.megaease.easeagent.plugin.utils.ImmutableMap;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class LastMinutesCounterGauge implements GaugeMetricModel {
    private final long m1Count;
    private final long m5Count;
    private final long m15Count;
    private final String prefix;

    @Override
    public Map<String, Object> toHashMap() {
        String px = this.prefix == null ? "" : this.prefix;
        return ImmutableMap.<String, Object>builder()
                .put(px + "m1cnt", m1Count)
                .put(px + "m5cnt", m5Count)
                .put(px + "m15cnt", m15Count)
                .build();
    }
}
