package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.pvtd.students.ui.utils.ReportWorker;
import com.pvtd.students.ui.utils.ReportUtils;

import com.pvtd.students.models.Student;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class SucssfullPageEdit extends javax.swing.JFrame {

    private com.pvtd.students.ui.utils.ReportFilterPanel filterPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SucssfullPageEdit.class.getName());

    /** Caches the exam_system fetched from loadStudents — used by both reports */
    private String loadedSystemName = "نظامي";

    private void buttonSecretReportActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalSelected = selectedRows.length;
        
        String[] filters = filterPanel != null ? filterPanel.getSelectedMonths() : new String[]{"", "", "", "", "", ""};
        if (filters == null || filters.length < 6) return;
        final String selMonth = filters[4];
        final String admMonth = filters[5];

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = combobox1.getSelectedItem() != null ? combobox1.getSelectedItem().toString() : "";
        final String systemName = loadedSystemName;

        ReportWorker worker = new ReportWorker(this, "كشف الناجحين بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                // تخزين الطلاب حسب المنطقة مع الحفاظ على ترتيب الاختيار
                java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> allStudentsByRegion = new java.util.LinkedHashMap<>();
                java.util.Map<String, String> allStudentSystems = new java.util.HashMap<>();

                try (Connection con = DatabaseConnection.getConnection()) {
                    // 1. جمع كافة أرقام الجلوس المختارة في قائمة
                    java.util.List<String> seatNumbers = new java.util.ArrayList<>();
                    for (int row : selectedRows) {
                        int modelRow = jTable1.convertRowIndexToModel(row);
                        seatNumbers.add(String.valueOf(model1.getValueAt(modelRow, 1)));
                    }

                    if (seatNumbers.isEmpty()) return null;

                    // 2. جلب بيانات الطلاب دفعة واحدة (مقسمة لمجموعات لتجنب حدود الـ IN clause)
                    java.util.Map<String, com.pvtd.students.models.Student> studentMap = new java.util.HashMap<>();
                    java.util.Map<String, String> studentRegions = new java.util.HashMap<>();
                    java.util.List<Integer> studentIds = new java.util.ArrayList<>();

                    int batchSize = 500;
                    for (int i = 0; i < seatNumbers.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, seatNumbers.size());
                        java.util.List<String> batch = seatNumbers.subList(i, end);
                        
                        StringBuilder sb = new StringBuilder("SELECT id, name, registration_no, seat_no, status, national_id, profession, professional_group, secret_no, coordination_no, region, exam_system FROM students WHERE seat_no IN (");
                        for (int j = 0; j < batch.size(); j++) {
                            sb.append("?");
                            if (j < batch.size() - 1) sb.append(",");
                        }
                        sb.append(")");

                        try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
                            for (int j = 0; j < batch.size(); j++) {
                                ps.setString(j + 1, batch.get(j));
                            }
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    com.pvtd.students.models.Student st = new com.pvtd.students.models.Student();
                                    st.setId(rs.getInt("id"));
                                    st.setName(rs.getString("name"));
                                    st.setRegistrationNo(rs.getString("registration_no"));
                                    st.setSeatNo(rs.getString("seat_no"));
                                    st.setStatus(rs.getString("status"));
                                    st.setNationalId(rs.getString("national_id"));
                                    st.setProfessionalGroup(rs.getString("professional_group"));
                                    st.setExamSystem(rs.getString("exam_system"));
                                    st.setCoordinationNo(rs.getString("coordination_no"));
                                    st.setSecretNo(rs.getString("secret_no"));
                                    st.setProfession(rs.getString("profession"));
                                    
                                    String r = rs.getString("region");
                                    if (r == null || r.trim().isEmpty()) r = regionName;
                                    
                                    studentMap.put(st.getSeatNo(), st);
                                    studentRegions.put(st.getSeatNo(), r);
                                    studentIds.add(st.getId());
                                }
                            }
                        }
                    }

                    // 3. جلب كافة الدرجات دفعة واحدة
                    java.util.Map<Integer, java.util.Map<Integer, Integer>> gradesMap = new java.util.HashMap<>();
                    if (!studentIds.isEmpty()) {
                        for (int i = 0; i < studentIds.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, studentIds.size());
                            java.util.List<Integer> batchIds = studentIds.subList(i, end);
                            
                            StringBuilder sbG = new StringBuilder("SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN (");
                            for (int j = 0; j < batchIds.size(); j++) {
                                sbG.append("?");
                                if (j < batchIds.size() - 1) sbG.append(",");
                            }
                            sbG.append(")");
                            
                            try (PreparedStatement psG = con.prepareStatement(sbG.toString())) {
                                for (int j = 0; j < batchIds.size(); j++) {
                                    psG.setInt(j + 1, batchIds.get(j));
                                }
                                try (ResultSet rsG = psG.executeQuery()) {
                                    while (rsG.next()) {
                                        int sid = rsG.getInt("student_id");
                                        int subid = rsG.getInt("subject_id");
                                        int mark = rsG.getInt("obtained_mark");
                                        gradesMap.computeIfAbsent(sid, k -> new java.util.HashMap<>()).put(subid, mark);
                                    }
                                }
                            }
                        }
                    }

                    // 4. جلب أنظمة المهن دفعة واحدة
                    java.util.Set<String> professions = new java.util.HashSet<>();
                    for (com.pvtd.students.models.Student s : studentMap.values()) {
                        if (s.getProfession() != null) professions.add(s.getProfession().trim());
                    }
                    if (!professions.isEmpty()) {
                        java.util.List<String> profList = new java.util.ArrayList<>(professions);
                        for (int i = 0; i < profList.size(); i += batchSize) {
                            java.util.List<String> batch = profList.subList(i, Math.min(i + batchSize, profList.size()));
                            String inClause = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
                            try (PreparedStatement psSys = con.prepareStatement("SELECT name, exam_system FROM professions WHERE TRIM(name) IN (" + inClause + ")")) {
                                for (int j = 0; j < batch.size(); j++) psSys.setString(j + 1, batch.get(j));
                                try (ResultSet rsSys = psSys.executeQuery()) {
                                    while (rsSys.next()) {
                                        allStudentSystems.put(rsSys.getString("name").trim(), rsSys.getString("exam_system") != null ? rsSys.getString("exam_system").trim() : systemName);
                                    }
                                }
                            }
                        }
                    }

                    // 5. تجميع البيانات النهائية بالترتيب الأصلي للاختيار (0-20%)
                    int total = seatNumbers.size();
                    for (int i = 0; i < total; i++) {
                        if (i % 50 == 0 || i == total - 1) {
                            updateStatus((i + 1) * 20 / total, 100, "جاري تحضير البيانات: " + (i + 1) + "/" + total);
                        }
                        
                        String seatNo = seatNumbers.get(i);
                        com.pvtd.students.models.Student st = studentMap.get(seatNo);
                        if (st != null) {
                            st.setGrades(gradesMap.getOrDefault(st.getId(), new java.util.HashMap<>()));
                            String region = studentRegions.get(seatNo);
                            allStudentsByRegion.computeIfAbsent(region, k -> new java.util.ArrayList<>()).add(st);
                        }
                    }

                    // توليد الملفات (20-100%)
                    generatePdfFiles(allStudentsByRegion, allStudentSystems, centerName, selMonth, admMonth, "التقارير/تبييضة/ناجحين", "جاري إنشاء التقارير", 20, 100);
                }

                java.io.File folder = new java.io.File("التقارير/تبييضة/ناجحين");
                java.awt.Desktop.getDesktop().open(folder);
                return null;
            }

            private void generatePdfFiles(java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> data, 
                                        java.util.Map<String, String> systems, String centerName, String selM, String admM, String folderPath,
                                        String label, int startPct, int endPct) throws Exception {
                java.io.File folder = new java.io.File(folderPath);
                if (!folder.exists()) folder.mkdirs();

                String combinedFn = folderPath + "/مجمع تبييض ناجحين.pdf";
                com.itextpdf.text.Document combinedDoc = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
                combinedDoc.open();

                int totalStudents = data.values().stream().mapToInt(java.util.List::size).sum();
                if (totalStudents == 0) return; 
                int processed = 0;

                for (java.util.Map.Entry<String, java.util.List<com.pvtd.students.models.Student>> rEntry : data.entrySet()) {
                    if (rEntry.getValue().isEmpty()) continue;
                    
                    String region = rEntry.getKey();
                    String sanitized = region.replace("/", "_").replace("\\", "_").replace(":", "_");
                    String fn = folderPath + "/" + sanitized + ".pdf";

                    com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
                    com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(fn));
                    doc.open();

                    // استخدام التقرير التتابعي الذي يدعم المهن المختلطة (مثل الراسبين)
                    gradReportSequential report = new gradReportSequential("تلاميذ ناجحون", new Color(0, 102, 204), centerName, region, rEntry.getValue(), selM, admM);
                    report.appendToDocument(doc);
                    report.appendToDocument(combinedDoc);

                    // تحديث الحالة
                    processed += rEntry.getValue().size();
                    int currentPct = startPct + (processed * (endPct - startPct) / totalStudents);
                    updateStatus(currentPct, 100, label + ": منطقة " + region);
                    
                    doc.close();
                }
                if (processed > 0) {
                    combinedDoc.close();
                    if (folder.exists()) java.awt.Desktop.getDesktop().open(folder);
                } else {
                    combinedDoc.close(); // If we opened it, we must close it, but it might fail if 0 pages. 
                    // Actually, if processed is 0, we shouldn't have opened it or we should add a dummy page.
                }
            }
        };
        worker.start();
    }

    public SucssfullPageEdit() {
        initComponents();
        loadRegions();
        setTitle("تقرير الناجحين");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                String center = (String) cmdcenter.getSelectedItem();
                String region = (String) combobox1.getSelectedItem();
                if (center != null && region != null && !center.startsWith("اختر") && !region.startsWith("اختر")) {
                    loadStudents(center, region);
                }
            }
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {}
        });

        loadRegions();
        setupTableUi();
        
        filterPanel = new com.pvtd.students.ui.utils.ReportFilterPanel();
        filterPanel.addFilterChangeListener(e -> cmdcenterActionPerformed(null));
        javax.swing.JPanel topContainer = new javax.swing.JPanel(new java.awt.BorderLayout());
        topContainer.add(jPanel2, java.awt.BorderLayout.CENTER);
        topContainer.add(filterPanel, java.awt.BorderLayout.SOUTH);
        jPanel1.add(topContainer, java.awt.BorderLayout.PAGE_START);
    }

    private void setupTableUi() {
        if (jTable1 == null) return;
        jTable1.setRowHeight(35);
        jTable1.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
        jTable1.setForeground(java.awt.Color.BLACK);
        jTable1.setSelectionBackground(new java.awt.Color(135, 206, 250));
        jTable1.setSelectionForeground(java.awt.Color.BLACK);
        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(java.awt.Color.WHITE);
                }
                c.setForeground(java.awt.Color.BLACK);
                c.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return c;
            }
        });
    }

    public void loadRegions() {
        combobox1.removeAllItems();
        combobox1.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            combobox1.addItem(r);
        }
    }

    public void loadStudents(String center, String region) {
        new javax.swing.SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                java.util.List<Object[]> data = new java.util.ArrayList<>();
                try (Connection con = DatabaseConnection.getConnection()) {
                    String sql = "SELECT name, profession, registration_no, seat_no, status, national_id, coordination_no, exam_system "
                            + "FROM students "
                            + "WHERE center_name = ? "
                            + "AND region = ? "
                            + "AND status = 'ناجح' "
                            + "AND NOT EXISTS (SELECT 1 FROM student_grades sg WHERE sg.student_id = students.id AND sg.obtained_mark < 0) ";
                    sql += "ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, center);
                        ps.setString(2, region);
                        try (ResultSet rs = ps.executeQuery()) {
                            int i = 1;
                            while (rs.next()) {
                                data.add(new Object[]{
                                    rs.getString("status"),
                                    rs.getString("seat_no"),
                                    rs.getString("coordination_no"),
                                    rs.getString("national_id"),
                                    rs.getString("registration_no"),
                                    rs.getString("profession"),
                                    rs.getString("name"),
                                    i++
                                });
                            }
                        }
                    }
                }
                return data;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                    model.setRowCount(0);
                    java.util.List<Object[]> data = get();
                    for (Object[] row : data) {
                        model.addRow(row);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("اختر المركز...");
        java.util.Map<String, String> centers = com.pvtd.students.services.StudentService.getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) {
            cmdcenter.addItem(c);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        combobox1 = new com.pvtd.students.ui.components.Combobox();
        buttonGradient3 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel2.setBackground(new java.awt.Color(0, 102, 51));
        jPanel2.setPreferredSize(new java.awt.Dimension(1091, 80));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3; gridBagConstraints.gridy = 0; gridBagConstraints.gridheight = 4;
        gridBagConstraints.ipadx = 162; gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 12, 19, 0);
        jPanel2.add(cmdcenter, gridBagConstraints);

        combobox1.setLabeText("المنطقة");
        combobox1.addActionListener(this::combobox1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4; gridBagConstraints.gridy = 0; gridBagConstraints.gridheight = 4;
        gridBagConstraints.ipadx = 167; gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 12, 19, 0);
        jPanel2.add(combobox1, gridBagConstraints);

        buttonGradient3.setText("كشف الناجحين");
        buttonGradient3.setColor1(new java.awt.Color(68, 160, 141));
        buttonGradient3.setColor2(new java.awt.Color(9, 54, 55));
        buttonGradient3.setFont(new java.awt.Font("Segoe UI", 1, 12));
        buttonGradient3.setRadius(40);
        buttonGradient3.addActionListener(this::buttonGradient3ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0; gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 26; gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 6, 0, 0);
        jPanel2.add(buttonGradient3, gridBagConstraints);

        buttonGradient1.setText("كشف الناجحين بي الدرجات");
        buttonGradient1.setColor1(new java.awt.Color(35, 122, 87));
        buttonGradient1.setColor2(new java.awt.Color(9, 48, 40));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 12));
        buttonGradient1.setRadius(40);
        buttonGradient1.addActionListener(this::buttonSecretReportActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1; gridBagConstraints.gridy = 0; gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 18, 0, 0);
        jPanel2.add(buttonGradient1, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 0; gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 28; gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(26, 6, 0, 0);
        jPanel2.add(jButton1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف تقارير بي الطلاب الناجحين");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5; gridBagConstraints.gridy = 0; gridBagConstraints.ipady = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 19);
        jPanel2.add(jLabel1, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {{null, null, null, null, null, null, null, null}},
            new String [] {"حالة التلميذ", "رقم الجلوس ", "كود التنسيق", "الرقم القومي", "رقم التسجيل", "المهنة", "الاسم", "م"}
        ));
        jScrollPane1.setViewportView(jTable1);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE));
        pack();
    }// </editor-fold>

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter.getSelectedItem() != null && combobox1.getSelectedItem() != null) {
            String center = cmdcenter.getSelectedItem().toString();
            String region = combobox1.getSelectedItem().toString();
            if (center.equals("اختر المركز...") || region.equals("اختر المنطقة...")) {
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadStudents(center, region);
        }
    }

    private void buttonGradient3ActionPerformed(java.awt.event.ActionEvent evt) {                                                
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = combobox1.getSelectedItem() != null ? combobox1.getSelectedItem().toString() : "";
        String[] months = filterPanel != null ? filterPanel.getSelectedMonths() : new String[]{"", "", "", "", "", ""};
        if (months == null || months.length < 6) return;
        successful report = new successful(months[4], months[5]);
        if (report.isCancelled) return;

        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        ReportWorker worker = new ReportWorker(this, "كشف الطلاب الناجحين", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(10, 100, "جاري جلب أنظمة المهن...");
                java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem = new java.util.LinkedHashMap<>();
                java.util.Map<String, String> profToSystem = new java.util.HashMap<>();
                try (java.sql.Connection con = com.pvtd.students.db.DatabaseConnection.getConnection()) {
                    String sql = "SELECT name, exam_system FROM professions";
                    try (java.sql.PreparedStatement ps = con.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String pName = rs.getString("name");
                            String pSys = rs.getString("exam_system");
                            if (pName != null) profToSystem.put(pName.trim(), pSys != null ? pSys : "نظامي");
                        }
                    }
                    for (int i = 0; i < selectedRows.length; i++) {
                        int r = jTable1.convertRowIndexToModel(selectedRows[i]);
                        String name = String.valueOf(model1.getValueAt(r, 6) != null ? model1.getValueAt(r, 6) : "");
                        String prof = String.valueOf(model1.getValueAt(r, 5) != null ? model1.getValueAt(r, 5) : "");
                        String cleanProf = prof.replaceAll("<[^>]*>", "").trim();
                        String systemName = profToSystem.getOrDefault(cleanProf, "نظامي");
                        String htmlName = "<html><center>" + name.trim() + "</center></html>";
                        String htmlProf = "<html><center>" + prof.trim() + "</center></html>";
                        java.util.Vector rowData = new java.util.Vector();
                        rowData.add(model1.getValueAt(r, 0)); rowData.add(model1.getValueAt(r, 1)); rowData.add(model1.getValueAt(r, 4));
                        rowData.add(htmlProf); rowData.add(htmlName); rowData.add(i + 1);
                        bySystem.computeIfAbsent(systemName, k -> new java.util.ArrayList<>()).add(rowData);
                    }
                }
                updateStatus(50, 100, "جاري إنشاء ملف PDF...");
                report.createPDFGroupedBySystem(bySystem, centerName, regionName, true);
                return null;
            }
        };
        worker.start();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) { jTable1.selectAll(); }

    private void combobox1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (combobox1.getSelectedItem() != null) {
            String region = combobox1.getSelectedItem().toString();
            if (region.equals("اختر المنطقة...")) {
                cmdcenter.removeAllItems();
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadCenters(region);
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) { logger.log(java.util.logging.Level.SEVERE, null, ex); }
        java.awt.EventQueue.invokeLater(() -> new SucssfullPageEdit().setVisible(true));
    }

    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient3;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox combobox1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
}
