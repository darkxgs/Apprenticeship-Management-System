package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubjectService {

    public static int countSubjectsByProfession(String profession) {
        String sql = "SELECT COUNT(*) FROM subjects WHERE TRIM(profession) = TRIM(?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profession != null ? profession.trim() : "");
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void ensureStandardSubjectsExist(String profession) {
        String normalizedProfession = profession != null ? profession.trim() : "";
        if (normalizedProfession.isEmpty()) {
            return;
        }
        if (countSubjectsByProfession(normalizedProfession) == 0) {
            autoGenerateStandardSubjects(normalizedProfession);
        }
    }

    public static List<Subject> getSubjectsByProfession(String profession) {
        List<Subject> list = new ArrayList<>();
        // Improved sorting: uses COALESCE to ensure children inherit parent's
        // display_order for sorting.
        // This keeps the profession columns in the correct logical order across all
        // reports.
        String sql = """
                SELECT s.*, COALESCE(p.display_order, s.display_order) as effective_order
                FROM subjects s
                LEFT JOIN subjects p ON s.parent_subject_id = p.id
                WHERE TRIM(s.profession) = TRIM(?)
                ORDER BY effective_order ASC, s.parent_subject_id ASC NULLS FIRST, s.display_order ASC
                """;
        String normalizedProfession = profession != null ? profession.trim() : "";

        ensureStandardSubjectsExist(normalizedProfession);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizedProfession);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("profession"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("pass_mark"),
                        rs.getInt("max_mark"),
                        rs.getInt("display_order"),
                        rs.getObject("parent_subject_id") != null ? rs.getInt("parent_subject_id") : null,
                        rs.getString("sub_name")));
            }
            System.out.println("[SubjectService] profession='" + normalizedProfession + "' -> subjects=" + list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static int getOrCreateSpecialization(Connection conn, String profession) throws Exception {
        // 1. Try to find existing specialization by name
        String sel = "SELECT id FROM specializations WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, profession);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("id");
            }
        }

        // 2. Not found, find a default department to attach it to
        int deptId = 1;
        String checkDept = "SELECT id FROM (SELECT id FROM departments ORDER BY id ASC) WHERE ROWNUM = 1";
        try (PreparedStatement dps = conn.prepareStatement(checkDept);
                ResultSet drs = dps.executeQuery()) {
            if (drs.next())
                deptId = drs.getInt("id");
        }

        // 3. Insert new specialization and retrieve generated ID
        String ins = "INSERT INTO specializations (department_id, name, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins, new String[] { "ID" })) {
            ps.setInt(1, deptId);
            ps.setString(2, profession);
            ps.setString(3, "تم إنشاء التخصص تلقائياً مع المادة");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return 1; // Fallback
    }

    public static boolean addSubject(String profession, String name, String type, int passMark, int maxMark,
            int displayOrder) {
        return addSubject(profession, name, type, passMark, maxMark, displayOrder, null, null);
    }

    public static boolean addSubject(String profession, String name, String type, int passMark, int maxMark,
            int displayOrder, Integer parentSubjectId, String subName) {
        String sql = "INSERT INTO subjects (profession, name, type, pass_mark, max_mark, specialization_id, display_order, parent_subject_id, sub_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Resolve specialization ID first (Foreign Key constraint requires this)
            int specId = getOrCreateSpecialization(conn, profession);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, profession);
                ps.setString(2, name);
                ps.setString(3, type);
                ps.setInt(4, passMark);
                ps.setInt(5, maxMark);
                ps.setInt(6, specId);
                ps.setInt(7, displayOrder);
                if (parentSubjectId != null)
                    ps.setInt(8, parentSubjectId);
                else
                    ps.setNull(8, java.sql.Types.INTEGER);
                ps.setString(9, subName);

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    LogService.logAction("SYSTEM", "ADD_SUBJECT", "تم إضافة مادة: " + name + " لتخصص: " + profession);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateSubject(int id, String name, String type, int passMark, int maxMark, int displayOrder) {
        return updateSubject(id, name, type, passMark, maxMark, displayOrder, null, null);
    }

    public static boolean updateSubject(int id, String name, String type, int passMark, int maxMark, int displayOrder,
            Integer parentSubjectId, String subName) {
        String sql = "UPDATE subjects SET name = ?, type = ?, pass_mark = ?, max_mark = ?, display_order = ?, parent_subject_id = ?, sub_name = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setInt(3, passMark);
            ps.setInt(4, maxMark);
            ps.setInt(5, displayOrder);
            if (parentSubjectId != null)
                ps.setInt(6, parentSubjectId);
            else
                ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, subName);
            ps.setInt(8, id);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                LogService.logAction("SYSTEM", "UPDATE_SUBJECT", "تم تعديل المادة رقم: " + id);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void autoGenerateStandardSubjects(String profession) {
        // New order: 2 dynamic (user-editable) → 2 fixed theory → practical → applied
        addSubject(profession, "تكنولوجيا", "نظري", 50, 100, 1);
        addSubject(profession, "رسم", "نظري", 50, 100, 2);
        addSubject(profession, "ميكانيكا عامة", "نظري", 25, 50, 3);
        addSubject(profession, "لغة انجليزية", "نظري", 25, 50, 4);

        addSubject(profession, "عملي", "عملي", 120, 200, 5);
        addSubject(profession, "تطبيقي", "تطبيقي", 50, 100, 6);
    }

    /**
     * Returns all direct children (sub-subjects) of a given parent subject.
     */
    public static List<Subject> getChildrenOf(int parentId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE parent_subject_id = ? ORDER BY display_order ASC, id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("profession"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("pass_mark"),
                        rs.getInt("max_mark"),
                        rs.getInt("display_order"),
                        rs.getObject("parent_subject_id") != null ? rs.getInt("parent_subject_id") : null,
                        rs.getString("sub_name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Enables the 30/70 split on a subject.
     * Creates two child records (30 and 70 marks) linked to the parent.
     * 
     * @param parentId   ID of the parent subject
     * @param profession profession name (for INSERT)
     * @param type       subject type
     * @param name30     display name for the 30-mark part
     * @param name70     display name for the 70-mark part
     */
    public static boolean enableComposite(int parentId, String profession, String type, String name30, String name70) {
        // First remove existing children
        disableComposite(parentId);

        // Fetch parent subject to get actual name, max/pass marks
        int parentMax = 100, parentPass = 50;
        String parentName = profession; // default fallback
        String sql = "SELECT name, max_mark, pass_mark FROM subjects WHERE id = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    parentName = rs.getString("name");
                    parentMax = rs.getInt("max_mark");
                    parentPass = rs.getInt("pass_mark");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Split: 30% portion and 70% portion (proportional to parent)
        int max30 = Math.round(parentMax * 0.30f);
        int max70 = parentMax - max30;
        int pass30 = Math.round(parentPass * 0.30f);
        int pass70 = parentPass - pass30;

        // Use parentName for the children to preserve subject identity
        boolean ok1 = addSubject(profession, parentName, type, pass30, max30, 1, parentId, name30);
        boolean ok2 = addSubject(profession, parentName, type, pass70, max70, 2, parentId, name70);
        if (ok1 && ok2) {
            LogService.logAction("SYSTEM", "ENABLE_COMPOSITE", "تم تفعيل التقسيم 30/70 للمادة رقم: " + parentId);
        }
        return ok1 && ok2;
    }

    /**
     * Disables the 30/70 split by deleting all children of a subject.
     */
    public static boolean disableComposite(int parentId) {
        String sql = "DELETE FROM subjects WHERE parent_subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                LogService.logAction("SYSTEM", "DISABLE_COMPOSITE", "تم إلغاء التقسيم 30/70 للمادة رقم: " + parentId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSubject(int id) {
        // Delete children first, then the subject itself
        disableComposite(id);
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
