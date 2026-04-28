package com.pvtd.students.ui.pages.Report;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.ReportWorker;
import java.io.File;

/**
 *
 * @author Seif
 */
public class ScoundRoundFramePage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ScoundRoundFramePage.class.getName());

    /**
     * Creates new form ScoundRoundFramePage
     */
    public ScoundRoundFramePage() {
        initComponents();
        setTitle("تقرير دور ثاني");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable1.setShowGrid(true);
        jTable1.setGridColor(new Color(220, 220, 220));
        jTable1.setRowHeight(32);
        jTable1.setFont(new Font("Tahoma", Font.PLAIN, 14));
        jTable1.setFillsViewportHeight(true);
        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
                c.setFont(new Font("Tahoma", Font.BOLD, 14));
                ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
                return c;
            }
        });
        jTable1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                c.setBackground(new Color(180, 100, 0)); c.setForeground(Color.WHITE);
                c.setFont(new Font("Tahoma", Font.BOLD, 15));
                ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
                return c;
            }
        });
        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));

        jPanel2.setBackground(new Color(180, 100, 0));
        jLabel1.setFont(new Font("Tahoma", Font.BOLD, 20)); 
        jLabel1.setForeground(Color.WHITE);
        
        buttonGradient1.setText("📄  كشف بدون درجات");
        
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 210, 230), 1));

        loadRegions();
    }
    
    
    public void loadRegions() {
        cmdcenter1.removeAllItems();
        cmdcenter1.addItem("اختر...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            cmdcenter1.addItem(r);
        }
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("اختر...");
        java.util.Map<String, String> centers = com.pvtd.students.services.StudentService.getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) {
            cmdcenter.addItem(c);
        }
    }
    
