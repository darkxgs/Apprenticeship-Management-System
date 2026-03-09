import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;

public class DiagnoseExcel {
    public static void main(String[] args) throws Exception {
        String excelPath = "C:\\Users\\seifd\\OneDrive\\Desktop\\Java Project\\New Microsoft Excel Worksheet.xlsx";
        String imageFolder = "C:\\Users\\seifd\\Downloads\\هرم برمجة قومي";

        // Print image folder contents
        File imgDir = new File(imageFolder);
        System.out.println("=== IMAGE FOLDER ===");
        System.out.println("Folder exists: " + imgDir.exists());
        if (imgDir.exists()) {
            File[] imgs = imgDir.listFiles();
            System.out.println("Total images: " + (imgs != null ? imgs.length : 0));
            if (imgs != null) {
                for (int i = 0; i < Math.min(5, imgs.length); i++) {
                    System.out.println("  Sample: " + imgs[i].getName());
                }
            }
        }

        // Read Excel
        System.out.println("\n=== EXCEL ANALYSIS ===");
        try (FileInputStream fis = new FileInputStream(excelPath);
                Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            System.out.println("Physical rows: " + sheet.getPhysicalNumberOfRows());
            System.out.println("Last row index: " + sheet.getLastRowNum());
            System.out.println("First row index: " + sheet.getFirstRowNum());

            // Header row
            Row header = sheet.getRow(sheet.getFirstRowNum());
            System.out.println("\n--- HEADER COLUMNS ---");
            if (header != null) {
                for (int i = 0; i <= header.getLastCellNum(); i++) {
                    Cell c = header.getCell(i);
                    System.out.println("  Col " + i + ": " + (c != null ? c.toString() : "NULL"));
                }
            }

            // Count rows with empty seat_no (col 4)
            System.out.println("\n--- ROW ANALYSIS (checking seat_no at col 4) ---");
            int totalRows = 0, emptySeats = 0, validRows = 0;
            boolean first = true;
            for (Row row : sheet) {
                if (first) {
                    first = false;
                    continue;
                }
                totalRows++;
                Cell seatCell = row.getCell(4);
                String seat = (seatCell != null) ? seatCell.toString().trim() : "";
                if (seat.isEmpty()) {
                    emptySeats++;
                } else {
                    validRows++;
                }
            }
            System.out.println("Total data rows: " + totalRows);
            System.out.println("Rows with empty seat_no (skipped): " + emptySeats);
            System.out.println("Rows with valid seat_no (should import): " + validRows);

            // Print first 3 valid rows as sample
            System.out.println("\n--- FIRST 3 VALID DATA ROWS ---");
            int printed = 0;
            first = true;
            for (Row row : sheet) {
                if (first) {
                    first = false;
                    continue;
                }
                Cell seatCell = row.getCell(4);
                String seat = (seatCell != null) ? seatCell.toString().trim() : "";
                if (!seat.isEmpty() && printed < 3) {
                    System.out.println("  Row " + row.getRowNum() +
                            " | Col0(serial)=" + getCellValue(row.getCell(0)) +
                            " | Col1(name)=" + getCellValue(row.getCell(1)) +
                            " | Col2(natId)=" + getCellValue(row.getCell(2)) +
                            " | Col4(seat)=" + seat +
                            " | Col21(center)=" + getCellValue(row.getCell(21)));
                    printed++;
                }
            }
        }
    }

    static String getCellValue(Cell cell) {
        if (cell == null)
            return "NULL";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
