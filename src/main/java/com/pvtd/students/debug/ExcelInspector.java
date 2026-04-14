package com.pvtd.students.debug;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;

/**
 * Run with: mvn exec:java
 * -Dexec.mainClass="com.pvtd.students.debug.ExcelInspector"
 * Prints exact column indexes and header names from the Excel file.
 */
public class ExcelInspector {
    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\seifd\\OneDrive\\Desktop\\Java Project\\New Microsoft Excel Worksheet.xlsx";

        try (FileInputStream fis = new FileInputStream(path);
                Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            System.out.println("Total rows (getLastRowNum): " + sheet.getLastRowNum());
            System.out.println("Physical rows: " + sheet.getPhysicalNumberOfRows());
            System.out.println();

            // Print header row
            Row header = sheet.getRow(0);
            if (header == null) {
                System.out.println("Header row is null!");
                return;
            }

            System.out.println("=== HEADER ROW (Column Index => Name) ===");
            for (int i = 0; i <= header.getLastCellNum(); i++) {
                Cell c = header.getCell(i);
                String val = (c == null) ? "<empty>" : getVal(c);
                System.out.printf("  getCell(%2d) => %s%n", i, val);
            }

            System.out.println();
            System.out.println("=== ROW 2 (First Data Row) ===");
            Row row2 = sheet.getRow(1);
            if (row2 != null) {
                for (int i = 0; i <= row2.getLastCellNum(); i++) {
                    Cell c = row2.getCell(i);
                    String val = (c == null) ? "<empty>" : getVal(c);
                    if (!val.equals("<empty>") && !val.isEmpty()) {
                        System.out.printf("  getCell(%2d) => %s%n", i, val);
                    }
                }
            }
        }
    }

    static String getVal(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getLocalDateTimeCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
}
