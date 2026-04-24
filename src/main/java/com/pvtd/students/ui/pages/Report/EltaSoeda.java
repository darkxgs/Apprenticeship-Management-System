package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.services.DictionaryService;
import com.pvtd.students.models.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import com.pvtd.students.ui.utils.ReportWorker;

public class EltaSoeda extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EltaSoeda.class.getName());

    public EltaSoeda() {
        initComponents();
        setupTableUi();
        loadGroups();
        setTitle("تصفية الطلاب - التسويدة");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
    }

    private void setupTableUi() {
        jTable1.setRowHeight(55);
        jTable1.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 24));
        if (jTable1.getTableHeader() != null) {
            jTable1.getTableHeader().setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 24));
            jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 55));
        }
        jTable1.setForeground(java.awt.Color.BLACK);
        jTable1.setSelectionBackground(new java.awt.Color(135, 206, 250)); // Light sky blue for better contrast
        jTable1.setSelectionForeground(java.awt.Color.BLACK);
        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(java.awt.Color.WHITE);
                }
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return c;
            }
        });
    }

    private void loadGroups() {
        comboGroup.removeAllItems();
        comboGroup.addItem("الكل");
        List<String> groups = DictionaryService.getCombinedItems(DictionaryService.CAT_PROF_GROUP);
        for (String g : groups) {
            comboGroup.addItem(g);
        }
    }

    private void loadProfessions(String group) {
        comboProf.removeAllItems();
        comboProf.addItem("الكل");
        List<String> professions = DictionaryService.getProfessionsByGroup(group);
        for (String p : professions) {
            comboProf.addItem(p);
        }
    }

    private void loadStudents(String group, String profession) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        StringBuilder sql = new StringBuilder("SELECT name, seat_no, registration_no, coordination_no, professional_group, profession, status FROM students WHERE 1=1 ");
        if (group != null && !group.equals("الكل")) {
            sql.append("AND TRIM(professional_group) = TRIM(?) ");
        }
        if (profession != null && !profession.equals("الكل")) {
            sql.append("AND TRIM(profession) = TRIM(?) ");
        }
        sql.append("ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            int paramIdx = 1;
            if (group != null && !group.equals("الكل")) {
                ps.setString(paramIdx++, group);
            }
            if (profession != null && !profession.equals("الكل")) {
                ps.setString(paramIdx++, profession);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("status"),
                        rs.getString("profession"),
                        rs.getString("registration_no"),
                        rs.getString("coordination_no"),
                        rs.getString("seat_no"),
                        rs.getString("name")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "خطأ في تحميل البيانات: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        comboGroup = new com.pvtd.students.ui.components.Combobox();
        comboProf = new com.pvtd.students.ui.components.Combobox();
        btnSelectAll = new javax.swing.JButton();
        btnTasoeda = new com.pvtd.students.ui.components.ButtonGradient();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(0, 102, 51));
        jPanel2.setPreferredSize(new java.awt.Dimension(1000, 100));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("تصفية الطلاب - التسويدة");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 20);
        jPanel2.add(jLabel1, gridBagConstraints);

        comboGroup.setLabeText("المجموعة المهنية");
        comboGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboGroupActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel2.add(comboGroup, gridBagConstraints);

        comboProf.setLabeText("المهنة");
        comboProf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboProfActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel2.add(comboProf, gridBagConstraints);

        btnSelectAll.setBackground(new java.awt.Color(51, 102, 255));
        btnSelectAll.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSelectAll.setForeground(new java.awt.Color(255, 255, 255));
        btnSelectAll.setText("تحديد الكل");
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel2.add(btnSelectAll, gridBagConstraints);

        btnTasoeda.setText("إخراج التسويدة");
        btnTasoeda.setColor1(new java.awt.Color(0, 153, 102));
        btnTasoeda.setColor2(new java.awt.Color(0, 102, 51));
        btnTasoeda.addActionListener(this::btnTasoedaActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel2.add(btnTasoeda, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "الحالة", "المهنة", "رقم التسجيل", "كود التنسيق", "رقم الجلوس", "الاسم"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void comboGroupActionPerformed(java.awt.event.ActionEvent evt) {                                           
        if (comboGroup.getSelectedItem() != null) {
            String group = comboGroup.getSelectedItem().toString();
            loadProfessions(group);
            refreshTable();
        }
    }                                          

    private void comboProfActionPerformed(java.awt.event.ActionEvent evt) {                                          
        refreshTable();
    }

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {
        jTable1.selectAll();
    }

    private void refreshTable() {
        String group = comboGroup.getSelectedItem() != null ? comboGroup.getSelectedItem().toString() : "الكل";
        String profession = comboProf.getSelectedItem() != null ? comboProf.getSelectedItem().toString() : "الكل";
        loadStudents(group, profession);
    }

    private void btnTasoedaActionPerformed(java.awt.event.ActionEvent evt) {
        generateReport();
    }

    private boolean checkIfProfessionIs3070(String profession) {
        String sql = "SELECT 1 FROM subjects WHERE TRIM(profession) = TRIM(?) AND parent_subject_id IS NOT NULL FETCH FIRST 1 ROW ONLY";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, profession);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void generateReport() {
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        int[] selectedRows = jTable1.getSelectedRows();
        
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.LinkedHashMap<String, java.util.List<String>> byProfession = new java.util.LinkedHashMap<>();
        int totalSelected = selectedRows.length;
        for (int i : selectedRows) {
            String seatNoCol = String.valueOf(model1.getValueAt(i, 4)); // col 4 = رقم الجلوس
            String profCol   = String.valueOf(model1.getValueAt(i, 1)); // col 1 = المهنة
            String seatNo = seatNoCol != null ? seatNoCol.trim() : "";
            String prof   = profCol != null ? profCol.trim() : "";
            byProfession.computeIfAbsent(prof, k -> new java.util.ArrayList<>()).add(seatNo);
        }

        String[] months = {
            "يناير", "فبراير", "مارس", "أبريل",
            "مايو", "يونيو", "يوليو", "أغسطس",
            "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };
        String selectedMonth = (String) javax.swing.JOptionPane.showInputDialog(this, "اختر شهر الامتحان:", "تحديد الموعد",
                javax.swing.JOptionPane.QUESTION_MESSAGE, null, months, months[4]);
        if (selectedMonth == null) return;

        String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));

        ReportWorker worker = new ReportWorker(this, "تسويدة رصد الدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                String centerName = "";
                String regionName = "";
                
                // COMBINED DOCUMENT
                com.itextpdf.text.Document combinedDoc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A3.rotate());
                String combinedFn = "التقارير/التسويدة/تقرير_التسويدة_المجمع.pdf";
                com.itextpdf.text.pdf.PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
                combinedDoc.open();

                try (Connection con = DatabaseConnection.getConnection()) {
                    String getStudentSql = "SELECT id, name, registration_no, coordination_no, seat_no, status, national_id, professional_group, secret_no, region, center_name FROM students WHERE seat_no = ?";
                    PreparedStatement getStudentPs = con.prepareStatement(getStudentSql);

                    String getGradesSql = "SELECT subject_id, obtained_mark FROM student_grades WHERE student_id = ?";
                    PreparedStatement getGradesPs = con.prepareStatement(getGradesSql);

                    int processed = 0;
                    // First, collect all students and their metadata
                    java.util.List<Student> allSelectedStudents = new java.util.ArrayList<>();
                    for (java.util.Map.Entry<String, java.util.List<String>> entry : byProfession.entrySet()) {
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
                                    
                                    if (centerName.isEmpty()) centerName = rsStudent.getString("center_name");
                                    if (regionName.isEmpty()) regionName = rsStudent.getString("region");

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
                        processed += list.size();
                        updateStatus(processed, totalSelected, "جاري معالجة بيانات التسويدة مهنة: " + prof);
                        
                        // Sort within profession by secret number (Numerical)
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
                        
                        gradReportTasoeda report = new gradReportTasoeda(prof, centerName, regionName, list, false, selectedMonth, currentYear);
                        report.createPDF(combinedDoc);
                    }
                }
                
                combinedDoc.close();
                java.awt.Desktop.getDesktop().open(new java.io.File(combinedFn));

                return null;
            }
        };
        worker.start();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new EltaSoeda().setVisible(true));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton btnSelectAll;
    private com.pvtd.students.ui.components.ButtonGradient btnTasoeda;
    private com.pvtd.students.ui.components.Combobox comboGroup;
    private com.pvtd.students.ui.components.Combobox comboProf;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration                   
}
