package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DictionaryService {

    public static final String CAT_PROFESSION = "PROFESSION";
    public static final String CAT_REGION = "REGION";
    public static final String CAT_CENTER = "CENTER";
    public static final String CAT_PROF_GROUP = "PROF_GROUP";

    /**
     * Gets a combined list of dictionary items explicitly added + whatever is distinct in the students table.
     */
    public static List<String> getCombinedItems(String category) {
        List<String> items = new ArrayList<>();
        String dictQuery = "SELECT value FROM system_dictionaries WHERE category = ? ORDER BY value";

        String studentCol = getStudentColumnForCategory(category);
        String studentsQuery = "SELECT DISTINCT " + studentCol + " FROM students WHERE " + studentCol + " IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st1 = conn.prepareStatement(dictQuery);
             PreparedStatement st2 = conn.prepareStatement(studentsQuery)) {

            st1.setString(1, category);
            ResultSet rs1 = st1.executeQuery();
            while (rs1.next()) {
                String val = rs1.getString(1).trim();
                if (!items.contains(val)) items.add(val);
            }

            ResultSet rs2 = st2.executeQuery();
            while (rs2.next()) {
                String val = rs2.getString(1).trim();
                if (!items.contains(val)) items.add(val);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        items.sort(String::compareTo);
        return items;
    }

    public static void addItem(String category, String value, String username) throws SQLException {
        if (value == null || value.trim().isEmpty()) return;
        String query = "INSERT INTO system_dictionaries (category, value) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
            stmt.setString(2, value.trim());
            stmt.executeUpdate();
            LogService.logAction(username, "ADD_DICT_ITEM", "تم إضافة " + value + " لقائمة " + category);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1) { // Ignore Unique Constraint error (ORA-00001)
                throw e;
            }
        }
    }

    public static void deleteItem(String category, String value, String username) throws SQLException {
        String query = "DELETE FROM system_dictionaries WHERE category = ? AND value = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
            stmt.setString(2, value);
            stmt.executeUpdate();
            LogService.logAction(username, "DEL_DICT_ITEM", "تم حذف " + value + " من قائمة " + category);
        }
    }

    public static void renameItem(String category, String oldVal, String newVal, String username) throws SQLException {
        if (newVal == null || newVal.trim().isEmpty()) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update dictionary mapping
                String updateDict = "UPDATE system_dictionaries SET value = ? WHERE category = ? AND value = ?";
                try (PreparedStatement stmt1 = conn.prepareStatement(updateDict)) {
                    stmt1.setString(1, newVal.trim());
                    stmt1.setString(2, category);
                    stmt1.setString(3, oldVal);
                    stmt1.executeUpdate();
                }

                // 2. Cascade rename into students table
                String col = getStudentColumnForCategory(category);
                String updateStudents = "UPDATE students SET " + col + " = ? WHERE " + col + " = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(updateStudents)) {
                    stmt2.setString(1, newVal.trim());
                    stmt2.setString(2, oldVal);
                    stmt2.executeUpdate();
                }

                // If updating profession, update subjects mapped to this profession
                if (CAT_PROFESSION.equals(category)) {
                    String updateSubjects = "UPDATE subjects SET profession = ? WHERE profession = ?";
                    try (PreparedStatement stmt3 = conn.prepareStatement(updateSubjects)) {
                        stmt3.setString(1, newVal.trim());
                        stmt3.setString(2, oldVal);
                        stmt3.executeUpdate();
                    }
                }

                conn.commit();
                LogService.logAction(username, "RENAME_DICT_ITEM", "تم تعديل " + oldVal + " إلى " + newVal + " في قائمة " + category);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static String getStudentColumnForCategory(String category) {
        switch (category) {
            case CAT_PROFESSION: return "profession";
            case CAT_REGION: return "region";
            case CAT_CENTER: return "center_name";
            case CAT_PROF_GROUP: return "professional_group";
            default: return "other_notes";
        }
    }
}
