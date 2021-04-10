/*
 *   Copyright (c) 2017, MegaEase
 *   All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.megaease.easeagent.metrics;

import com.megaease.easeagent.httpserver.AgentHttpHandler;
import com.megaease.easeagent.httpserver.HttpResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;

public class PrometheusAgentHttpHandler extends AgentHttpHandler {

    @Override
    public String getPath() {
        return "/prometheus/metrics";
    }

    @Override
    public HttpResponse process(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        String contentType = TextFormat.chooseContentType(headers.getFirst("Accept"));
        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        StringWriter stringWriter = new StringWriter();
        try (Writer writer = new BufferedWriter(stringWriter)) {
            TextFormat.writeFormat(contentType, writer, samples);
            writer.flush();
        }
        String data = stringWriter.toString();
        return HttpResponse.builder().statusCode(200).data(data).build();
    }

}
