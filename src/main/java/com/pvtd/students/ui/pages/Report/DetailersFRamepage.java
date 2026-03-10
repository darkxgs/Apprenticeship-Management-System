
package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


public class DetailersFRamepage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DetailersFRamepage.class.getName());

  
    
    public DetailersFRamepage() {
        initComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
          jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
         jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         
         jTable1.setShowGrid(true);
jTable1.setGridColor(Color.GRAY);

jTable1.setRowHeight(30); // ارتفاع الصف

jTable1.setShowHorizontalLines(true);
jTable1.setShowVerticalLines(true);
         
         
         
                 JTableHeader header = jTable1.getTableHeader();

header.setDefaultRenderer(new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        c.setBackground(Color.decode("#CCFFFF")); // لون الخلفية
        c.setForeground(Color.BLACK);            // لون النص
        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
        c.setFont(new Font("Tahoma", Font.BOLD, 14));
c.setBackground(new Color(200, 230, 220));
c.setForeground(Color.BLACK);
        return c;
    }
});
        loadCenters();
    }
    public void loadCenters() {

    try {

        Connection con = DatabaseConnection.getConnection();

        String sql = "SELECT DISTINCT center_name FROM students";

        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        cmdcenter.removeAllItems();

        while (rs.next()) {

            cmdcenter.addItem(rs.getString("center_name"));

        }

    } catch (Exception e) {

        e.printStackTrace();

    }

}
    
    
    
     //-------------------------------------------------------
public void loadStudents(String center, int count) {

    try {

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        Connection con = DatabaseConnection.getConnection();

        String sql =
        "SELECT name, profession, registration_no, seat_no, status " +
        "FROM students " +
        "WHERE center_name = ? " +
        "AND status = 'مفصول' " +
        "AND ROWNUM <= ?";

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, center);
        ps.setInt(2, count);

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

    } catch (Exception e) {
        e.printStackTrace();
    }
}
 //---------------------------------------------------------------
public void loadCount(String center) {

    try {

        cmdcount.removeAllItems();

        Connection con = DatabaseConnection.getConnection();

        String sql = "SELECT COUNT(*) FROM students WHERE status='مفصول' AND center_name=?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, center);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            int total = rs.getInt(1);

            for (int i = 1; i <= total; i++) {
                cmdcount.addItem(String.valueOf(i));
            }

        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcount = new com.pvtd.students.ui.components.Combobox();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        jPanel3 = new javax.swing.JPanel();
        buttonGradient3 = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        cmdcount.setLabeText("عدد الطلاب");
        cmdcount.addActionListener(this::cmdcountActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 139;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(28, 224, 30, 0);
        jPanel2.add(cmdcount, gridBagConstraints);

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 134;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(28, 98, 30, 287);
        jPanel2.add(cmdcenter, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        buttonGradient3.setForeground(new java.awt.Color(0, 0, 0));
        buttonGradient3.setText("رفع تقرير بي الطلاب الناجحين بدون الدرجات");
        buttonGradient3.setColor1(new java.awt.Color(224, 234, 252));
        buttonGradient3.setColor2(new java.awt.Color(207, 222, 243));
        buttonGradient3.setRadius(40);
        buttonGradient3.addActionListener(this::buttonGradient3ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 225, 23, 0);
        jPanel3.add(buttonGradient3, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(28, 172, 0, 232);
        jPanel3.add(jButton1, gridBagConstraints);

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "م", "الاسم", "المهنه", "رقم التسجيل", "رقم الجلوس ", "حالة الطالب"
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonGradient3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGradient3ActionPerformed

               Detailers report = new Detailers();

    DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
    DefaultTableModel model2 = (DefaultTableModel) report.jTable2.getModel();

    model2.setRowCount(0);

    int[] selectedRows = jTable1.getSelectedRows();

    for (int i = 0; i < selectedRows.length; i++) {

        Object[] row = new Object[model1.getColumnCount()];

        for (int j = 0; j < model1.getColumnCount(); j++) {

            row[j] = model1.getValueAt(selectedRows[i], j);

        }

        model2.addRow(row);
    }

    report.createPDF();
    }                                               
    {
        
        
    }//GEN-LAST:event_buttonGradient3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        jTable1.selectAll();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdcenterActionPerformed

        if (cmdcenter.getSelectedItem() != null) {

        String center = cmdcenter.getSelectedItem().toString();

        loadCount(center);

    }
        
    }//GEN-LAST:event_cmdcenterActionPerformed

    private void cmdcountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdcountActionPerformed
   
         if (cmdcenter.getSelectedItem() != null && cmdcount.getSelectedItem() != null) {

        String center = cmdcenter.getSelectedItem().toString();
        int count = Integer.parseInt(cmdcount.getSelectedItem().toString());

        loadStudents(center, count);

    }
    
        
    }//GEN-LAST:event_cmdcountActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new DetailersFRamepage().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient3;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcount;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
