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
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement()) {

            // Create sequence for archive_groups
            String seqGroups =
                "BEGIN EXECUTE IMMEDIATE 'CREATE SEQUENCE archive_groups_seq START WITH 1 INCREMENT BY 1';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            stmt.execute(seqGroups);

            // Table: archive_groups
            String createGroups =
                "BEGIN EXECUTE IMMEDIATE '" +
                "CREATE TABLE archive_groups (" +
                "id NUMBER PRIMARY KEY, " +
                "name VARCHAR2(200) NOT NULL, " +
                "description VARCHAR2(500), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            stmt.execute(createGroups);

            // Trigger for archive_groups
            String trgGroups =
                "CREATE OR REPLACE TRIGGER archive_groups_trg\n" +
                "BEFORE INSERT ON archive_groups\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "  IF :new.id IS NULL THEN\n" +
                "    SELECT archive_groups_seq.NEXTVAL INTO :new.id FROM dual;\n" +
                "  END IF;\n" +
                "END;";
            try { stmt.execute(trgGroups); } catch(SQLException ignore) {}

            // Create sequence for archived_students
            String seqStudents =
                "BEGIN EXECUTE IMMEDIATE 'CREATE SEQUENCE archived_students_seq START WITH 1 INCREMENT BY 1';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            stmt.execute(seqStudents);

            // Table: archived_students
            String createArchived =
                "BEGIN EXECUTE IMMEDIATE '" +
                "CREATE TABLE archived_students (" +
                "id NUMBER PRIMARY KEY, " +
                "archive_group_id NUMBER NOT NULL, " +
                "original_student_id NUMBER, " +
                "student_name VARCHAR2(300), " +
                "secret_no VARCHAR2(20), " +
                "center_name VARCHAR2(200), " +
                "profession VARCHAR2(200), " +
                "status VARCHAR2(100), " +
                "total_grade NUMBER, " +
                "archive_note VARCHAR2(500), " +
                "archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)';"
                + " EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END;";
            stmt.execute(createArchived);

            // Trigger for archived_students
            String trgArchived =
                "CREATE OR REPLACE TRIGGER archived_students_trg\n" +
                "BEFORE INSERT ON archived_students\n" +
                "FOR EACH ROW\n" +
                "BEGIN\n" +
                "  IF :new.id IS NULL THEN\n" +
                "    SELECT archived_students_seq.NEXTVAL INTO :new.id FROM dual;\n" +
                "  END IF;\n" +
                "END;";
            try { stmt.execute(trgArchived); } catch(SQLException ignore) {}

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
                "SELECT ?, id, name, secret_no, center_name, profession, status, ? " +
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

    // ─── Restore archived students back to active students table ──────────────
    /**
     * Moves all archived students in the given group back into the active students table.
     * The archive group and records are deleted after successful restore.
     *
     * @return count of restored students, or -1 on failure
     */
    public static int restoreStudents(int groupId) {
        ensureTables();
        int count = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            // Re-insert into students table from archived snapshot
            String insertSql =
                "INSERT INTO students (name, secret_no, center_name, profession, status, other_notes) " +
                "SELECT student_name, secret_no, center_name, profession, status, archive_note " +
                "FROM archived_students WHERE archive_group_id = ?";

            PreparedStatement ins = con.prepareStatement(insertSql);
            ins.setInt(1, groupId);
            count = ins.executeUpdate();
            ins.close();

            // Remove from archive
            con.createStatement().executeUpdate(
                "DELETE FROM archived_students WHERE archive_group_id = " + groupId);
            con.createStatement().executeUpdate(
                "DELETE FROM archive_groups WHERE id = " + groupId);

            con.commit();
        } catch (Exception e) {
            System.err.println("[ArchiveService] restoreStudents error: " + e.getMessage());
            return -1;
        }
        return count;
    }

    // ─── Export archived group to Excel (.xlsx) ───────────────────────────────
    /**
     * Exports all archived students of the given group to an xlsx file.
     *
     * @param groupId   archive group ID
     * @param groupName display name, used in sheet title and filename suggestion
     * @param destFile  target .xlsx file chosen by the user
     * @return true on success
     */
    public static boolean exportArchivedToExcel(int groupId, String groupName, java.io.File destFile) {
        ensureTables();
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet(groupName.length() > 30 ? groupName.substring(0, 30) : groupName);

            // Header style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font wf = wb.createFont();
            wf.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            wf.setBold(true);
            headerStyle.setFont(wf);

            // Header row
            String[] headers = {"الاسم", "الرقم السري", "المركز", "المهنة", "الحالة", "الملاحظة", "تاريخ الأرشفة"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Data rows
            List<String[]> students = getArchivedStudents(groupId);
            int rowIdx = 1;
            for (String[] s : students) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                for (int col = 0; col < s.length && col < 7; col++) {
                    row.createCell(col).setCellValue(s[col] != null ? s[col] : "");
                }
            }

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile)) {
                wb.write(fos);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ArchiveService] exportArchivedToExcel error: " + e.getMessage());
            return false;
        }
    }
}
