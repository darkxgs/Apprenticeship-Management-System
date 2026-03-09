package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ExcelService {

    public interface ProgressCallback {
        void onProgress(int current, int total, String message);
    }

    public static int importStudentsFromExcel(File file, File profileFolder, File frontIdFolder, File backIdFolder,
            ProgressCallback callback) {
        int importedCount = 0;
        int skippedCount = 0;
        int totalRows = 0;

        String mergeQuery = "MERGE INTO students s " +
                "USING (SELECT ? as seat FROM DUAL) src " +
                "ON (s.seat_no = src.seat) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, " +
                "  exam_system=?, secret_no=?, professional_group=?, coordination_no=?, dob_day=?, dob_month=?, " +
                "  dob_year=?, gender=?, neighborhood=?, governorate=?, religion=?, nationality=?, address=?, " +
                "  other_notes=?, image_path=?, center_name=?, id_front_path=?, id_back_path=? " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (seat_no, serial, name, registration_no, national_id, region, profession, " +
                "  exam_system, secret_no, professional_group, coordination_no, dob_day, dob_month, dob_year, " +
                "  gender, neighborhood, governorate, religion, nationality, address, other_notes, image_path, center_name, id_front_path, id_back_path, status) "
                +
                "  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'غير محدد')";

        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis);
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(mergeQuery)) {

            // Auto-commit OFF so we control each row individually
            conn.setAutoCommit(false);

            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum(); // 0-indexed so last row = total data rows
            if (totalRows <= 0)
                return 0;

            boolean isFirstRow = true;
            int currentRowCount = 0;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Skip empty rows
                if (isRowEmpty(row))
                    continue;

                currentRowCount++;

                String serial = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String registrationNo = getCellValue(row.getCell(2));
                String nationalId = getCellValue(row.getCell(3)).trim();
                String region = getCellValue(row.getCell(4));
                String centerName = getCellValue(row.getCell(5));
                String profession = getCellValue(row.getCell(6));
                String examSystem = getCellValue(row.getCell(7));
                String seatNo = getCellValue(row.getCell(8)).trim();
                String secretNo = getCellValue(row.getCell(9));
                String profGroup = getCellValue(row.getCell(10));
                String coordNo = getCellValue(row.getCell(11));
                String dobDay = getCellValue(row.getCell(12));
                String dobMonth = getCellValue(row.getCell(13));
                String dobYear = getCellValue(row.getCell(14));
                String gender = getCellValue(row.getCell(15));
                String neighborhood = getCellValue(row.getCell(16));
                String governorate = getCellValue(row.getCell(17));
                String religion = getCellValue(row.getCell(18));
                String nationality = getCellValue(row.getCell(19));
                String address = getCellValue(row.getCell(20));

                // Fallback: column 21 is also named 'رقم قومي' in some sheets
                if (nationalId.isEmpty() && !getCellValue(row.getCell(21)).trim().isEmpty()) {
                    nationalId = getCellValue(row.getCell(21)).trim();
                }
                String otherNotes = getCellValue(row.getCell(22));

                // If seat_no is empty, generate a placeholder to not skip the student
                if (seatNo.isEmpty()) {
                    // Try national ID as fallback key
                    seatNo = "AUTO_" + nationalId;
                    if (seatNo.equals("AUTO_")) {
                        skippedCount++;
                        continue; // skip completely empty rows
                    }
                }

                String savedProfilePath = "";
                String savedFrontIdPath = "";
                String savedBackIdPath = "";

                if (nationalId != null && !nationalId.trim().isEmpty()) {
                    String cleanId = nationalId.trim();
                    if (profileFolder != null) {
                        savedProfilePath = findAndCopyImage(profileFolder, cleanId, "profile.jpg");
                    }
                    if (frontIdFolder != null) {
                        savedFrontIdPath = findAndCopyImage(frontIdFolder, cleanId, "id_front.jpg");
                    }
                    if (backIdFolder != null) {
                        savedBackIdPath = findAndCopyImage(backIdFolder, cleanId, "id_back.jpg");
                    }
                }

                // Progress callback every 5 rows
                if (callback != null && (currentRowCount % 5 == 0 || currentRowCount == totalRows)) {
                    callback.onProgress(currentRowCount, totalRows, "جاري معالجة: " + name);
                }

                // --- Bind Parameters ---
                // MERGE key (1)
                stmt.setString(1, seatNo);

                // UPDATE SET (2-25)
                stmt.setString(2, serial);
                stmt.setString(3, name);
                stmt.setString(4, registrationNo);
                stmt.setString(5, nationalId);
                stmt.setString(6, region);
                stmt.setString(7, profession);
                stmt.setString(8, examSystem);
                stmt.setString(9, secretNo);
                stmt.setString(10, profGroup);
                stmt.setString(11, coordNo);
                stmt.setString(12, dobDay);
                stmt.setString(13, dobMonth);
                stmt.setString(14, dobYear);
                stmt.setString(15, gender);
                stmt.setString(16, neighborhood);
                stmt.setString(17, governorate);
                stmt.setString(18, religion);
                stmt.setString(19, nationality);
                stmt.setString(20, address);
                stmt.setString(21, otherNotes);
                stmt.setString(22, savedProfilePath);
                stmt.setString(23, centerName);
                stmt.setString(24, savedFrontIdPath);
                stmt.setString(25, savedBackIdPath);

                // INSERT VALUES (26-51)
                stmt.setString(26, seatNo);
                stmt.setString(27, serial);
                stmt.setString(28, name);
                stmt.setString(29, registrationNo);
                stmt.setString(30, nationalId);
                stmt.setString(31, region);
                stmt.setString(32, profession);
                stmt.setString(33, examSystem);
                stmt.setString(34, secretNo);
                stmt.setString(35, profGroup);
                stmt.setString(36, coordNo);
                stmt.setString(37, dobDay);
                stmt.setString(38, dobMonth);
                stmt.setString(39, dobYear);
                stmt.setString(40, gender);
                stmt.setString(41, neighborhood);
                stmt.setString(42, governorate);
                stmt.setString(43, religion);
                stmt.setString(44, nationality);
                stmt.setString(45, address);
                stmt.setString(46, otherNotes);
                stmt.setString(47, savedProfilePath);
                stmt.setString(48, centerName);
                stmt.setString(49, savedFrontIdPath);
                stmt.setString(50, savedBackIdPath);

                // *** PER-ROW execute + commit so one failure doesn't kill the rest ***
                try {
                    stmt.execute();
                    conn.commit();
                    importedCount++;
                } catch (Exception rowEx) {
                    System.err.println("Skipping row " + currentRowCount + " (" + name + "): " + rowEx.getMessage());
                    try {
                        conn.rollback();
                    } catch (Exception ignored) {
                    }
                    skippedCount++;
                }
            }

            System.out.println("Import complete: " + importedCount + " imported, " + skippedCount + " skipped.");
            LogService.logAction("SYSTEM", "EXCEL_IMPORT",
                    "تم استيراد " + importedCount + " سجل، تخطي " + skippedCount + " سجل من الإكسل");

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return importedCount;
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellValue(cell).trim();
                if (!val.isEmpty())
                    return false;
            }
        }
        return true;
    }

    private static String findAndCopyImage(File sourceFolder, String nationalId, String targetFilename) {
        if (sourceFolder == null || !sourceFolder.exists() || !sourceFolder.isDirectory()) {
            return "";
        }

        // Normalize: trim spaces, remove any control characters
        String cleanId = nationalId.trim().replaceAll("\\s+", "");
        String safeId = cleanId.replaceAll("[^a-zA-Z0-9\u0621-\u064A\u0660-\u0669.-]", "_");

        // Scan the directory for any file whose basename equals the national ID
        File[] matchingFiles = sourceFolder.listFiles((dir, name) -> {
            int dotIndex = name.lastIndexOf('.');
            String baseName = (dotIndex == -1) ? name : name.substring(0, dotIndex);
            // Compare both cleaned and safe variants
            return baseName.equals(cleanId) || baseName.equals(safeId)
                    || baseName.equalsIgnoreCase(cleanId) || baseName.equalsIgnoreCase(safeId);
        });

        if (matchingFiles == null || matchingFiles.length == 0) {
            return "";
        }

        File srcFile = matchingFiles[0];
        try {
            String userHome = System.getProperty("user.home");
            File studentFolder = new File(userHome, ".student_mgmt/students/" + safeId + "/images");
            if (!studentFolder.exists()) {
                studentFolder.mkdirs();
            }

            // Preserve original extension
            String ext = ".jpg";
            int dotIdx = srcFile.getName().lastIndexOf('.');
            if (dotIdx > 0) {
                ext = srcFile.getName().substring(dotIdx);
            }

            // Strip extension from targetFilename and use real ext
            String baseTarget = targetFilename;
            int targetDot = targetFilename.lastIndexOf('.');
            if (targetDot > 0) {
                baseTarget = targetFilename.substring(0, targetDot);
            }

            File destFile = new File(studentFolder, baseTarget + ext);

            if (!destFile.exists() || srcFile.length() != destFile.length()) {
                java.nio.file.Files.copy(srcFile.toPath(), destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Could not copy image for ID " + nationalId + ": " + e.getMessage());
            return "";
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getLocalDateTimeCellValue().toString();
                // Return as long (no decimal point) for IDs/numbers
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf((long) cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }
            default:
                return "";
        }
    }
}
