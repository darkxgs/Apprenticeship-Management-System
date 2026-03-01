package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubjectService {

    public static int countSubjectsBySpecialization(int specializationId) {
        String sql = "SELECT COUNT(*) FROM subjects WHERE specialization_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, specializationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<Subject> getSubjectsBySpecialization(int specializationId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE specialization_id = ? ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, specializationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getInt("specialization_id"),
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

    public static boolean addSubject(int specializationId, String name, String type, int passMark, int maxMark) {
        String sql = "INSERT INTO subjects (specialization_id, name, type, pass_mark, max_mark) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, specializationId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setInt(4, passMark);
            ps.setInt(5, maxMark);
            return ps.executeUpdate() > 0;

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
