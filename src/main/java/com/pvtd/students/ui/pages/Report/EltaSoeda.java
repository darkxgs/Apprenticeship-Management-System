package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.services.DictionaryService;
import com.pvtd.students.models.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import com.pvtd.students.ui.utils.ReportWorker;

public class EltaSoeda extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(EltaSoeda.class.getName());

    public EltaSoeda() {
        initComponents();
        setupTableUi();
        loadFilters();
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
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (!isSelected) {
                    c.setBackground(java.awt.Color.WHITE);
                }
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return c;
            }
        });
    }

    private void loadFilters() {
        comboRegion.removeAllItems();
        comboRegion.addItem("الكل");
        List<String> regions = DictionaryService.getCombinedItems(DictionaryService.CAT_REGION);
        for (String r : regions) {
            comboRegion.addItem(r);
        }

        comboCenter.removeAllItems();
        comboCenter.addItem("الكل");

        comboProf.removeAllItems();
        comboProf.addItem("الكل");
        List<String> professions = DictionaryService.getCombinedItems(DictionaryService.CAT_PROFESSION);
        for (String p : professions) {
            comboProf.addItem(p);
        }
    }

    private void loadCenters(String region) {
        comboCenter.removeAllItems();
        comboCenter.addItem("الكل");
        if (region.equals("الكل")) {
            List<String> centers = DictionaryService.getCombinedItems(DictionaryService.CAT_CENTER);
            for (String c : centers) {
                comboCenter.addItem(c);
            }
        } else {
            java.util.Map<String, String> centers = com.pvtd.students.services.StudentService
                    .getCentersByRegionWithCodes(region);
            for (String c : centers.keySet()) {
                comboCenter.addItem(c);
            }
        }
    }

    private void loadStudents(String region, String center, String profession) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        StringBuilder sql = new StringBuilder(
                "SELECT name, seat_no, registration_no, coordination_no, professional_group, profession, status FROM students WHERE 1=1 ");
        if (region != null && !region.equals("الكل")) {
            sql.append("AND TRIM(region) = TRIM(?) ");
        }
        if (center != null && !center.equals("الكل")) {
            sql.append("AND TRIM(center_name) = TRIM(?) ");
        }
        if (profession != null && !profession.equals("الكل")) {
            sql.append("AND TRIM(profession) = TRIM(?) ");
        }
        sql.append(
                "ORDER BY TO_NUMBER(REGEXP_REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(seat_no, '٠', '0'), '١', '1'), '٢', '2'), '٣', '3'), '٤', '4'), '٥', '5'), '٦', '6'), '٧', '7'), '٨', '8'), '٩', '9'), '[^0-9]', '')) ASC, id ASC");

        try (Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            if (region != null && !region.equals("الكل")) {
                ps.setString(paramIdx++, region);
            }
            if (center != null && !center.equals("الكل")) {
                ps.setString(paramIdx++, center);
            }
            if (profession != null && !profession.equals("الكل")) {
                ps.setString(paramIdx++, profession);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
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
        comboRegion = new com.pvtd.students.ui.components.Combobox();
        comboCenter = new com.pvtd.students.ui.components.Combobox();
        comboProf = new com.pvtd.students.ui.components.Combobox();

        comboRegion.setLabeText("المنطقة");
        comboRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (comboRegion.getSelectedItem() != null) {
                    loadCenters(comboRegion.getSelectedItem().toString());
                }
                refreshTable();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel2.add(comboRegion, gridBagConstraints);

        comboCenter.setLabeText("المركز");
        comboCenter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshTable();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel2.add(comboCenter, gridBagConstraints);

        comboProf.setLabeText("المهنة");
        comboProf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshTable();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel2.add(comboProf, gridBagConstraints);

        btnSelectAll = new javax.swing.JButton();
        btnTasoeda = new com.pvtd.students.ui.components.ButtonGradient();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());

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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel2.add(btnSelectAll, gridBagConstraints);

        btnTasoeda.setText("إخراج التسويدة");
        btnTasoeda.setColor1(new java.awt.Color(0, 153, 102));
        btnTasoeda.setColor2(new java.awt.Color(0, 102, 51));
        btnTasoeda.addActionListener(this::btnTasoedaActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel2.add(btnTasoeda, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("تصفية الطلاب - التسويدة");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 50, 0, 20);
        jPanel2.add(jLabel1, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "الحالة", "المهنة", "رقم التسجيل", "كود التنسيق", "رقم الجلوس", "الاسم"
                }) {
            boolean[] canEdit = new boolean[] {
                    false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {
        jTable1.selectAll();
    }

    private void refreshTable() {
        String region = comboRegion.getSelectedItem() != null ? comboRegion.getSelectedItem().toString() : "الكل";
        String center = comboCenter.getSelectedItem() != null ? comboCenter.getSelectedItem().toString() : "الكل";
        String profession = comboProf.getSelectedItem() != null ? comboProf.getSelectedItem().toString() : "الكل";
        loadStudents(region, center, profession);
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
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.LinkedHashMap<String, java.util.List<String>> byProfession = new java.util.LinkedHashMap<>();
        int totalSelected = selectedRows.length;
        for (int i : selectedRows) {
            String seatNoCol = String.valueOf(model1.getValueAt(i, 4)); // col 4 = رقم الجلوس
            String profCol = String.valueOf(model1.getValueAt(i, 1)); // col 1 = المهنة
            String seatNo = seatNoCol != null ? seatNoCol.trim() : "";
            String prof = profCol != null ? profCol.trim() : "";
            byProfession.computeIfAbsent(prof, k -> new java.util.ArrayList<>()).add(seatNo);
        }

        String[] monthsResult = com.pvtd.students.ui.utils.ReportUtils.chooseMonths(this);
        if (monthsResult == null)
            return;
        String selectedMonth = monthsResult[0];
        String admissionMonth = monthsResult[1];


        String currentYear = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));

        ReportWorker worker = new ReportWorker(this, "تسويدة رصد الدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                String centerName = "";
                String regionName = "";

                try (Connection con = DatabaseConnection.getConnection()) {
                    String getStudentSql = "SELECT id, name, registration_no, coordination_no, seat_no, status, national_id, professional_group, secret_no, region, center_name FROM students WHERE seat_no = ?";
                    PreparedStatement getStudentPs = con.prepareStatement(getStudentSql);

                    String getGradesSql = "SELECT subject_id, obtained_mark FROM student_grades WHERE student_id = ?";
                    PreparedStatement getGradesPs = con.prepareStatement(getGradesSql);

                    // 1. First, collect all selected students and their grades
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
                                    st.setCenterName(rsStudent.getString("center_name") != null ? rsStudent.getString("center_name").trim() : "غير محدد");
                                    st.setRegion(rsStudent.getString("region") != null ? rsStudent.getString("region").trim() : "غير محدد");

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

                    // 2. GLOBAL SORT by Seat Number (Numerical) before any grouping
                    allSelectedStudents.sort((s1, s2) -> {
                        String sn1 = s1.getSeatNo() != null ? s1.getSeatNo().trim() : "";
                        String sn2 = s2.getSeatNo() != null ? s2.getSeatNo().trim() : "";
                        
                        // Robust normalization: Replace Arabic/Indian digits with English
                        String sn1Norm = sn1.replace("٠", "0").replace("١", "1").replace("٢", "2").replace("٣", "3").replace("٤", "4")
                                            .replace("٥", "5").replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9");
                        String sn2Norm = sn2.replace("٠", "0").replace("١", "1").replace("٢", "2").replace("٣", "3").replace("٤", "4")
                                            .replace("٥", "5").replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9");

                        // Extract only digits
                        String sn1Clean = sn1Norm.replaceAll("\\D", "");
                        String sn2Clean = sn2Norm.replaceAll("\\D", "");
                                            
                        if (!sn1Clean.isEmpty() && !sn2Clean.isEmpty()) {
                            try {
                                return Long.compare(Long.parseLong(sn1Clean), Long.parseLong(sn2Clean));
                            } catch (Exception ex) {}
                        }
                        return sn1Norm.compareTo(sn2Norm);
                    });

                    // 3. Group by Profession (Preserving the globally sorted order)
                    java.util.LinkedHashMap<String, java.util.List<Student>> groupedByProfession = new java.util.LinkedHashMap<>();
                    for (Student s : allSelectedStudents) {
                        String prof = s.getProfession() != null ? s.getProfession().trim() : "بدون حرفة";
                        groupedByProfession.computeIfAbsent(prof, k -> new java.util.ArrayList<>()).add(s);
                    }

                    java.io.File folder = new java.io.File("التقارير/تسويدة");
                    if (!folder.exists()) folder.mkdirs();

                    int processed = 0;
                    for (java.util.Map.Entry<String, java.util.List<Student>> entry : groupedByProfession.entrySet()) {
                        String professionName = entry.getKey();
                        java.util.List<Student> professionStudents = entry.getValue();

                        // Determine center name for the header
                        String displayCenterName = "مراكز متعددة";
                        java.util.Set<String> uniqueCenters = professionStudents.stream()
                            .map(Student::getCenterName)
                            .filter(Objects::nonNull)
                            .collect(java.util.stream.Collectors.toSet());
                        if (uniqueCenters.size() == 1) {
                            displayCenterName = uniqueCenters.iterator().next();
                        } else if (uniqueCenters.isEmpty()) {
                            displayCenterName = "غير محدد";
                        }

                        // Create a combined document for the profession
                        com.itextpdf.text.Document combinedDoc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A3.rotate());
                        String sanitizedProfession = professionName.replace("/", "_").replace("\\", "_").replace(":", "_");
                        String combinedFn = "التقارير/تسويدة/" + sanitizedProfession + ".pdf";
                        com.itextpdf.text.pdf.PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
                        combinedDoc.open();

                        updateStatus(processed, totalSelected, "جاري إنشاء التسويدة لمهنة: " + professionName);

                        String rName = !professionStudents.isEmpty() ? professionStudents.get(0).getRegion() : "";

                        gradReportTasoeda report = new gradReportTasoeda(professionName, displayCenterName, rName, professionStudents, false,
                                selectedMonth, currentYear, admissionMonth);
                        
                        report.createPDF(combinedDoc, 1, 0);

                        combinedDoc.close();
                        processed += professionStudents.size();
                    }

                    java.awt.Desktop.getDesktop().open(folder);

                    java.awt.Desktop.getDesktop().open(folder);
                }

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
    private com.pvtd.students.ui.components.Combobox comboRegion;
    private com.pvtd.students.ui.components.Combobox comboCenter;
    private com.pvtd.students.ui.components.Combobox comboProf;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration
}
