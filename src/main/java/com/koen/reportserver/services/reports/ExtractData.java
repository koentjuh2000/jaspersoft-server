package com.koen.reportserver.services.reports;

import java.util.HashMap;
import java.util.Map;

import com.koen.reportserver.services.logging.Logger;

public class ExtractData {

    static Map<String, String> logFormDataAttributes(String requestBody) {
        String[] formDataEntries = requestBody.split("--");
        Map<String, String> reportData = new HashMap<>();
        
        for (String entry : formDataEntries) {
            if (entry.contains("Content-Disposition")) {
                int nameIndex = entry.indexOf("name=\"") + 6;
                int endIndex = entry.indexOf("\"", nameIndex);
                if (nameIndex > -1 && endIndex > -1) {
                    String name = entry.substring(nameIndex, endIndex);
                    String value = extractValue(entry, endIndex);
                    storeAttribute(reportData, name, value);
                }
            }
        }
        return reportData;
    }

    private static String extractValue(String entry, int endIndex) {
        return entry.substring(entry.indexOf("\n", endIndex) + 1).trim();
    }

    private static void storeAttribute(Map<String, String> reportData, String name, String value) {
        switch (name) {
            case "file", "nameReport", "paramsCollection", "params", "fileType" -> reportData.put(name, value);
            default -> Logger.log(Logger.Level.WARN, "Unknown form data attribute: " + name);
        }
        Logger.log(Logger.Level.INFO, "[" + name + "] " + value);
    }
}
