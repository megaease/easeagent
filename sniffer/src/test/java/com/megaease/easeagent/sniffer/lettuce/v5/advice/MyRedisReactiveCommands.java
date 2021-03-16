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
