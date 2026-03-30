package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogService {

    // ── Data Model ────────────────────────────────────────────────────────────

    public static class LogEntry {
        public final String username;
        public final String action;
        public final String details;
        public final String timestamp;

        public LogEntry(String username, String action, String details, String timestamp) {
            this.username  = username;
            this.action    = action;
            this.details   = details;
            this.timestamp = timestamp;
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Records a system action to the {@code logs} table.
     *
     * @param username The user performing the action.
     * @param action   The high-level action category (e.g. "ADD_STUDENT", "LOGIN").
     * @param details  Specific details about the action.
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

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns all log entries ordered newest-first.
     */
    public static List<LogEntry> getAllLogs() {
        List<LogEntry> logs = new ArrayList<>();
        String query = "SELECT username, action, details, TO_CHAR(timestamp, 'YYYY-MM-DD HH24:MI:SS') " +
                       "FROM logs ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(new LogEntry(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3) != null ? rs.getString(3) : "",
                    rs.getString(4) != null ? rs.getString(4) : ""
                ));
            }

        } catch (SQLException e) {
            System.err.println("Failed to read system logs.");
            e.printStackTrace();
        }
        return logs;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Deletes all system logs, then records the clear action itself.
     */
    public static void clearAllLogs(String username) throws SQLException {
        String query = "DELETE FROM logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
        // Record that logs were cleared (attributed to the user)
        logAction(username, "CLEAR_LOGS", "تم مسح جميع سجلات النظام");
    }
}
