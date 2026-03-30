/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


public class Deprived extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Deprived.class.getName());

    
    
    public Deprived() {
        initComponents();
        this.setSize(1200, 800);
        jPanel1.setPreferredSize(new Dimension(1123, 794));
         jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
         jTable2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         
         jTable2.setShowGrid(true);
jTable2.setGridColor(Color.GRAY);

jTable2.setRowHeight(30); // ارتفاع الصف

jTable2.setShowHorizontalLines(true);
jTable2.setShowVerticalLines(true);
         
         
         
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
        c.setFont(new Font("Tahoma", Font.BOLD, 14));
c.setBackground(new Color(200, 230, 220));
c.setForeground(Color.BLACK);
        return c;
    }
});
        String month = chooseMonth();

if(month == null){
    return;
}

int year = java.time.Year.now().getValue();

String arabicYear = toArabicNumbers(String.valueOf(year));

jLabel10.setText("دفعه قبول : "+month+" "+"لسنه "+arabicYear+",وماقبلها");

jLabel11.setText("المنعقد فى :"+ month + " "+"لسنه"+arabicYear);
        
    }

private void resizeTable() {

    int rowsPerPage = 35;
    int rowHeight = jTable2.getRowHeight();

    int panelWidth = 1130;

    int headerHeight = 200;
    int footerHeight = 250; // 👈 زودنا عشان الفوتر

    int tableHeight = rowsPerPage * rowHeight;

    // 👇 الجدول
    jScrollPane2.setBounds(0, headerHeight, panelWidth, tableHeight);

    // 👇 مهم جدًا: نحط الفوتر تحت الجدول مباشرة
    int footerY = headerHeight + tableHeight;

    jSeparator1.setBounds(0, footerY, 1100, 10);
    jLabel14.setBounds(780, footerY + 20, 170, 20);
    jLabel16.setBounds(0, footerY + 20, 200, 20);
    jLabel17.setBounds(250, footerY + 20, 160, 20);
    jLabel18.setBounds(510, footerY + 20, 170, 20);

    // 👇 ارتفاع البانل النهائي
    int totalHeight = footerY + footerHeight;

    jPanel1.setPreferredSize(new Dimension(panelWidth, totalHeight));
    jPanel1.setSize(panelWidth, totalHeight);

    jPanel1.revalidate();
    jPanel1.repaint();
}

public void createPDF() {
    try {

        int rowsPerPage = 35;

        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        Vector<Vector> originalData = new Vector<>(model.getDataVector());

        int totalRows = originalData.size();
        int pageCount = (int) Math.ceil((double) totalRows / rowsPerPage);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream("report.pdf"));
        document.open();

        int globalIndex = 1;

        for (int page = 0; page < pageCount; page++) {

            model.setRowCount(0);

            int start = page * rowsPerPage;
            int end = Math.min(start + rowsPerPage, totalRows);

            for (int i = start; i < end; i++) {

                Vector row = new Vector(originalData.get(i));

                row.set(5, globalIndex++); // 👈 رقم الطالب (آخر عمود عندك)

                model.addRow(row);
            }

            resizeTable();

            jPanel1.doLayout();
            jPanel1.validate();

            int width = jPanel1.getWidth();
            int height = jPanel1.getHeight();

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            jPanel1.printAll(g2d);
            g2d.dispose();

            Image pdfImg = Image.getInstance(img, null);
            pdfImg.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());

            document.add(pdfImg);

            if (page < pageCount - 1) {
                document.newPage();
            }
        }

        // رجّع البيانات
        model.setRowCount(0);
        for (Vector row : originalData) {
            model.addRow(row);
        }

        document.close();

        Desktop.getDesktop().open(new File("report.pdf"));

    } catch (Exception e) {
        e.printStackTrace();
    }
}
   private String toArabicNumbers(String number) {

    return number
            .replace("0", "٠")
            .replace("1", "١")
            .replace("2", "٢")
            .replace("3", "٣")
            .replace("4", "٤")
            .replace("5", "٥")
            .replace("6", "٦")
            .replace("7", "٧")
            .replace("8", "٨")
            .replace("9", "٩");
}
   
   
   private String chooseMonth() {

    String[] months = {
        "يناير","فبراير","مارس","أبريل",
        "مايو","يونيو","يوليو","أغسطس",
        "سبتمبر","أكتوبر","نوفمبر","ديسمبر"
    };

    return (String) JOptionPane.showInputDialog(
            this,
            "اختر شهر الشهادة",
            "تاريخ الشهادة",
            JOptionPane.QUESTION_MESSAGE,
            null,
            months,
            months[7]
    );
}
    
   
 public void loadCenterData(String centerName) {

    try {

        String sql = """
        SELECT DISTINCT region, exam_system
        FROM students
        WHERE center_name = ?
        """;

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, centerName);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            String regio = rs.getString("region");
            String syste = rs.getString("exam_system");

            regoin.setText(regio);   // المنطقة
            system.setText(syste);   // النظام
        } else {
            regoin.setText("—");
            system.setText("—");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        regoin = new javax.swing.JLabel();
        ce = new javax.swing.JLabel();
        system = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "حاله التلميذ", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 196, 1080, 296));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(42, 82, 152));
        jLabel6.setText("تلاميذ محرمومين");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(499, 116, 170, 60));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(30, 60, 114));
        jLabel1.setText("وزارة التجارة والصناعة");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(811, 6, 140, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(30, 60, 114));
        jLabel2.setText("مصلحة الكفاية االنتاجية والتدريب المهنى");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(751, 26, 250, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(30, 60, 114));
        jLabel3.setText("الرئاسة العامة المتحانات دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(731, 46, 290, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(30, 60, 114));
        jLabel4.setText("لجنة النظام والمراقبة");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(801, 66, 140, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(30, 60, 114));
        jLabel5.setText("نتائج أمتحان دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 60, 220, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(30, 60, 114));
        jLabel10.setText("دفعة قبول : أكتوبر لسنة 2019 وما قبلها");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 96, 250, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(30, 60, 114));
        jLabel11.setText("المنعقد فى : أغسطس لسنة 2022");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 116, 210, -1));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 18, -1, 58));

        jLabel7.setText("المنطقة /");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 90, 70, 20));

        jLabel9.setText("مــــركز/");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 120, 60, 20));

        jLabel12.setText("النظام /");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 150, 70, 30));

        jLabel13.setForeground(new java.awt.Color(30, 60, 114));
        jLabel13.setText("صفحة 1 من 1 ");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 174, 90, -1));

        jSeparator1.setForeground(new java.awt.Color(255, 102, 0));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 492, 1086, 10));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(42, 82, 152));
        jLabel16.setText("رئيس لجنة النظام والمراقبة");
        jPanel1.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 514, -1, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(42, 82, 152));
        jLabel17.setText("راجعه                ");
        jPanel1.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(317, 514, 160, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(42, 82, 152));
        jLabel14.setText("كتبه                 ");
        jPanel1.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(819, 514, 170, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(42, 82, 152));
        jLabel18.setText("املاه                ");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(579, 514, 170, -1));

        regoin.setText("jLabel15");
        jPanel1.add(regoin, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 90, 170, 30));

        ce.setText("jLabel19");
        jPanel1.add(ce, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 120, 180, 30));

        system.setText("jLabel20");
        jPanel1.add(system, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 150, 180, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1083, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        java.awt.EventQueue.invokeLater(() -> new Deprived().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel ce;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JTable jTable2;
    private javax.swing.JLabel regoin;
    private javax.swing.JLabel system;
    // End of variables declaration//GEN-END:variables
}
