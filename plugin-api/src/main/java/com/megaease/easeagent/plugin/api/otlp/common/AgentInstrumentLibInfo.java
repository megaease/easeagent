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
package com.megaease.easeagent.plugin.api.otlp.common;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

import java.util.concurrent.ConcurrentHashMap;

public class AgentInstrumentLibInfo {
    static ConcurrentHashMap<String, InstrumentationLibraryInfo> infoMap = new ConcurrentHashMap<>();

    public static InstrumentationLibraryInfo getInfo(String loggerName) {
        InstrumentationLibraryInfo info = infoMap.get(loggerName);
        if (info != null) {
            return info;
        }
        info = InstrumentationLibraryInfo.create(loggerName, null);
        infoMap.putIfAbsent(loggerName, info);
        return info;
    }
}
