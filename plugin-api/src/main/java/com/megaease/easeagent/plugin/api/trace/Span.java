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

package com.megaease.easeagent.plugin.api.trace;

import javax.annotation.Nullable;

public interface Span {
    enum Kind {
        CLIENT,
        SERVER,
        PRODUCER,
        CONSUMER
    }

    Span name(String name);

    Span tag(String key, String value);

    Span annotate(String value);

    boolean isNoop();

    Span start();

    Span start(long timestamp);

    Span kind(@Nullable Kind kind);

    Span annotate(long timestamp, String value);

    Span error(Throwable throwable);

    Span remoteServiceName(String remoteServiceName);

    boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort);

    void abandon();

    void finish();

    void finish(long timestamp);

    void flush();

    void inject(Request request);

    Scope maybeScope();

    Span cacheScope();

    String traceIdString();

    String spanIdString();

    String parentIdString();
}
