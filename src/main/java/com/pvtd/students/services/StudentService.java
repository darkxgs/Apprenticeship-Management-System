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
        String query = "SELECT * FROM students";
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

    public static List<Student> searchStudents(String keyword, String seatNo, String governorate, String profession,
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
        if (governorate != null && !governorate.trim().isEmpty() && !governorate.equals("الكل")) {
            query.append("AND governorate = ? ");
            parameters.add(governorate.trim());
        }
        if (profession != null && !profession.trim().isEmpty() && !profession.equals("الكل")) {
            query.append("AND profession = ? ");
            parameters.add(profession.trim());
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("الكل")) {
            query.append("AND status = ? ");
            parameters.add(status.trim());
        }
        if (centerName != null && !centerName.trim().isEmpty() && !centerName.equals("الكل")) {
            query.append("AND center_name = ? ");
            parameters.add(centerName.trim());
        }

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
        return DictionaryService.getCombinedItems(DictionaryService.CAT_REGION);
    }

    public static List<String> getDistinctCenters() {
        return DictionaryService.getCombinedItems(DictionaryService.CAT_CENTER);
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

    public static String generateUniqueSecretNo() {
        String secret = "";
        java.util.Random rnd = new java.util.Random();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM students WHERE secret_no = ?")) {
            boolean unique = false;
            while (!unique) {
                int number = 10000 + rnd.nextInt(90000); // 10000 to 99999
                secret = String.valueOf(number);
                stmt.setString(1, secret);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        unique = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            secret = String.valueOf(10000 + rnd.nextInt(90000));
        }
        return secret;
    }

    public static void addStudent(Student s, String username) {
        String query = "INSERT INTO students (serial, name, registration_no, national_id, region, profession, exam_system, seat_no, secret_no, professional_group, coordination_no, dob_day, dob_month, dob_year, gender, neighborhood, governorate, religion, nationality, address, other_notes, image_path, center_name, id_front_path, id_back_path, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, new String[] { "id" })) { // get generated ID

            if (s.getSecretNo() == null || s.getSecretNo().trim().isEmpty()) {
                s.setSecretNo(generateUniqueSecretNo());
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

            String tempStatus = s.getStatus();
            if (tempStatus == null || tempStatus.trim().isEmpty() || tempStatus.equals("غير محدد")
                    || tempStatus.equals("ناجح") || tempStatus.equals("راسب") || tempStatus.equals("دور ثاني")) {
                tempStatus = calculateStatus(s.getProfession(), s.getGrades());
            }
            stmt.setString(26, tempStatus);

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
        String query = "UPDATE students SET serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, exam_system=?, seat_no=?, secret_no=?, professional_group=?, coordination_no=?, dob_day=?, dob_month=?, dob_year=?, gender=?, neighborhood=?, governorate=?, religion=?, nationality=?, address=?, other_notes=?, image_path=?, center_name=?, id_front_path=?, id_back_path=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            if (s.getSecretNo() == null || s.getSecretNo().trim().isEmpty()) {
                s.setSecretNo(generateUniqueSecretNo());
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

            String tempStatus = s.getStatus();
            if (tempStatus == null || tempStatus.trim().isEmpty() || tempStatus.equals("غير محدد")
                    || tempStatus.equals("ناجح") || tempStatus.equals("راسب") || tempStatus.equals("دور ثاني")) {
                tempStatus = calculateStatus(s.getProfession(), s.getGrades());
            }
            stmt.setString(26, tempStatus);
            stmt.setInt(27, s.getId());

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
        // -1 = غائب
        // -2 = محروم
        // -3 = مفصول
        // -4 = معتذر
        // -5 = مؤجل
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

        int failedTheoryPracticalCount = 0; // Only نظري + عملي (NOT التطبيقي)
        boolean failedAppliedSubject = false;

        for (com.pvtd.students.models.Subject sub : subjects) {
            int obtained = grades.getOrDefault(sub.getId(), 0);

            // التطبيقي is identified by its name containing "تطبيقي"
            // Regular عملي (practical) subjects do NOT count as applied
            boolean isApplied = (sub.getName() != null && sub.getName().contains("تطبيقي"));

            if (obtained < sub.getPassMark()) {
                if (isApplied) {
                    failedAppliedSubject = true;
                } else {
                    // This is a نظري or عملي (non-applied) subject
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
}
