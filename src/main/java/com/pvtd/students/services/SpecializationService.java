package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Specialization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SpecializationService {

    /**
     * Returns the ID of the first department in the DB.
     * Used instead of hardcoding department_id = 1, since the auto-trigger
     * may assign a different ID if the departments_seq has already advanced.
     */
    public static int getDefaultDepartmentId() {
        String sql = "SELECT id FROM (SELECT id FROM departments ORDER BY id ASC) WHERE ROWNUM = 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // fallback
    }

    public static List<Specialization> getSpecializationsByDepartment(int departmentId) {
        List<Specialization> list = new ArrayList<>();
        String sql = "SELECT * FROM specializations WHERE department_id = ? ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Specialization(
                        rs.getInt("id"),
                        rs.getInt("department_id"),
                        rs.getString("name"),
                        rs.getString("description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Specialization getSpecializationById(int id) {
        String sql = "SELECT * FROM specializations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Specialization(
                        rs.getInt("id"),
                        rs.getInt("department_id"),
                        rs.getString("name"),
                        rs.getString("description"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addSpecialization(int departmentId, String name, String desc) {
        String sql = "INSERT INTO specializations (department_id, name, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            ps.setString(2, name);
            ps.setString(3, desc);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSpecialization(int id) {
        String sql = "DELETE FROM specializations WHERE id = ?";
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
