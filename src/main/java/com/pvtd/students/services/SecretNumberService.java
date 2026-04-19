package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * خدمة توليد وإدارة الأرقام السرية
 */
public class SecretNumberService {

    /**
     * توليد الرقم السري لطالب واحد بناءً على الخوارزمية:
     * [كود المنطقة] + [كود المركز] + [عكس آخر 3 أرقام من رقم الجلوس] + الحسبة (الفاكتور)
     */
    public static String generateSecretNumber(String regionName, String centerName, String seatNo) {
        if (seatNo == null || seatNo.trim().isEmpty()) return "";

        String rCode = getRegionCode(regionName);
        String cCode = getCenterCode(centerName);

        // تنظيف رقم الجلوس وأخذ آخر 3 أرقام
        String cleanSeat = seatNo.trim().replaceAll("[^0-9]", "");
        while (cleanSeat.length() < 3) cleanSeat = "0" + cleanSeat;
        String last3 = cleanSeat.substring(cleanSeat.length() - 3);
        
        // تجميع الرقم: كود المنطقة + كود المركز + آخر 3 أرقام (بدون عكس)
        String combined = rCode + cCode + last3;

        // إضافة الفاكتور (secret_number_increment) من الإعدادات
        int factor = getSecretNumberFactor();
        
        try {
            long result = Long.parseLong(combined) + factor;
            return String.valueOf(result);
        } catch (NumberFormatException e) {
            return combined; // في حالة وجود حروف أو أرقام ضخمة جداً
        }
    }

    private static String getRegionCode(String name) {
        if (name == null) return "0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT code FROM regions WHERE name = ?")) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    return (code != null) ? code.trim() : "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    private static String getCenterCode(String name) {
        if (name == null) return "0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT code FROM centers WHERE name = ?")) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    return (code != null) ? code.trim() : "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    private static int getSecretNumberFactor() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT setting_value FROM system_settings WHERE setting_key = 'secret_number_increment'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Integer.parseInt(rs.getString("setting_value").trim());
            }
        } catch (Exception ignored) {}
        return 0; // Default factor
    }

    /**
     * تحديث الأرقام السرية لجميع الطلاب الذين ليس لديهم رقم سري (أو للكل حسب الرغبة)
     */
    public static int generateForAllMissing() {
        int updated = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psLoad = conn.prepareStatement("SELECT id, name, region, center_name, seat_no FROM students WHERE secret_no IS NULL");
             PreparedStatement psUpdate = conn.prepareStatement("UPDATE students SET secret_no = ? WHERE id = ?");
             ResultSet rs = psLoad.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String secret = generateSecretNumber(rs.getString("region"), rs.getString("center_name"), rs.getString("seat_no"));
                if (!secret.isEmpty()) {
                    psUpdate.setString(1, secret);
                    psUpdate.setInt(2, id);
                    psUpdate.addBatch();
                    updated++;
                }
            }
            psUpdate.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updated;
    }
}
