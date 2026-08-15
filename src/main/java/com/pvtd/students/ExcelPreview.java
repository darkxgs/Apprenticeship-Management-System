package com.pvtd.students;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.File;

/**
 * Temporary utility to preview Excel file structure.
 * Run via: mvn exec:java -Dexec.mainClass="com.pvtd.students.ExcelPreview"
 */
public class ExcelPreview {
    public static void main(String[] args) throws Exception {
        String base = "c:/Users/seifd/OneDrive/Desktop/Java Project/";
        previewFile(base + "اسماء المراكز والمحطات.xlsx", "Centers File");
        previewFile(base + "cleaned_subjects_raw.xlsx", "Cleaned Subjects");
        previewFile(base + "المواد الدراسية 25-26دبلوم.xlsx", "Subjects Diploma");
    }

    static void previewFile(String path, String label) {
        System.out.println("\n========== " + label + " ==========");
        try (FileInputStream fis = new FileInputStream(new File(path));
             Workbook wb = new XSSFWorkbook(fis)) {
            System.out.println("Sheets: " + wb.getNumberOfSheets());
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                System.out.println("  Sheet[" + s + "]: " + sheet.getSheetName() 
                    + " | rows: " + (sheet.getLastRowNum() + 1));
                // Print first 8 rows
                for (int r = 0; r <= Math.min(7, sheet.getLastRowNum()); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    StringBuilder sb = new StringBuilder("  Row " + r + ": ");
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        String val = cellStr(cell);
                        if (!val.isEmpty()) sb.append("[C").append(c).append("=").append(val).append("] ");
                    }
                    System.out.println(sb);
                }
            }
        } catch (Exception e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }

    static String cellStr(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
