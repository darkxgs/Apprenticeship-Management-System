package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LogService {

    /**
     * Records a system action to the `logs` table.
     * 
     * @param username The user performing the action.
     * @param action   The high-level action category (e.g. "ADD_STUDENT", "LOGIN").
     * @param details  Specific details about the action (e.g. "Added student Ahmed,
     *                 ID 2501").
     */
    public static void logAction(String username, String action, String details) {
        String query = "INSERT INTO logs (username, action, details) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username != null ? username : "SYSTEM");
            stmt.setString(2, action);
            stmt.setString(3, details);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to write to system logs.");
            e.printStackTrace();
        }
    }

    /**
     * Delete all system logs.
     */
    public static void clearAllLogs() throws SQLException {
        String query = "DELETE FROM logs";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();

            // Log that the logs were cleared (starts fresh)
            logAction("SYSTEM", "CLEAR_LOGS", "تم مسح جميع سجلات النظام");
        }
    }
}
