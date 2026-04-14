package com.pvtd.students.services;

import java.util.HashMap;
import java.util.Map;

/**
 * خدمة التحقق من صحة البيانات — الرقم القومي المصري ورقم التليفون
 */
public class ValidationService {

    // Egyptian National ID structure (14 digits):
    // [Century 1][Year 2][Month 2][Day 2][Governorate 2][Serial 5] → last digit is check/gender
    // Century: 2=1900s, 3=2000s
    // Gender: odd serial ending = Male, even = Female

    private static final Map<String, String> GOVERNORATE_CODES = new HashMap<>();
    static {
        GOVERNORATE_CODES.put("01", "القاهرة");
        GOVERNORATE_CODES.put("02", "الإسكندرية");
        GOVERNORATE_CODES.put("03", "بورسعيد");
        GOVERNORATE_CODES.put("04", "السويس");
        GOVERNORATE_CODES.put("11", "دمياط");
        GOVERNORATE_CODES.put("12", "الدقهلية");
        GOVERNORATE_CODES.put("13", "الشرقية");
        GOVERNORATE_CODES.put("14", "القليوبية");
        GOVERNORATE_CODES.put("15", "كفر الشيخ");
        GOVERNORATE_CODES.put("16", "الغربية");
        GOVERNORATE_CODES.put("17", "المنوفية");
        GOVERNORATE_CODES.put("18", "البحيرة");
        GOVERNORATE_CODES.put("19", "الإسماعيلية");
        GOVERNORATE_CODES.put("21", "الجيزة");
        GOVERNORATE_CODES.put("22", "بني سويف");
        GOVERNORATE_CODES.put("23", "الفيوم");
        GOVERNORATE_CODES.put("24", "المنيا");
        GOVERNORATE_CODES.put("25", "أسيوط");
        GOVERNORATE_CODES.put("26", "سوهاج");
        GOVERNORATE_CODES.put("27", "قنا");
        GOVERNORATE_CODES.put("28", "أسوان");
        GOVERNORATE_CODES.put("29", "الأقصر");
        GOVERNORATE_CODES.put("31", "البحر الأحمر");
        GOVERNORATE_CODES.put("32", "الوادي الجديد");
        GOVERNORATE_CODES.put("33", "مطروح");
        GOVERNORATE_CODES.put("34", "شمال سيناء");
        GOVERNORATE_CODES.put("35", "جنوب سيناء");
        GOVERNORATE_CODES.put("88", "خارج مصر");
    }

    /**
     * Result object for national ID validation
     */
    public static class NationalIdInfo {
        public final boolean valid;
        public final String errorMessage;
        public final String dobDay;
        public final String dobMonth;
        public final String dobYear;
        public final String gender;        // "ذكر" or "أنثى"
        public final String governorate;   // Arabic name

        public NationalIdInfo(boolean valid, String errorMessage, String dobDay, String dobMonth,
                              String dobYear, String gender, String governorate) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.dobDay = dobDay;
            this.dobMonth = dobMonth;
            this.dobYear = dobYear;
            this.gender = gender;
            this.governorate = governorate;
        }

        public static NationalIdInfo invalid(String msg) {
            return new NationalIdInfo(false, msg, null, null, null, null, null);
        }
    }

    /**
     * Validates an Egyptian national ID (14 digits) and extracts personal info.
     */
    public static NationalIdInfo validateNationalId(String nationalId) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return NationalIdInfo.invalid("الرقم القومي فارغ");
        }

        String id = nationalId.trim().replaceAll("[^0-9]", "");

        if (id.length() != 14) {
            return NationalIdInfo.invalid("الرقم القومي يجب أن يكون 14 رقماً (الحالي: " + id.length() + ")");
        }

        // Parse century
        char centuryChar = id.charAt(0);
        int centuryBase;
        if (centuryChar == '2') {
            centuryBase = 1900;
        } else if (centuryChar == '3') {
            centuryBase = 2000;
        } else {
            return NationalIdInfo.invalid("رقم القرن غير صحيح: " + centuryChar);
        }

        // Parse date
        int year = centuryBase + Integer.parseInt(id.substring(1, 3));
        int month = Integer.parseInt(id.substring(3, 5));
        int day = Integer.parseInt(id.substring(5, 7));

        if (month < 1 || month > 12) {
            return NationalIdInfo.invalid("الشهر غير صحيح: " + month);
        }
        if (day < 1 || day > 31) {
            return NationalIdInfo.invalid("اليوم غير صحيح: " + day);
        }

        // Governorate
        String govCode = id.substring(7, 9);
        String governorate = GOVERNORATE_CODES.getOrDefault(govCode, "غير معروف (" + govCode + ")");

        // Gender: 13th digit (index 12) — odd = male, even = female
        int genderDigit = Character.getNumericValue(id.charAt(12));
        String gender = (genderDigit % 2 != 0) ? "ذكر" : "أنثى";

        String dobDay = String.format("%02d", day);
        String dobMonth = String.format("%02d", month);
        String dobYear = String.valueOf(year);

        return new NationalIdInfo(true, null, dobDay, dobMonth, dobYear, gender, governorate);
    }

    /**
     * Validates an Egyptian phone number (01XXXXXXXXX — 11 digits).
     */
    public static String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // Not required
        }
        String cleaned = phone.trim().replaceAll("[^0-9]", "");
        if (cleaned.length() != 11) {
            return "رقم التليفون يجب أن يكون 11 رقماً";
        }
        if (!cleaned.startsWith("01")) {
            return "رقم التليفون يجب أن يبدأ بـ 01";
        }
        char third = cleaned.charAt(2);
        if (third != '0' && third != '1' && third != '2' && third != '5') {
            return "بادئة الشبكة غير صحيحة (يجب أن تكون 010, 011, 012, أو 015)";
        }
        return null; // Valid
    }
}
