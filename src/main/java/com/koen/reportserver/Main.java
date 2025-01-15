package com.koen.reportserver;

import java.io.IOException;

import com.koen.reportserver.services.http.ReportHttpServer;
import com.koen.reportserver.services.logging.Logger;

public class Main {
    public static void main(String[] args) {
        try {
            ReportHttpServer.start();
        } catch (IOException e) {
            Logger.log(Logger.Level.ERROR, "Error: " + e.getMessage());
        }
    }
}
