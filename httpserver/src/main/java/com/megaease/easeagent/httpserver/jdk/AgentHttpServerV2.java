package com.megaease.easeagent.httpserver.jdk;

import com.megaease.easeagent.httpserver.IHttpHandler;
import com.megaease.easeagent.httpserver.IHttpServer;
import com.megaease.easeagent.plugin.async.AgentThreadFactory;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentHttpServerV2 implements IHttpServer {
    RootContextHandler httpRootHandler;
    HttpServer server;

    @Override
    public void start(int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
            this.httpRootHandler = new RootContextHandler();
            this.server.createContext("/", this.httpRootHandler);

            ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1, new AgentThreadFactory());
            this.server.setExecutor(threadPoolExecutor);

            this.server.start();
        } catch (IOException ex) {
        }
    }

    @Override
    public void addHttpRoutes(List<IHttpHandler> agentHttpHandlers) {
        agentHttpHandlers.forEach(
            handler -> {
                this.httpRootHandler.addRoute(handler);
            }
        );
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }
}
