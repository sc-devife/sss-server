package com.sss.app.bulkimport;

import com.sss.app.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns an uploaded CSV or XLSX file into a list of header-keyed row maps.
 * One shared parser for every entity's bulk import, per the Section 10
 * requirement that this be a generic framework rather than per-entity
 * importers.
 */
@Component
public class SpreadsheetParser {

    public List<Map<String, String>> parse(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            if (filename.endsWith(".csv")) {
                return parseCsv(file);
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return parseXlsx(file);
            }
            throw new BadRequestException("Unsupported file type — upload a .csv or .xlsx file");
        } catch (IOException e) {
            throw new BadRequestException("Failed to read the uploaded file: " + e.getMessage());
        }
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                record.toMap().forEach((k, v) -> row.put(k, v == null ? "" : v.trim()));
                if (row.values().stream().anyMatch(v -> !v.isEmpty())) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private List<Map<String, String>> parseXlsx(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return rows;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(formatter.formatCellValue(cell).trim());
            }

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row sheetRow = sheet.getRow(r);
                if (sheetRow == null) continue;
                Map<String, String> row = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = sheetRow.getCell(c);
                    row.put(headers.get(c), cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                if (row.values().stream().anyMatch(v -> !v.isEmpty())) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
