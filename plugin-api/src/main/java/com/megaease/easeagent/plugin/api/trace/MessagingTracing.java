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

import java.util.function.Function;

/**
 * a MessagingTracing
 *
 * @param <R>
 */
public interface MessagingTracing<R extends MessagingRequest> {

    /**
     * @return {@link Extractor}
     */
    Extractor<R> extractor();

    /**
     * @return {@link Injector}
     */
    Injector<R> producerInjector();

    /**
     * @return {@link Injector}
     */
    Injector<R> consumerInjector();

    /**
     * Returns an overriding sampling decision for a new trace. Defaults to ignore the request and use
     * the {@link #consumerSampler()}  trace ID instead}.
     *
     * <p>This decision happens when trace IDs were not in headers, or a sampling decision has not
     * yet been made. For example, if a trace is already in progress, this function is not called. You
     * can implement this to skip channels that you never want to trace.
     */
    Function<R, Boolean> consumerSampler();

    /**
     * Returns an overriding sampling decision for a new trace. Defaults to ignore the request and use
     * the {@link #producerSampler()}  trace ID instead}.
     *
     * <p>This decision happens when a trace was not yet started in process. For example, you may be
     * making an messaging request as a part of booting your application. You may want to opt-out of
     * tracing producer requests that did not originate from a consumer request.
     */
    Function<R, Boolean> producerSampler();

    /**
     * Returns an overriding sampling decision for a new trace.
     *
     * @param request parameter to evaluate for a sampling decision. null input results in a null result
     * @return true to sample a new trace or false to deny. Null defers the decision.
     */
    boolean consumerSampler(R request);

    /**
     * @param request
     * @return
     * @see #consumerSampler()
     */
    boolean producerSampler(R request);
}
