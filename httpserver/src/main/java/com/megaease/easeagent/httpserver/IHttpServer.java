package com.megaease.easeagent.httpserver;

import java.util.List;

public interface IHttpServer {
    void start(int port);

    void addHttpRoutes(List<IHttpHandler> agentHttpHandlers);

    void stop();
}
