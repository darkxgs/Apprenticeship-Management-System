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
                    s.setSpecializationId(rs.getInt("specialization_id"));
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
            String status) {
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

    // Helper to map DB ResultSet to Student
    private static Student extractStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setSpecializationId(rs.getInt("specialization_id"));
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
        s.setSchool(rs.getString("school"));
        s.setAcademicYear(rs.getString("academic_year"));
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

    public static void addStudent(Student s) {
        String query = "INSERT INTO students (specialization_id, serial, name, registration_no, national_id, region, profession, exam_system, seat_no, secret_no, professional_group, coordination_no, dob_day, dob_month, dob_year, gender, neighborhood, governorate, religion, nationality, address, school, academic_year, other_notes, image_path, center_name, id_front_path, id_back_path, status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, new String[] { "id" })) { // get generated ID

            stmt.setInt(1, s.getSpecializationId());
            stmt.setString(2, s.getSerial());
            stmt.setString(3, s.getName());
            stmt.setString(4, s.getRegistrationNo());
            stmt.setString(5, s.getNationalId());
            stmt.setString(6, s.getRegion());
            stmt.setString(7, s.getProfession());
            stmt.setString(8, s.getExamSystem());
            stmt.setString(9, s.getSeatNo());
            stmt.setString(10, s.getSecretNo());
            stmt.setString(11, s.getProfessionalGroup());
            stmt.setString(12, s.getCoordinationNo());
            stmt.setString(13, s.getDobDay());
            stmt.setString(14, s.getDobMonth());
            stmt.setString(15, s.getDobYear());
            stmt.setString(16, s.getGender());
            stmt.setString(17, s.getNeighborhood());
            stmt.setString(18, s.getGovernorate());
            stmt.setString(19, s.getReligion());
            stmt.setString(20, s.getNationality());
            stmt.setString(21, s.getAddress());
            stmt.setString(22, s.getSchool());
            stmt.setString(23, s.getAcademicYear());
            stmt.setString(24, s.getOtherNotes());
            stmt.setString(25, s.getImagePath());
            stmt.setString(26, s.getCenterName());
            stmt.setString(27, s.getIdFrontPath());
            stmt.setString(28, s.getIdBackPath());

            // Administrative Status Override Logic
            String tempStatus = s.getStatus();
            if (tempStatus == null || tempStatus.trim().isEmpty() || tempStatus.equals("غير محدد")
                    || tempStatus.equals("ناجح") || tempStatus.equals("راسب") || tempStatus.equals("دور ثاني")) {
                tempStatus = calculateStatus(s.getSpecializationId(), s.getGrades());
            }
            stmt.setString(29, tempStatus);

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
                LogService.logAction("SYSTEM", "ADD_STUDENT", "تم إضافة الطالب بنجاح: " + s.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStudent(Student s) {
        String query = "UPDATE students SET specialization_id=?, serial=?, name=?, registration_no=?, national_id=?, region=?, profession=?, exam_system=?, seat_no=?, secret_no=?, professional_group=?, coordination_no=?, dob_day=?, dob_month=?, dob_year=?, gender=?, neighborhood=?, governorate=?, religion=?, nationality=?, address=?, school=?, academic_year=?, other_notes=?, image_path=?, center_name=?, id_front_path=?, id_back_path=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, s.getSpecializationId());
            stmt.setString(2, s.getSerial());
            stmt.setString(3, s.getName());
            stmt.setString(4, s.getRegistrationNo());
            stmt.setString(5, s.getNationalId());
            stmt.setString(6, s.getRegion());
            stmt.setString(7, s.getProfession());
            stmt.setString(8, s.getExamSystem());
            stmt.setString(9, s.getSeatNo());
            stmt.setString(10, s.getSecretNo());
            stmt.setString(11, s.getProfessionalGroup());
            stmt.setString(12, s.getCoordinationNo());
            stmt.setString(13, s.getDobDay());
            stmt.setString(14, s.getDobMonth());
            stmt.setString(15, s.getDobYear());
            stmt.setString(16, s.getGender());
            stmt.setString(17, s.getNeighborhood());
            stmt.setString(18, s.getGovernorate());
            stmt.setString(19, s.getReligion());
            stmt.setString(20, s.getNationality());
            stmt.setString(21, s.getAddress());
            stmt.setString(22, s.getSchool());
            stmt.setString(23, s.getAcademicYear());
            stmt.setString(24, s.getOtherNotes());
            stmt.setString(25, s.getImagePath());
            stmt.setString(26, s.getCenterName());
            stmt.setString(27, s.getIdFrontPath());
            stmt.setString(28, s.getIdBackPath());

            String tempStatus = s.getStatus();
            if (tempStatus == null || tempStatus.trim().isEmpty() || tempStatus.equals("غير محدد")
                    || tempStatus.equals("ناجح") || tempStatus.equals("راسب") || tempStatus.equals("دور ثاني")) {
                tempStatus = calculateStatus(s.getSpecializationId(), s.getGrades());
            }
            stmt.setString(29, tempStatus);
            stmt.setInt(30, s.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // Wipe old grades and re-insert new ones map
                deleteStudentGrades(conn, s.getId());
                if (s.getGrades() != null) {
                    saveStudentGrades(conn, s.getId(), s.getGrades());
                }
                LogService.logAction("SYSTEM", "UPDATE_STUDENT", "تم تعديل بيانات الطالب: " + s.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteStudent(int id) {
        String query = "DELETE FROM students WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                LogService.logAction("SYSTEM", "DELETE_STUDENT", "تم حذف الطالب ذو المعرف: " + id);
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

    public static String calculateStatus(int specializationId, Map<Integer, Integer> grades) {
        if (grades == null || grades.isEmpty())
            return "غير محدد";

        // Fetch passing rules for the subjects of this specialization
        List<com.pvtd.students.models.Subject> subjects = SubjectService.getSubjectsBySpecialization(specializationId);
        if (subjects.isEmpty())
            return "غير محدد";

        int failedSubjects = 0;

        for (com.pvtd.students.models.Subject sub : subjects) {
            int obtained = grades.getOrDefault(sub.getId(), 0);
            if (obtained < sub.getPassMark()) {
                failedSubjects++;
            }
        }

        // Logic (can be updated): Fail in 3+ subjects is a total Fail. Fail in 1-2
        // subjects is Second Try.
        if (failedSubjects >= 3) {
            return "راسب";
        } else if (failedSubjects > 0) {
            return "دور ثاني";
        } else {
            return "ناجح";
        }
    }
}
