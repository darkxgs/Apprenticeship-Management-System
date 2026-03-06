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

            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getPhysicalNumberOfRows() - 1; // subtract header
            if (totalRows <= 0)
                return 0;

            boolean isFirstRow = true;
            int currentRowCount = 0;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                currentRowCount++;

                String seatNo = getCellValue(row.getCell(4));
                if (seatNo == null || seatNo.trim().isEmpty())
                    continue;

                String serial = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String nationalId = getCellValue(row.getCell(2));
                String registrationNo = getCellValue(row.getCell(3));
                String region = getCellValue(row.getCell(5));
                String profession = getCellValue(row.getCell(6));
                String examSystem = getCellValue(row.getCell(7));
                String secretNo = getCellValue(row.getCell(8));
                String profGroup = getCellValue(row.getCell(9));
                String coordNo = getCellValue(row.getCell(10));
                String dobDay = getCellValue(row.getCell(11));
                String dobMonth = getCellValue(row.getCell(12));
                String dobYear = getCellValue(row.getCell(13));
                String gender = getCellValue(row.getCell(14));
                String neighborhood = getCellValue(row.getCell(15));
                String governorate = getCellValue(row.getCell(16));
                String religion = getCellValue(row.getCell(17));
                String nationality = getCellValue(row.getCell(18));
                String address = getCellValue(row.getCell(19));
                String otherNotes = getCellValue(row.getCell(20));
                String centerName = getCellValue(row.getCell(21));

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

                if (callback != null && (currentRowCount % 10 == 0 || currentRowCount == totalRows)) {
                    callback.onProgress(currentRowCount, totalRows, "جاري استيراد الطالب: " + name);
                }

                // MERGE ON
                stmt.setString(1, seatNo);

                // MATCHED UPDATE
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

                // NOT MATCHED INSERT
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

                stmt.addBatch();
                importedCount++;

                if (importedCount % 100 == 0) {
                    try {
                        stmt.executeBatch();
                        conn.commit();
                    } catch (Exception ex) {
                        System.err.println("Batch error, skipping block.");
                    }
                }
            }
            try {
                stmt.executeBatch();
                conn.commit();
            } catch (Exception ex) {
                System.err.println("Final batch error.");
            }

            LogService.logAction("SYSTEM", "EXCEL_IMPORT",
                    "تم استيراد/تحديث " + importedCount + " سجل من الإكسل");

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return importedCount;
    }

    private static String findAndCopyImage(File sourceFolder, String nationalId, String targetFilename) {
        if (sourceFolder == null || !sourceFolder.exists() || !sourceFolder.isDirectory()) {
            return "";
        }

        String safeId = nationalId.replaceAll("[^a-zA-Z0-9.-]", "_");

        // Find any file in the directory whose name (without extension) matches the
        // National ID exactly
        File[] matchingFiles = sourceFolder.listFiles((dir, name) -> {
            int dotIndex = name.lastIndexOf('.');
            String nameWithoutExt = (dotIndex == -1) ? name : name.substring(0, dotIndex);
            return nameWithoutExt.equals(nationalId) || nameWithoutExt.equals(safeId);
        });

        if (matchingFiles != null && matchingFiles.length > 0) {
            File srcFile = matchingFiles[0]; // Take the first match
            try {
                String userHome = System.getProperty("user.home");
                File studentFolder = new File(userHome, ".student_mgmt/students/" + safeId + "/images");
                if (!studentFolder.exists()) {
                    studentFolder.mkdirs();
                }

                // Keep the original extension if possible
                String ext = ".jpg";
                int dotIndex = srcFile.getName().lastIndexOf('.');
                if (dotIndex > 0) {
                    ext = srcFile.getName().substring(dotIndex);
                }

                // If targetFilename is like "profile.jpg", strip its extension and use the real
                // one.
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
                e.printStackTrace();
            }
        }
        return "";
    }

    private static String getCellValue(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getLocalDateTimeCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
