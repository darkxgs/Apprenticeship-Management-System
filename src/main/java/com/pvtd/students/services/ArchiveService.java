package com.pvtd.students.services;

import com.pvtd.students.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages archiving of students at end-of-year.
 * Creates archive_groups table and archived_students table if they don't exist.
 * Archives are permanent snapshots — students are moved out of the active table.
 */
public class ArchiveService {

    // ─── Ensure tables exist ───────────────────────────────────────────────────
    public static void ensureTables() {
        try (Connection con = DatabaseConnection.getConnection()) {
            // Create sequence for archive_groups
            String seqGroups =
                "BEGIN EXECUTE IMMEDIATE 'CREATE SEQUENCE archive_groups_seq START WITH 1 INCREMENT BY 1';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            con.createStatement().execute(seqGroups);

            // Table: archive_groups
            String createGroups =
                "BEGIN EXECUTE IMMEDIATE '" +
                "CREATE TABLE archive_groups (" +
                "id NUMBER DEFAULT archive_groups_seq.NEXTVAL PRIMARY KEY, " +
                "name VARCHAR2(200) NOT NULL, " +
                "description VARCHAR2(500), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)'';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            con.createStatement().execute(createGroups);

            // Create sequence for archived_students
            String seqStudents =
                "BEGIN EXECUTE IMMEDIATE 'CREATE SEQUENCE archived_students_seq START WITH 1 INCREMENT BY 1';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            con.createStatement().execute(seqStudents);

            // Table: archived_students
            String createArchived =
                "BEGIN EXECUTE IMMEDIATE '" +
                "CREATE TABLE archived_students (" +
                "id NUMBER DEFAULT archived_students_seq.NEXTVAL PRIMARY KEY, " +
                "archive_group_id NUMBER NOT NULL, " +
                "original_student_id NUMBER, " +
                "student_name VARCHAR2(300), " +
                "secret_no VARCHAR2(20), " +
                "center_name VARCHAR2(200), " +
                "profession VARCHAR2(200), " +
                "status VARCHAR2(100), " +
                "total_grade NUMBER, " +
                "archive_note VARCHAR2(500), " +
                "archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)'';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            con.createStatement().execute(createArchived);
        } catch (Exception e) {
            System.err.println("[ArchiveService] Failed to ensure tables: " + e.getMessage());
        }
    }

    // ─── Create a new archive group ───────────────────────────────────────────
    public static boolean createArchiveGroup(String name, String description) {
        ensureTables();
        String sql = "INSERT INTO archive_groups(name, description) VALUES(?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("[ArchiveService] createArchiveGroup error: " + e.getMessage());
            return false;
        }
    }

    // ─── List all archive groups ──────────────────────────────────────────────
    public static List<String[]> getArchiveGroups() {
        ensureTables();
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT id, name, description, created_at FROM archive_groups ORDER BY created_at DESC";
        try (Connection con = DatabaseConnection.getConnection();
             ResultSet rs = con.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("created_at")
                });
            }
        } catch (Exception e) {
            System.err.println("[ArchiveService] getArchiveGroups error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Archives ALL students in a given center to the specified archive group,
     * then removes them from the active students table.
     * @param archiveGroupId  the ID of the archive group
     * @param centerName      center to archive (null/empty = all centers)
     * @param note            optional note to attach to archived records
     * @return count of archived students, or -1 on failure
     */
    public static int archiveStudents(int archiveGroupId, String centerName, String note) {
        ensureTables();
        int count = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            // Build WHERE clause
            String whereClause = (centerName != null && !centerName.isEmpty())
                    ? "WHERE center_name = '" + centerName.replace("'", "''") + "'"
                    : "";

            // Insert snapshot into archived_students
            String insertSql =
                "INSERT INTO archived_students " +
                "(archive_group_id, original_student_id, student_name, secret_no, center_name, profession, status, archive_note) " +
                "SELECT ?, id, student_name, secret_no, center_name, profession, status, ? " +
                "FROM students " + whereClause;

            PreparedStatement ins = con.prepareStatement(insertSql);
            ins.setInt(1, archiveGroupId);
            ins.setString(2, note != null ? note : "");
            count = ins.executeUpdate();
            ins.close();

            // Delete from active students
            String deleteSql = "DELETE FROM students " + whereClause;
            con.createStatement().executeUpdate(deleteSql);

            con.commit();
        } catch (Exception e) {
            System.err.println("[ArchiveService] archiveStudents error: " + e.getMessage());
            return -1;
        }
        return count;
    }

    // ─── List archived students in a group ───────────────────────────────────
    public static List<String[]> getArchivedStudents(int groupId) {
        ensureTables();
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT student_name, secret_no, center_name, profession, status, archive_note, archived_at " +
                     "FROM archived_students WHERE archive_group_id = ? ORDER BY archived_at";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("student_name"),
                    rs.getString("secret_no"),
                    rs.getString("center_name"),
                    rs.getString("profession"),
                    rs.getString("status"),
                    rs.getString("archive_note"),
                    rs.getString("archived_at")
                });
            }
        } catch (Exception e) {
            System.err.println("[ArchiveService] getArchivedStudents error: " + e.getMessage());
        }
        return result;
    }

    // ─── Delete an archive group and its students ─────────────────────────────
    public static boolean deleteArchiveGroup(int groupId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            String delStudents = "DELETE FROM archived_students WHERE archive_group_id = ?";
            PreparedStatement ps1 = con.prepareStatement(delStudents);
            ps1.setInt(1, groupId);
            ps1.executeUpdate();
            ps1.close();

            String delGroup = "DELETE FROM archive_groups WHERE id = ?";
            PreparedStatement ps2 = con.prepareStatement(delGroup);
            ps2.setInt(1, groupId);
            ps2.executeUpdate();
            ps2.close();

            con.commit();
            return true;
        } catch (Exception e) {
            System.err.println("[ArchiveService] deleteArchiveGroup error: " + e.getMessage());
            return false;
        }
    }
}
