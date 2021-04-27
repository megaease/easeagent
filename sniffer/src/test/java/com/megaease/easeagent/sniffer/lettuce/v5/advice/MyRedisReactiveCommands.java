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

package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import io.lettuce.core.AbstractRedisReactiveCommands;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.codec.RedisCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MyRedisReactiveCommands<K, V> extends AbstractRedisReactiveCommands<K, V> {

    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on.
     * @param codec      the codec for command encoding.
     */
    public MyRedisReactiveCommands(StatefulConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    @Override
    public Mono<V> get(Object key) {
        return (Mono<V>) Mono.just("data");
    }

    @Override
    public Flux<KeyValue<K, V>> mget(K... keys) {
        return Flux.just(KeyValue.just((K) "key", (V) "data"), KeyValue.just((K) "123", (V) "data-123"));
    }
}
