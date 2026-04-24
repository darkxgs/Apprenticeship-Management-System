package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import java.awt.*;
import com.pvtd.students.models.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * تسويدة رصد درجات الـ 30/70
 * يعرض فقط الطلاب الذين حرفتهم تستخدم نظام الـ (مادة أب / مادة ابن) - 30/70
 */
public class EltaSoeda3070 extends JFrame {

    private JComboBox<String> comboGroup;
    private JComboBox<String> comboProf;
    private JTable jTable1;
    private JScrollPane jScrollPane1;
    private JButton btnSelectAll;
    private com.pvtd.students.ui.components.ButtonGradient btnGenerate;

    public EltaSoeda3070() {
        setTitle("تسويدة الدرجات - نظام 30/70");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        setupTableUi();
        loadGroups();
        loadStudents(null, null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ── Toolbar ──────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setBackground(new Color(0, 102, 51));
        toolbar.setPreferredSize(new Dimension(1000, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // Title
        JLabel title = new JLabel("تسويدة رصد درجات 30 / 70");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        gbc.gridx = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 50, 0, 20);
        toolbar.add(title, gbc);

        // Combo المجموعة
        comboGroup = new JComboBox<>();
        comboGroup.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboGroup.setPreferredSize(new Dimension(220, 40));
        comboGroup.addActionListener(e -> onGroupChanged());
        gbc.gridx = 1;
        gbc.ipadx = 180;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 15, 0, 15);
        toolbar.add(comboGroup, gbc);

        // Combo المهنة
        comboProf = new JComboBox<>();
        comboProf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboProf.setPreferredSize(new Dimension(220, 40));
        comboProf.addActionListener(e -> refreshTable());
        gbc.gridx = 0;
        toolbar.add(comboProf, gbc);

        // تحديد الكل
        btnSelectAll = new JButton("تحديد الكل");
        btnSelectAll.setBackground(new Color(51, 102, 255));
        btnSelectAll.setForeground(Color.WHITE);
        btnSelectAll.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSelectAll.addActionListener(e -> jTable1.selectAll());
        gbc.gridx = 2;
        gbc.ipadx = 10;
        toolbar.add(btnSelectAll, gbc);

        // زر إنشاء التسويده
        btnGenerate = new com.pvtd.students.ui.components.ButtonGradient();
        btnGenerate.setText("إنشاء التسويده 30/70");
        btnGenerate.setColor1(new Color(0, 153, 102));
        btnGenerate.setColor2(new Color(0, 102, 51));
        btnGenerate.addActionListener(e -> generateReport());
        gbc.gridx = 3;
        gbc.ipadx = 30;
        toolbar.add(btnGenerate, gbc);

        add(toolbar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        jTable1 = new JTable(new DefaultTableModel(
            new Object[][]{},
            new String[]{"الحالة", "الرقم السري", "رقم الجلوس", "الرقم القومي", "الحرفة", "كود التنسيق", "رقم التسجيل", "الاسم"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        jScrollPane1 = new JScrollPane(jTable1);
        add(jScrollPane1, BorderLayout.CENTER);
    }

    private void setupTableUi() {
        jTable1.setRowHeight(45);
        jTable1.setFont(new Font("Arial", Font.PLAIN, 18));
        if (jTable1.getTableHeader() != null) {
            jTable1.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
            jTable1.getTableHeader().setPreferredSize(new Dimension(0, 45));
            jTable1.getTableHeader().setBackground(new Color(204, 255, 255));
        }
        jTable1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        jTable1.setForeground(Color.BLACK);
        jTable1.setSelectionBackground(new Color(135, 206, 250));
        jTable1.setSelectionForeground(Color.BLACK);
        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }

    // ── Data loading ──────────────────────────────────────────────────

    /**
     * يحمّل المجموعات المهنية التي تحتوي على مواد بنظام 30/70 فقط.
     */
    private void loadGroups() {
        comboGroup.removeAllItems();
        comboGroup.addItem("الكل");
        String sql = "SELECT DISTINCT s.professional_group FROM students s " +
                     "WHERE s.professional_group IS NOT NULL " +
                     "AND EXISTS (" +
                     "  SELECT 1 FROM subjects sub " +
                     "  WHERE TRIM(sub.profession) = TRIM(s.profession) " +
                     "  AND sub.parent_subject_id IS NOT NULL" +
                     ") ORDER BY s.professional_group";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String g = rs.getString(1);
                if (g != null && !g.isBlank()) comboGroup.addItem(g.trim());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * يحمّل المهن التي تستخدم نظام 30/70 للمجموعة المختارة.
     */
    private void loadProfessions(String group) {
        comboProf.removeAllItems();
        comboProf.addItem("الكل");
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT s.profession FROM students s " +
            "WHERE s.profession IS NOT NULL " +
            "AND EXISTS (" +
            "  SELECT 1 FROM subjects sub " +
            "  WHERE TRIM(sub.profession) = TRIM(s.profession) " +
            "  AND sub.parent_subject_id IS NOT NULL" +
            ")");
        if (group != null && !group.equals("الكل")) {
            sql.append(" AND TRIM(s.professional_group) = TRIM(?)");
        }
        sql.append(" ORDER BY s.profession");
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (group != null && !group.equals("الكل")) ps.setString(1, group);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    if (p != null && !p.isBlank()) comboProf.addItem(p.trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * يحمّل الطلاب الذين حرفتهم تستخدم نظام 30/70.
     */
    private void loadStudents(String group, String profession) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        StringBuilder sql = new StringBuilder(
            "SELECT name, seat_no, registration_no, coordination_no, professional_group, profession, status, national_id, secret_no " +
            "FROM students s WHERE EXISTS (" +
            "  SELECT 1 FROM subjects sub " +
            "  WHERE TRIM(sub.profession) = TRIM(s.profession) " +
            "  AND sub.parent_subject_id IS NOT NULL" +
            ")");
        if (group != null && !group.equals("الكل"))
            sql.append(" AND TRIM(s.professional_group) = TRIM(?)");
        if (profession != null && !profession.equals("الكل"))
            sql.append(" AND TRIM(s.profession) = TRIM(?)");
        sql.append(" ORDER BY s.name ASC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (group != null && !group.equals("الكل"))     ps.setString(idx++, group);
            if (profession != null && !profession.equals("الكل")) ps.setString(idx++, profession);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("status"),
                        rs.getString("secret_no"),
                        rs.getString("seat_no"),
                        rs.getString("national_id"),
                        rs.getString("profession"),
                        rs.getString("coordination_no"),
                        rs.getString("registration_no"),
                        rs.getString("name")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "خطأ في تحميل البيانات: " + ex.getMessage());
        }

        // Auto-select all after load
        SwingUtilities.invokeLater(() -> jTable1.selectAll());
    }

    // ── Events ────────────────────────────────────────────────────────

    private void onGroupChanged() {
        String group = (String) comboGroup.getSelectedItem();
        if (group != null) {
            loadProfessions(group);
            refreshTable();
        }
    }

    private void refreshTable() {
        String group = (String) comboGroup.getSelectedItem();
        String prof  = (String) comboProf.getSelectedItem();
        loadStudents(group == null ? "الكل" : group, prof == null ? "الكل" : prof);
    }

    // ── Report generation ─────────────────────────────────────────────

    private void generateReport() {
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        int[] selectedRows = jTable1.getSelectedRows();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Add Month selection
        String[] months = {
            "يناير", "فبراير", "مارس", "أبريل",
            "مايو", "يونيو", "يوليو", "أغسطس",
            "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };
        String selectedMonth = (String) JOptionPane.showInputDialog(this, "اختر شهر الامتحان:", "تحديد الموعد",
                JOptionPane.QUESTION_MESSAGE, null, months, months[4]);
        if (selectedMonth == null) return;
        
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        // Group selected students by profession
        java.util.LinkedHashMap<String, java.util.List<String>> byProfession = new java.util.LinkedHashMap<>();
        for (int i : selectedRows) {
            String seatNo = String.valueOf(model1.getValueAt(i, 2)); // col 2 = رقم الجلوس
            String prof   = String.valueOf(model1.getValueAt(i, 4)); // col 4 = الحرفة
            byProfession.computeIfAbsent(prof, k -> new java.util.ArrayList<>()).add(seatNo);
        }

        String centerName = "";
        String regionName = "";

        try {
            // COMBINED DOCUMENT
            com.itextpdf.text.Document combinedDoc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A3.rotate());
            String combinedFn = "التقارير/التسويدة/تقرير_التسويدة_المجمع_30_70.pdf";
            com.itextpdf.text.pdf.PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
            combinedDoc.open();

            try (Connection con = DatabaseConnection.getConnection()) {
                String getStudentSql =
                    "SELECT id, name, registration_no, coordination_no, seat_no, status, " +
                    "national_id, professional_group, secret_no, region, center_name " +
                    "FROM students WHERE seat_no = ?";
                PreparedStatement getStudentPs = con.prepareStatement(getStudentSql);

                // Fetch child-subject grades (مواد الابن اللي فيها درجات الـ 30 والـ 70)
                String getGradesSql = "SELECT subject_id, obtained_mark FROM student_grades WHERE student_id = ?";
                PreparedStatement getGradesPs = con.prepareStatement(getGradesSql);

                java.util.List<Student> allSelectedStudents = new java.util.ArrayList<>();
                for (Map.Entry<String, java.util.List<String>> entry : byProfession.entrySet()) {
                    String professionName = entry.getKey();
                    for (String seatNo : entry.getValue()) {
                        getStudentPs.setString(1, seatNo);
                        try (ResultSet rsStudent = getStudentPs.executeQuery()) {
                            if (rsStudent.next()) {
                                Student st = new Student();
                                st.setId(rsStudent.getInt("id"));
                                st.setName(rsStudent.getString("name"));
                                st.setRegistrationNo(rsStudent.getString("registration_no"));
                                st.setCoordinationNo(rsStudent.getString("coordination_no"));
                                st.setSeatNo(rsStudent.getString("seat_no"));
                                st.setStatus(rsStudent.getString("status"));
                                st.setNationalId(rsStudent.getString("national_id"));
                                st.setProfessionalGroup(rsStudent.getString("professional_group"));
                                st.setSecretNo(rsStudent.getString("secret_no"));
                                st.setProfession(professionName);

                                if (centerName.isEmpty() && rsStudent.getString("center_name") != null)
                                    centerName = rsStudent.getString("center_name");
                                if (regionName.isEmpty() && rsStudent.getString("region") != null)
                                    regionName = rsStudent.getString("region");

                                // تحميل الدرجات (مواد الابن + مواد الأب العادية)
                                java.util.Map<Integer, Integer> grades = new java.util.HashMap<>();
                                getGradesPs.setInt(1, st.getId());
                                try (ResultSet rsGrades = getGradesPs.executeQuery()) {
                                    while (rsGrades.next()) {
                                        grades.put(rsGrades.getInt("subject_id"), rsGrades.getInt("obtained_mark"));
                                    }
                                }
                                st.setGrades(grades);
                                allSelectedStudents.add(st);
                            }
                        }
                    }
                }

                // Group and Sort Professions by their minimum Secret Number (Numerical)
                java.util.Map<String, java.util.List<Student>> grouped = allSelectedStudents.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Student::getProfession));

                java.util.List<String> sortedProfessions = grouped.keySet().stream()
                    .sorted((p1, p2) -> {
                        Integer min1 = grouped.get(p1).stream()
                            .map(s -> s.getSecretNo() != null ? s.getSecretNo().replaceAll("\\D", "") : "99999999")
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .min(Integer::compare).orElse(99999999);
                        Integer min2 = grouped.get(p2).stream()
                            .map(s -> s.getSecretNo() != null ? s.getSecretNo().replaceAll("\\D", "") : "99999999")
                            .filter(s -> !s.isEmpty())
                            .map(Integer::parseInt)
                            .min(Integer::compare).orElse(99999999);
                        return min1.compareTo(min2);
                    })
                    .collect(java.util.stream.Collectors.toList());

                for (String prof : sortedProfessions) {
                    java.util.List<Student> list = grouped.get(prof);
                    if (!list.isEmpty()) {
                        // Sort by secret number (Numerical)
                        list.sort((s1, s2) -> {
                            String sn1Str = s1.getSecretNo() != null ? s1.getSecretNo().replaceAll("\\D", "") : "";
                            String sn2Str = s2.getSecretNo() != null ? s2.getSecretNo().replaceAll("\\D", "") : "";
                            
                            try {
                                if (!sn1Str.isEmpty() && !sn2Str.isEmpty()) {
                                    return Integer.compare(Integer.parseInt(sn1Str), Integer.parseInt(sn2Str));
                                }
                            } catch (Exception ex) {}
                            return sn1Str.compareTo(sn2Str);
                        });

                        // is3070 = true → gradReportTasoeda يعرض مواد الابن (30/70)
                        gradReportTasoeda report = new gradReportTasoeda(prof, centerName, regionName, list, true, selectedMonth, currentYear);
                        report.createPDF(combinedDoc);
                    }
                }
            }

            combinedDoc.close();
            Desktop.getDesktop().open(new java.io.File(combinedFn));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "حدث خطأ أثناء إنشاء التقرير: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EltaSoeda3070().setVisible(true));
    }
}
