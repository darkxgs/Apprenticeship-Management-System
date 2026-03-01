package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ExcelService {

    /**
     * Imports students from Excel file into the new relational schema.
     * Old grade columns (grade_english, grade_mechanics, etc.) and center_name
     * are removed. specialization_id is left NULL on import; users must assign it
     * via the student form afterward.
     *
     * Expected Excel columns (0-indexed):
     * 0=serial, 1=name, 2=national_id, 3=registration_no, 4=seat_no,
     * 5=region, 6=profession, 7=exam_system, 8=secret_no, 9=professional_group,
     * 10=coordination_no, 11=dob_day, 12=dob_month, 13=dob_year, 14=gender,
     * 15=neighborhood, 16=governorate, 17=religion, 18=nationality, 19=address,
     * 20=other_notes
     */
    public static int importStudentsFromExcel(File file) {
        int importedCount = 0;

        // Oracle MERGE INTO with new schema - no grade columns, no center_name
        String mergeQuery = "MERGE INTO students s " +
                "USING (SELECT ? as seat FROM DUAL) src " +
                "ON (s.seat_no = src.seat) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, " +
                "  exam_system=?, secret_no=?, professional_group=?, coordination_no=?, dob_day=?, dob_month=?, " +
                "  dob_year=?, gender=?, neighborhood=?, governorate=?, religion=?, nationality=?, address=?, " +
                "  other_notes=? " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (seat_no, serial, name, registration_no, national_id, region, profession, " +
                "  exam_system, secret_no, professional_group, coordination_no, dob_day, dob_month, dob_year, " +
                "  gender, neighborhood, governorate, religion, nationality, address, other_notes, status) " +
                "  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'غير محدد')";

        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis);
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(mergeQuery)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                } // Skip header

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

                // MERGE ON seat parameter (1)
                stmt.setString(1, seatNo);

                // WHEN MATCHED UPDATE parameters (2..21)
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

                // WHEN NOT MATCHED INSERT parameters (22..42)
                stmt.setString(22, seatNo);
                stmt.setString(23, serial);
                stmt.setString(24, name);
                stmt.setString(25, registrationNo);
                stmt.setString(26, nationalId);
                stmt.setString(27, region);
                stmt.setString(28, profession);
                stmt.setString(29, examSystem);
                stmt.setString(30, secretNo);
                stmt.setString(31, profGroup);
                stmt.setString(32, coordNo);
                stmt.setString(33, dobDay);
                stmt.setString(34, dobMonth);
                stmt.setString(35, dobYear);
                stmt.setString(36, gender);
                stmt.setString(37, neighborhood);
                stmt.setString(38, governorate);
                stmt.setString(39, religion);
                stmt.setString(40, nationality);
                stmt.setString(41, address);
                stmt.setString(42, otherNotes);

                stmt.addBatch();
                importedCount++;

                if (importedCount % 100 == 0)
                    stmt.executeBatch();
            }
            stmt.executeBatch();
            LogService.logAction("SYSTEM", "EXCEL_IMPORT",
                    "تم استيراد/تحديث " + importedCount + " سجل من الإكسل");

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return importedCount;
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
