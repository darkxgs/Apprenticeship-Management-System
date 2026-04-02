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

    public static class ImportResult {
        public int importedCount = 0;
        public int skippedCount = 0;
        public java.util.List<String> errors = new java.util.ArrayList<>();
    }

    public static ImportResult importStudentsFromExcel(File file, File profileFolder, File frontIdFolder, File backIdFolder,
            String username, ProgressCallback callback) {
        ImportResult result = new ImportResult();
        int totalRows = 0;

        String mergeQuery = "MERGE INTO students s " +
                "USING (SELECT ? as seat FROM DUAL) src " +
                "ON (s.seat_no = src.seat) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, " +
                "  exam_system=?, secret_no=?, professional_group=?, coordination_no=?, dob_day=?, dob_month=?, " +
                "  dob_year=?, gender=?, neighborhood=?, governorate=?, religion=?, nationality=?, address=?, " +
                "  other_notes=?, image_path=?, center_name=?, id_front_path=?, id_back_path=?, phone_number=? " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (seat_no, serial, name, registration_no, national_id, region, profession, " +
                "  exam_system, secret_no, professional_group, coordination_no, dob_day, dob_month, dob_year, " +
                "  gender, neighborhood, governorate, religion, nationality, address, other_notes, image_path, center_name, id_front_path, id_back_path, status, phone_number) " +
                "  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'غير محدد', ?)";

        // Build image caches
        java.util.Map<String, File> profileCache = buildImageCache(profileFolder);
        java.util.Map<String, File> frontCache = buildImageCache(frontIdFolder);
        java.util.Map<String, File> backCache = buildImageCache(backIdFolder);

        // Preload database lookup caches
        java.util.Map<String, String> regionCodesCache = new java.util.HashMap<>();
        java.util.Map<String, String> centerCodesCache = new java.util.HashMap<>();
        java.util.Set<String> syncedProfGroups = new java.util.HashSet<>();

        try (Connection c = DatabaseConnection.getConnection();
             java.sql.Statement s = c.createStatement()) {
            try (java.sql.ResultSet r = s.executeQuery("SELECT name, code FROM regions")) {
                while(r.next()) regionCodesCache.put(r.getString(1), r.getString(2));
            }
            try (java.sql.ResultSet r = s.executeQuery("SELECT name, code FROM centers")) {
                while(r.next()) centerCodesCache.put(r.getString(1), r.getString(2));
            }
        } catch(Exception ignored){}

        try (FileInputStream fis = new FileInputStream(file);
                Workbook workbook = new XSSFWorkbook(fis);
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(mergeQuery)) {

            conn.setAutoCommit(false);
            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum();
            if (totalRows <= 0) return result;

            boolean isFirstRow = true;
            int currentRowCount = 0;
            int increment = StudentService.getSecretNumberIncrement();

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                if (isRowEmpty(row)) continue;

                currentRowCount++;

                String serial = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String registrationNo = getCellValue(row.getCell(2));
                String nationalId = getCellValue(row.getCell(3)).trim();
                String region = getCellValue(row.getCell(4));
                String centerName = getCellValue(row.getCell(5));
                String examSystem = getCellValue(row.getCell(7));
                String seatNo = getCellValue(row.getCell(8)).trim();
                
                String profGroup = getCellValue(row.getCell(10));
                String profession = getCellValue(row.getCell(6));
                
                // Cached DB Sync
                String syncKey = profession + "@@" + profGroup;
                if (!syncedProfGroups.contains(syncKey)) {
                    StudentService.syncProfessionAndGroup(conn, profession, profGroup);
                    syncedProfGroups.add(syncKey);
                }
                
                // Cached Secret No Generator
                String secretNo = null;
                String rCode = regionCodesCache.get(region);
                String cCode = centerCodesCache.get(centerName);
                if (rCode != null && cCode != null) {
                    try {
                        String formattedR = (rCode.length() > 2) ? rCode.substring(0, 2) : String.format("%02d", Integer.parseInt(rCode));
                        String formattedC = (cCode.length() > 3) ? cCode.substring(0, 3) : String.format("%03d", Integer.parseInt(cCode));
                        String seatPref = seatNo.length() >= 3 ? seatNo.substring(0, 3) : String.format("%03d", Integer.parseInt(seatNo));
                        secretNo = formattedR + formattedC + (Integer.parseInt(seatPref) + increment);
                    } catch (Exception e) {
                        secretNo = StudentService.generateSecretNo(conn, region, centerName, seatNo, increment);
                    }
                } else {
                    secretNo = StudentService.generateSecretNo(conn, region, centerName, seatNo, increment);
                    if (rCode == null) regionCodesCache.put(region, "cached");
                    if (cCode == null) centerCodesCache.put(centerName, "cached");
                }
                
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
                String picRef = getCellValue(row.getCell(23)).trim();
                String phoneNumber = getCellValue(row.getCell(24)).trim();
                String otherNotes = getCellValue(row.getCell(22));

                if (seatNo.isEmpty()) {
                    seatNo = "AUTO_" + nationalId;
                    if (seatNo.equals("AUTO_")) {
                        result.skippedCount++;
                        continue;
                    }
                }

                boolean hasNatId = (nationalId != null && !nationalId.trim().isEmpty());
                boolean hasPicRef = (picRef != null && !picRef.trim().isEmpty());

                String savedProfilePath = "";
                String savedFrontIdPath = "";
                String savedBackIdPath = "";

                if (hasNatId || hasPicRef) {
                    savedProfilePath = findAndCopyImageUsingCache(profileCache, nationalId, picRef, "profile.jpg");
                    savedFrontIdPath = findAndCopyImageUsingCache(frontCache, nationalId, picRef, "id_front.jpg");
                    savedBackIdPath = findAndCopyImageUsingCache(backCache, nationalId, picRef, "id_back.jpg");
                }

                if (callback != null && (currentRowCount % 5 == 0 || currentRowCount == totalRows)) {
                    callback.onProgress(currentRowCount, totalRows, "جاري معالجة: " + name);
                }

                stmt.setString(1, seatNo);
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
                stmt.setString(26, phoneNumber);

                stmt.setString(27, seatNo);
                stmt.setString(28, serial);
                stmt.setString(29, name);
                stmt.setString(30, registrationNo);
                stmt.setString(31, nationalId);
                stmt.setString(32, region);
                stmt.setString(33, profession);
                stmt.setString(34, examSystem);
                stmt.setString(35, secretNo);
                stmt.setString(36, profGroup);
                stmt.setString(37, coordNo);
                stmt.setString(38, dobDay);
                stmt.setString(39, dobMonth);
                stmt.setString(40, dobYear);
                stmt.setString(41, gender);
                stmt.setString(42, neighborhood);
                stmt.setString(43, governorate);
                stmt.setString(44, religion);
                stmt.setString(45, nationality);
                stmt.setString(46, address);
                stmt.setString(47, otherNotes);
                stmt.setString(48, savedProfilePath);
                stmt.setString(49, centerName);
                stmt.setString(50, savedFrontIdPath);
                stmt.setString(51, savedBackIdPath);
                stmt.setString(52, phoneNumber);

                try {
                    stmt.execute();
                    result.importedCount++;
                } catch (Exception rowEx) {
                    System.err.println("Skipping row " + currentRowCount + " (" + name + "): " + rowEx.getMessage());
                    result.errors.add("الصف " + currentRowCount + " (" + name + "): " + rowEx.getMessage().split("\n")[0]);
                    result.skippedCount++;
                }

                if (currentRowCount % 500 == 0 || currentRowCount == totalRows) {
                    conn.commit();
                }
            }

            System.out.println("Import complete: " + result.importedCount + " imported, " + result.skippedCount + " skipped.");
            LogService.logAction(username, "EXCEL_IMPORT",
                    "تم استيراد " + result.importedCount + " سجل، تخطي " + result.skippedCount + " سجل من الإكسل");

        } catch (Exception e) {
            e.printStackTrace();
            result.errors.add("فشل فادح: " + e.getMessage());
            return result;
        }
        return result;
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

    private static java.util.Map<String, File> buildImageCache(File folder) {
        java.util.Map<String, File> cache = new java.util.HashMap<>();
        if (folder == null || !folder.exists() || !folder.isDirectory()) return cache;
        scanFolderRecursively(folder, cache);
        return cache;
    }

    private static void scanFolderRecursively(File dir, java.util.Map<String, File> cache) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanFolderRecursively(f, cache);
            } else {
                String name = f.getName();
                int dotIndex = name.lastIndexOf('.');
                String base = (dotIndex == -1) ? name : name.substring(0, dotIndex);
                String norm = base.trim().replaceAll("\\s+", "").toLowerCase();
                if (!norm.isEmpty()) cache.put(norm, f);
            }
        }
    }

    private static String findAndCopyImageUsingCache(java.util.Map<String, File> cache, String nationalId, String picRef, String targetFilename) {
        File srcFile = null;

        if (nationalId != null && !nationalId.trim().isEmpty()) {
            String cleanId = nationalId.trim().replaceAll("\\s+", "").toLowerCase();
            srcFile = cache.get(cleanId);
            if (srcFile == null) {
                String safeId = cleanId.replaceAll("[^a-z0-9\u0621-\u064A\u0660-\u0669.-]", "_");
                srcFile = cache.get(safeId);
            }
        }

        if (srcFile == null && picRef != null && !picRef.trim().isEmpty()) {
            String cleanRef = picRef.trim().replaceAll("\\s+", "").toLowerCase();
            srcFile = cache.get(cleanRef);
            if (srcFile == null) {
                String safeRef = cleanRef.replaceAll("[^a-z0-9\u0621-\u064A\u0660-\u0669.-]", "_");
                srcFile = cache.get(safeRef);
            }
        }

        if (srcFile == null) return "";

        String folderId = (nationalId != null && !nationalId.trim().isEmpty()) ? nationalId : picRef;
        String safeFolderId = folderId.trim().replaceAll("\\s+", "").replaceAll("[^a-zA-Z0-9\u0621-\u064A\u0660-\u0669.-]", "_");

        try {
            String userHome = System.getProperty("user.home");
            File studentFolder = new File(userHome, ".student_mgmt/students/" + safeFolderId + "/images");
            if (!studentFolder.exists()) studentFolder.mkdirs();

            String ext = ".jpg";
            int dotIdx = srcFile.getName().lastIndexOf('.');
            if (dotIdx > 0) ext = srcFile.getName().substring(dotIdx);

            String baseTarget = targetFilename;
            int targetDot = targetFilename.lastIndexOf('.');
            if (targetDot > 0) baseTarget = targetFilename.substring(0, targetDot);

            File destFile = new File(studentFolder, baseTarget + ext);

            String extLower = ext.toLowerCase();
            if (extLower.equals(".jpg") || extLower.equals(".jpeg")) {
                correctJpegOrientation(srcFile, destFile);
            } else {
                copyRaw(srcFile, destFile);
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Could not copy image for " + safeFolderId + ": " + e.getMessage());
            return "";
        }
    }

    private static void copyRaw(File src, File dest) throws Exception {
        if (!dest.exists() || src.length() != dest.length()) {
            java.nio.file.Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void correctJpegOrientation(File src, File dest) throws Exception {
        try {
            int orientation = readJpegExifOrientation(src);
            java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(src);
            if (original == null) {
                copyRaw(src, dest);
                return;
            }
            java.awt.image.BufferedImage corrected = applyExifRotation(original, orientation);
            javax.imageio.ImageIO.write(corrected, "jpg", dest);
        } catch (Exception e) {
            // Force copy the original image without modifications if ImageIO crashes
            copyRaw(src, dest);
        }
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
