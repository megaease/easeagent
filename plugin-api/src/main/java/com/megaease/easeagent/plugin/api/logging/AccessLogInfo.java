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

package com.megaease.easeagent.plugin.api.logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.megaease.easeagent.plugin.report.EncodedData;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class AccessLogInfo {
    public static final TypeReference<AccessLogInfo> TYPE_REFERENCE = new TypeReference<AccessLogInfo>() {
    };

    @JsonProperty("span_id")
    protected String spanId;

    @JsonProperty("trace_id")
    protected String traceId;

    @JsonProperty("pspan_id")
    protected String parentSpanId;

    private String type = "access-log";

    private String service;

    private String system;

    @JsonProperty("client_ip")
    private String clientIP = "-";

    private String user = "-";

    @JsonProperty("response_size")
    private int responseSize;

    private long beginTime;

    private long beginCpuTime;

    @JsonProperty("request_time")
    private long requestTime;

    private long cpuElapsedTime;

    private String url;

    private String method;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("host_name")
    private String hostName;

    @JsonProperty("host_ipv4")
    private String hostIpv4;

    private String category = "application";

    @JsonProperty("match_url")
    private String matchUrl;

    private Map<String, String> headers;

    private Map<String, String> queries;

    private long timestamp;

    @JsonIgnore
    private EncodedData encodedData;
}
