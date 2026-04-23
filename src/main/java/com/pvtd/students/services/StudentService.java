package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentService {

    public static Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();

        // Dynamically initialize all known statuses to 0
        List<String> allStatuses = StatusesService.getAllStatuses();
        for (String s : allStatuses) {
            stats.put(s, 0);
        }
        stats.put("total", 0);

        String query = "SELECT status, COUNT(*) as count FROM students GROUP BY status";
        int total = 0;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString("status");
                // Default fallback if a student has an empty or null status
                if (status == null || status.trim().isEmpty()) {
                    status = "غير محدد";
                }
                int count = rs.getInt("count");
                total += count;

                // If it's a completely new status not in DB, add it to map anyway
                stats.put(status, count);
            }
            stats.put("total", total);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static List<Student> getRecentStudents(int limit) {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM (SELECT * FROM students ORDER BY id DESC) WHERE ROWNUM <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student s = new Student();
                    s.setId(rs.getInt("id"));
                    s.setSeatNo(rs.getString("seat_no"));
                    s.setName(rs.getString("name"));
                    s.setStatus(rs.getString("status"));
                    students.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM students ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Student s = extractStudent(rs);
                s.setGrades(getStudentGrades(conn, s.getId()));
                students.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static List<Student> searchStudents(String keyword, String seatNo, String governorate, String region, String profession,
            String status, String centerName) {
        List<Student> students = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND (name LIKE ? OR national_id LIKE ? OR serial LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            parameters.add(term);
            parameters.add(term);
            parameters.add(term);
        }
        if (seatNo != null && !seatNo.trim().isEmpty()) {
            query.append("AND seat_no LIKE ? ");
            parameters.add("%" + seatNo.trim() + "%");
        }
        if (governorate != null && !governorate.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "").isEmpty() && !governorate.equals("الكل")) {
            query.append("AND TRIM(governorate) = ? ");
            parameters.add(governorate.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", ""));
        }
        if (region != null && !region.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "").isEmpty() && !region.equals("الكل")) {
            query.append("AND TRIM(region) = ? ");
            parameters.add(region.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", ""));
        }
        if (profession != null && !profession.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "").isEmpty() && !profession.equals("الكل")) {
            query.append("AND TRIM(profession) = ? ");
            parameters.add(profession.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", ""));
        }
        if (status != null && !status.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "").isEmpty() && !status.equals("الكل")) {
            query.append("AND TRIM(status) = ? ");
            parameters.add(status.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", ""));
        }
        if (centerName != null && !centerName.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "").isEmpty() && !centerName.equals("الكل")) {
            query.append("AND center_name LIKE ? ");
            parameters.add("%" + centerName.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "") + "%");
        }

        query.append(" ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student s = extractStudent(rs);
                    s.setGrades(getStudentGrades(conn, s.getId()));
                    students.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static List<String> getDistinctGovernorates() {
        return DictionaryService.getCombinedItems(DictionaryService.CAT_GOVERNORATE);
    }

    public static List<String> getDistinctCenters() {
        return DictionaryService.getCombinedItems(DictionaryService.CAT_CENTER);
    }

    /**
     * Returns a map of region name -> region code for the dropdowns.
     */
    public static java.util.Map<String, String> getRegionsWithCodes() {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        List<String> names = DictionaryService.getCombinedItems(DictionaryService.CAT_REGION);
        if (names.isEmpty()) return map;

        String sql = "SELECT name, code FROM regions WHERE TRIM(name) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String name : names) {
                stmt.setString(1, name.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String code = rs.getString("code");
                        map.put(name, (code != null && !code.trim().isEmpty()) ? code.trim() : name);
                    } else {
                        map.put(name, name);
                    }
                }
            }
        } catch (Exception e) {
            for (String n : names) map.put(n, n);
        }
        return map;
    }

    /**
     * Returns a map of center name -> center code for the DataEntry dropdown.
     * If a center has no code in the centers table, uses the name as fallback.
     */
    public static java.util.Map<String, String> getCentersWithCodes() {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        List<String> names = getDistinctCenters();
        if (names.isEmpty()) return map;

        String sql = "SELECT name, code FROM centers WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String name : names) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String code = rs.getString("code");
                        map.put(name, (code != null && !code.trim().isEmpty()) ? code.trim() : name);
                    } else {
                        map.put(name, name); // no code yet
                    }
                }
            }
        } catch (Exception e) {
            // Fallback: just use name as key and value
            for (String n : names) map.put(n, n);
        }
        return map;
    }

    public static java.util.Map<String, String> getCentersByRegionWithCodes(String regionName) {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        if (regionName == null || regionName.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "").isEmpty() || regionName.equals("الكل")) {
            return getCentersWithCodes(); // Fallback to all
        }

        String safeRegion = regionName.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "");

        // 1. Get from proper metadata (centers joined with regions)
        String sql = "SELECT c.name, c.code FROM centers c " +
                     "JOIN regions r ON c.region_id = r.id " +
                     "WHERE TRIM(r.name) = TRIM(?) ORDER BY c.code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, safeRegion);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String code = rs.getString("code");
                    map.put(name, (code != null && !code.trim().isEmpty()) ? code.trim() : name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 2. Also retrieve centers implicitly mapped to this region in the students table (useful for imported Excel data)
        String sqlFallback = "SELECT DISTINCT center_name FROM students WHERE TRIM(region) = TRIM(?) AND center_name IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlFallback)) {
            stmt.setString(1, safeRegion);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cName = rs.getString("center_name");
                    if (cName != null) {
                        cName = cName.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "");
                        if (!cName.isEmpty() && !map.containsKey(cName)) {
                        // Try to find if this implicit center has a code somehow
                        String codeSql = "SELECT code FROM centers WHERE TRIM(name) = ?";
                        try(PreparedStatement cStmt = conn.prepareStatement(codeSql)) {
                            cStmt.setString(1, cName);
                            try(ResultSet crs = cStmt.executeQuery()) {
                                if(crs.next()) {
                                    String code = crs.getString("code");
                                    map.put(cName, (code != null && !code.trim().isEmpty()) ? code.trim() : cName);
                                } else {
                                    map.put(cName, cName); // Fallback code is name
                                }
                            }
                        }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return map;
    }

    public static List<String> getDistinctProfessions() {
        return DictionaryService.getCombinedItems(DictionaryService.CAT_PROFESSION);
    }

    // Helper to map DB ResultSet to Student
    private static Student extractStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setSerial(rs.getString("serial"));
        s.setName(rs.getString("name"));
        s.setRegistrationNo(rs.getString("registration_no"));
        s.setNationalId(rs.getString("national_id"));
        s.setRegion(rs.getString("region"));
        s.setProfession(rs.getString("profession"));
        s.setExamSystem(rs.getString("exam_system"));
        s.setSeatNo(rs.getString("seat_no"));
        s.setSecretNo(rs.getString("secret_no"));
        s.setProfessionalGroup(rs.getString("professional_group"));
        s.setCoordinationNo(rs.getString("coordination_no"));
        s.setDobDay(rs.getString("dob_day"));
        s.setDobMonth(rs.getString("dob_month"));
        s.setDobYear(rs.getString("dob_year"));
        s.setGender(rs.getString("gender"));
        s.setNeighborhood(rs.getString("neighborhood"));
        s.setGovernorate(rs.getString("governorate"));
        s.setReligion(rs.getString("religion"));
        s.setNationality(rs.getString("nationality"));
        s.setAddress(rs.getString("address"));
        s.setOtherNotes(rs.getString("other_notes"));
        s.setImagePath(rs.getString("image_path"));
        s.setCenterName(rs.getString("center_name"));
        s.setIdFrontPath(rs.getString("id_front_path"));
        s.setIdBackPath(rs.getString("id_back_path"));
        s.setStatus(rs.getString("status"));
        
        // Use try-catch or safe check for phone_number to maintain backward compatibility if column is missing on older reads
        try {
            s.setPhoneNumber(rs.getString("phone_number"));
        } catch (SQLException ignore) { }

        return s;
    }

    // Helper to get dynamic grades for a student
    private static Map<Integer, Integer> getStudentGrades(Connection conn, int studentId) throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT subject_id, obtained_mark FROM student_grades WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("subject_id"), rs.getInt("obtained_mark"));
                }
            }
        }
        return map;
    }

    public static String autoGenerateSerial() {
        String query = "SELECT MAX(CAST(serial AS NUMBER)) as max_serial FROM students WHERE REGEXP_LIKE(serial, '^[0-9]+$')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                long max = rs.getLong("max_serial");
                if (max > 0) return String.valueOf(max + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "1";
    }

    private static String getRegionCode(Connection conn, String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) return "00";
        try (PreparedStatement stmt = conn.prepareStatement("SELECT code FROM regions WHERE name = ?")) {
            stmt.setString(1, regionName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    if (code != null && !code.isEmpty()) {
                        return String.format("%02d", Integer.parseInt(code));
                    }
                }
            }
        } catch (Exception e) {}
        return autoGenerateRegionCode(conn, regionName);
    }

    private static String autoGenerateRegionCode(Connection conn, String regionName) {
        try {
            int maxCode = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT code FROM regions");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String c = rs.getString("code");
                    if (c != null && c.matches("\\d+")) {
                        maxCode = Math.max(maxCode, Integer.parseInt(c));
                    }
                }
            }
            int nextCode = maxCode + 1;
            String newCodeStr = String.format("%02d", nextCode);
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO regions (name, code) VALUES (?, ?)")) {
                ins.setString(1, regionName);
                ins.setString(2, newCodeStr);
                ins.executeUpdate();
            }
            return newCodeStr;
        } catch (Exception e) {}
        return "00";
    }

    private static String getCenterCode(Connection conn, String centerName, String regionName) {
        if (centerName == null || centerName.trim().isEmpty()) return "000";
        try (PreparedStatement stmt = conn.prepareStatement("SELECT code FROM centers WHERE name = ?")) {
            stmt.setString(1, centerName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString("code");
                    if (code != null && !code.isEmpty()) {
                        return String.format("%03d", Integer.parseInt(code));
                    }
                }
            }
        } catch (Exception e) {}
        return autoGenerateCenterCode(conn, centerName, regionName);
    }

    public static void syncProfessionAndGroup(Connection conn, String profession, String profGroup) {
        if (profession == null || profession.trim().isEmpty()) return;
        profession = profession.trim();
        if (profGroup != null) profGroup = profGroup.trim();

        try {
            Integer pgId = null;
            if (profGroup != null && !profGroup.isEmpty()) {
                // Ensure profGroup exists
                try (PreparedStatement stmt = conn.prepareStatement("MERGE INTO professional_groups pg USING (SELECT ? n FROM DUAL) src ON (pg.name = src.n) WHEN NOT MATCHED THEN INSERT (name) VALUES (src.n)")) {
                    stmt.setString(1, profGroup);
                    stmt.executeUpdate();
                }
                // Get its ID
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM professional_groups WHERE name = ?")) {
                    stmt.setString(1, profGroup);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) pgId = rs.getInt(1);
                    }
                }
            }

            // Merge Profession
            if (pgId != null) {
                String sql = "MERGE INTO professions p USING (SELECT ? n, ? pgid FROM DUAL) src ON (p.name = src.n) WHEN NOT MATCHED THEN INSERT (name, professional_group_id) VALUES (src.n, src.pgid)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, profession);
                    stmt.setInt(2, pgId);
                    stmt.executeUpdate();
                }
            } else {
                String sql = "MERGE INTO professions p USING (SELECT ? n FROM DUAL) src ON (p.name = src.n) WHEN NOT MATCHED THEN INSERT (name) VALUES (src.n)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, profession);
                    stmt.executeUpdate();
                }
            }
        } catch (Exception e) {}
    }

    private static String autoGenerateCenterCode(Connection conn, String centerName, String regionName) {
        try {
            int maxCode = 0;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT code FROM centers");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String c = rs.getString("code");
                    if (c != null && c.matches("\\d+")) {
                        maxCode = Math.max(maxCode, Integer.parseInt(c));
                    }
                }
            }
            int nextCode = maxCode + 1;
            String newCodeStr = String.format("%03d", nextCode);
            
            Integer regionId = null;
            if (regionName != null && !regionName.isEmpty()) {
                try (PreparedStatement st = conn.prepareStatement("SELECT id FROM regions WHERE name = ?")) {
                    st.setString(1, regionName);
                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next()) regionId = rs.getInt(1);
                    }
                }
            }

            String sql = (regionId != null) 
                    ? "INSERT INTO centers (name, code, region_id) VALUES (?, ?, ?)"
                    : "INSERT INTO centers (name, code) VALUES (?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(sql)) {
                ins.setString(1, centerName);
                ins.setString(2, newCodeStr);
                if (regionId != null) ins.setInt(3, regionId);
                ins.executeUpdate();
            }
            return newCodeStr;
        } catch (Exception e) {}
        return "000";
    }

    public static int getSecretNumberIncrement() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT setting_value FROM system_settings WHERE setting_key = 'secret_number_increment'")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.parseInt(rs.getString("setting_value"));
                }
            }
        } catch (Exception e) {}
        return 10;
    }

    public static String generateSecretNo(Connection conn, String regionName, String centerName, String seatNo, int increment) {
        String regionCode = getRegionCode(conn, regionName);
        if (regionCode.length() > 2) regionCode = regionCode.substring(0, 2);
        else if (regionCode.length() < 2) regionCode = String.format("%02d", Integer.parseInt(regionCode));

        String centerCode = getCenterCode(conn, centerName, regionName);
        if (centerCode.length() > 3) centerCode = centerCode.substring(0, 3);
        else if (centerCode.length() < 3) centerCode = String.format("%03d", Integer.parseInt(centerCode));

        if (seatNo == null || seatNo.trim().isEmpty()) seatNo = "000";
        String seatPrefix = seatNo.length() >= 3 ? seatNo.substring(0, 3) : String.format("%03d", Integer.parseInt(seatNo));
        int seatNumInt = 0;
        try {
            seatNumInt = Integer.parseInt(seatPrefix);
        } catch (NumberFormatException ignored) {}

        int lastDigitCombined = seatNumInt + increment;
        return regionCode + centerCode + lastDigitCombined;
    }

    public static String generateSecretNo(String regionName, String centerName, String seatNo) {
        int increment = getSecretNumberIncrement();
        try (Connection conn = DatabaseConnection.getConnection()) {
            return generateSecretNo(conn, regionName, centerName, seatNo, increment);
        } catch (Exception e) {}
        return "00000" + seatNo;
    }

    public static String generateSecretNo(Student s) {
        return generateSecretNo(s.getRegion(), s.getCenterName(), s.getSeatNo());
    }

    public static void addStudent(Student s, String username) {
        String query = "INSERT INTO students (serial, name, registration_no, national_id, region, profession, exam_system, seat_no, secret_no, professional_group, coordination_no, dob_day, dob_month, dob_year, gender, neighborhood, governorate, religion, nationality, address, other_notes, image_path, center_name, id_front_path, id_back_path, status, phone_number) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, new String[] { "id" })) { // get generated ID

            // Handle Serial Auto Generation
            if (s.getSerial() == null || s.getSerial().trim().isEmpty()) {
                s.setSerial(autoGenerateSerial());
            }

            // Handle Secret Number Auto Generation
            s.setSecretNo(generateSecretNo(s));

            stmt.setString(1, s.getSerial());
            stmt.setString(2, s.getName());
            stmt.setString(3, s.getRegistrationNo());
            stmt.setString(4, s.getNationalId());
            stmt.setString(5, s.getRegion());
            stmt.setString(6, s.getProfession());
            stmt.setString(7, s.getExamSystem());
            stmt.setString(8, s.getSeatNo());
            stmt.setString(9, s.getSecretNo());
            stmt.setString(10, s.getProfessionalGroup());
            stmt.setString(11, s.getCoordinationNo());
            stmt.setString(12, s.getDobDay());
            stmt.setString(13, s.getDobMonth());
            stmt.setString(14, s.getDobYear());
            stmt.setString(15, s.getGender());
            stmt.setString(16, s.getNeighborhood());
            stmt.setString(17, s.getGovernorate());
            stmt.setString(18, s.getReligion());
            stmt.setString(19, s.getNationality());
            stmt.setString(20, s.getAddress());
            stmt.setString(21, s.getOtherNotes());
            stmt.setString(22, s.getImagePath());
            stmt.setString(23, s.getCenterName());
            stmt.setString(24, s.getIdFrontPath());
            stmt.setString(25, s.getIdBackPath());

            String tempStatus = s.getStatus();
            if (tempStatus == null || tempStatus.trim().isEmpty() || tempStatus.equals("غير محدد")
                    || tempStatus.equals("ناجح") || tempStatus.equals("راسب") || tempStatus.equals("دور ثاني")) {
                tempStatus = calculateStatus(s.getProfession(), s.getGrades());
            }
            stmt.setString(26, tempStatus);
            stmt.setString(27, s.getPhoneNumber());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // Get inserted ID
                int newStudentId = 0;
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next())
                        newStudentId = rs.getInt(1);
                }

                // Insert generic grades
                if (newStudentId > 0 && s.getGrades() != null) {
                    saveStudentGrades(conn, newStudentId, s.getGrades());
                }
                LogService.logAction(username, "ADD_STUDENT", "تم إضافة الطالب بنجاح: " + s.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStudent(Student s, String username) {
        String query = "UPDATE students SET serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, exam_system=?, seat_no=?, secret_no=?, professional_group=?, coordination_no=?, dob_day=?, dob_month=?, dob_year=?, gender=?, neighborhood=?, governorate=?, religion=?, nationality=?, address=?, other_notes=?, image_path=?, center_name=?, id_front_path=?, id_back_path=?, status=?, phone_number=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            // Handle Secret Number Auto Generation
            if (s.getSecretNo() == null || s.getSecretNo().trim().isEmpty()) {
                s.setSecretNo(generateSecretNo(s));
            }

            stmt.setString(1, s.getSerial());
            stmt.setString(2, s.getName());
            stmt.setString(3, s.getRegistrationNo());
            stmt.setString(4, s.getNationalId());
            stmt.setString(5, s.getRegion());
            stmt.setString(6, s.getProfession());
            stmt.setString(7, s.getExamSystem());
            stmt.setString(8, s.getSeatNo());
            stmt.setString(9, s.getSecretNo());
            stmt.setString(10, s.getProfessionalGroup());
            stmt.setString(11, s.getCoordinationNo());
            stmt.setString(12, s.getDobDay());
            stmt.setString(13, s.getDobMonth());
            stmt.setString(14, s.getDobYear());
            stmt.setString(15, s.getGender());
            stmt.setString(16, s.getNeighborhood());
            stmt.setString(17, s.getGovernorate());
            stmt.setString(18, s.getReligion());
            stmt.setString(19, s.getNationality());
            stmt.setString(20, s.getAddress());
            stmt.setString(21, s.getOtherNotes());
            stmt.setString(22, s.getImagePath());
            stmt.setString(23, s.getCenterName());
            stmt.setString(24, s.getIdFrontPath());
            stmt.setString(25, s.getIdBackPath());

            String currentStatus = s.getStatus();
            if (currentStatus == null || currentStatus.trim().isEmpty() || currentStatus.equals("غير محدد")
                    || currentStatus.equals("ناجح") || currentStatus.equals("راسب") || currentStatus.equals("دور ثاني")) {
                currentStatus = calculateStatus(s.getProfession(), s.getGrades());
            }
            stmt.setString(26, currentStatus);
            stmt.setString(27, s.getPhoneNumber());
            stmt.setInt(28, s.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // Wipe old grades and re-insert new ones map
                deleteStudentGrades(conn, s.getId());
                if (s.getGrades() != null) {
                    saveStudentGrades(conn, s.getId(), s.getGrades());
                }
                LogService.logAction(username, "UPDATE_STUDENT", "تم تعديل بيانات الطالب: " + s.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStudent(int id, String username) {
        String query = "DELETE FROM students WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LogService.logAction(username, "DELETE_STUDENT", "تم حذف الطالب ذو المعرف: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveStudentGrades(Connection conn, int studentId, Map<Integer, Integer> grades)
            throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM student_grades WHERE student_id=? AND subject_id=?";
        String insertSql = "INSERT INTO student_grades (student_id, subject_id, obtained_mark) VALUES (?,?,?)";
        String updateSql = "UPDATE student_grades SET obtained_mark=? WHERE student_id=? AND subject_id=?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            for (Map.Entry<Integer, Integer> entry : grades.entrySet()) {
                int subjectId = entry.getKey();
                int mark = entry.getValue();

                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, subjectId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    updateStmt.setInt(1, mark);
                    updateStmt.setInt(2, studentId);
                    updateStmt.setInt(3, subjectId);
                    updateStmt.executeUpdate();
                } else {
                    insertStmt.setInt(1, studentId);
                    insertStmt.setInt(2, subjectId);
                    insertStmt.setInt(3, mark);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    private static void deleteStudentGrades(Connection conn, int studentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM student_grades WHERE student_id=?")) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
    }

    public static boolean updateStudentGrades(int studentId, Map<Integer, Integer> grades, String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                saveStudentGrades(conn, studentId, grades);
                conn.commit();
                LogService.logAction(username, "UPDATE_GRADES", "تم تحديث درجات الطالب ذو المعرف: " + studentId);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateStudentStatusDirectly(int studentId, String status) {
        String sql = "UPDATE students SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, studentId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String calculateStatus(String profession, Map<Integer, Integer> grades) {
        if (grades == null || grades.isEmpty())
            return "غير محدد";

        // Check for specific negative markers first (global status override)
        for (Integer mark : grades.values()) {
            if (mark != null) {
                if (mark == -1) return "غائب";
                if (mark == -2) return "محروم";
                if (mark == -3) return "مفصول";
                if (mark == -4) return "معتذر";
                if (mark == -5) return "مؤجل";
            }
        }

        // Fetch passing rules for the subjects of this profession
        List<com.pvtd.students.models.Subject> subjects = SubjectService.getSubjectsByProfession(profession);
        if (subjects.isEmpty())
            return "غير محدد";

        // Resolve composite totals (sum children into parents)
        Map<Integer, Integer> resGrades = GradeCalculationService.resolveCompositeGrades(subjects, grades);

        int failedTheoryPracticalCount = 0; // Only نظري + عملي (NOT التطبيقي)
        boolean failedAppliedSubject = false;

        for (com.pvtd.students.models.Subject sub : subjects) {
            // Only evaluate pass/fail for top-level subjects
            if (sub.getParentSubjectId() != null) continue;

            int obtained = resGrades.getOrDefault(sub.getId(), 0);

            // التطبيقي is identified by its name containing "تطبيقي"
            boolean isApplied = (sub.getName() != null && sub.getName().contains("تطبيقي"));

            if (obtained < sub.getPassMark()) {
                if (isApplied) {
                    failedAppliedSubject = true;
                } else {
                    failedTheoryPracticalCount++;
                }
            }
        }

        // Rule 1: Failed التطبيقي → راسب
        if (failedAppliedSubject) {
            return "راسب";
        }
        // Rule 2: Failed 3 or more نظري+عملي subjects → راسب
        if (failedTheoryPracticalCount >= 3) {
            return "راسب";
        }
        // Rule 3: Passed التطبيقي, failed 1 or 2 نظري+عملي → دور ثاني
        if (failedTheoryPracticalCount > 0) {
            return "دور ثاني";
        }
        // Rule 4: Passed التطبيقي AND all نظري+عملي → ناجح
        return "ناجح";
    }

    /**
     * Fetch students filtered by profession, optional center, and status.
     * Includes their grades map.
     */
    public static List<Student> getStudentsByProfessionAndStatus(
            String profession, String centerName, String status) {

        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT s.*, sg.subject_id, sg.obtained_mark " +
            "FROM students s " +
            "LEFT JOIN student_grades sg ON s.id = sg.student_id " +
            "WHERE s.profession = ? AND s.status = ?");
        if (centerName != null) sql.append(" AND s.center_name = ?");
        sql.append(" ORDER BY s.seat_no");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, profession);
            ps.setString(2, status);
            if (centerName != null) ps.setString(3, centerName);
            ResultSet rs = ps.executeQuery();

            Map<Integer, Student> map = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                int sid = rs.getInt("id");
                if (!map.containsKey(sid)) {
                    Map<Integer, Integer> grades = new HashMap<>();
                    Student st = new Student(
                        sid, rs.getString("serial"), rs.getString("name"),
                        rs.getString("registration_no"), rs.getString("national_id"),
                        rs.getString("region"), rs.getString("center_name"),
                        rs.getString("profession"), rs.getString("exam_system"),
                        rs.getString("seat_no"), rs.getString("secret_no"),
                        rs.getString("professional_group"), rs.getString("coordination_no"),
                        rs.getString("dob_day"), rs.getString("dob_month"), rs.getString("dob_year"),
                        rs.getString("gender"), rs.getString("neighborhood"),
                        rs.getString("governorate"), rs.getString("religion"),
                        rs.getString("nationality"), rs.getString("address"),
                        rs.getString("other_notes"), rs.getString("image_path"),
                        rs.getString("id_front_path"), rs.getString("id_back_path"),
                        grades, rs.getString("status"));
                    map.put(sid, st);
                }
                int subjectId = rs.getInt("subject_id");
                if (!rs.wasNull()) {
                    map.get(sid).getGrades().put(subjectId, rs.getInt("obtained_mark"));
                }
            }
            list.addAll(map.values());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
