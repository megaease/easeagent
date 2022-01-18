/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.api.trace;

public interface SpanContext {

    /**
     * When true, do nothing anything and nothing is reported . However, this Tracing should
     * still be injected into outgoing requests. Use this flag to avoid performing expensive
     * computation.
     *
     * @return boolean
     */
    boolean isNoop();

    /**
     * Returns the underlying Span Context object or {@code null} if there is none. Here is a span Context
     * objects: {@code brave.propagation.TraceContext}.
     * @return
     */
    Object unwrap();
}
