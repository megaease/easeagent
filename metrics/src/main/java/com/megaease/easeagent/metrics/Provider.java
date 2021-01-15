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

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.common.NamedDaemonThreadFactory;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configurable(bind = "metrics.report")
public abstract class Provider {

    @Injection.Bean
    public CallTrace callTrace() {
        return new CallTrace();
    }

    @Injection.Bean
    public Metrics metrics() {
        final MetricRegistry registry = new MetricRegistry();
        final Logger logger = LoggerFactory.getLogger(reporter_name());
        final Map<String, String> hostInfo = ImmutableMap.<String, String>builder()
                .put("system", system())
                .put("application", application())
                .put("hostname", hostname())
                .put("hostipv4",hostipv4())
                .build();
        Executors.newSingleThreadScheduledExecutor(new NamedDaemonThreadFactory("easeagent-metrics-report"))
                 .scheduleWithFixedDelay(
                         new LogReporter(logger, registry, hostInfo, TimeUnit.valueOf(rate_unit()), TimeUnit.valueOf(duration_unit())),
                         period_seconds(), period_seconds(), TimeUnit.SECONDS
                 );
        return new Metrics(registry);
    }

    @Configurable.Item
    String reporter_name() {
        return "metrics";
    }

    @Configurable.Item
    String rate_unit() {
        return TimeUnit.SECONDS.toString();
    }

    @Configurable.Item
    String duration_unit(){
        return TimeUnit.MILLISECONDS.toString();
    }

    @Configurable.Item
    long period_seconds() {
        return 30;
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
    abstract String system();

    @Configurable.Item
    abstract String application();


}
