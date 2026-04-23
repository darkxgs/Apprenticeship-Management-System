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
import com.pvtd.students.models.Student;
import java.io.File;
import java.io.FileOutputStream;

public class SucssfullPageEdit extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SucssfullPageEdit.class.getName());

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
            String prof   = String.valueOf(model1.getValueAt(row, 5)); 
            byProfession.computeIfAbsent(prof, k -> new java.util.ArrayList<>()).add(seatNo);
        }

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = combobox1.getSelectedItem() != null ? combobox1.getSelectedItem().toString() : "";
        String systemName = "نظامي";

        ReportWorker worker = new ReportWorker(this, "كشف الناجحين بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                String fn = "Detailed_Success_Report_All.pdf";
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fn));
                document.open();

                try (Connection con = DatabaseConnection.getConnection()) {
                    String getStudentSql = "SELECT id, name, registration_no, seat_no, status, national_id, professional_group, secret_no, coordination_no FROM students WHERE seat_no = ?";
                    PreparedStatement getStudentPs = con.prepareStatement(getStudentSql);

                    String getGradesSql = "SELECT subject_id, obtained_mark FROM student_grades WHERE student_id = ?";
                    PreparedStatement getGradesPs = con.prepareStatement(getGradesSql);

                    int processed = 0;
                    for (java.util.Map.Entry<String, java.util.List<String>> entry : byProfession.entrySet()) {
                        String prof = entry.getKey();
                        java.util.List<Student> list = new java.util.ArrayList<>();

                        for (String seatNo : entry.getValue()) {
                            processed++;
                            updateStatus(processed, totalSelected, "جاري جلب بيانات الطالب: " + seatNo);

                            getStudentPs.setString(1, seatNo);
                            try (ResultSet rsStudent = getStudentPs.executeQuery()) {
                                if (rsStudent.next()) {
                                    Student st = new Student();
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
                        gradReportSucc report = new gradReportSucc(prof, centerName, regionName, systemName, list);
                        report.appendToDocument(document);
                    }
                }

                document.close();
                java.awt.Desktop.getDesktop().open(new java.io.File(fn));
                return null;
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
//
//        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//        jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        jTable1.setShowGrid(true);
//        jTable1.setGridColor(new Color(220, 220, 220));
//        jTable1.setRowHeight(32);
//        jTable1.setFont(new Font("Tahoma", Font.PLAIN, 14));
//        jTable1.setFillsViewportHeight(true);
//        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
//                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
//                if (!sel) {
//                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 255, 245));
//                }
//                c.setFont(new Font("Tahoma", Font.PLAIN, 14));
//                ((DefaultTableCellRenderer) c).setHorizontalAlignment(CENTER);
//                return c;
//            }
//        });
//        jTable1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
//                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
//                c.setBackground(new Color(22, 100, 52));
//                c.setForeground(Color.WHITE);
//                c.setFont(new Font("Tahoma", Font.BOLD, 15));
//                ((DefaultTableCellRenderer) c).setHorizontalAlignment(CENTER);
//                return c;
//            }
//        });
//        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));
//
//        jPanel2.setBackground(new Color(22, 100, 52));
//        javax.swing.JLabel titleLbl = new javax.swing.JLabel("  تقرير الناجحين", javax.swing.SwingConstants.RIGHT);
//        titleLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
//        titleLbl.setForeground(Color.WHITE);
//        java.awt.GridBagConstraints gbcT = new java.awt.GridBagConstraints();
//        gbcT.gridx = 2;
//        gbcT.gridy = 0;
//        gbcT.weightx = 1.0;
//        gbcT.anchor = java.awt.GridBagConstraints.EAST;
//        gbcT.insets = new java.awt.Insets(10, 30, 10, 20);
//        jPanel2.add(titleLbl, gbcT);
//
//        javax.swing.JButton btnClose = new javax.swing.JButton("✖  إغلاق");
//        btnClose.setForeground(Color.WHITE);
//        btnClose.setFont(new Font("Tahoma", Font.BOLD, 14));
//        btnClose.setFocusPainted(false);
//        btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
//        btnClose.addActionListener(e -> this.dispose());
//        UITheme.styleButton(btnClose, new Color(220, 38, 38), new Color(180, 20, 20), new Color(140, 10, 10));
//        java.awt.GridBagConstraints gbcC = new java.awt.GridBagConstraints();
//        gbcC.gridx = 0;
//        gbcC.gridy = 0;
//        gbcC.anchor = java.awt.GridBagConstraints.WEST;
//        gbcC.insets = new java.awt.Insets(8, 12, 8, 12);
//        jPanel2.add(btnClose, gbcC);
//
//        jPanel3.setBackground(new Color(245, 247, 250));
//        buttonGradient2.setText("📊  تقرير PDF بالدرجات");
//        buttonGradient3.setText("📄  تقرير PDF بدون الدرجات");
//        jButton1.setText("✔  تحديد الكل");
//        jButton1.setFont(new Font("Tahoma", Font.BOLD, 13));
//        UITheme.styleButton(jButton1, new Color(37, 99, 235), new Color(29, 78, 216), new Color(23, 64, 180));
//        jButton1.setForeground(Color.WHITE);
//        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 210, 230), 1));

        loadRegions();
        setupTableUi();
    }

    
    private void setupTableUi() {
        if (jTable1 == null) return;
        jTable1.setRowHeight(35);
        jTable1.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
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

    //-------------------------------------------------------

    public void loadStudents(String center, String region) {

    try {

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try (Connection con = DatabaseConnection.getConnection()) {

            String sql = "SELECT name, profession, registration_no, seat_no, status, national_id, coordination_no "
                    + "FROM students "
                    + "WHERE center_name = ? "
                    + "AND region = ? "
                    + "AND status = 'ناجح' "
                    + "ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, center);
            ps.setString(2, region);

            ResultSet rs = ps.executeQuery();

            int i = 1;

            while (rs.next()) {

                model.addRow(new Object[]{
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

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    //---------------------------------------------------------------

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
        gridBagConstraints = new java.awt.GridBagConstraints();
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.ipadx = 162;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 12, 19, 0);
        jPanel2.add(cmdcenter, gridBagConstraints);

        combobox1.setLabeText("المنطقة");
        combobox1.addActionListener(this::combobox1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.ipadx = 167;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 12, 19, 0);
        jPanel2.add(combobox1, gridBagConstraints);

        buttonGradient3.setText("كشف الناجحين");
        buttonGradient3.setColor1(new java.awt.Color(68, 160, 141));
        buttonGradient3.setColor2(new java.awt.Color(9, 54, 55));
        buttonGradient3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        buttonGradient3.setRadius(40);
        buttonGradient3.addActionListener(this::buttonGradient3ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 6, 0, 0);
        jPanel2.add(buttonGradient3, gridBagConstraints);

        buttonGradient1.setText("كشف الناجحين بي الدرجات");
        buttonGradient1.setColor1(new java.awt.Color(35, 122, 87));
        buttonGradient1.setColor2(new java.awt.Color(9, 48, 40));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        buttonGradient1.setRadius(40);
        buttonGradient1.addActionListener(this::buttonSecretReportActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 18, 0, 0);
        jPanel2.add(buttonGradient1, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(26, 6, 0, 0);
        jPanel2.add(jButton1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف تقارير بي الطلاب الناجحين");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 19);
        jPanel2.add(jLabel1, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);


        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "حالة التلميذ", "رقم الجلوس ", "كود التنسيق", "الرقم القومي", "رقم التسجيل", "المهنة", "الاسم", "م"
            }
        ));
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
    }//GEN-LAST:event_cmdcenterActionPerformed



    private void buttonGradient3ActionPerformed(java.awt.event.ActionEvent evt) {                                                
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        successful report = new successful();
        if (report.isCancelled) return;
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        DefaultTableModel model2 = (DefaultTableModel) report.jTable2.getModel();
        model2.setRowCount(0);

        // SucssfullPageEdit columns (8): 0:حالة التلميذ, 1:رقم الجلوس, 2:كود التنسيق, 3:الرقم القومي, 4:رقم التسجيل, 5:المهنة, 6:الاسم, 7:م
        // successful.java columns (6):   0:حاله التلميذ, 1:رقم الجلوس, 2:رقم التسجيل, 3:المهنة, 4:الاسم, 5:م
        for (int i = 0; i < selectedRows.length; i++) {
            int r = selectedRows[i];
            String name = String.valueOf(model1.getValueAt(r, 6) != null ? model1.getValueAt(r, 6) : "");
            String prof = String.valueOf(model1.getValueAt(r, 5) != null ? model1.getValueAt(r, 5) : "");
            String htmlName = "<html><center>" + name.trim() + "</center></html>";
            String htmlProf = "<html><center>" + prof.trim() + "</center></html>";

            Object[] row = new Object[]{
                model1.getValueAt(r, 0),  // 0: حالة التلميذ
                model1.getValueAt(r, 1),  // 1: رقم الجلوس
                model1.getValueAt(r, 4),  // 2: رقم التسجيل
                htmlProf,                 // 3: المهنة
                htmlName,                 // 4: الاسم
                i + 1                     // 5: م
            };
            model2.addRow(row);
        }

        if (cmdcenter.getSelectedItem() != null) {
            String centerName = cmdcenter.getSelectedItem().toString();
            report.loadCenterData(centerName);
            report.cent.setText(centerName);
        }
        
        ReportWorker worker = new ReportWorker(this, "كشف الطلاب الناجحين", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(50, 100, "جاري إنشاء ملف PDF...");
                report.createPDF();
                return null;
            }
        };
        worker.start();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        jTable1.selectAll();

    }//GEN-LAST:event_jButton1ActionPerformed

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
    }//GEN-LAST:event_combobox1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new SucssfullPageEdit().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    // End of variables declaration//GEN-END:variables
}
