package com.koen.reportserver.services.http;

import java.io.IOException;
import java.io.OutputStream;

import com.koen.reportserver.services.logging.ErrorResponse;
import com.koen.reportserver.services.logging.Logger;
import com.koen.reportserver.services.reports.GenerateReport;
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

                // Generate JasperPrint

                JasperPrint jasperPrint = GenerateReport.generateAndSave(requestBody);

                if (jasperPrint == null) {
                    throw new JRException("JasperPrint generation failed.");
                }

                // Convert JasperPrint to PDF bytes
                byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

                // Set PDF response headers
                exchange.getResponseHeaders().set("Content-Type", "application/pdf");
                exchange.getResponseHeaders().set("Content-Disposition", "inline; filename=\"report.pdf\"");
                exchange.sendResponseHeaders(200, pdfBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(pdfBytes);
                }
            } catch (Exception e) {
                Logger.log(Logger.Level.ERROR, e.getMessage());

                // Generate error PDF using PDFBox
                byte[] pdfBytes = ErrorResponse.generatePdfFromString("An error occurred while processing the request.");
                if (pdfBytes == null) {
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }

                // Send error PDF as response
                exchange.getResponseHeaders().set("Content-Type", "application/pdf");
                exchange.getResponseHeaders().set("Content-Disposition", "inline; filename=\"error.pdf\"");
                exchange.sendResponseHeaders(200, pdfBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(pdfBytes);
                }
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method not allowed
        }
    }
}
