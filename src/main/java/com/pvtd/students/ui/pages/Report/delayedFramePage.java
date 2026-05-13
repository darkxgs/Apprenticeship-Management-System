package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Desktop;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.pvtd.students.ui.utils.ReportWorker;
import com.pvtd.students.ui.utils.ReportUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class delayedFramePage extends javax.swing.JFrame {
    
    private com.pvtd.students.ui.utils.ReportFilterPanel filterPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(delayedFramePage.class.getName());

    public delayedFramePage() {
        initComponents();
        loadRegions();
        setTitle("تقرير المؤجلين");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                String center = (String) cmdcenter.getSelectedItem();
                String region = (String) cmdcenter1.getSelectedItem();
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
        cmdcenter1.removeAllItems();
        cmdcenter1.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            cmdcenter1.addItem(r);
        }
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("اختر المركز...");
        java.util.Map<String, String> centers = com.pvtd.students.services.StudentService.getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) {
            cmdcenter.addItem(c);
        }
    }

    public void loadStudents(String center, String region) {
        try {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            try (Connection con = DatabaseConnection.getConnection()) {
                String sql = "SELECT name, profession, registration_no, seat_no, status FROM students " +
                             "WHERE center_name = ? AND region = ? AND status LIKE '%مؤجل%' ";
                sql += "ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, center);
                ps.setString(2, region);
                ResultSet rs = ps.executeQuery();
                int i = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("status"),
                        rs.getString("seat_no"),                
                        rs.getString("registration_no"),
                        rs.getString("profession"),
                        rs.getString("name"),
                        i++
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        cmdcenter1 = new com.pvtd.students.ui.components.Combobox();
        jLabel1 = new javax.swing.JLabel();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradientGrades = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel2.setBackground(new java.awt.Color(0, 102, 102));
        jPanel2.setPreferredSize(new java.awt.Dimension(475, 100));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 1; gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 134; gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 24, 0, 0);
        jPanel2.add(cmdcenter, gridBagConstraints);

        cmdcenter1.setLabeText("المنطقة");
        cmdcenter1.addActionListener(this::cmdcenter1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3; gridBagConstraints.gridy = 1; gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 134; gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 18, 0, 0);
        jPanel2.add(cmdcenter1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف التلاميذ مؤجلون");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4; gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 56, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        buttonGradient1.setText("كشف الطلاب مؤجلون بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(9, 54, 55));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 12));
        buttonGradient1.setRadius(30);
        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(buttonGradient1, gridBagConstraints);

        buttonGradientGrades.setText("كشف مؤجلون بالدرجات");
        buttonGradientGrades.setColor1(new java.awt.Color(9, 54, 55));
        buttonGradientGrades.setFont(new java.awt.Font("Segoe UI", 1, 12));
        buttonGradientGrades.setRadius(30);
        buttonGradientGrades.addActionListener(this::buttonSecretReportActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel2.add(buttonGradientGrades, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1; gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 6, 0, 0);
        jPanel2.add(jButton1, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {{null, null, null, null, null, null}},
            new String [] {"حالة الطالب", "رقم الجلوس ", "رقم التسجيل", "المهنه", "الاسم", "م"}
        ));
        jScrollPane1.setViewportView(jTable1);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE));
        pack();
    }// </editor-fold>

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter.getSelectedItem() != null && cmdcenter1.getSelectedItem() != null) {
            String center = cmdcenter.getSelectedItem().toString();
            String region = cmdcenter1.getSelectedItem().toString();
            if (center.equals("اختر المركز...") || region.equals("اختر المنطقة...")) {
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadStudents(center, region);
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) { jTable1.selectAll(); }

    private void cmdcenter1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter1.getSelectedItem() != null) {
            String region = cmdcenter1.getSelectedItem().toString();
            if (region.equals("اختر المنطقة...")) {
                cmdcenter.removeAllItems();
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadCenters(region);
        }
    }

    private void buttonSecretReportActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalSelected = selectedRows.length;
        String[] filters = filterPanel.getSelectedMonths();
        if (filters == null || filters.length < 6) return;
        final String selMonth = filters[4];
        final String admMonth = filters[5];

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";

        ReportWorker worker = new ReportWorker(this, "كشف المؤجلين بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> studentsByRegion = new java.util.LinkedHashMap<>();
                java.util.List<String> selectedSeatNos = new java.util.ArrayList<>();
                for (int row : selectedRows) selectedSeatNos.add(String.valueOf(model1.getValueAt(row, 1)));

                try (Connection con = DatabaseConnection.getConnection()) {
                    java.util.Map<String, com.pvtd.students.models.Student> studentMap = new java.util.HashMap<>();
                    java.util.Map<String, String> studentRegions = new java.util.HashMap<>();
                    java.util.List<Integer> studentIds = new java.util.ArrayList<>();
                    
                    int batchSize = 500;
                    for (int i = 0; i < selectedSeatNos.size(); i += batchSize) {
                        java.util.List<String> batch = selectedSeatNos.subList(i, Math.min(i + batchSize, selectedSeatNos.size()));
                        String inClause = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
                        String sql = "SELECT id, name, registration_no, seat_no, status, national_id, profession, professional_group, secret_no, coordination_no, region, exam_system FROM students WHERE seat_no IN (" + inClause + ")";
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            for (int j = 0; j < batch.size(); j++) ps.setString(j + 1, batch.get(j));
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    com.pvtd.students.models.Student st = new com.pvtd.students.models.Student();
                                    st.setId(rs.getInt("id")); st.setName(rs.getString("name"));
                                    st.setRegistrationNo(rs.getString("registration_no")); st.setSeatNo(rs.getString("seat_no"));
                                    st.setStatus(rs.getString("status")); st.setNationalId(rs.getString("national_id"));
                                    st.setProfessionalGroup(rs.getString("professional_group")); st.setCoordinationNo(rs.getString("coordination_no"));
                                    st.setExamSystem(rs.getString("exam_system"));
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

                    java.util.Map<Integer, java.util.Map<Integer, Integer>> gradesMap = new java.util.HashMap<>();
                    if (!studentIds.isEmpty()) {
                        for (int i = 0; i < studentIds.size(); i += batchSize) {
                            java.util.List<Integer> batch = studentIds.subList(i, Math.min(i + batchSize, studentIds.size()));
                            String inClause = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
                            try (PreparedStatement psG = con.prepareStatement("SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN (" + inClause + ")")) {
                                for (int j = 0; j < batch.size(); j++) psG.setInt(j + 1, batch.get(j));
                                try (ResultSet rsG = psG.executeQuery()) {
                                    while (rsG.next()) {
                                        gradesMap.computeIfAbsent(rsG.getInt("student_id"), k -> new java.util.HashMap<>()).put(rsG.getInt("subject_id"), rsG.getInt("obtained_mark"));
                                    }
                                }
                            }
                        }
                    }

                    for (String seatNo : selectedSeatNos) {
                        com.pvtd.students.models.Student st = studentMap.get(seatNo);
                        if (st != null) {
                            st.setGrades(gradesMap.getOrDefault(st.getId(), new java.util.HashMap<>()));
                            String reg = studentRegions.get(seatNo);
                            studentsByRegion.computeIfAbsent(reg, k -> new java.util.ArrayList<>()).add(st);
                        }
                    }

                    generatePdfFiles(studentsByRegion, centerName, selMonth, admMonth, "التقارير/تبييضة/مؤجلين");
                }
                java.awt.Desktop.getDesktop().open(new java.io.File("التقارير/تبييضة/مؤجلين"));
                return null;
            }

            private void generatePdfFiles(java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> data, 
                                        String center, String selM, String admM, String folderPath) throws Exception {
                java.io.File folder = new java.io.File(folderPath);
                if (!folder.exists()) folder.mkdirs();

                String combinedFn = folderPath + "/تبييض الطلاب المختارين.pdf";
                Document combinedDoc = new Document();
                PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
                combinedDoc.open();

                for (java.util.Map.Entry<String, java.util.List<com.pvtd.students.models.Student>> entry : data.entrySet()) {
                    String region = entry.getKey();
                    String sanitized = region.replace("/", "_").replace("\\", "_").replace(":", "_");
                    String fn = folderPath + "/" + sanitized + ".pdf";

                    Document doc = new Document();
                    PdfWriter.getInstance(doc, new java.io.FileOutputStream(fn));
                    doc.open();

                    gradReportSequential report = new gradReportSequential("تلاميذ مؤجلون", new java.awt.Color(200, 150, 0), center, region, entry.getValue(), selM, admM);
                    report.appendToDocument(doc);
                    report.appendToDocument(combinedDoc);

                    doc.close();
                }
                combinedDoc.close();
            }
        };
        worker.start();
    }

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";
        String[] months = filterPanel.getSelectedMonths();
        if (months == null || months.length < 6) return;
        delayed report = new delayed(months[4], months[5]);
        if (report.isCancelled) return;

        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        ReportWorker worker = new ReportWorker(this, "كشف طلاب مؤجلين", null) {
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
                        java.util.Vector rowData = (java.util.Vector) model1.getDataVector().get(selectedRows[i]);
                        String prof = String.valueOf(model1.getValueAt(selectedRows[i], 2)).trim();
                        if (prof.startsWith("<html>")) prof = prof.replaceAll("<[^>]*>", "");
                        String systemName = profToSystem.getOrDefault(prof, "نظامي");
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

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) { logger.log(java.util.logging.Level.SEVERE, null, ex); }
        java.awt.EventQueue.invokeLater(() -> new delayedFramePage().setVisible(true));
    }

    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradientGrades;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
}
