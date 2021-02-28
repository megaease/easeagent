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

package com.megaease.easeagent.zipkin;

import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.sampler.CountingSampler;
import com.google.common.base.Strings;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

@Configurable(bind = "zipkin.tracer")
abstract class Provider {

    @Injection.Bean
    public CallTrace callTrace() {
        return new CallTrace();
    }

    @Injection.Bean
    public Tracer tracer() {
        return Tracing.newBuilder()
                .localServiceName(service_name())
                .traceId128Bit(trace_id_128b())
                .sampler(CountingSampler.create((float) sample_rate()))
                .addSpanHandler(spanHandler())
                .build().tracer();

    }

    private Sender sender() {
        return Strings.isNullOrEmpty(send_endpoint())
                ? new LogSender()
                : new GatewaySender(message_max_bytes(), send_endpoint(), connect_timeout(), read_timeout(),
                send_compression(), user_agent());
    }

    private SpanHandler spanHandler() {
        return AsyncZipkinSpanHandler.create(sender());
    }

    @Configurable.Item
    abstract String send_endpoint();

    @Configurable.Item
    abstract String system();

    @Configurable.Item
    abstract String application();

    @Configurable.Item
    String instance() {
        return "unknown";
    }

    @Configurable.Item
    String service_name() {
        return system() + "-" + application() + "-" + hostname();
    }

    @Configurable.Item
    double sample_rate() {
        return 1f;
    }

    @Configurable.Item
    boolean send_compression() {
        return false;
    }

    @Configurable.Item
    int reporter_queued_max_spans() {
        return 10000;
    }

    @Configurable.Item
    long reporter_message_timeout_seconds() {
        return 1;
    }

    @Configurable.Item
    boolean trace_id_128b() {
        return false;
    }

    @Configurable.Item
    int message_max_bytes() {
        return 1024 * 1024;
    }

    @Configurable.Item
    int connect_timeout() {
        return 10 * 1000;
    }

    @Configurable.Item
    int read_timeout() {
        return 60 * 1000;
    }

    @Configurable.Item
    String user_agent() {
        return "easeagent/0.1.0";
    }

    @Configurable.Item
    String host_ipv4() {
        return HostAddress.localaddr().getHostAddress();
    }

    @Configurable.Item
    String hostname() {
        return HostAddress.localhost();
    }

}
