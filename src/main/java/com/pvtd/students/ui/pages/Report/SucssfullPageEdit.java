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
import javax.swing.table.JTableHeader;

public class SucssfullPageEdit extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SucssfullPageEdit.class.getName());

    public SucssfullPageEdit() {
        initComponents();
        setTitle("تقرير الناجحين");
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
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 255, 245));
                }
                c.setFont(new Font("Tahoma", Font.PLAIN, 14));
                ((DefaultTableCellRenderer) c).setHorizontalAlignment(CENTER);
                return c;
            }
        });
        jTable1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                c.setBackground(new Color(22, 100, 52));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Tahoma", Font.BOLD, 15));
                ((DefaultTableCellRenderer) c).setHorizontalAlignment(CENTER);
                return c;
            }
        });
        jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));

        jPanel2.setBackground(new Color(22, 100, 52));
        javax.swing.JLabel titleLbl = new javax.swing.JLabel("  تقرير الناجحين", javax.swing.SwingConstants.RIGHT);
        titleLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);
        java.awt.GridBagConstraints gbcT = new java.awt.GridBagConstraints();
        gbcT.gridx = 2;
        gbcT.gridy = 0;
        gbcT.weightx = 1.0;
        gbcT.anchor = java.awt.GridBagConstraints.EAST;
        gbcT.insets = new java.awt.Insets(10, 30, 10, 20);
        jPanel2.add(titleLbl, gbcT);

        javax.swing.JButton btnClose = new javax.swing.JButton("✖  إغلاق");
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> this.dispose());
        UITheme.styleButton(btnClose, new Color(220, 38, 38), new Color(180, 20, 20), new Color(140, 10, 10));
        java.awt.GridBagConstraints gbcC = new java.awt.GridBagConstraints();
        gbcC.gridx = 0;
        gbcC.gridy = 0;
        gbcC.anchor = java.awt.GridBagConstraints.WEST;
        gbcC.insets = new java.awt.Insets(8, 12, 8, 12);
        jPanel2.add(btnClose, gbcC);

        jPanel3.setBackground(new Color(245, 247, 250));
        buttonGradient2.setText("📊  تقرير PDF بالدرجات");
        buttonGradient3.setText("📄  تقرير PDF بدون الدرجات");
        jButton1.setText("✔  تحديد الكل");
        jButton1.setFont(new Font("Tahoma", Font.BOLD, 13));
        UITheme.styleButton(jButton1, new Color(37, 99, 235), new Color(29, 78, 216), new Color(23, 64, 180));
        jButton1.setForeground(Color.WHITE);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 210, 230), 1));

        loadCenters();
    }

    public void loadCenters() {

        try {

            try (Connection con = DatabaseConnection.getConnection()) {

                String sql = "SELECT DISTINCT center_name FROM students";

                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery();

                cmdcenter.removeAllItems();

                while (rs.next()) {

                    cmdcenter.addItem(rs.getString("center_name"));

                }

            }
        } catch (Exception e) {

            e.printStackTrace();

        }

    }
    //-------------------------------------------------------

    public void loadStudents(String center) {

        try {

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            try (Connection con = DatabaseConnection.getConnection()) {

                String sql
                        = "SELECT name, profession, registration_no, seat_no, status "
                        + "FROM students "
                        + "WHERE center_name = ? "
                        + "AND status = 'ناجح'";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, center);
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
    //---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        jPanel3 = new javax.swing.JPanel();
        buttonGradient2 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradient3 = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 139;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 57, 15, 279);
        jPanel2.add(cmdcenter, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        buttonGradient2.setForeground(new java.awt.Color(0, 0, 0));
        buttonGradient2.setText("رفع تقرير بي الطلاب الناجحين بي الدرجات");
        buttonGradient2.setColor1(new java.awt.Color(224, 234, 252));
        buttonGradient2.setColor2(new java.awt.Color(207, 222, 243));
        buttonGradient2.setRadius(40);
        buttonGradient2.addActionListener(this::buttonGradient2ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 74, 15, 0);
        jPanel3.add(buttonGradient2, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(16, 167, 15, 0);
        jPanel3.add(buttonGradient3, gridBagConstraints);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 115, 0, 97);
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
                "حالة التلميذ", "رقم الجلوس ", "رقم التسجيل", "المهنة", "الاسم", "م"
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdcenterActionPerformed

        if (cmdcenter.getSelectedItem() != null) {

            String center = cmdcenter.getSelectedItem().toString();

            loadStudents(center);
        }


    }//GEN-LAST:event_cmdcenterActionPerformed

    private void buttonGradient2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGradient2ActionPerformed
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار الطلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();

        String profession = (String) model1.getValueAt(selectedRows[0], 2);
        for (int i = 1; i < selectedRows.length; i++) {
            String p = (String) model1.getValueAt(selectedRows[i], 2);
            if (!profession.equals(p)) {
                javax.swing.JOptionPane.showMessageDialog(this, "لا يمكن طباعة كشف بالدرجات لمهن مختلفة في نفس الوقت. برجاء اختيار طلاب من مهنة واحدة فقط.", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        java.util.List<String[]> subjects = new java.util.ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, max_mark, pass_mark FROM subjects WHERE profession = ? ORDER BY display_order ASC, id ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, profession);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                subjects.add(new String[]{
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("max_mark"),
                    rs.getString("pass_mark")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        successful report = new successful();
        DefaultTableModel model2 = (DefaultTableModel) report.jTable2.getModel();

        model2.setColumnCount(0);
        model2.addColumn("م");
        model2.addColumn("الاسم");
        model2.addColumn("المهنة");
        model2.addColumn("رقم التسجيل");
        model2.addColumn("رقم الجلوس");
        model2.addColumn("حالة التلميذ");

        for (String[] sub : subjects) {
            String htmlHeader = "<html><center>" + sub[1] + "<br>عظمى: " + sub[2] + " | صغرى: " + sub[3] + "</center></html>";
            model2.addColumn(htmlHeader);
        }
        model2.setRowCount(0);

        try (Connection con = DatabaseConnection.getConnection()) {
            String getStudentIdSql = "SELECT id FROM students WHERE seat_no = ?";
            PreparedStatement getStudentIdPs = con.prepareStatement(getStudentIdSql);

            String getGradeSql = "SELECT obtained_mark FROM student_grades WHERE student_id = ? AND subject_id = ?";
            PreparedStatement getGradePs = con.prepareStatement(getGradeSql);

            for (int i = 0; i < selectedRows.length; i++) {
                java.util.List<Object> rowData = new java.util.ArrayList<>();
                for (int col = 0; col < 6; col++) {
                    rowData.add(model1.getValueAt(selectedRows[i], col));
                }

                String seatNo = (String) model1.getValueAt(selectedRows[i], 4);
                int studentId = -1;
                getStudentIdPs.setString(1, seatNo);
                ResultSet rsStudent = getStudentIdPs.executeQuery();
                if (rsStudent.next()) {
                    studentId = rsStudent.getInt("id");
                }

                for (String[] sub : subjects) {
                    String mark = "-";
                    if (studentId != -1) {
                        getGradePs.setInt(1, studentId);
                        getGradePs.setInt(2, Integer.parseInt(sub[0]));
                        ResultSet rsGrade = getGradePs.executeQuery();
                        if (rsGrade.next()) {
                            mark = rsGrade.getString("obtained_mark");
                        }
                    }
                    rowData.add(mark);
                }
                model2.addRow(rowData.toArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        report.createPDF();
    }//GEN-LAST:event_buttonGradient2ActionPerformed

    private void buttonGradient3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGradient3ActionPerformed
        successful report = new successful();

    DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
    DefaultTableModel model2 = (DefaultTableModel) report.jTable2.getModel();

    model2.setRowCount(0);

    int[] selectedRows = jTable1.getSelectedRows();

    // 👇 لو مفيش اختيار → خد كل الجدول
    if (selectedRows.length == 0) {
        selectedRows = new int[model1.getRowCount()];
        for (int i = 0; i < model1.getRowCount(); i++) {
            selectedRows[i] = i;
        }
    }

    // 👇 كده دايماً هيجيب كل اللي انت عايزه
    for (int i = 0; i < selectedRows.length; i++) {

        Object[] row = new Object[model1.getColumnCount()];

        for (int j = 0; j < model1.getColumnCount(); j++) {
            row[j] = model1.getValueAt(selectedRows[i], j);
        }

        model2.addRow(row);
    }

    String centerName = cmdcenter.getSelectedItem().toString();
    report.loadStudents(centerName);
    report.loadCenterData(centerName);
    report.cent.setText(centerName);

    report.createPDF();
    }//GEN-LAST:event_buttonGradient3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        jTable1.selectAll();

    }//GEN-LAST:event_jButton1ActionPerformed

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
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient2;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient3;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
