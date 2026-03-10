/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Seif
 */
public class CertificateOfSuccess1 extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CertificateOfSuccess1.class.getName());

    /**
     * Creates new form CertificateOfSuccess1
     */
    public CertificateOfSuccess1() {
        initComponents();
    }

    public void loadStudentData(String seatNo) {

        String sql = "SELECT s.name, s.national_id, s.center_name, "
                + "sp.name AS specialization, "
                + "s.professional_group, s.region "
                + "FROM students s "
                + "LEFT JOIN specializations sp ON s.specialization_id = sp.id "
                + "WHERE s.seat_no = ?";

        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, seatNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                lblName.setText(rs.getString("name"));
                lblNationalId.setText(rs.getString("national_id"));
                lblcenter.setText(rs.getString("center_name"));
                lblProfession.setText(rs.getString("specialization"));
                lblGroup.setText(rs.getString("professional_group"));
                lblRegion.setText(rs.getString("region"));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public void printCertificates(List<Student> students) {

    try {

        // إنشاء الفولدرات
        File mainFolder = new File("تقارير");
        if (!mainFolder.exists()) {
            mainFolder.mkdir();
        }

        File certFolder = new File(mainFolder, "شهادة نجاح");
        if (!certFolder.exists()) {
            certFolder.mkdir();
        }

        // ملف الطباعة الجماعي
        String allFilePath = certFolder.getAbsolutePath() + "/all_certificates.pdf";

        Document allDoc = new Document(PageSize.A4);
        PdfWriter.getInstance(allDoc, new FileOutputStream(allFilePath));
        allDoc.open();

        for (Student s : students) {

            loadStudentData(s.getSeatNo());

            String nationalId = lblNationalId.getText();

            // إنشاء صورة من الشهادة
            BufferedImage image = new BufferedImage(
                    jPanel2.getWidth(),
                    jPanel2.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2 = image.createGraphics();
            jPanel2.paint(g2);
            g2.dispose();

            Image img = Image.getInstance(image, null);
            img.scaleToFit(PageSize.A4.getWidth() - 40, PageSize.A4.getHeight() - 40);
            img.setAlignment(Image.ALIGN_CENTER);

            // -----------------------
            // 1️⃣ إضافة للشهادة الجماعية
            // -----------------------
            allDoc.add(img);
            allDoc.newPage();

            // -----------------------
            // 2️⃣ إنشاء ملف منفصل لكل طالب
            // -----------------------
            String singlePath = certFolder.getAbsolutePath() + "/" + nationalId + ".pdf";

            Document singleDoc = new Document(PageSize.A4);
            PdfWriter.getInstance(singleDoc, new FileOutputStream(singlePath));
            singleDoc.open();

            Image img2 = Image.getInstance(image, null);
            img2.scaleToFit(PageSize.A4.getWidth() - 40, PageSize.A4.getHeight() - 40);
            img2.setAlignment(Image.ALIGN_CENTER);

            singleDoc.add(img2);
            singleDoc.close();
        }

        allDoc.close();

        // فتح ملف الطباعة في المتصفح
        Desktop.getDesktop().open(new File(allFilePath));

        JOptionPane.showMessageDialog(this, "تم إنشاء الشهادات بنجاح");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        qRCodeComponent1 = new com.pvtd.students.ui.components.QRCodeComponent();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblNationalId = new javax.swing.JLabel();
        lblProfession = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblGroup = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lblGrade = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        lblcenter = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        lblRegion = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(null);
        jPanel2.add(qRCodeComponent1);
        qRCodeComponent1.setBounds(6, 6, 160, 150);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        jLabel1.setText("شهادة");
        jPanel2.add(jLabel1);
        jLabel1.setBounds(364, 25, 110, 41);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("دبلوم التلمذة الصناعية");
        jPanel2.add(jLabel2);
        jLabel2.setBounds(325, 84, 190, 25);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("تشهد وزارة التجارة والصناعة بأن السيد :");
        jPanel2.add(jLabel3);
        jLabel3.setBounds(622, 175, 310, 25);

        lblName.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblName);
        lblName.setBounds(400, 180, 220, 20);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("قد نجـح فى إمتحــــان دبلوم التـلـمذة الصنـاعـيـة");
        jPanel2.add(jLabel5);
        jLabel5.setBounds(20, 212, 350, 25);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("التخصص :");
        jPanel2.add(jLabel6);
        jLabel6.setBounds(840, 270, 90, 25);

        lblNationalId.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblNationalId);
        lblNationalId.setBounds(636, 228, 177, 20);

        lblProfession.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblProfession);
        lblProfession.setBounds(635, 270, 200, 30);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("المجموعة المهنية :");
        jPanel2.add(jLabel10);
        jLabel10.setBounds(224, 267, 140, 25);

        lblGroup.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblGroup);
        lblGroup.setBounds(53, 271, 171, 20);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setText("دور ثانى :");
        jPanel2.add(jLabel12);
        jLabel12.setBounds(856, 310, 80, 25);

        jLabel13.setText("أغسطس ٢٠٢٢ م");
        jPanel2.add(jLabel13);
        jLabel13.setBounds(679, 317, 101, 16);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("سنه الفان واثنان وعشرون");
        jPanel2.add(jLabel15);
        jLabel15.setBounds(438, 314, 160, 20);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setText("بتقديـر:");
        jPanel2.add(jLabel16);
        jLabel16.setBounds(274, 310, 60, 25);

        lblGrade.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblGrade.setText("ناجح");
        jPanel2.add(lblGrade);
        lblGrade.setBounds(155, 314, 119, 20);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel18.setText("مركز / محطة :");
        jPanel2.add(jLabel18);
        jLabel18.setBounds(828, 377, 109, 25);

        lblcenter.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblcenter);
        lblcenter.setBounds(685, 381, 143, 20);

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel20.setText("منطقة :");
        jPanel2.add(jLabel20);
        jLabel20.setBounds(216, 377, 80, 25);

        lblRegion.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblRegion);
        lblRegion.setBounds(73, 381, 143, 20);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("وهى معادلة لشهادة دبلوم المدارس الصناعية ومناظرة لها بجمهورية مصر العربية وذلك طبقا للقرار الوزارى");
        jPanel2.add(jLabel22);
        jLabel22.setBounds(160, 460, 637, 20);

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("لوزارة التربية والتعليم رقم ٩٢ الصادر فى ١٧ / ٦ / ١٩٦٨ م وتم تعديله بالقرار رقم ٥٧ لسنة ١٩٦٩ م.");
        jPanel2.add(jLabel23);
        jLabel23.setBounds(170, 480, 590, 20);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setText("مصلحة الكفاية الإنتاجية والتدريب المهني حاصلة على نظام إدارة الجودة ٩٠٠١ ISO");
        jPanel2.add(jLabel24);
        jLabel24.setBounds(230, 500, 480, 20);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("تحريرا فى :");
        jPanel2.add(jLabel14);
        jLabel14.setBounds(773, 570, 70, 20);

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setText("المدير العام");
        jPanel2.add(jLabel25);
        jLabel25.setBounds(461, 570, 80, 20);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("رئيس المصلحة");
        jPanel2.add(jLabel26);
        jLabel26.setBounds(114, 570, 90, 20);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("الرقم القومى :");
        jPanel2.add(jLabel7);
        jLabel7.setBounds(818, 224, 110, 25);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 653, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 943, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 653, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
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
        java.awt.EventQueue.invokeLater(() -> new CertificateOfSuccess1().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblGrade;
    private javax.swing.JLabel lblGroup;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNationalId;
    private javax.swing.JLabel lblProfession;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JLabel lblcenter;
    private com.pvtd.students.ui.components.QRCodeComponent qRCodeComponent1;
    // End of variables declaration//GEN-END:variables
}
