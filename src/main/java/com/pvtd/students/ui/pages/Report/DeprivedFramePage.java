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
public class DeprivedFramePage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DeprivedFramePage.class.getName());

    /**
     * Creates new form DeprivedFramePage
     */
    public DeprivedFramePage() {
        initComponents();
        loadRegions();
        setTitle("تقرير المحرومين");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

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
//                c.setBackground(new Color(30, 60, 114)); c.setForeground(Color.WHITE);
//                c.setFont(new Font("Tahoma", Font.BOLD, 15));
//                ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
//                return c;
//            }
//        });
//        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));
//
//        jPanel2.setBackground(new Color(30, 60, 114));
//        javax.swing.JLabel titleLbl = new javax.swing.JLabel("  تقرير المحرومين", javax.swing.SwingConstants.RIGHT);
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
//        buttonGradient3.setText("📄  إنشاء تقرير PDF للمحرومين");
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
        cn.removeAllItems();
        cn.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            cn.addItem(r);
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
        "AND status LIKE '%محروم%' " 
       ;

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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        cn = new com.pvtd.students.ui.components.Combobox();
        jLabel1 = new javax.swing.JLabel();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(0, 102, 102));
        jPanel2.setPreferredSize(new java.awt.Dimension(498, 100));

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);

        cn.setLabeText("المنطقة");
        cn.addActionListener(this::cnActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف التلاميذ المحرومين");

        buttonGradient1.setText("كشف الطلاب المحرومين بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(9, 54, 55));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        buttonGradient1.setRadius(30);
        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);


        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(109, 109, 109)
                .addComponent(cmdcenter, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(96, 96, 96)
                .addComponent(cn, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(19, 19, 19))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmdcenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonGradient1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1)
                            .addComponent(jLabel1))))
                .addContainerGap(10, Short.MAX_VALUE))
        );

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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter.getSelectedItem() != null && cn.getSelectedItem() != null) {
            String center = cmdcenter.getSelectedItem().toString();
            String region = cn.getSelectedItem().toString();
            if (center.equals("اختر المركز...") || region.equals("اختر المنطقة...")) {
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadStudents(center, region);
        }
    }//GEN-LAST:event_cmdcenterActionPerformed

    private void cnActionPerformed(java.awt.event.ActionEvent evt) {
        if (cn.getSelectedItem() != null) {
            String region = cn.getSelectedItem().toString();
            if (region.equals("اختر المنطقة...")) {
                cmdcenter.removeAllItems();
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadCenters(region);
        }
    }//GEN-LAST:event_cnActionPerformed

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        Deprived report = new Deprived();
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

        ReportWorker worker = new ReportWorker(this, "كشف طلاب محرومين (أساسي)", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(50, 100, "جاري إنشاء ملف PDF...");
                report.createPDF();
                return null;
            }
        };
        worker.start();
    }



    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        jTable1.selectAll();
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
        java.awt.EventQueue.invokeLater(() -> new DeprivedFramePage().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;

    private javax.swing.JButton jButton1;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
