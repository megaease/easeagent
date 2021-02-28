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

public enum MetricSubType {
    DEFAULT("00"),
    ERROR("01"),
    CHANNEL("02"), //for rabbitmq
    MQ_CONSUMER("03"), //for messaging kafka/rabbitmq consumer
    MQ_PRODUCER("04"), //for messaging kafka/rabbitmq producer
    MQ_CONSUMER_ERROR("05"), //for messaging kafka/rabbitmq consumer error
    MQ_PRODUCER_ERROR("06"), //for messaging kafka/rabbitmq producer error
    NONE("99");

    private final String code;

    public String getCode() {
        return code;
    }

    MetricSubType(String s) {
        this.code = s;
    }

    public static MetricSubType valueFor(String code) {
        MetricSubType[] values = MetricSubType.values();
        for (MetricSubType value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("code " + code + " is invalid");
    }

}