//-------------------------------------------------------
public void loadStudents(String center, String region) {

    try {

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try (Connection con = DatabaseConnection.getConnection()) {

        String sql =
        "SELECT name, profession, registration_no, seat_no, status " +
        "FROM students " +
        "WHERE center_name = ? " +
        "AND region = ? " +
        "AND status = 'دور ثاني' " +
        "ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";
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

    } } catch (Exception e) {
        e.printStackTrace();
    }
}
 //---------------------------------------------------------------

    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        jPanel2.setBackground(new java.awt.Color(34, 84, 48));
        jPanel2.setPreferredSize(new java.awt.Dimension(1200, 140));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); 
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف التلاميذ الدور ثاني");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        cmdcenter1.setLabeText("المنطقة");
        cmdcenter1.addActionListener(this::cmdcenter1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel2.add(cmdcenter1, gridBagConstraints);

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel2.add(cmdcenter, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jButton1.setForeground(new java.awt.Color(34, 84, 48));
        jButton1.setText("اختيار الكل");
        jButton1.setPreferredSize(new java.awt.Dimension(120, 40));
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 10);
        jPanel2.add(jButton1, gridBagConstraints);

        buttonGradient1.setText("كشف الطلاب بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(255, 255, 255));
        buttonGradient1.setColor2(new java.awt.Color(200, 200, 200));
        buttonGradient1.setForeground(new java.awt.Color(0, 51, 51));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 13));
        buttonGradient1.setRadius(40);
        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel2.add(buttonGradient1, gridBagConstraints);
        
        buttonGradientGrades.setText("كشف دور ثاني بالدرجات");
        buttonGradientGrades.setColor1(new java.awt.Color(255, 255, 255));
        buttonGradientGrades.setColor2(new java.awt.Color(200, 200, 200));
        buttonGradientGrades.setForeground(new java.awt.Color(0, 51, 51));
        buttonGradientGrades.setFont(new java.awt.Font("Segoe UI", 1, 13));
        buttonGradientGrades.setRadius(40);
        buttonGradientGrades.addActionListener(this::buttonSecretReportActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel2.add(buttonGradientGrades, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "حالة الطالب", "رقم الجلوس ", "رقم التسجيل", "المهنه", "الاسم", "م"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdcenterActionPerformed
        if (cmdcenter.getSelectedItem() != null && cmdcenter1.getSelectedItem() != null) {
            String center = cmdcenter.getSelectedItem().toString();
            String region = cmdcenter1.getSelectedItem().toString();
            if (center.equals("اختر...") || region.equals("اختر...")) {
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                model.setRowCount(0);
                return;
            }
            loadStudents(center, region);
        }
    }//GEN-LAST:event_cmdcenterActionPerformed

    private void cmdcenter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdcenter1ActionPerformed
        if (cmdcenter1.getSelectedItem() != null) {
            String region = cmdcenter1.getSelectedItem().toString();
            if (region.equals("اختر...")) {
                cmdcenter.removeAllItems();
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                model.setRowCount(0);
                return;
            }
            loadCenters(region);
        }
    }//GEN-LAST:event_cmdcenter1ActionPerformed

    private void buttonSecretReportActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.LinkedHashMap<String, java.util.List<String>> byProfession = new java.util.LinkedHashMap<>();
        int totalSelected = selectedRows.length;
        for (int row : selectedRows) {
            String seatNo = String.valueOf(model1.getValueAt(row, 1)); 
            String prof   = String.valueOf(model1.getValueAt(row, 3)); 
            byProfession.computeIfAbsent(prof, k -> new java.util.ArrayList<>()).add(seatNo);
        }

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";
        String getProfSystemSql = "SELECT exam_system FROM professions WHERE TRIM(name) = TRIM(?)";

        ReportWorker worker = new ReportWorker(this, "كشف الدور الثاني بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                // Group all selected students by region first
                java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, java.util.List<String>>> byRegion = new java.util.LinkedHashMap<>();

                try (Connection con = DatabaseConnection.getConnection()) {
                    PreparedStatement getRegionPs = con.prepareStatement("SELECT region FROM students WHERE seat_no = ?");

                    for (java.util.Map.Entry<String, java.util.List<String>> entry : byProfession.entrySet()) {
                        String prof = entry.getKey();
                        for (String seatNo : entry.getValue()) {
                            String region = regionName;
                            getRegionPs.setString(1, seatNo);
                            try (ResultSet rsR = getRegionPs.executeQuery()) {
                                if (rsR.next() && rsR.getString("region") != null && !rsR.getString("region").isEmpty()) {
                                    region = rsR.getString("region");
                                }
                            }
                            byRegion
                                .computeIfAbsent(region, k -> new java.util.LinkedHashMap<>())
                                .computeIfAbsent(prof, k -> new java.util.ArrayList<>())
                                .add(seatNo);
                        }
                    }

                    java.io.File folder = new java.io.File("التقارير/تبييضة/دور ثاني");
                    if (!folder.exists()) folder.mkdirs();

                    for (java.util.Map.Entry<String, java.util.LinkedHashMap<String, java.util.List<String>>> regionEntry : byRegion.entrySet()) {
                        String currentRegion = regionEntry.getKey();
                        java.util.LinkedHashMap<String, java.util.List<String>> profMap = regionEntry.getValue();

                        String sanitizedRegion = currentRegion.replace("/", "_").replace("\\", "_").replace(":", "_");
                        String fn = "التقارير/تبييضة/دور ثاني/" + sanitizedRegion + ".pdf";

                        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fn));
                        document.open();

                        String getStudentSql = "SELECT id, name, registration_no, seat_no, status, national_id, professional_group, secret_no, coordination_no FROM students WHERE seat_no = ?";
                        PreparedStatement getStudentPs = con.prepareStatement(getStudentSql);
                        PreparedStatement localGetProfSystemPs = con.prepareStatement(getProfSystemSql);

                        String getGradesSql = "SELECT subject_id, obtained_mark FROM student_grades WHERE student_id = ?";
                        PreparedStatement getGradesPs = con.prepareStatement(getGradesSql);

                        int processed = 0;
                        for (java.util.Map.Entry<String, java.util.List<String>> entry : profMap.entrySet()) {
                            String prof = entry.getKey();
                            java.util.List<com.pvtd.students.models.Student> list = new java.util.ArrayList<>();

                            String systemName = "نظامي";
                            localGetProfSystemPs.setString(1, prof);
                            try (ResultSet rsSys = localGetProfSystemPs.executeQuery()) {
                                if (rsSys.next() && rsSys.getString(1) != null) systemName = rsSys.getString(1);
                            }

                            for (String seatNo : entry.getValue()) {
                                processed++;
                                updateStatus(processed, totalSelected, "جاري جلب بيانات الطالب: " + seatNo);

                                getStudentPs.setString(1, seatNo);
                                try (ResultSet rsStudent = getStudentPs.executeQuery()) {
                                    if (rsStudent.next()) {
                                        com.pvtd.students.models.Student st = new com.pvtd.students.models.Student();
                                        st.setId(rsStudent.getInt("id"));
                                        st.setName(rsStudent.getString("name"));
                                        st.setRegistrationNo(rsStudent.getString("registration_no"));
                                        st.setSeatNo(rsStudent.getString("seat_no"));
                                        st.setStatus(rsStudent.getString("status"));
                                        st.setNationalId(rsStudent.getString("national_id"));
                                        st.setProfessionalGroup(rsStudent.getString("professional_group"));
                                        st.setCoordinationNo(rsStudent.getString("coordination_no"));
                                        st.setSecretNo(rsStudent.getString("secret_no"));
                                        st.setProfession(prof);

                                        java.util.Map<Integer, Integer> grades = new java.util.HashMap<>();
                                        getGradesPs.setInt(1, st.getId());
                                        try (ResultSet rsGrades = getGradesPs.executeQuery()) {
                                            while (rsGrades.next()) {
                                                grades.put(rsGrades.getInt("subject_id"), rsGrades.getInt("obtained_mark"));
                                            }
                                        }
                                        st.setGrades(grades);
                                        list.add(st);
                                    }
                                }
                            }

                            updateStatus(processed, totalSelected, "جاري توليد تقرير مهنة: " + prof);
                            gradReportGeneric report = new gradReportGeneric(prof, centerName, currentRegion, systemName, list, "تلاميذ دور ثاني", new java.awt.Color(180, 100, 0), "Detailed_SecondRound_Report");
                            report.appendToDocument(document);
                        }
                        document.close();
                    }
                }

                java.io.File folder = new java.io.File("التقارير/تبييضة/دور ثاني");
                java.awt.Desktop.getDesktop().open(folder);
                return null;
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

        SecondRound report = new SecondRound();
        if (report.isCancelled) return;

        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();

        ReportWorker worker = new ReportWorker(this, "كشف طلاب الدور الثاني", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(10, 100, "جاري جلب أنظمة المهن...");

                java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem = new java.util.LinkedHashMap<>();
                java.util.Map<String, String> profToSystem = new java.util.HashMap<>();

                try (java.sql.Connection con = com.pvtd.students.db.DatabaseConnection.getConnection()) {
                    String sql = "SELECT name, exam_system FROM professions";
                    try (java.sql.PreparedStatement ps = con.prepareStatement(sql);
                         java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String pName = rs.getString("name");
                            String pSys = rs.getString("exam_system");
                            if (pName != null) {
                                profToSystem.put(pName.trim(), pSys != null ? pSys : "نظامي");
                            }
                        }
                    }

                    for (int i = 0; i < selectedRows.length; i++) {
                        java.util.Vector rowData = (java.util.Vector) model1.getDataVector().get(selectedRows[i]);
                        // In ScoundRoundFramePage, profession is index 3
                        String prof = String.valueOf(model1.getValueAt(selectedRows[i], 3)).trim();
                        if (prof.startsWith("<html>")) {
                            prof = prof.replaceAll("<[^>]*>", "");
                        }

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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ScoundRoundFramePage().setVisible(true));
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        jTable1.selectAll();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradientGrades;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
