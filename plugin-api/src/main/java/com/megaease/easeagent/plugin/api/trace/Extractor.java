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

/**
 * Used to continue an incoming trace. For example, by reading http headers.
 *
 * <p><em>Note</em>: This type is safe to implement as a lambda, or use as a method reference as
 * it is effectively a {@code FunctionalInterface}. It isn't annotated as such because the project
 * has a minimum Java language level 6.
 *
 * @see Tracing#nextSpan(Message)
 */
public interface Extractor<R extends MessagingRequest> {
    /**
     * Returns either a trace context or sampling flags parsed from the request. If nothing was
     * parsable, sampling flags will be set to {@link com.megaease.easeagent.plugin.bridge.NoOpTracer.EmptyExtractor#INSTANCE}.
     *
     * @param request holds propagation fields. For example, an incoming message or http request.
     */

    Message extract(R request);
}
