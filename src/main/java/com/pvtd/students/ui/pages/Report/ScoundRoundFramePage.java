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
        try {
            try (Connection con = DatabaseConnection.getConnection()) {
                String sql = "SELECT DISTINCT region FROM students WHERE region IS NOT NULL";
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery();
                cmdcenter1.removeAllItems();
                cmdcenter1.addItem("اختر...");
                while (rs.next()) {
                    cmdcenter1.addItem(rs.getString("region"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCenters(String region) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT center_name FROM students WHERE region = ? AND center_name IS NOT NULL";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, region);
            ResultSet rs = ps.executeQuery();
            cmdcenter.removeAllItems();
            cmdcenter.addItem("اختر...");
            while (rs.next()) {
                cmdcenter.addItem(rs.getString("center_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        "AND status = 'دور ثاني' " ;
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

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        SecondRound report = new SecondRound();
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

        ReportWorker worker = new ReportWorker(this, "كشف طلاب الدور الثاني", null) {
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
        java.awt.EventQueue.invokeLater(() -> new ScoundRoundFramePage().setVisible(true));
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        jTable1.selectAll();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
