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
 * Interface request type used for parsing and sampling of messaging producers and consumers.
 */
public interface MessagingRequest extends Request {

    /**
     * The unqualified, case-sensitive semantic message operation name. The currently defined names
     * are "send" and "receive".
     *
     * <p>Examples:
     * <pre><ul>
     *   <li>Amazon SQS - {@code AmazonSQS.sendMessageBatch()} is a "send" operation</li>
     *   <li>JMS - {@code MessageProducer.send()} is a "send" operation</li>
     *   <li>Kafka - {@code Consumer.poll()} is a "receive" operation</li>
     *   <li>RabbitMQ - {@code Consumer.handleDelivery()} is a "receive" operation</li>
     * </ul></pre>
     *
     * <p>Note: There is no constant set of operations, yet. Even when there is a constant set, there
     * may be operations such as "browse" or "purge" which aren't defined. Once implementation
     * matures, a constant file will be defined, with potentially more names.
     *
     * <p>Conventionally associated with the tag "messaging.operation"
     *
     * @return the messaging operation or null if unreadable.
     */
    String operation();

    /**
     * Type of channel, e.g. "queue" or "topic". {@code null} if unreadable.
     *
     * <p>Conventionally associated with the tag "messaging.channel_kind"
     *
     * @see #channelName()
     */
    // Naming matches conventions for Span
    String channelKind();

    /**
     * Messaging channel name, e.g. "hooks" or "complaints". {@code null} if unreadable.
     *
     * <p>Conventionally associated with the tag "messaging.channel_name"
     *
     * @see #channelKind()
     */
    String channelName();

    /**
     * Returns the underlying request object or {@code null} if there is none. Here are some request
     * objects: {@code org.apache.http.HttpRequest}, {@code org.apache.dubbo.rpc.Invocation}, {@code
     * org.apache.kafka.clients.consumer.ConsumerRecord}.
     *
     * <p>Note: Some implementations are composed of multiple types, such as a request and a socket
     * address of the client. Moreover, an implementation may change the type returned due to
     * refactoring. Unless you control the implementation, cast carefully (ex using {@code
     * instanceof}) instead of presuming a specific type will always be returned.
     */
    Object unwrap();
}
