package com.pvtd.students.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Oracle 11g Configuration
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "system";
    private static final String PASSWORD = "123";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Ensure ojdbc8 is in your dependencies.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String createUsersSeq = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createUsersSeq);

            String createUsersTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE users (" +
                    "id NUMBER PRIMARY KEY," +
                    "username VARCHAR2(50) UNIQUE NOT NULL," +
                    "password VARCHAR2(255) NOT NULL," +
                    "role VARCHAR2(50) NOT NULL," +
                    "full_name VARCHAR2(100))';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createUsersTable);

            String userTrigger = "CREATE OR REPLACE TRIGGER trg_users_seq BEFORE INSERT ON users FOR EACH ROW BEGIN :new.id := users_seq.nextval; END;";
            stmt.execute(userTrigger);

            createSequence(stmt, "departments_seq");
            String createDeptsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE departments (" +
                    "id NUMBER PRIMARY KEY," +
                    "name VARCHAR2(100) NOT NULL," +
                    "description VARCHAR2(255))';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createDeptsTable);
            createTrigger(stmt, "departments");

            try {
                stmt.execute(
                        "INSERT INTO departments (name, description) " +
                                "SELECT 'القسم الرئيسي', 'القسم الافتراضي للنظام' FROM DUAL " +
                                "WHERE NOT EXISTS (SELECT 1 FROM departments WHERE ROWNUM = 1)");
            } catch (SQLException e) {}

            createSequence(stmt, "specializations_seq");
            String createSpecsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE specializations (" +
                    "id NUMBER PRIMARY KEY," +
                    "department_id NUMBER NOT NULL," +
                    "name VARCHAR2(150) NOT NULL," +
                    "description VARCHAR2(255)," +
                    "CONSTRAINT fk_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createSpecsTable);
            createTrigger(stmt, "specializations");

            createSequence(stmt, "subjects_seq");
            String createSubjectsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE subjects (" +
                    "id NUMBER PRIMARY KEY," +
                    "profession VARCHAR2(200)," +
                    "name VARCHAR2(200) NOT NULL," +
                    "type VARCHAR2(50) DEFAULT ''نظري''," +
                    "pass_mark NUMBER NOT NULL," +
                    "max_mark NUMBER NOT NULL)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createSubjectsTable);
            createTrigger(stmt, "subjects");

            createSequence(stmt, "students_seq");
            String createStudentsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE students (" +
                    "id NUMBER PRIMARY KEY," +
                    "specialization_id NUMBER," +
                    "serial VARCHAR2(50)," +
                    "name VARCHAR2(200)," +
                    "registration_no VARCHAR2(100)," +
                    "national_id VARCHAR2(100)," +
                    "region VARCHAR2(100)," +
                    "profession VARCHAR2(100)," +
                    "exam_system VARCHAR2(100)," +
                    "seat_no VARCHAR2(100) UNIQUE," +
                    "secret_no VARCHAR2(100)," +
                    "professional_group VARCHAR2(100)," +
                    "coordination_no VARCHAR2(100)," +
                    "dob_day VARCHAR2(10)," +
                    "dob_month VARCHAR2(10)," +
                    "dob_year VARCHAR2(10)," +
                    "gender VARCHAR2(20)," +
                    "neighborhood VARCHAR2(100)," +
                    "governorate VARCHAR2(100)," +
                    "religion VARCHAR2(50)," +
                    "nationality VARCHAR2(50)," +
                    "address VARCHAR2(255)," +
                    "school VARCHAR2(255)," +
                    "academic_year VARCHAR2(50)," +
                    "other_notes VARCHAR2(255)," +
                    "image_path VARCHAR2(255)," +
                    "status VARCHAR2(50)," +
                    "CONSTRAINT fk_student_spec FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE SET NULL)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createStudentsTable);
            createTrigger(stmt, "students");

            String createGradesTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE student_grades (" +
                    "student_id NUMBER NOT NULL," +
                    "subject_id NUMBER NOT NULL," +
                    "obtained_mark NUMBER NOT NULL," +
                    "PRIMARY KEY (student_id, subject_id)," +
                    "CONSTRAINT fk_sg_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_sg_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createGradesTable);

            createSequence(stmt, "logs_seq");
            String createLogsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE logs (" +
                    "id NUMBER PRIMARY KEY," +
                    "username VARCHAR2(50) NOT NULL," +
                    "action VARCHAR2(100) NOT NULL," +
                    "details VARCHAR2(500)," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createLogsTable);
            createTrigger(stmt, "logs");

            createSequence(stmt, "student_statuses_seq");
            String createStatusesTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE student_statuses (" +
                    "id NUMBER PRIMARY KEY," +
                    "status_name VARCHAR2(100) UNIQUE NOT NULL)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createStatusesTable);
            createTrigger(stmt, "student_statuses");

            String[] defaultStatuses = { "غائب", "محروم", "مفصول", "معتذر", "مؤجل" };
            for (String s : defaultStatuses) {
                try {
                    stmt.execute("INSERT INTO student_statuses (status_name) SELECT '" + s +
                            "' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM student_statuses WHERE status_name = '" + s + "')");
                } catch (SQLException e) {}
            }

            addColumnIfMissing(stmt, "students", "image_path", "VARCHAR2(255)");
            addColumnIfMissing(stmt, "students", "other_notes", "VARCHAR2(255)");
            addColumnIfMissing(stmt, "students", "status", "VARCHAR2(50)");
            addColumnIfMissing(stmt, "students", "serial", "VARCHAR2(50)");
            addColumnIfMissing(stmt, "students", "registration_no", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "region", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "exam_system", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "secret_no", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "professional_group", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "coordination_no", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "dob_day", "VARCHAR2(10)");
            addColumnIfMissing(stmt, "students", "dob_month", "VARCHAR2(10)");
            addColumnIfMissing(stmt, "students", "dob_year", "VARCHAR2(10)");
            addColumnIfMissing(stmt, "students", "gender", "VARCHAR2(20)");
            addColumnIfMissing(stmt, "students", "neighborhood", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "students", "religion", "VARCHAR2(50)");
            addColumnIfMissing(stmt, "students", "nationality", "VARCHAR2(50)");
            addColumnIfMissing(stmt, "students", "address", "VARCHAR2(255)");
            addColumnIfMissing(stmt, "students", "center_name", "VARCHAR2(150)");
            addColumnIfMissing(stmt, "students", "id_front_path", "VARCHAR2(255)");
            addColumnIfMissing(stmt, "students", "id_back_path", "VARCHAR2(255)");
            addColumnIfMissing(stmt, "students", "profession", "VARCHAR2(100)");
            addColumnIfMissing(stmt, "subjects", "profession", "VARCHAR2(200)");

            try {
                stmt.execute("INSERT INTO users (username, password, role, full_name) " +
                        "SELECT 'admin', 'admin123', 'admin', 'مدير النظام' FROM DUAL " +
                        "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')");
            } catch (SQLException e) {}

            System.out.println("Oracle Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize Oracle DB schemas.");
            e.printStackTrace();
        }
    }

    private static void createSequence(Statement stmt, String seqName) {
        String query = "BEGIN\n" +
                "  EXECUTE IMMEDIATE 'CREATE SEQUENCE " + seqName + " START WITH 1 INCREMENT BY 1';\n" +
                "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                "END;";
        try { stmt.execute(query); } catch (Exception e) {}
    }

    private static void createTrigger(Statement stmt, String tableName) {
        String trigger = "CREATE OR REPLACE TRIGGER trg_" + tableName + "_seq BEFORE INSERT ON " + tableName +
                " FOR EACH ROW BEGIN :new.id := " + tableName + "_seq.nextval; END;";
        try { stmt.execute(trigger); } catch (Exception e) {}
    }

    private static void addColumnIfMissing(Statement stmt, String table, String column, String type) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD (" + column + " " + type + ")");
        } catch (SQLException e) {
            if (e.getErrorCode() != 1430) {
                System.err.println("Warning: could not add column " + column + " to " + table);
            }
        }
    }
}