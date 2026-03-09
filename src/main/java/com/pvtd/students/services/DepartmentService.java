package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentService {

    public static List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String query = "SELECT * FROM departments ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Department(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching departments: " + e.getMessage());
        }
        return list;
    }

    public static boolean addDepartment(String name, String description) {
        String query = "INSERT INTO departments (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding department: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteDepartment(int id) {
        String query = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting department: " + e.getMessage());
            return false;
        }
    }

    public static int getSpecializationCount(int departmentId) {
        String sql = "SELECT COUNT(*) FROM specializations WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getStudentCountByDepartment(int departmentId) {
        String sql = "SELECT COUNT(*) FROM students s " +
                "JOIN specializations sp ON s.specialization_id = sp.id " +
                "WHERE sp.department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
