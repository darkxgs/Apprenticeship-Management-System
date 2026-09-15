package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * خدمة بيانات الدور (دور الامتحان).
 *
 * إدارة الامتحانات تضبط شهر الدور الأول وشهر الدور الثاني وسنة الامتحان مرة
 * واحدة من إعدادات النظام، وكل الشهادات والاستمارات تشتق الدور تلقائياً من
 * حالة الطالب — بدون أي سؤال للمستخدم وبدون أي قيمة ثابتة داخل الكود.
 *
 * القاعدة: طالب الدور الثاني -> شهر الدور الثاني، وأي حالة أخرى -> شهر الدور الأول.
 * السنة واحدة للدورين.
 */
public class ExamSessionService {

    /** مفاتيح الإعدادات في جدول system_settings */
    public static final String KEY_ROUND1_MONTH = "exam_round1_month";
    public static final String KEY_ROUND2_MONTH = "exam_round2_month";
    public static final String KEY_EXAM_YEAR    = "exam_year";

    /** القيم الافتراضية عند غياب الإعداد من قاعدة البيانات */
    public static final String DEFAULT_ROUND1_MONTH = "مايو";
    public static final String DEFAULT_ROUND2_MONTH = "أغسطس";

    /** حالات الطلاب التي تعتبر «دور ثاني» */
    private static final java.util.Set<String> SECOND_ROUND_STATUSES = new java.util.HashSet<>(
            java.util.Arrays.asList(
                    "دور ثاني",
                    "مؤجل",
                    "ناجح دور ثاني",
                    "ناجح من الدور الثاني",
                    "راسب من الدور الثاني"));

    // ─────────────────────────── الكاش (يُقرأ لكل طالب) ───────────────────────────

    private static String cachedRound1Month = null;
    private static String cachedRound2Month = null;
    private static Integer cachedExamYear   = null;

    private ExamSessionService() {
        // خدمة ثابتة — لا تُنشأ
    }

    /** مسح الكاش ليُعاد تحميل الإعدادات من قاعدة البيانات */
    public static synchronized void reload() {
        cachedRound1Month = null;
        cachedRound2Month = null;
        cachedExamYear    = null;
    }

    /** شهر الدور الأول (افتراضياً «مايو») */
    public static synchronized String getRound1Month() {
        if (cachedRound1Month == null) {
            cachedRound1Month = readSetting(KEY_ROUND1_MONTH, DEFAULT_ROUND1_MONTH);
        }
        return cachedRound1Month;
    }

    /** شهر الدور الثاني (افتراضياً «أغسطس») */
    public static synchronized String getRound2Month() {
        if (cachedRound2Month == null) {
            cachedRound2Month = readSetting(KEY_ROUND2_MONTH, DEFAULT_ROUND2_MONTH);
        }
        return cachedRound2Month;
    }

    /** سنة الامتحان (افتراضياً السنة الحالية) */
    public static synchronized int getExamYear() {
        if (cachedExamYear == null) {
            int fallback = java.time.LocalDate.now().getYear();
            String raw = readSetting(KEY_EXAM_YEAR, String.valueOf(fallback));
            int parsed = fallback;
            try {
                parsed = Integer.parseInt(raw.trim().replaceAll("[^0-9]", ""));
            } catch (Exception ignore) {
                parsed = fallback;
            }
            if (parsed < 1900 || parsed > 2999) parsed = fallback;
            cachedExamYear = parsed;
        }
        return cachedExamYear;
    }

    /** حفظ الإعدادات الثلاثة معاً ثم تحديث الكاش */
    public static synchronized void save(String round1Month, String round2Month, int examYear) throws Exception {
        String m1 = (round1Month == null || round1Month.trim().isEmpty())
                ? DEFAULT_ROUND1_MONTH : round1Month.trim();
        String m2 = (round2Month == null || round2Month.trim().isEmpty())
                ? DEFAULT_ROUND2_MONTH : round2Month.trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            writeSetting(conn, KEY_ROUND1_MONTH, m1);
            writeSetting(conn, KEY_ROUND2_MONTH, m2);
            writeSetting(conn, KEY_EXAM_YEAR, String.valueOf(examYear));
        }

        cachedRound1Month = m1;
        cachedRound2Month = m2;
        cachedExamYear    = examYear;
    }

    // ─────────────────────────── اشتقاق الدور من الحالة ───────────────────────────

    /** هل الطالب من طلاب الدور الثاني؟ */
    public static boolean isSecondRound(String status) {
        if (status == null) return false;
        return SECOND_ROUND_STATUSES.contains(status.trim());
    }

    /** شهر الدور المناسب لحالة الطالب: الدور الثاني أو الدور الأول */
    public static String monthForStatus(String status) {
        return isSecondRound(status) ? getRound2Month() : getRound1Month();
    }

    /**
     * سطر الدور الكامل كما يظهر على الشهادات، مثال:
     * «دور / مايو عام 2026 الميلادية / ألفان وستة وعشرون ،،،»
     */
    public static String sessionLine(String status) {
        int year = getExamYear();
        return "دور / " + monthForStatus(status) + " عام " + year
                + " الميلادية / " + yearInArabicWords(year) + " ،،،";
    }

    // ─────────────────────────── السنة بالحروف ───────────────────────────

    /**
     * تحويل السنة إلى حروف عربية (بدون بادئة «سنة»)، مثال: 2026 -> «ألفان وستة وعشرون».
     * هذه هي النسخة الوحيدة من المحوّل في المشروع.
     */
    public static String yearInArabicWords(int year) {

        String[] ones = {
            "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة",
            "ستة", "سبعة", "ثمانية", "تسعة"
        };

        String[] tens = {
            "", "عشرة", "عشرون", "ثلاثون", "أربعون",
            "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"
        };

        int thousands = year / 1000;
        int remainder = year % 1000;

        String result = "";

        // آلاف
        if (thousands == 2) {
            result += "ألفان";
        } else if (thousands == 1) {
            result += "ألف";
        } else if (thousands > 2) {
            result += ones[thousands] + " آلاف";
        }

        // باقي الرقم (زي 26 في 2026)
        if (remainder > 0) {
            int lastTwo = remainder % 100;
            int t = lastTwo / 10;
            int o = lastTwo % 10;

            result += " و";

            if (o > 0) {
                result += ones[o];
                if (t > 0) result += " و";
            }

            if (t > 0) {
                result += tens[t];
            }
        }

        return result;
    }

    /** نفس التحويل مسبوقاً بكلمة «سنة» كما في شهادة النجاح */
    public static String yearInArabicWordsWithPrefix(int year) {
        return "سنة " + yearInArabicWords(year);
    }

    // ─────────────────────────── قراءة/كتابة الإعدادات ───────────────────────────

    private static String readSetting(String key, String fallback) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT setting_value FROM system_settings WHERE setting_key = ?")) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String v = rs.getString("setting_value");
                    if (v != null && !v.trim().isEmpty()) return v.trim();
                }
            }
        } catch (Exception ignore) {
            // الإعداد غير موجود أو قاعدة البيانات غير متاحة — نكمل بالقيمة الافتراضية
        }
        return fallback;
    }

    private static void writeSetting(Connection conn, String key, String value) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(
                "MERGE INTO system_settings s USING (SELECT ? k, ? v FROM DUAL) src " +
                "ON (s.setting_key = src.k) " +
                "WHEN MATCHED THEN UPDATE SET setting_value = src.v " +
                "WHEN NOT MATCHED THEN INSERT (setting_key, setting_value) VALUES (src.k, src.v)")) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        }
    }
}
