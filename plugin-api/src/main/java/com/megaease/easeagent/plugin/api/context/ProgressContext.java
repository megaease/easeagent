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

package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Response;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;

import java.util.Map;

/**
 * A cross-process data context, including tracing and penetration fields
 */
public interface ProgressContext {
    /**
     * When true, do nothing and nothing is reported . However, this ProgressContext should
     * still be injected into outgoing requests. Use this flag to avoid performing expensive
     * computation.
     */
    boolean isNoop();

    /**
     * @return {@link Span} for next progress client span
     */
    Span span();

    /**
     * @return {@link Scope} for current Span
     */
    Scope scope();

    /**
     * set header for next progress
     *
     * @param name  of header
     * @param value of header
     */
    void setHeader(String name, String value);

    /**
     * @return headers from the progress data context
     */
    Map<String, String> getHeaders();

    /**
     * Convert ProgressContext into AsyncContext and return
     *
     * @return {@link AsyncContext} for async
     */
    AsyncContext async();

    /**
     * @return current {@link Context} for session
     */
    Context getContext();

    /**
     * finish the progress span and save tag from {@link Response#header(String)}
     *
     * @param response {@link Response}
     */
    void finish(Response response);
}
