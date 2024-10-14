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

import com.megaease.easeagent.plugin.api.Context;

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
    Extractor<R> producerExtractor();

    /**
     * @return {@link Extractor}
     */
    Extractor<R> consumerExtractor();

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
     * Obtain key:value from the message request and create a Span, Examples: kafka consumer, rabbitmq consumer
     * <p>
     * It will set the Span's kind, name and cached scope through {@link Request#kind()}, {@link Request#name()}
     * and {@link Request#cacheScope()}.
     *
     * <p>
     * It will set the Span's tags "messaging.operation", "messaging.channel_kind", "messaging.channel_name" from request
     * {@link MessagingRequest#operation()} {@link MessagingRequest#channelKind()} {@link MessagingRequest#channelName()}
     *
     * <p>
     * It just only obtain the key:value required by Trace from the {@link Request#header(String)},
     * If you need and get Span, generate result use {@link Context#consumerSpan(MessagingRequest)}.
     *
     * @param request {@link MessagingRequest}
     * @return {@link Span}
     * @see Context#consumerSpan(MessagingRequest)
     */
    Span consumerSpan(R request);


    /**
     * Create a Span for message producer. Examples: kafka producer, rabbitmq producer
     * <p>
     * It will set the Span's tags "messaging.operation", "messaging.channel_kind", "messaging.channel_name" from request
     * {@link MessagingRequest#operation()} {@link MessagingRequest#channelKind()} {@link MessagingRequest#channelName()}
     *
     * <p>
     * It just only pass multiple key:value values required by Trace through
     * {@link Request#setHeader(String, String)}, And set the Span's kind, name and
     * cached scope through {@link Request#kind()}, {@link Request#name()} and {@link Request#cacheScope()}.
     * If you need and get Span, generate result use {@link Context#producerSpan(MessagingRequest)}.
     *
     * @param request {@link MessagingRequest}
     * @return {@link Span}
     * @see Context#producerSpan(MessagingRequest)
     */
    Span producerSpan(R request);
}
