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

 package com.megaease.easeagent.requests;

import brave.sampler.CountingSampler;
import brave.sampler.Sampler;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import org.slf4j.LoggerFactory;

@Configurable(bind = "requests.report")
abstract class Provider {
    @Injection.Bean
    public CallTrace callStack() {
        return new CallTrace();
    }

    @Injection.Bean
    public Sampler sampler() {
        return CountingSampler.create((float) capture_rate());
    }

    @Injection.Bean
    public Reporter reporter() {
        return new AsyncLogReporter(LoggerFactory.getLogger(reporter_name()), reporter_queue_capacity(), hostipv4(),
                                    hostname(), system(), application(), type(), callstack());
    }

    @Configurable.Item
    int reporter_queue_capacity() {
        return 1024;
    }

    @Configurable.Item
    String type() {
        return "http_request";
    }

    @Configurable.Item
    boolean callstack() {
        return false;
    }

    @Configurable.Item
    String hostipv4() {
        return HostAddress.localaddr().getHostAddress();
    }

    @Configurable.Item
    String hostname() {
        return HostAddress.localhost();
    }

    @Configurable.Item
    String reporter_name() {
        return "requests";
    }

    @Configurable.Item
    abstract String system();

    @Configurable.Item
    abstract String application();

    @Configurable.Item
    double capture_rate() {
        return 1.0;
    }

}
