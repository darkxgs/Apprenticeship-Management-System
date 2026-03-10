/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Seif
 */
public class SecondRound extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SecondRound.class.getName());

    /**
     * Creates new form SecondRound
     */
    public SecondRound() {
        initComponents();
        this.setSize(1200,800);
        
        jPanel1.setPreferredSize(new Dimension(1123, 794));
         jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                 JTableHeader header = jTable2.getTableHeader();

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
        
        return c;
    }
});
        
        
    }

public void createPDF() {

    try {

        String path = "C:\\Users\\Seif\\OneDrive\\Desktop\\report.pdf";

        this.validate();
        this.repaint();

        int width = jPanel1.getWidth();
        int height = jPanel1.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        jPanel1.printAll(g2);

        g2.dispose();

        Document document = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);

        PdfWriter.getInstance(document, new FileOutputStream(path));

        document.open();

        com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(image, null);

        float pageWidth = PageSize.A4.rotate().getWidth();
        float pageHeight = PageSize.A4.rotate().getHeight();

        float scaleX = pageWidth / width;
        float scaleY = pageHeight / height;

        float scale = Math.max(scaleX, scaleY);

        img.scaleAbsolute(width * scale, height * scale);
        img.setAbsolutePosition(0, 0);

        document.add(img);

        document.close();

        Desktop.getDesktop().open(new File(path));

    } catch (Exception e) {
        e.printStackTrace();
    }
}
   
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(30, 60, 114));
        jLabel2.setText("مصلحة الكفاية االنتاجية والتدريب المهنى");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(745, 20, 250, 20);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(30, 60, 114));
        jLabel3.setText("الرئاسة العامة المتحانات دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(725, 40, 290, 20);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(30, 60, 114));
        jLabel4.setText("لجنة النظام والمراقبة");
        jPanel1.add(jLabel4);
        jLabel4.setBounds(795, 60, 140, 20);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(30, 60, 114));
        jLabel1.setText("وزارة التجارة والصناعة");
        jPanel1.add(jLabel1);
        jLabel1.setBounds(805, 0, 140, 20);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(30, 60, 114));
        jLabel5.setText("نتائج أمتحان دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel5);
        jLabel5.setBounds(421, 86, 220, 20);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(42, 82, 152));
        jLabel6.setText("تلاميذ دور ثاني");
        jPanel1.add(jLabel6);
        jLabel6.setBounds(442, 122, 170, 60);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(30, 60, 114));
        jLabel10.setText("دفعة قبول : أكتوبر لسنة 2019 وما قبلها");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(0, 70, 250, 20);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(30, 60, 114));
        jLabel11.setText("المنعقد فى : أغسطس لسنة 2022");
        jPanel1.add(jLabel11);
        jLabel11.setBounds(20, 90, 210, 20);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N
        jPanel1.add(jLabel8);
        jLabel8.setBounds(11, 6, 60, 58);

        jLabel7.setText("المنطقة /");
        jPanel1.add(jLabel7);
        jLabel7.setBounds(930, 90, 70, 20);

        jLabel9.setText("مــــركز/");
        jPanel1.add(jLabel9);
        jLabel9.setBounds(940, 120, 60, 20);

        jLabel12.setText("النظام /");
        jPanel1.add(jLabel12);
        jLabel12.setBounds(940, 150, 70, 30);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(42, 82, 152));
        jLabel16.setText("رئيس لجنة النظام والمراقبة");
        jPanel1.add(jLabel16);
        jLabel16.setBounds(10, 480, 180, 30);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(42, 82, 152));
        jLabel17.setText("راجعه                ");
        jPanel1.add(jLabel17);
        jLabel17.setBounds(250, 480, 140, 30);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(42, 82, 152));
        jLabel14.setText("كتبه                 ");
        jPanel1.add(jLabel14);
        jLabel14.setBounds(720, 480, 160, 30);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(42, 82, 152));
        jLabel18.setText("املاه                ");
        jPanel1.add(jLabel18);
        jLabel18.setBounds(490, 480, 160, 30);

        jSeparator1.setForeground(new java.awt.Color(255, 153, 0));
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(0, 463, 1030, 10);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "م", "الاسم", "المهنة", "رقم التسجيل", "رقم الجلوس", "حالة التلميذ"
            }
        ));
        jScrollPane1.setViewportView(jTable2);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(0, 180, 1030, 260);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1032, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
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
        java.awt.EventQueue.invokeLater(() -> new SecondRound().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
