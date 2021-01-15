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

 package com.megaease.easeagent.requests;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

interface TracedRequestEvent {
    @JSONField(name = "@timestamp", format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    Date getTimestamp();

    String getId();

    String getName();

    String getType();

    String getMethod();

    String getUrl();

    @JSONField(serializeUsing = ContextSerializer.class)
    Context getCallStackJson();

    boolean getContainsCallTree();

    boolean getError();

    int getStatusCode();

    long getExecutionCountDb();

    long getExecutionTimeDb();

    long getExecutionTime();

    long getExecutionTimeCpu();

    String getHostipv4();

    String getHostname();

    String getSystem();

    String getApplication();

}