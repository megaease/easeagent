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

package com.megaease.easeagent.plugin.jdbc.common;

import com.megaease.easeagent.plugin.utils.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MD5DictionaryItem {
    //global field
    private long timestamp;

    private String category;

    @JsonProperty("host_name")
    private String hostName;

    @JsonProperty("host_ipv4")
    private String hostIpv4;

    private String gid;

    private String service;

    private String system;

    private String type;

    private String tags;

    private String id;

    // self field
    private String md5;

    private String sql;
}
