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

package com.megaease.easeagent.mock.report;

import com.megaease.easeagent.plugin.api.trace.Span;

import java.util.Map;

public interface MockSpan {
    Span.Kind kind();

    String traceId();

    String spanId();

    String parentId();

    String tag(String key);

    Map<String,String> tags();

    String remoteServiceName();

    String annotationValueAt(int i);

    long timestamp();

    Long duration();

    int annotationCount();

    int remotePort();

    int localPort();

    String remoteIp();

    String localIp();

    String name();

    String localServiceName();

    Boolean shared();

    int tagCount();

    boolean hasError();

    String errorInfo();
}
