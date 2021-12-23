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
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;

import java.util.List;

/**
 * Subtype of {@link Tracing} which can exportAsync,importAsync,clientRequest and serverReceive.
 *
 * <p>This type can be extended so that the object graph can be built differently or overridden,
 * for example via zipkin or when mocking.
 */
public interface ITracing extends Tracing {
    /**
     * Export a {@link AsyncContext} for async
     * It will only export the information about the current Span.
     * If you need AsyncContext, generate result use {@link Context#exportAsync()}.
     *
     * @return {@link AsyncContext}
     * @see Context#exportAsync()
     */
    AsyncContext exportAsync();

    /**
     * Import a {@link AsyncContext} for async
     * It will only import the information about the async TraceContext.
     * If you need import AsyncContext and get Scope, generate result use {@link Context#importAsync(AsyncContext)}.
     *
     * @param snapshot {@link AsyncContext}
     * @return {@link Scope}
     * @see Context#importAsync(AsyncContext)
     */
    Scope importAsync(AsyncContext snapshot);


    /**
     * Create a RequestContext for Cross-server Trace link
     * <p>
     * It just only pass multiple key:value values required by Trace through
     * {@link Request#setHeader(String, String)}, And set the Span's kind, name and
     * cached scope through {@link Request#kind()}, {@link Request#name()} and {@link Request#cacheScope()}.
     * If you need Cross-process and get RequestContext, generate result use {@link Context#clientRequest(Request)}.
     *
     * @param request {@link Request}
     * @return {@link RequestContext}
     * @see Context#clientRequest(Request)
     */
    RequestContext nextServer(Request request);

    /**
     * Obtain key:value from the request passed by a parent server and create a RequestContext
     * <p>
     * It will set the Span's kind, name and cached scope through {@link Request#kind()}, {@link Request#name()}
     * and {@link Request#cacheScope()}.
     * <p>
     * It just only obtain the key:value required by Trace from the {@link Request#header(String)},
     * If you need and get RequestContext, generate result use {@link Context#serverReceive(Request)} }.
     * <p>
     *
     * @param request {@link Request}
     * @return {@link RequestContext}
     * @see Context#serverReceive(Request)
     */
    RequestContext serverImport(Request request);

    /**
     * @return the keys necessary for Span
     */
    List<String> propagationKeys();

    /**
     * Obtain key:value from the message request and create a Span, Examples: kafka consumer, rebbitmq consumer
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
    Span consumerSpan(MessagingRequest request);


    /**
     * Create a Span for message producer. Examples: kafka producer, rebbitmq producer
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
    Span producerSpan(MessagingRequest request);

}
