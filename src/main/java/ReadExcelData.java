import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;

public class ReadExcelData {
    public static void main(String[] args) {
        try {
            System.out.println("=========================================");
            System.out.println("Reading 'المواد الدراسية 25-26دبلوم.xlsx':");
            System.out.println("=========================================");
            readAndPrint(new File("المواد الدراسية 25-26دبلوم.xlsx"), 15);

            System.out.println("\n\n=========================================");
            System.out.println("Reading 'اسماء المراكز والمحطات.xlsx':");
            System.out.println("=========================================");
            readAndPrint(new File("اسماء المراكز والمحطات.xlsx"), 15);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readAndPrint(File file, int maxRows) throws Exception {
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0); // first sheet
            int rowCount = 0;
            
            for (Row row : sheet) {
                if (rowCount >= maxRows) break;
                
                StringBuilder sb = new StringBuilder();
                for (Cell cell : row) {
                    sb.append(getCellValueAsString(cell)).append(" | ");
                }
                
                // Only print non-empty rows
                if (sb.toString().trim().length() > 0 && !sb.toString().replace("|", "").trim().isEmpty()) {
                    System.out.println(sb.toString());
                    rowCount++;
                }
            }
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    }
                    // Avoid scientific notation and trailing zero for integers
                    double val = cell.getNumericCellValue();
                    if (val == Math.floor(val) && !Double.isInfinite(val)) {
                        return String.format("%.0f", val);
                    }
                    return String.valueOf(val);
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception e) {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BLANK:
                    return "";
                default:
                    return "";
            }
        } catch (Exception e) {
            return "<Error reading cell>";
        }
    }
}
