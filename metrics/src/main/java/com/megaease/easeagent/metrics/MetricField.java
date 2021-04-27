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

package com.megaease.easeagent.metrics;


/**
 * MetricField describes a metric value attributes including what's metric name , what's metric type, and what's metrics precision.
 */
public enum MetricField {

    MIN_EXECUTION_TIME("min", ConverterType.DURATION, 2),
    MAX_EXECUTION_TIME("max", ConverterType.DURATION, 2),
    MEAN_EXECUTION_TIME("mean", ConverterType.DURATION, 2),
    P25_EXECUTION_TIME("p25", ConverterType.DURATION, 2),
    P50_EXECUTION_TIME("p50", ConverterType.DURATION, 2),
    P75_EXECUTION_TIME("p75", ConverterType.DURATION, 2),
    P95_EXECUTION_TIME("p95", ConverterType.DURATION, 2),
    P98_EXECUTION_TIME("p98", ConverterType.DURATION, 2),
    P99_EXECUTION_TIME("p99", ConverterType.DURATION, 2),
    P999_EXECUTION_TIME("p999", ConverterType.DURATION, 2),
    STD("std"),
    EXECUTION_COUNT("cnt"),
    EXECUTION_ERROR_COUNT("errcnt"),
    M1_RATE("m1", ConverterType.RATE, 5),
    M5_RATE("m5", ConverterType.RATE, 5),
    M15_RATE("m15", ConverterType.RATE, 5),
    RETRY_M1_RATE("retrym1", ConverterType.RATE, 5),
    RETRY_M5_RATE("retrym5", ConverterType.RATE, 5),
    RETRY_M15_RATE("retrym15", ConverterType.RATE, 5),
    RATELIMITER_M1_RATE("rlm1", ConverterType.RATE, 5),
    RATELIMITER_M5_RATE("rlm5", ConverterType.RATE, 5),
    RATELIMITER_M15_RATE("rlm15", ConverterType.RATE, 5),
    CIRCUITBREAKER_M1_RATE("cbm1", ConverterType.RATE, 5),
    CIRCUITBREAKER_M5_RATE("cbm5", ConverterType.RATE, 5),
    CIRCUITBREAKER_M15_RATE("cbm15", ConverterType.RATE, 5),
    MEAN_RATE("mean_rate", ConverterType.RATE, 5),
    M1_ERROR_RATE("m1err", ConverterType.RATE, 5),
    M5_ERROR_RATE("m5err", ConverterType.RATE, 5),
    M15_ERROR_RATE("m15err", ConverterType.RATE, 5),
    M1_COUNT("m1cnt", ConverterType.RATE, 0),
    M5_COUNT("m5cnt", ConverterType.RATE, 0),
    M15_COUNT("m15cnt", ConverterType.RATE, 0),
    TIMES_RATE("time_rate", ConverterType.RATE, 5),
    TOTAL_COLLECTION_TIME("total_collection_time", ConverterType.RATE, 0),
    TIMES("times", ConverterType.RATE, 0),
    /* channel is for rabbitmq */
    CHANNEL_M1_RATE("channel_m1_rate", ConverterType.RATE, 5),
    CHANNEL_M5_RATE("channel_m5_rate", ConverterType.RATE, 5),
    CHANNEL_M15_RATE("channel_m15_rate", ConverterType.RATE, 5),
    QUEUE_M1_RATE("queue_m1_rate", ConverterType.RATE, 5),
    QUEUE_M5_RATE("queue_m5_rate", ConverterType.RATE, 5),
    QUEUE_M15_RATE("queue_m15_rate", ConverterType.RATE, 5),
    QUEUE_M1_ERROR_RATE("queue_m1_error_rate", ConverterType.RATE, 5),
    QUEUE_M5_ERROR_RATE("queue_m5_error_rate", ConverterType.RATE, 5),
    QUEUE_M15_ERROR_RATE("queue_m15_error_rate", ConverterType.RATE, 5),
    /*producer and consumer is for message kafka rabbitmq service*/
    PRODUCER_M1_RATE("prodrm1", ConverterType.RATE, 5),
    PRODUCER_M5_RATE("prodrm5", ConverterType.RATE, 5),
    PRODUCER_M15_RATE("prodrm15", ConverterType.RATE, 5),
    PRODUCER_M1_ERROR_RATE("prodrm1err", ConverterType.RATE, 5),
    PRODUCER_M5_ERROR_RATE("prodrm5err", ConverterType.RATE, 5),
    PRODUCER_M15_ERROR_RATE("prodrm15err", ConverterType.RATE, 5),
    CONSUMER_M1_RATE("consrm1", ConverterType.RATE, 5),
    CONSUMER_M5_RATE("consrm5", ConverterType.RATE, 5),
    CONSUMER_M15_RATE("consrm15", ConverterType.RATE, 5),
    CONSUMER_M1_ERROR_RATE("consrm1err", ConverterType.RATE, 5),
    CONSUMER_M5_ERROR_RATE("consrm5err", ConverterType.RATE, 5),
    CONSUMER_M15_ERROR_RATE("consrm15err", ConverterType.RATE, 5),
    EXECUTION_PRODUCER_ERROR_COUNT("prodrerrcnt"),
    EXECUTION_CONSUMER_ERROR_COUNT("consrerrcnt"),
    EXECUTION_PRODUCER_COUNT("prodrcnt"),
    EXECUTION_CONSUMER_COUNT("consrcnt"),
    PRODUCER_MIN_EXECUTION_TIME("prodrmin", ConverterType.DURATION, 2),
    PRODUCER_MAX_EXECUTION_TIME("prodrmax", ConverterType.DURATION, 2),
    PRODUCER_MEAN_EXECUTION_TIME("prodrmean", ConverterType.DURATION, 2),
    PRODUCER_P25_EXECUTION_TIME("prodrp25", ConverterType.DURATION, 2),
    PRODUCER_P50_EXECUTION_TIME("prodrp50", ConverterType.DURATION, 2),
    PRODUCER_P75_EXECUTION_TIME("prodrp75", ConverterType.DURATION, 2),
    PRODUCER_P95_EXECUTION_TIME("prodrp95", ConverterType.DURATION, 2),
    PRODUCER_P98_EXECUTION_TIME("prodrp98", ConverterType.DURATION, 2),
    PRODUCER_P99_EXECUTION_TIME("prodrp99", ConverterType.DURATION, 2),
    PRODUCER_P999_EXECUTION_TIME("prodrp999", ConverterType.DURATION, 2),
    CONSUMER_MIN_EXECUTION_TIME("consrmin", ConverterType.DURATION, 2),
    CONSUMER_MAX_EXECUTION_TIME("consrmax", ConverterType.DURATION, 2),
    CONSUMER_MEAN_EXECUTION_TIME("consrmean", ConverterType.DURATION, 2),
    CONSUMER_P25_EXECUTION_TIME("consrp25", ConverterType.DURATION, 2),
    CONSUMER_P50_EXECUTION_TIME("consrp50", ConverterType.DURATION, 2),
    CONSUMER_P75_EXECUTION_TIME("consrp75", ConverterType.DURATION, 2),
    CONSUMER_P95_EXECUTION_TIME("consrp95", ConverterType.DURATION, 2),
    CONSUMER_P98_EXECUTION_TIME("consrp98", ConverterType.DURATION, 2),
    CONSUMER_P99_EXECUTION_TIME("consrp99", ConverterType.DURATION, 2),
    CONSUMER_P999_EXECUTION_TIME("consrp999", ConverterType.DURATION, 2),
    NONE("", ConverterType.RATE, 0);


    private final String field;
    private final ConverterType type;
    private final int scale;

    MetricField(String field, ConverterType type, int scale) {
        this.field = field;
        this.type = type;
        this.scale = scale;
    }

    MetricField(String field) {
        this(field, ConverterType.NONE, 0);
    }

    public String getField() {
        return field;
    }

    public ConverterType getType() {
        return type;
    }

    public int getScale() {
        return scale;
    }
}
