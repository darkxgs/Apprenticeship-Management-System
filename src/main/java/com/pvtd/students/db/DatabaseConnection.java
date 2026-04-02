package com.pvtd.students.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String URL = ConfigManager.get("db.url", "jdbc:oracle:thin:@localhost:1521:xe");
    private static final String USER = ConfigManager.get("db.user", "system");
    private static final String PASSWORD = ConfigManager.get("db.password", "123");
    private static final int MAX_POOL_SIZE = 20;

    // Simple connection pool
    private static final List<Connection> pool = new ArrayList<>();

    /**
     * Get a connection from the pool (or create a new one if pool not full).
     * Connections are reused, minimizing Oracle XE session exhaustion (ORA-12519).
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found.");
        }

        Connection rawConn = null;

        // Reuse any live pooled connection
        for (int i = 0; i < pool.size(); i++) {
            Connection c = pool.get(i);
            try {
                if (c != null && !c.isClosed() && c.isValid(1)) {
                    pool.remove(i);
                    rawConn = c;
                    break;
                } else {
                    pool.remove(i);
                    i--;
                }
            } catch (Exception e) {
                pool.remove(i);
                i--;
            }
        }

        // Create a new connection if pool is empty
        if (rawConn == null) {
            rawConn = DriverManager.getConnection(URL, USER, PASSWORD);
        }

        final Connection targetConn = rawConn;

        // Return a proxy that intercepts close() so try-with-resources returns to pool instead of destroying
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            new java.lang.reflect.InvocationHandler() {
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    if ("close".equals(method.getName())) {
                        returnConnection(targetConn);
                        return null;
                    }
                    if ("isClosed".equals(method.getName())) {
                        return targetConn.isClosed();
                    }
                    try {
                        return method.invoke(targetConn, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                }
            }
        );
    }

    /**
     * Return a connection to the pool for reuse. Call this instead of conn.close().
     * If pool is full, the connection is closed normally.
     */
    public static synchronized void returnConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) {
                if (conn.getAutoCommit() == false) {
                    try { conn.rollback(); } catch (Exception ignored) {}
                    conn.setAutoCommit(true);
                }
                if (pool.size() < MAX_POOL_SIZE) {
                    pool.add(conn);
                } else {
                    conn.close();
                }
            }
        } catch (Exception e) {
            try { conn.close(); } catch (Exception ignored) {}
        }
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
                    "max_mark NUMBER NOT NULL," +
                    "display_order NUMBER DEFAULT 0)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createSubjectsTable);
            createTrigger(stmt, "subjects");
            
            // Safe alter for existing databases
            try {
                stmt.execute("ALTER TABLE subjects ADD (display_order NUMBER DEFAULT 0)");
            } catch (SQLException ignore) {
                // Ignore ORA-01430 (column already exists)
            }

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

            createSequence(stmt, "system_dictionaries_seq");
            String createDictTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE system_dictionaries (" +
                    "id NUMBER PRIMARY KEY," +
                    "category VARCHAR2(50) NOT NULL," +
                    "value VARCHAR2(150) NOT NULL," +
                    "UNIQUE(category, value))';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createDictTable);
            createTrigger(stmt, "system_dictionaries");

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
            addColumnIfMissing(stmt, "students", "phone_number", "VARCHAR2(20)");
            addColumnIfMissing(stmt, "subjects", "profession", "VARCHAR2(200)");

            // Make national_id unique to prevent duplicates
            try {
                stmt.execute("ALTER TABLE students ADD CONSTRAINT uq_national_id UNIQUE (national_id)");
            } catch (SQLException ignore) {
                // Constraint may already exist or there are duplicates
            }

            // Create Regions Table
            createSequence(stmt, "regions_seq");
            String createRegionsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE regions (" +
                    "id NUMBER PRIMARY KEY," +
                    "name VARCHAR2(150) UNIQUE NOT NULL," +
                    "code VARCHAR2(20))';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createRegionsTable);
            createTrigger(stmt, "regions");

            // Create Centers Table
            createSequence(stmt, "centers_seq");
            String createCentersTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE centers (" +
                    "id NUMBER PRIMARY KEY," +
                    "name VARCHAR2(150) UNIQUE NOT NULL," +
                    "code VARCHAR2(20)," +
                    "region_id NUMBER," +
                    "CONSTRAINT fk_center_region FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE SET NULL)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createCentersTable);
            createTrigger(stmt, "centers");

            // Create Professional Groups Table
            createSequence(stmt, "professional_groups_seq");
            String createProfGroupsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE professional_groups (" +
                    "id NUMBER PRIMARY KEY," +
                    "name VARCHAR2(100) UNIQUE NOT NULL)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createProfGroupsTable);
            createTrigger(stmt, "professional_groups");

            // Create Professions Table
            createSequence(stmt, "professions_seq");
            String createProfessionsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE professions (" +
                    "id NUMBER PRIMARY KEY," +
                    "name VARCHAR2(150) UNIQUE NOT NULL," +
                    "professional_group_id NUMBER," +
                    "CONSTRAINT fk_prof_group FOREIGN KEY (professional_group_id) REFERENCES professional_groups(id) ON DELETE SET NULL)';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createProfessionsTable);
            createTrigger(stmt, "professions");

            // Create System Settings Table
            String createSettingsTable = "BEGIN\n" +
                    "  EXECUTE IMMEDIATE 'CREATE TABLE system_settings (" +
                    "setting_key VARCHAR2(100) PRIMARY KEY," +
                    "setting_value VARCHAR2(255))';\n" +
                    "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF;\n" +
                    "END;";
            stmt.execute(createSettingsTable);

            // Initialize default secret number increment setting
            try {
                stmt.execute("INSERT INTO system_settings (setting_key, setting_value) " +
                        "SELECT 'secret_number_increment', '10' FROM DUAL " +
                        "WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'secret_number_increment')");
            } catch (SQLException ignore) {}

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