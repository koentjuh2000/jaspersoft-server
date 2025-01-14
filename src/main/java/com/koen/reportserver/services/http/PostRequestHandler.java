package com.koen.reportserver.services.http;

import java.io.IOException;
import java.io.OutputStream;

import com.koen.reportserver.services.logging.Logger;
import com.koen.reportserver.services.reports.Process;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

public class PostRequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                byte[] requestBodyBytes = exchange.getRequestBody().readAllBytes();
                String requestBody = new String(requestBodyBytes);
                
                // Process the request body
                JasperPrint jasperPrint = Process.generateAndSave(requestBody);

                // Export the JasperPrint object to PDF byte array
                byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

                // Set response headers for PDF
                exchange.getResponseHeaders().set("Content-Type", "application/pdf");
                exchange.getResponseHeaders().set("Content-Disposition", "inline; filename=\"report.pdf\"");
                
                exchange.sendResponseHeaders(200, pdfBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(pdfBytes);
                }
            } catch (IOException | RuntimeException | JRException e) {
                Logger.log(Logger.Level.ERROR, e.getMessage());
                exchange.sendResponseHeaders(500, -1); // Internal server error
            }
        } else {
            exchange.sendResponseHeaders(200, -1); // -1 indicates no response body
        }
    }
}
