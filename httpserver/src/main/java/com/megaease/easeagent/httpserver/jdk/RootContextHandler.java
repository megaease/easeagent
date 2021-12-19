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
