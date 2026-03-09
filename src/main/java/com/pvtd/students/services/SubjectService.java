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
        String sql = "SELECT COUNT(*) FROM subjects WHERE profession = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profession);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Subject> getSubjectsByProfession(String profession) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE profession = ? ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, profession);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("profession"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("pass_mark"),
                        rs.getInt("max_mark")));
            }
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

    public static boolean addSubject(String profession, String name, String type, int passMark, int maxMark) {
        String sql = "INSERT INTO subjects (profession, name, type, pass_mark, max_mark, specialization_id) VALUES (?, ?, ?, ?, ?, ?)";
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
                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSubject(int id) {
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
