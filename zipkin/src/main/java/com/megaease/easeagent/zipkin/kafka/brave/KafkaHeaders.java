/*
 * Copyright 2013-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.megaease.easeagent.zipkin.kafka.brave;

import brave.internal.Nullable;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import static com.megaease.easeagent.zipkin.kafka.brave.KafkaTracing.log;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * copy from zipkin.kafka.brave
 */
final class KafkaHeaders {
  static void replaceHeader(Headers headers, String key, String value) {
    try {
      headers.remove(key);
      headers.add(key, value.getBytes(UTF_8));
    } catch (IllegalStateException e) {
      log(e, "error setting header {0} in headers {1}", key, headers);
    }
  }

  @Nullable static String lastStringHeader(Headers headers, String key) {
    Header header = headers.lastHeader(key);
    if (header == null || header.value() == null) return null;
    return new String(header.value(), UTF_8);
  }

  KafkaHeaders() {
  }
}
