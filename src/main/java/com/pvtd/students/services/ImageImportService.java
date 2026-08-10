package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;
import com.pvtd.students.services.ExcelService;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ImageImportService {

    public interface ProgressCallback {
        void onProgress(int current, int total, String message);
    }

    public static class ImageImportResult {
        public int linkedCount = 0;
        public int imagesCount = 0;
        public int unmatchedImagesCount = 0;
        public int studentsWithoutImagesCount = 0;
        public List<String> errors = new ArrayList<>();
        public List<String> unmatchedImageNames = new ArrayList<>();
    }

    public static ImageImportResult importImagesFromFolder(File folder, String username, ProgressCallback callback) {
        ImageImportResult result = new ImageImportResult();
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            result.errors.add("المجلد غير موجود أو غير صحيح.");
            return result;
        }

        // 1. Scan for image files
        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });

        if (files == null || files.length == 0) {
            result.errors.add("لم يتم العثور على صور في المجلد المختار.");
            return result;
        }

        result.imagesCount = files.length;
        Map<String, File> idToImageMap = new HashMap<>();
        for (File f : files) {
            String name = f.getName();
            // Extract only digits from the filename
            String id = name.replaceAll("\\D", "");
            if (id.length() >= 10) { // Simple validation for National ID length
                idToImageMap.put(id, f);
            } else {
                result.unmatchedImageNames.add(name + " (اسم الملف لا يحتوي على رقم قومي صحيح)");
                result.unmatchedImagesCount++;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            int totalImages = idToImageMap.size();
            int processedCount = 0;

            Set<String> matchedIds = new HashSet<>();

            for (Map.Entry<String, File> entry : idToImageMap.entrySet()) {
                String natId = entry.getKey();
                File imgFile = entry.getValue();
                processedCount++;
                
                if (callback != null) {
                    callback.onProgress(processedCount, totalImages, "جاري البحث عن الطالب: " + natId);
                }

                // Search for student in DB
                int studentId = findStudentIdByNationalId(conn, natId);
                if (studentId > 0) {
                    // Logic to copy image
                    String savedPath = copyImageToStorage(imgFile, natId);
                    if (!savedPath.isEmpty()) {
                        // Update DB
                        updateStudentImagePath(conn, studentId, savedPath);
                        result.linkedCount++;
                        matchedIds.add(natId);
                    } else {
                        result.errors.add("فشل نسخ الصورة للطالب ذو الرقم القومي: " + natId);
                    }
                } else {
                    result.unmatchedImagesCount++;
                    result.unmatchedImageNames.add(imgFile.getName() + " (لا يوجد طالب مطابق في قاعدة البيانات)");
                }
            }

            // Calculate students without images
            result.studentsWithoutImagesCount = countStudentsWithoutImages(conn, matchedIds);

            LogService.logAction(username, "IMAGE_IMPORT", 
                "تم ربط " + result.linkedCount + " صورة بالطلاب من مجلد " + folder.getName());

        } catch (Exception e) {
            e.printStackTrace();
            result.errors.add("خطأ في قاعدة البيانات: " + e.getMessage());
        }

        return result;
    }

    private static int findStudentIdByNationalId(Connection conn, String natId) throws java.sql.SQLException {
        String sql = "SELECT id FROM students WHERE national_id = ? OR seat_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, natId);
            ps.setString(2, natId); // Fallback to seat_no if ID matches seat_no
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return -1;
    }

    private static int countStudentsWithoutImages(Connection conn, Set<String> newlyMatchedIds) throws java.sql.SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE image_path IS NULL OR image_path = ''";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private static String copyImageToStorage(File srcFile, String nationalId) {
        // Re-use logic from ExcelService findAndCopyImageUsingCache but simplified
        String safeFolderId = nationalId.trim().replaceAll("\\s+", "").replaceAll("[^a-zA-Z0-9\u0621-\u064A\u0660-\u0669.-]", "_");
        try {
            String userHome = System.getProperty("user.home");
            File studentFolder = new File(userHome, ".student_mgmt/students/" + safeFolderId + "/images");
            if (!studentFolder.exists()) studentFolder.mkdirs();

            String ext = ".jpg";
            int dotIdx = srcFile.getName().lastIndexOf('.');
            if (dotIdx > 0) ext = srcFile.getName().substring(dotIdx);

            File destFile = new File(studentFolder, "profile" + ext);
            
            // Using nio for efficiency
            java.nio.file.Files.copy(srcFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void updateStudentImagePath(Connection conn, int studentId, String path) throws java.sql.SQLException {
        String sql = "UPDATE students SET image_path = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }
}
