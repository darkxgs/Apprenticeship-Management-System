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
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Seif
 */
public class CertificateOfSuccess1 extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CertificateOfSuccess1.class.getName());

    
    public CertificateOfSuccess1() {
        initComponents();
        lblGroup.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblProfession.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNationalId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRegion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblcenter.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        LocalDate date = LocalDate.now();

int year = date.getYear();
int month = date.getMonthValue();

// الشهر عربي
String arabicMonth = getArabicMonth(month);

// السنة بالحروف
String arabicYear = convertYearToArabicWWords(year);

// حطهم في الليبلز
jLabel13.setText(arabicMonth + " " + year + " م");
jLabel15.setText(arabicYear);
        
        
    }
    
    
    public String getArabicMonth(int month) {
    String[] months = {
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    };
    return months[month - 1];
}
    
    public String convertYearToArabicWords(int year) {
    if (year == 2022) return "سنة ألفان واثنان وعشرون";
    if (year == 2023) return "سنة ألفان وثلاثة وعشرون";
    if (year == 2024) return "سنة ألفان وأربعة وعشرون";
    if (year == 2025) return "سنة ألفان وخمسة وعشرون";
    
    return "سنة " + year; // fallback
}
    
    
    

    public void loadStudentData(String seatNo) {

        String sql = "SELECT s.id, s.name, s.national_id, s.center_name, "
                + "s.profession AS specialization, "
                + "s.professional_group, s.region, s.phone_number "
                + "FROM students s "
                + "WHERE s.seat_no = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, seatNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                
                double percentage = getStudentPercentage(seatNo);

// تنسيق رقمين بعد العلامة
String formatted = String.format("%.2f", percentage);

// تحويل لأرقام عربية
String percentArabic = formatted
        .replace("0","٠")
        .replace("1","١")
        .replace("2","٢")
        .replace("3","٣")
        .replace("4","٤")
        .replace("5","٥")
        .replace("6","٦")
        .replace("7","٧")
        .replace("8","٨")
        .replace("9","٩");

// حطه في الليبل
jLabel9.setText(percentArabic + "٪");

                int stuId = rs.getInt("id");
                String gradesText = "لا توجد درجات\n";
                String gradesSql = "SELECT sub.name, sg.obtained_mark FROM student_grades sg JOIN subjects sub ON sg.subject_id = sub.id WHERE sg.student_id = ?";
                try (PreparedStatement psG = con.prepareStatement(gradesSql)) {
                    psG.setInt(1, stuId);
                    try (ResultSet rsG = psG.executeQuery()) {
                        StringBuilder sb = new StringBuilder();
                        while (rsG.next()) {
                            sb.append(" - ").append(rsG.getString("name")).append(": ").append(rsG.getString("obtained_mark") != null ? rsG.getString("obtained_mark") : "0").append("\n");
                        }
                        if (sb.length() > 0) {
                            gradesText = sb.toString();
                        }
                    }
                }
                
                lblName.setText(rs.getString("name"));
                lblNationalId.setText(rs.getString("national_id"));
                lblcenter.setText(rs.getString("center_name"));
String specializationFromDB = rs.getString("specialization");

// لو الداتا بيز فاضية خليه فاضي مؤقتاً
if (specializationFromDB != null) {
    lblProfession.setText(specializationFromDB);
}                lblGroup.setText(rs.getString("professional_group"));
                lblRegion.setText(rs.getString("region"));
          qRCodeComponent2.setStudentData(
    rs.getString("name"),
    rs.getString("national_id"),
    seatNo,
    rs.getString("center_name"),
    rs.getString("professional_group"),
    percentArabic,
    rs.getString("phone_number"),
    gradesText
);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public String convertYearToArabicWWords(int year) {

    String[] ones = {
        "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة",
        "ستة", "سبعة", "ثمانية", "تسعة"
    };

    String[] tens = {
        "", "عشرة", "عشرون", "ثلاثون", "أربعون",
        "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"
    };

    int thousands = year / 1000;
    int remainder = year % 1000;

    String result = "";

    // آلاف
    if (thousands == 2) {
        result += "ألفان";
    } else if (thousands == 1) {
        result += "ألف";
    } else if (thousands > 2) {
        result += ones[thousands] + " آلاف";
    }

    // باقي الرقم (زي 22 في 2022)
    if (remainder > 0) {
        int lastTwo = remainder % 100;
        int t = lastTwo / 10;
        int o = lastTwo % 10;

        result += " و";

        if (o > 0) {
            result += ones[o];
            if (t > 0) result += " و";
        }

        if (t > 0) {
            result += tens[t];
        }
    }

    return "سنة " + result;
}
    
    public double getStudentPercentage(String seatNo) {

    double percentage = 0;

    String sql = "SELECT NVL(ROUND((SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100,2),0) AS percentage "
               + "FROM students s "
               + "LEFT JOIN student_grades sg ON s.id = sg.student_id "
               + "LEFT JOIN subjects sub ON sg.subject_id = sub.id "
               + "WHERE s.seat_no = ?";

    try (Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, seatNo);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            percentage = rs.getDouble("percentage");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return percentage;
}
    
    
    
    
public void printCertificates(List<Student> students, java.util.function.BiConsumer<Integer, Integer> progressCallback) {

    try {

        // إنشاء الفولدرات
        File mainFolder = new File("تقارير");
        if (!mainFolder.exists()) mainFolder.mkdir();

        File certFolder = new File(mainFolder, "شهادة نجاح");
        if (!certFolder.exists()) certFolder.mkdir();

        String allFilePath = certFolder.getAbsolutePath() + "/all_certificates.pdf";

        // 🔥 نخلي الصفحة نفس مقاس الشهادة
        com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(934, 686);

        Document allDoc = new Document(pageSize);
        PdfWriter.getInstance(allDoc, new FileOutputStream(allFilePath));
        allDoc.open();

        int total = students.size();
        for (int i = 0; i < total; i++) {
            Student s = students.get(i);
            if (progressCallback != null) {
                progressCallback.accept(i + 1, total);
            }

            // تحميل بيانات الطالب
            loadStudentData(s.getSeatNo());

            // ✅ لو التخصص جاي من الجدول حطه
            if (s.getProfession() != null && !s.getProfession().trim().isEmpty()) {
                lblProfession.setText(s.getProfession());
            } else if (lblProfession.getText() == null || lblProfession.getText().equals("null")) {
                lblProfession.setText("—");
            }
            String nationalId = lblNationalId.getText();

            // 🔥 رسم الشهادة كصورة
            BufferedImage image = new BufferedImage(934, 686, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();

            jPanel2.setSize(934, 686); // مهم جداً
            jPanel2.doLayout();
            jPanel2.printAll(g2);

            g2.dispose();

            Image img = Image.getInstance(image, null);

            // 🔥 نحط الصورة تملى الصفحة بالظبط
            img.scaleAbsolute(934, 686);
            img.setAbsolutePosition(0, 0);

            // -----------------------
            // 1️⃣ ملف مجمع
            // -----------------------
            allDoc.add(img);
            allDoc.newPage();

            // -----------------------
            // 2️⃣ ملف لكل طالب
            // -----------------------
            String singlePath = certFolder.getAbsolutePath() + "/" + (nationalId != null ? nationalId : "student_" + s.getSeatNo()) + ".pdf";

            Document singleDoc = new Document(pageSize);
            PdfWriter.getInstance(singleDoc, new FileOutputStream(singlePath));
            singleDoc.open();

            Image img2 = Image.getInstance(image, null);
            img2.scaleAbsolute(934, 686);
            img2.setAbsolutePosition(0, 0);

            singleDoc.add(img2);
            singleDoc.close();
        }

        allDoc.close();

        SwingUtilities.invokeLater(() -> {
            try {
                Desktop.getDesktop().open(new File(allFilePath));
                JOptionPane.showMessageDialog(null, "تم إنشاء الشهادات بنجاح");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
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
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        qRCodeComponent2 = new com.pvtd.students.ui.components.QRCodeComponent();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(null);

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
        jLabel3.setBounds(520, 160, 310, 25);

        lblName.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblName);
        lblName.setBounds(210, 160, 310, 20);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("قد نجـح فى إمتحــــان دبلوم التـلـمذة الصنـاعـيـة");
        jPanel2.add(jLabel5);
        jLabel5.setBounds(60, 210, 350, 25);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("التخصص :");
        jPanel2.add(jLabel6);
        jLabel6.setBounds(740, 250, 90, 25);

        lblNationalId.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblNationalId);
        lblNationalId.setBounds(530, 210, 177, 20);

        lblProfession.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblProfession);
        lblProfession.setBounds(530, 250, 200, 30);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("المجموعة المهنية :");
        jPanel2.add(jLabel10);
        jLabel10.setBounds(270, 270, 140, 25);

        lblGroup.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblGroup);
        lblGroup.setBounds(90, 270, 171, 20);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setText("دور  :");
        jPanel2.add(jLabel12);
        jLabel12.setBounds(770, 290, 60, 25);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("أغسطس ٢٠٢٢ م");
        jPanel2.add(jLabel13);
        jLabel13.setBounds(680, 280, 101, 50);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("سنه الفان واثنان وعشرون");
        jPanel2.add(jLabel15);
        jLabel15.setBounds(500, 280, 160, 40);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setText("بتقديـر:");
        jPanel2.add(jLabel16);
        jLabel16.setBounds(320, 310, 60, 25);

        lblGrade.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblGrade.setText("ناجح");
        jPanel2.add(lblGrade);
        lblGrade.setBounds(290, 300, 30, 50);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel18.setText("مركز / محطة :");
        jPanel2.add(jLabel18);
        jLabel18.setBounds(720, 360, 110, 25);

        lblcenter.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblcenter);
        lblcenter.setBounds(580, 360, 130, 30);

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel20.setText("منطقة :");
        jPanel2.add(jLabel20);
        jLabel20.setBounds(260, 380, 80, 25);

        lblRegion.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel2.add(lblRegion);
        lblRegion.setBounds(110, 380, 143, 20);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("وهى معادلة لشهادة دبلوم المدارس الصناعية ومناظرة لها بجمهورية مصر العربية وذلك طبقا للقرار الوزارى");
        jPanel2.add(jLabel22);
        jLabel22.setBounds(170, 430, 637, 20);

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("لوزارة التربية والتعليم رقم ٩٢ الصادر فى ١٧ / ٦ / ١٩٦٨ م وتم تعديله بالقرار رقم ٥٧ لسنة ١٩٦٩ م.");
        jPanel2.add(jLabel23);
        jLabel23.setBounds(180, 450, 590, 20);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setText("مصلحة الكفاية الإنتاجية والتدريب المهني حاصلة على نظام إدارة الجودة ٩٠٠١ ISO");
        jPanel2.add(jLabel24);
        jLabel24.setBounds(240, 470, 480, 20);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("تحريرا فى :");
        jPanel2.add(jLabel14);
        jLabel14.setBounds(770, 520, 70, 20);

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setText("المدير العام");
        jPanel2.add(jLabel25);
        jLabel25.setBounds(460, 520, 80, 20);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("رئيس المصلحة");
        jPanel2.add(jLabel26);
        jLabel26.setBounds(110, 520, 90, 20);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("الرقم القومى :");
        jPanel2.add(jLabel7);
        jLabel7.setBounds(710, 210, 110, 25);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N
        jPanel2.add(jLabel4);
        jLabel4.setBounds(790, 50, 60, 70);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("بنسبه:");
        jPanel2.add(jLabel8);
        jLabel8.setBounds(150, 310, 50, 40);

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("jLabel9");
        jPanel2.add(jLabel9);
        jLabel9.setBounds(60, 320, 90, 20);
        jPanel2.add(qRCodeComponent2);
        qRCodeComponent2.setBounds(30, 10, 200, 180);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 686, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addGap(0, 686, Short.MAX_VALUE)
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblGrade;
    private javax.swing.JLabel lblGroup;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNationalId;
    public javax.swing.JLabel lblProfession;
    private javax.swing.JLabel lblRegion;
    private javax.swing.JLabel lblcenter;
    private com.pvtd.students.ui.components.QRCodeComponent qRCodeComponent2;
    // End of variables declaration//GEN-END:variables
}
