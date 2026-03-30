package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatusesService {

    public static List<String> getAllStatuses() {
        List<String> statuses = new ArrayList<>();
        // Always include the defaults implicitly if needed, or rely purely on DB.
        String query = "SELECT status_name FROM student_statuses ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statuses.add(rs.getString("status_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Ensure core dynamic statuses exist in UI dropdowns at minimum if DB is empty
        if (!statuses.contains("ناجح"))
            statuses.add(0, "ناجح");
        if (!statuses.contains("راسب"))
            statuses.add(0, "راسب");
        if (!statuses.contains("دور ثاني"))
            statuses.add(0, "دور ثاني");

        return statuses;
    }

    public static void addStatus(String statusName, String username) throws SQLException {
        String query = "INSERT INTO student_statuses (status_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, statusName.trim());
            stmt.executeUpdate();
            LogService.logAction(username, "ADD_STATUS", "تمت إضافة حالة جديدة: " + statusName);
        }
    }

    public static void deleteStatus(String statusName, String username) throws SQLException {
        if (statusName.equals("ناجح") || statusName.equals("راسب") || statusName.equals("دور ثاني")) {
            throw new SQLException("لا يمكن حذف الحالات الأساسية للنظام");
        }
        String query = "DELETE FROM student_statuses WHERE status_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, statusName);
            stmt.executeUpdate();
            LogService.logAction(username, "DELETE_STATUS", "تم حذف الحالة: " + statusName);
        }
    }
}
