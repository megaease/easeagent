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

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.megaease.easeagent.plugin.kafka.interceptor.AsyncCallback;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class MetricCallback extends AsyncCallback {
    private final long start;
    private final KafkaMetric kafkaMetric;

    public MetricCallback(Callback delegate, KafkaMetric kafkaMetric) {
        super(delegate, isAsync(delegate));
        this.kafkaMetric = kafkaMetric;
        this.start = System.currentTimeMillis();
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        try {
            this.kafkaMetric.producerStop(start, metadata.topic());
            if (exception != null) {
                this.kafkaMetric.errorProducer(metadata.topic());
            }
        } finally {
            if (this.delegate != null) {
                this.delegate.onCompletion(metadata, exception);
            }
        }
    }
}
