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
     * توليد رقم سري متسلسل للدور الثاني:
     * [كود المنطقة] + [كود المركز] + [رقم متسلسل من 3 خانات] + الفاكتور
     * يستخدم عداد متسلسل بدل آخر 3 أرقام من الجلوس عشان الأرقام تطلع متجاورة.
     */
    public static String generateSequential(String regionName, String centerName, int seq) {
        String rCode = getRegionCode(regionName);
        String cCode = getCenterCode(centerName);

        String seqStr = String.format("%03d", seq);
        String combined = rCode + cCode + seqStr;

        int factor = getSecretNumberFactor();
        try {
            long result = Long.parseLong(combined) + factor;
            return String.valueOf(result);
        } catch (NumberFormatException e) {
            return combined;
        }
    }

    /**
     * إعادة توليد الأرقام السرية لكل طلاب الدور الثاني (والمؤجلين المسموح لهم)
     * دفعة واحدة: لكل مركز عداد مستقل يبدأ من ١ فتطلع الأرقام متسلسلة ومتجاورة
     * بدون أي طلاب آخرين بينهم. يستبدل الرقم السري الحالي (secret_no).
     *
     * @param statuses قائمة الحالات المستهدفة (مثال: دور ثاني، مؤجل، ناجح دور ثاني)
     * @return عدد الطلاب اللي اتحدثت أرقامهم
     */
    public static int regenerateSecondRoundSecretNumbers(java.util.List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return 0;

        StringBuilder in = new StringBuilder();
        for (int i = 0; i < statuses.size(); i++) {
            if (i > 0) in.append(", ");
            in.append("?");
        }

        String selectSql = "SELECT id, region, center_name, seat_no FROM students "
                + "WHERE TRIM(status) IN (" + in + ") "
                + "ORDER BY center_name, "
                + "CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";

        int updated = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psLoad = conn.prepareStatement(selectSql);
             PreparedStatement psUpdate = conn.prepareStatement("UPDATE students SET secret_no = ? WHERE id = ?")) {

            for (int i = 0; i < statuses.size(); i++) {
                psLoad.setString(i + 1, statuses.get(i).trim());
            }

            try (ResultSet rs = psLoad.executeQuery()) {
                String lastCenter = null;
                int counter = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String region = rs.getString("region");
                    String center = rs.getString("center_name");

                    // عداد مستقل لكل مركز
                    if (lastCenter == null || !java.util.Objects.equals(lastCenter, center)) {
                        counter = 0;
                        lastCenter = center;
                    }
                    counter++;

                    String secret = generateSequential(region, center, counter);
                    if (secret != null && !secret.isEmpty()) {
                        psUpdate.setString(1, secret);
                        psUpdate.setInt(2, id);
                        psUpdate.addBatch();
                        updated++;
                    }
                }
            }
            psUpdate.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updated;
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
