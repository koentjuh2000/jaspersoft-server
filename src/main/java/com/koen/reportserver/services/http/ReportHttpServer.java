package com.koen.reportserver.services.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.koen.reportserver.services.logging.Logger;
import com.sun.net.httpserver.HttpServer;

public class ReportHttpServer {
    public static void start() throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PostRequestHandler());
        server.setExecutor(null); // Creates a default executor
        server.start();
        Logger.started(port);
    }
}
