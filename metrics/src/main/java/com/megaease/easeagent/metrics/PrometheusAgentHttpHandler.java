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

import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Response;
import com.megaease.easeagent.httpserver.nanohttpd.protocols.http.response.Status;
import com.megaease.easeagent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

public class PrometheusAgentHttpHandler extends AgentHttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusAgentHttpHandler.class);

    @Override
    public String getPath() {
        return "/prometheus/metrics";
    }

    @Override
    public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        String contentType = TextFormat.chooseContentType(headers.get("Accept"));
        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        StringWriter stringWriter = new StringWriter();
        try (Writer writer = new BufferedWriter(stringWriter)) {
            TextFormat.writeFormat(contentType, writer, samples);
            writer.flush();
        } catch (IOException e) {
            LOGGER.warn("write data error. {}", e.getMessage());
        }
        String data = stringWriter.toString();
        return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, data);
    }
}
