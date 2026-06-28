package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Returns all statuses with their codes as a map: status_name -> status_code.
     * Statuses without a code will have null as value.
     */
    public static LinkedHashMap<String, Integer> getAllStatusesWithCodes() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        String query = "SELECT status_name, status_code FROM student_statuses ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("status_name");
                int code = rs.getInt("status_code");
                map.put(name, rs.wasNull() ? null : code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Returns a mapping of negative grade marker code -> status name.
     * Used by calculateStatus() to dynamically resolve negative marks.
     * Example: {-1: "غائب", -2: "محروم", ...}
     */
    public static Map<Integer, String> getCodeToStatusMap() {
        Map<Integer, String> map = new HashMap<>();
        String query = "SELECT status_name, status_code FROM student_statuses WHERE status_code IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("status_code"), rs.getString("status_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Checks if a given status_code is already used by another status.
     * Returns the name of the status using it, or null if available.
     */
    public static String getStatusNameByCode(int code) {
        String query = "SELECT status_name FROM student_statuses WHERE status_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("status_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addStatus(String statusName, String username) throws SQLException {
        addStatus(statusName, null, username);
    }

    public static void addStatus(String statusName, Integer statusCode, String username) throws SQLException {
        String query = "INSERT INTO student_statuses (status_name, status_code) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, statusName.trim());
            if (statusCode != null) {
                stmt.setInt(2, statusCode);
            } else {
                stmt.setNull(2, java.sql.Types.NUMERIC);
            }
            stmt.executeUpdate();
            String codeInfo = statusCode != null ? " (الكود: " + statusCode + ")" : "";
            LogService.logAction(username, "ADD_STATUS", "تمت إضافة حالة جديدة: " + statusName + codeInfo);
        }
    }

    /**
     * Updates a status name and/or code.
     */
    public static void updateStatus(String oldName, String newName, Integer newCode, String username) throws SQLException {
        String query = "UPDATE student_statuses SET status_name = ?, status_code = ? WHERE status_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newName.trim());
            if (newCode != null) {
                stmt.setInt(2, newCode);
            } else {
                stmt.setNull(2, java.sql.Types.NUMERIC);
            }
            stmt.setString(3, oldName);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                // If not found (maybe a core status not yet in DB), INSERT it
                addStatus(newName, newCode, username);
            }

            // Cascade rename in students table if name changed
            if (!oldName.equals(newName.trim())) {
                String updateStudents = "UPDATE students SET status = ? WHERE status = ?";
                try (Connection conn2 = DatabaseConnection.getConnection();
                     PreparedStatement stmt2 = conn2.prepareStatement(updateStudents)) {
                    stmt2.setString(1, newName.trim());
                    stmt2.setString(2, oldName);
                    stmt2.executeUpdate();
                }
            }

            if (rowsUpdated > 0) {
                String codeInfo = newCode != null ? " (الكود: " + newCode + ")" : " (بدون كود)";
                LogService.logAction(username, "UPDATE_STATUS",
                        "تم تعديل الحالة: " + oldName + " → " + newName + codeInfo);
            }
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
