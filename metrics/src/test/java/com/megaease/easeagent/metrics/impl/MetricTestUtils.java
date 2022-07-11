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

package com.megaease.easeagent.metrics.impl;

import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MetricTestUtils {
    public static void mockField(Object target, String fieldName, Object fieldValue, Runnable r) {
        Object oldField = AgentFieldReflectAccessor.getFieldValue(target, fieldName);
        AgentFieldReflectAccessor.setFieldValue(target, fieldName, fieldValue);
        try {
            r.run();
        } finally {
            AgentFieldReflectAccessor.setFieldValue(target, fieldName, oldField);
        }
    }

    public static void nextWindow(com.codahale.metrics.Meter meter) {
        Object movingAverages = AgentFieldReflectAccessor.getFieldValue(meter, "movingAverages");
        AtomicLong lastTick = AgentFieldReflectAccessor.getFieldValue(movingAverages, "lastTick");
        lastTick.addAndGet(TimeUnit.SECONDS.toNanos(5) * -1);
    }
}
