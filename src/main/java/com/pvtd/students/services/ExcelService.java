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
            String username, ProgressCallback callback) {
        int importedCount = 0;
        int skippedCount = 0;
        int totalRows = 0;

        String mergeQuery = "MERGE INTO students s " +
                "USING (SELECT ? as seat FROM DUAL) src " +
                "ON (s.seat_no = src.seat) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, " +
                "  exam_system=?, secret_no=COALESCE(s.secret_no, ?), professional_group=?, coordination_no=?, dob_day=?, dob_month=?, " +
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
                String secretNo = StudentService.generateUniqueSecretNo(); // Ignore Excel, Auto-Generate
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
                // col[23] = "pic" — sequential photo reference number in some datasets
                String picRef = getCellValue(row.getCell(23)).trim();

                // ============================================================
                // DEBUG: Print row data to console to verify column mapping
                // Remove or comment out after verifying data is correct
                // ============================================================
                if (currentRowCount <= 3) { // Print first 3 rows only
                    System.out.println("\n=== DEBUG ROW " + currentRowCount + " ===");
                    System.out.println("col[0]  مسلسل          : [" + serial + "]");
                    System.out.println("col[1]  الاسم           : [" + name + "]");
                    System.out.println("col[2]  رقم التسجيل    : [" + registrationNo + "]");
                    System.out.println("col[3]  الرقم القومى   : [" + getCellValue(row.getCell(3)) + "]");
                    System.out.println("col[4]  المنطقة         : [" + region + "]");
                    System.out.println("col[5]  اسم المركز      : [" + centerName + "]");
                    System.out.println("col[6]  المهنة           : [" + profession + "]");
                    System.out.println("col[7]  النظام           : [" + examSystem + "]");
                    System.out.println("col[8]  رقم الجلوس      : [" + seatNo + "]");
                    System.out.println("col[9]  الرقم السرى     : [" + secretNo + "]");
                    System.out.println("col[10] المجموعة المهنية: [" + profGroup + "]");
                    System.out.println("col[11] رقم التنسيق     : [" + coordNo + "]");
                    System.out.println("col[12] يوم              : [" + dobDay + "]");
                    System.out.println("col[13] شهر              : [" + dobMonth + "]");
                    System.out.println("col[14] سنة              : [" + dobYear + "]");
                    System.out.println("col[15] النوع            : [" + gender + "]");
                    System.out.println("col[16] حي/قرية          : [" + neighborhood + "]");
                    System.out.println("col[17] محافظة           : [" + governorate + "]");
                    System.out.println("col[18] ديانة            : [" + religion + "]");
                    System.out.println("col[19] جنسية            : [" + nationality + "]");
                    System.out.println("col[20] عنوان            : [" + address + "]");
                    System.out.println("col[21] رقم قومي (v2)   : [" + getCellValue(row.getCell(21)) + "]");
                    System.out.println("col[22] اخري             : [" + getCellValue(row.getCell(22)) + "]");
                    System.out.println("col[23] pic              : [" + picRef + "]");
                    System.out.println("=> nationalId USED       : [" + nationalId + "]");
                    System.out.println("=========================================");
                }
                // ============================================================

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
                        // Try national ID first, then fall back to pic reference number
                        savedProfilePath = findAndCopyImage(profileFolder, cleanId, "profile.jpg");
                        if (savedProfilePath.isEmpty() && !picRef.isEmpty()) {
                            savedProfilePath = findAndCopyImage(profileFolder, picRef, "profile.jpg");
                        }
                    }
                    if (frontIdFolder != null) {
                        savedFrontIdPath = findAndCopyImage(frontIdFolder, cleanId, "id_front.jpg");
                        if (savedFrontIdPath.isEmpty() && !picRef.isEmpty()) {
                            savedFrontIdPath = findAndCopyImage(frontIdFolder, picRef, "id_front.jpg");
                        }
                    }
                    if (backIdFolder != null) {
                        savedBackIdPath = findAndCopyImage(backIdFolder, cleanId, "id_back.jpg");
                        if (savedBackIdPath.isEmpty() && !picRef.isEmpty()) {
                            savedBackIdPath = findAndCopyImage(backIdFolder, picRef, "id_back.jpg");
                        }
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
            LogService.logAction(username, "EXCEL_IMPORT",
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

            // For JPEG: read EXIF orientation and rotate to correct upright before saving
            String extLower = ext.toLowerCase();
            if (extLower.equals(".jpg") || extLower.equals(".jpeg")) {
                correctJpegOrientation(srcFile, destFile);
            } else {
                if (!destFile.exists() || srcFile.length() != destFile.length()) {
                    java.nio.file.Files.copy(srcFile.toPath(), destFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Could not copy image for ID " + nationalId + ": " + e.getMessage());
            return "";
        }
    }

    /** Reads EXIF orientation, rotates pixels to upright, writes to dest. Falls back to raw copy. */
    private static void correctJpegOrientation(File src, File dest) throws Exception {
        int orientation = readJpegExifOrientation(src);
        java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(src);
        if (original == null) {
            java.nio.file.Files.copy(src.toPath(), dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        java.awt.image.BufferedImage corrected = applyExifRotation(original, orientation);
        javax.imageio.ImageIO.write(corrected, "jpg", dest);
    }

    /** Parses raw JPEG bytes to find EXIF IFD0 Orientation (tag 0x0112). Returns 1 if not found. */
    private static int readJpegExifOrientation(File file) {
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
            if (raf.readShort() != (short) 0xFFD8) return 1;
            while (raf.getFilePointer() < raf.length() - 2) {
                byte m1 = raf.readByte(), m2 = raf.readByte();
                if (m1 != (byte) 0xFF) return 1;
                int segLen = raf.readUnsignedShort();
                if (m2 == (byte) 0xE1 && segLen > 6) {
                    byte[] hdr = new byte[6]; raf.readFully(hdr);
                    if (hdr[0]=='E' && hdr[1]=='x' && hdr[2]=='i' && hdr[3]=='f') {
                        byte[] ord = new byte[2]; raf.readFully(ord);
                        boolean le = (ord[0] == 'I');
                        exifShort(raf, le);
                        long ifd0 = exifInt(raf, le) & 0xFFFFFFFFL;
                        long base = raf.getFilePointer() - 8;
                        raf.seek(base + ifd0);
                        int n = exifShort(raf, le) & 0xFFFF;
                        for (int i = 0; i < n; i++) {
                            int tag = exifShort(raf, le) & 0xFFFF;
                            exifShort(raf, le); exifInt(raf, le);
                            int val = exifShort(raf, le) & 0xFFFF;
                            exifShort(raf, le);
                            if (tag == 0x0112) return val;
                        }
                    } else { raf.seek(raf.getFilePointer() + (segLen - 8)); }
                } else { raf.seek(raf.getFilePointer() + (segLen - 2)); }
                if (m2 == (byte) 0xDA) break;
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private static int exifShort(java.io.RandomAccessFile r, boolean le) throws Exception {
        byte[] b = new byte[2]; r.readFully(b);
        return le ? ((b[1]&0xFF)<<8)|(b[0]&0xFF) : ((b[0]&0xFF)<<8)|(b[1]&0xFF);
    }

    private static int exifInt(java.io.RandomAccessFile r, boolean le) throws Exception {
        byte[] b = new byte[4]; r.readFully(b);
        return le ? ((b[3]&0xFF)<<24)|((b[2]&0xFF)<<16)|((b[1]&0xFF)<<8)|(b[0]&0xFF)
                  : ((b[0]&0xFF)<<24)|((b[1]&0xFF)<<16)|((b[2]&0xFF)<<8)|(b[3]&0xFF);
    }

    /** Applies AffineTransform for EXIF orientations 1-8. Orientation 1 = no-op. */
    private static java.awt.image.BufferedImage applyExifRotation(java.awt.image.BufferedImage img, int o) {
        int w = img.getWidth(), h = img.getHeight();
        java.awt.image.BufferedImage out;
        java.awt.geom.AffineTransform t = new java.awt.geom.AffineTransform();
        switch (o) {
            case 2: t.scale(-1,1); t.translate(-w,0);                      out=buf(w,h); break;
            case 3: t.translate(w,h); t.rotate(Math.PI);                   out=buf(w,h); break;
            case 4: t.scale(1,-1); t.translate(0,-h);                      out=buf(w,h); break;
            case 5: t.rotate(-Math.PI/2); t.scale(-1,1);                   out=buf(h,w); break;
            case 6: t.translate(h,0); t.rotate(Math.PI/2);                 out=buf(h,w); break;
            case 7: t.scale(-1,1); t.translate(-h,0);
                    t.translate(0,w); t.rotate(-Math.PI/2);                out=buf(h,w); break;
            case 8: t.translate(0,w); t.rotate(-Math.PI/2);                out=buf(h,w); break;
            default: return img;
        }
        java.awt.Graphics2D g = out.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.transform(t); g.drawImage(img,0,0,null); g.dispose();
        return out;
    }

    private static java.awt.image.BufferedImage buf(int w, int h) {
        return new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
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
