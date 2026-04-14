/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.pvtd.students.ui.utils.ReportWorker;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Seif
 */
public class delayedFramePage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(delayedFramePage.class.getName());

    
    public delayedFramePage() {
        initComponents();
        loadRegions();
        setTitle("تقرير المؤجلين");
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
//            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
//                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
//                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 248, 255));
//                c.setFont(new Font("Tahoma", Font.PLAIN, 14));
//                ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
//                return c;
//            }
//        });
//        jTable1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
//            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
//                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
//                c.setBackground(new Color(50, 100, 50)); c.setForeground(Color.WHITE);
//                c.setFont(new Font("Tahoma", Font.BOLD, 15));
//                ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
//                return c;
//            }
//        });
//        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));
//
//        jPanel2.setBackground(new Color(50, 100, 50));
//        javax.swing.JLabel titleLbl = new javax.swing.JLabel("  تقرير المؤجلين", javax.swing.SwingConstants.RIGHT);
//        titleLbl.setFont(new Font("Tahoma", Font.BOLD, 20)); titleLbl.setForeground(Color.WHITE);
//        java.awt.GridBagConstraints gbcT = new java.awt.GridBagConstraints();
//        gbcT.gridx = 2; gbcT.gridy = 0; gbcT.weightx = 1.0;
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
//        UITheme.styleButton(btnClose, new Color(220,38,38), new Color(180,20,20), new Color(140,10,10));
//        java.awt.GridBagConstraints gbcC = new java.awt.GridBagConstraints();
//        gbcC.gridx = 0; gbcC.gridy = 0; gbcC.anchor = java.awt.GridBagConstraints.WEST;
//        gbcC.insets = new java.awt.Insets(8, 12, 8, 12);
//        jPanel2.add(btnClose, gbcC);
//
//        jPanel3.setBackground(new Color(245, 247, 250));
//        buttonGradient3.setText("📄  إنشاء تقرير PDF للمؤجلين");
//        jButton1.setText("✔  تحديد الكل"); jButton1.setFont(new Font("Tahoma", Font.BOLD, 13));
//        UITheme.styleButton(jButton1, new Color(37,99,235), new Color(29,78,216), new Color(23,64,180));
//        jButton1.setForeground(Color.WHITE);
//        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200,210,230), 1));

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

        String sql =
        "SELECT name, profession, registration_no, seat_no, status " +
        "FROM students " +
        "WHERE center_name = ? " +
                        "AND region = ? " +
        "AND status LIKE '%مؤجل%' " ;

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, center);
                ps.setString(2, region);

        ResultSet rs = ps.executeQuery();

        int i = 1;

        while (rs.next()) {

            model.addRow(new Object[]{
                i++,
                rs.getString("name"),
                rs.getString("profession"),
                rs.getString("registration_no"),
                rs.getString("seat_no"),
                rs.getString("status")
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
        jButton1 = new javax.swing.JButton();
        gridBagConstraints = new java.awt.GridBagConstraints();
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 134;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 24, 0, 0);
        jPanel2.add(cmdcenter, gridBagConstraints);

        cmdcenter1.setLabeText("المنطقة");
        cmdcenter1.addActionListener(this::cmdcenter1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 134;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 18, 0, 0);
        jPanel2.add(cmdcenter1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف التلاميذ مؤجلون");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 56, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);



        buttonGradient1.setText("كشف الطلاب مؤجلون بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(9, 54, 55));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        buttonGradient1.setRadius(30);
        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        jPanel2.add(buttonGradient1, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 6, 0, 0);
        jPanel2.add(jButton1, gridBagConstraints);

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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
    }//GEN-LAST:event_cmdcenterActionPerformed



    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        jTable1.selectAll();
    }//GEN-LAST:event_jButton1ActionPerformed

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
    }//GEN-LAST:event_cmdcenter1ActionPerformed

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        delayed report = new delayed();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        DefaultTableModel model2 = (DefaultTableModel) report.jTable2.getModel();
        model2.setRowCount(0);

        for (int i = 0; i < selectedRows.length; i++) {
            Object[] row = new Object[model1.getColumnCount()];
            for (int j = 0; j < model1.getColumnCount(); j++) {
                row[j] = model1.getValueAt(selectedRows[i], j);
            }
            model2.addRow(row);
        }

        if (cmdcenter.getSelectedItem() != null) {
            String centerName = cmdcenter.getSelectedItem().toString();
            report.loadCenterData(centerName);
            report.cent.setText(centerName);
        }

        ReportWorker worker = new ReportWorker(this, "كشف طلاب مؤجلين", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(50, 100, "جاري إنشاء ملف PDF...");
                report.createPDF();
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
        java.awt.EventQueue.invokeLater(() -> new delayedFramePage().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
