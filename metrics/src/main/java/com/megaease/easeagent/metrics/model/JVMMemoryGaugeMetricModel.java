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

package com.megaease.easeagent.metrics.model;

import com.megaease.easeagent.plugin.tools.metrics.GaugeMetricModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class JVMMemoryGaugeMetricModel implements GaugeMetricModel {
    private Long bytesInit;
    private Long bytesUsed;
    private Long bytesCommitted;
    private Long bytesMax;

    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("bytes-init", bytesInit);
        result.put("bytes-used", bytesUsed);
        result.put("bytes-committed", bytesCommitted);
        result.put("bytes-max", bytesMax);
        return result;
    }
}
