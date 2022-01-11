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

package com.megaease.easeagent.plugin.kafka.interceptor;

import com.megaease.easeagent.plugin.api.Context;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KafkaUtils {
    public static String getUri(Object bootstrapServers) {
        String uri = null;
        if (bootstrapServers instanceof String) {
            uri = (String) bootstrapServers;
        } else if (bootstrapServers instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> serverConfig = (List<String>) bootstrapServers;
            uri = String.join(",", serverConfig);
        }
        return uri;
    }

    // We can't just skip clearing headers we use because we might consumerInject B3 single, yet have stale B3
    // multi, or visa versa.
    public static Map<String, String> clearHeaders(Context context, ConsumerRecord<?, ?> record) {
        Map<String, String> result = null;
        Headers headers = record.headers();
        // Headers::remove creates and consumes an iterator each time. This does one loop instead.
        for (Iterator<Header> i = headers.iterator(); i.hasNext(); ) {
            Header next = i.next();
            String key = next.key();
            if (context.isNecessaryKeys(key)) {
                if (result == null) {
                    result = new HashMap<>();
                }
                result.put(key, new String(next.value()));
                i.remove();
            }
        }
        return result;
    }

    public static String getTopic(ProducerRecord producerRecord) {
        return producerRecord.topic();
    }

}
