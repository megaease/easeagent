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

import com.megaease.easeagent.plugin.bridge.NoOpTracer;

public interface Tracing {

    /**
     * When true, do anything and nothing is reported . However, this Tracing should
     * still be injected into outgoing requests. Use this flag to avoid performing expensive
     * computation.
     *
     * @return boolean
     */
    boolean isNoop();

    /**
     * Returns the current span in scope or {@link NoOpTracer#NO_OP_SPAN} if there isn't one.
     *
     * <p> as it is a stable type and will never return null.
     *
     * @return {@link Span}
     */
    Span currentSpan();

    /**
     * Returns a new child span if there's a {@link #currentSpan()} or a new trace if there isn't.
     *
     * @return {@link Span}
     */
    Span nextSpan();

    /**
     * This creates a new span based on parameters message from an incoming request. This will
     * always result in a new span. If no trace identifiers were incoming, a span will be created
     * based on the implicit context in the same manner as {@link #nextSpan()}. If a sampling decision
     * has not yet been made, one will happen here.
     *
     * <p>Ex.
     * <pre>{@code
     * message = extractor.extract(request);
     * span = tracer.nextSpan(message);
     * }</pre>
     *
     * @param message {@link Message}
     * @return {@link Span}
     * @see Extractor#extract(MessagingRequest)
     */
    Span nextSpan(Message message);

    /**
     * build a MessagingTracing for message tracing
     *
     * @return {@link MessagingRequest}
     */
    MessagingTracing<? extends Request> messagingTracing();
}
