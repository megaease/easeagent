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

import com.megaease.easeagent.plugin.report.EncodedData;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.EaseAgentResource;

import java.util.Map;

public interface AgentLogData extends LogData {
    /**
     * get logger thread name
     * @return thread name
     */
    String getThreadName();

    /**
     * get logger name
     * @return logger name
     */
    String getLocation();

    /**
     * get unix timestamp in milliseconds
     * @return timestamp
     */
    long getEpochMillis();

    /**
     * get agent resource - system/service
     * @return agent resource
     */
    EaseAgentResource getAgentResource();

    /**
     * complete attributes
     */
    void completeAttributes();

    /**
     * return pattern map
     * @return pattern map
     */
    Map<String, String> getPatternMap();

    /**
     * return throwable/Exception
     * @return throwbale
     */
    Throwable getThrowable();

    /**
     * return encoded data
     * @return encoded data
     */
    EncodedData getEncodedData();

    void setEncodedData(EncodedData data);
}
