package com.megaease.easeagent.plugin.jdkhttpserver;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class JdkHttpServer {
    private final Consumer<HttpExchange> beforeF;
    private final BiFunction<HttpExchange, String, String> afterF;

    public JdkHttpServer(Consumer<HttpExchange> beforeF, BiFunction<HttpExchange, String, String> afterF) {
        this.beforeF = beforeF;
        this.afterF = afterF;
    }


    public void startHttpServer() throws IOException {
        DatagramSocket s = new DatagramSocket(0);
        int port = s.getLocalPort();
        String httpServer = "http://127.0.0.1:" + port;
        System.out.println("run up http server : " + httpServer);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext context = server.createContext("/getResult");
        context.setHandler(JdkHttpServer.this::handleRequest);
        server.start();
    }

    public void handleRequest(HttpExchange exchange) throws IOException {
        beforeF.accept(exchange);
        URI requestURI = exchange.getRequestURI();
        printRequestInfo(exchange);
        String response = "result: " + requestURI;
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        afterF.apply(exchange, response);
    }

    public void printRequestInfo(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();
        System.out.println(String.format("HTTP method : %s", requestMethod));
        HttpPrincipal principal = exchange.getPrincipal();
        System.out.println(String.format("principal: %s", principal));
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        System.out.println(String.format("query: %s", query));
        System.out.println(" ------------------------ headers  ------------------------");
        Headers requestHeaders = exchange.getRequestHeaders();
        requestHeaders.entrySet().forEach(System.out::println);
        System.out.println(" ------------------------ headers end  ------------------------\n\n");
    }

    public static void main(String[] args) throws IOException {
        new JdkHttpServer((a) -> {
        }, (a, b) -> "").startHttpServer();
    }
}
