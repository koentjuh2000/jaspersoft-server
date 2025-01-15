package com.koen.reportserver.services.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koen.reportserver.services.logging.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

public class GenerateReport {

    public static JasperPrint generateAndSave(String body) throws JsonProcessingException {
        Map<String, String> reportData = ExtractData.logFormDataAttributes(body);
        String reportName = reportData.get("nameReport");
        String params = new String(reportData.get("params").getBytes(), StandardCharsets.UTF_8);
        String paramsCollection = new String(reportData.get("paramsCollection").getBytes(), StandardCharsets.UTF_8);

        Map<String, Object> paramsReport = new HashMap<>();
        if (params != null) {
            ObjectMapper mapper = new ObjectMapper();
            paramsReport = mapper.readValue(params, new TypeReference<Map<String, Object>>() {
            });
        }

        JRDataSource defaultDataSource = new JREmptyDataSource();

        if (paramsCollection != null) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Object>> paramsReportTmp = mapper.readValue(
                    paramsCollection, new TypeReference<Map<String, List<Object>>>() {
                    });
            for (String key : paramsReportTmp.keySet()) {
                if (key.equalsIgnoreCase("defaultDataSource")) {
                    defaultDataSource = new JRBeanCollectionDataSource(checkSubProperty(paramsReportTmp.get(key)));
                } else {
                    paramsReport.put(key, new JRBeanCollectionDataSource(checkSubProperty(paramsReportTmp.get(key))));
                }
            }
        }

        try {
            String currentDir = new File(".").getCanonicalPath();
            File reportsFolder = new File(currentDir, "reports");

            if (!reportsFolder.exists()) {
                if (reportsFolder.mkdirs()) {
                    Logger.log(Logger.Level.INFO, "Reports folder created at: " + reportsFolder.getAbsolutePath());
                } else {
                    Logger.log(Logger.Level.ERROR,
                            "Failed to create reports folder at: " + reportsFolder.getAbsolutePath());
                }
            }

            File reportFile = new File(reportsFolder, reportName + ".jasper");

            if (!reportFile.exists()) {
                Logger.log(Logger.Level.ERROR, "Report file not found at: " + reportFile.getAbsolutePath());
            }

            try (InputStream reportStream = new FileInputStream(reportFile)) {
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);

                Map<String, Object> parameters = new HashMap<>();
                parameters.putAll(paramsReport);
                parameters.put("ReportParameter", body);

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, defaultDataSource);

                return jasperPrint;
            }
        } catch (IOException | RuntimeException | JRException e) {
            Logger.log(Logger.Level.ERROR, e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> checkSubProperty(List<Object> subList) {
        subList.forEach((x) -> {
            Map<String, Object> tmp = (Map<String, Object>) x;
            tmp.entrySet().forEach(entry -> {
                if (entry.getValue() instanceof List) {
                    entry.setValue(new JRBeanCollectionDataSource((List<Object>) entry.getValue()));
                }
            });
        });
        return subList;
    }
}
