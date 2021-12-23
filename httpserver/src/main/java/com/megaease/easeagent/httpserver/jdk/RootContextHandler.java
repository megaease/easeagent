/*
 * Copyright (c) 2021, MegaEase
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
 *
 */
package com.megaease.easeagent.httpserver.jdk;

import com.megaease.easeagent.httpserver.IHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class RootContextHandler implements HttpHandler {
    CopyOnWriteArrayList<IHttpHandler> handlers = new CopyOnWriteArrayList<>();
    DefaultRoutes routes = new DefaultRoutes();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        InetSocketAddress inetRemote = exchange.getRemoteAddress();
        exchange.getRequestBody();
        exchange.getRequestHeaders();
    }

    public void addRoute(IHttpHandler handler) {
        this.routes.addRoute(handler);
    }

    public static class DefaultRoutes {
        protected final Collection<UriResource> mappings;

        public DefaultRoutes() {
            this.mappings = newMappingCollection();
        }

        public void addRoute(String url, int priority, Class<?> handler, Object... initParameter) {
            if (url != null) {
                if (handler != null) {
                    mappings.add(new UriResource(url, priority + mappings.size(), handler, initParameter));
                }
            }
        }

        public void addRoute(IHttpHandler handler) {
            this.addRoute(handler.getPath(), handler.priority(), handler.getClass());
        }

        public void removeRoute(String url) {
            String uriToDelete = UriResource.normalizeUri(url);
            Iterator<UriResource> iter = mappings.iterator();
            while (iter.hasNext()) {
                UriResource uriResource = iter.next();
                if (uriToDelete.equals(uriResource.getUri())) {
                    iter.remove();
                    break;
                }
            }
        }

        public Collection<UriResource> getPrioritizedRoutes() {
            return Collections.unmodifiableCollection(mappings);
        }

        protected Collection<UriResource> newMappingCollection() {
            return new PriorityQueue<UriResource>();
        }
    }
}
