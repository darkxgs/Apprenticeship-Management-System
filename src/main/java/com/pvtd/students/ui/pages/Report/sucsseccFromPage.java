
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class sucsseccFromPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(sucsseccFromPage.class.getName());

    
    public sucsseccFromPage() {
        initComponents();
     
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
    
    
    private String numberToArabicWords(int number) {

    String[] ones = {
        "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة",
        "ستة", "سبعة", "ثمانية", "تسعة", "عشرة",
        "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر",
        "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    };

    String[] tens = {
        "", "", "عشرون", "ثلاثون", "أربعون", "خمسون",
        "ستون", "سبعون", "ثمانون", "تسعون"
    };

    String[] hundreds = {
        "", "مائة", "مائتان", "ثلاثمائة", "أربعمائة",
        "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    };

    String result = "";

    int h = number / 100;
    int t = number % 100;

    if (h > 0) {
        result += hundreds[h];
    }

    if (t > 0) {

        if (!result.isEmpty()) {
            result += " و ";
        }

        if (t < 20) {
            result += ones[t];
        } else {

            int o = t % 10;
            int tt = t / 10;

            if (o > 0) {
                result += ones[o] + " و ";
            }

            result += tens[tt];
        }
    }

    return result;
}
    
    public void loadStudentImage(String nationalId) {

    try {

        String folderPath = "C:\\Users\\Seif\\OneDrive\\Desktop\\";

        File imgFile = new File(folderPath + nationalId + ".jpg");

        if (imgFile.exists()) {

            ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());

            java.awt.Image img = icon.getImage().getScaledInstance(
                    studentImageLbl.getWidth(),
                    studentImageLbl.getHeight(),
                    java.awt.Image.SCALE_SMOOTH
            );

            studentImageLbl.setIcon(new ImageIcon(img));

        } else {
            studentImageLbl.setIcon(null);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
    public void loadStudentInfo(String seatNo) {

    try {

        String sql = "SELECT s.name, s.seat_no, s.national_id, s.coordination_no,"
                + " s.professional_group, s.profession, s.center_name, s.governorate,"
                + " sp.name specialization,"
                + " ROUND((SUM(sg.obtained_mark) / SUM(sub.max_mark)) * 100,2) percentage,"
                + " CASE"
                + " WHEN (SUM(sg.obtained_mark) / SUM(sub.max_mark)) * 100 >= 85 THEN 'ممتاز'"
                + " WHEN (SUM(sg.obtained_mark) / SUM(sub.max_mark)) * 100 >= 75 THEN 'جيد جداً'"
                + " WHEN (SUM(sg.obtained_mark) / SUM(sub.max_mark)) * 100 >= 65 THEN 'جيد'"
                + " WHEN (SUM(sg.obtained_mark) / SUM(sub.max_mark)) * 100 >= 50 THEN 'مقبول'"
                + " ELSE 'راسب'"
                + " END grade,"
                + " CASE"
                + " WHEN SUM(CASE WHEN sg.obtained_mark < sub.pass_mark THEN 1 ELSE 0 END) = 0"
                + " THEN 'دور أول'"
                + " ELSE 'دور ثاني'"
                + " END exam_round"
                + " FROM students s"
                + " LEFT JOIN specializations sp ON s.specialization_id = sp.id"
                + " LEFT JOIN student_grades sg ON s.id = sg.student_id"
                + " LEFT JOIN subjects sub ON sg.subject_id = sub.id"
                + " WHERE s.seat_no = ?"
                + " GROUP BY s.name,s.seat_no,s.national_id,s.coordination_no,"
                + " s.professional_group,s.profession,s.center_name,s.governorate,sp.name";

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, seatNo);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            nameLbl.setText(rs.getString("name"));
            seatNoLbl.setText(rs.getString("seat_no"));
            nationalIdLbl.setText(rs.getString("national_id"));

            coordinationLbl.setText(rs.getString("coordination_no"));

            groupLbl.setText(rs.getString("professional_group"));
            specLbl.setText(rs.getString("specialization"));

            centerLbl.setText(rs.getString("center_name"));
            govLbl.setText(rs.getString("governorate"));

            percentLbl.setText(rs.getString("percentage") + "%");

            gradeLbl.setText(rs.getString("grade"));

            String round = rs.getString("exam_round");

            if (round.equals("دور أول")) {
                roundLbl.setText("أول");
            } else {
                roundLbl.setText("ثاني");
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
   public void loadStudentSubjects(String seatNo) {

    try {

        String sql = """
        SELECT
        sub.name subject_name,
        sub.type,
        sub.max_mark,
        sub.pass_mark,
        sg.obtained_mark
        FROM students s
        LEFT JOIN student_grades sg ON s.id = sg.student_id
        LEFT JOIN subjects sub ON sub.id = sg.subject_id
        WHERE s.seat_no = ?
        """;

        Connection con = DatabaseConnection.getConnection();

        PreparedStatement ps = con.prepareStatement(sql);

        ps.setString(1, seatNo);

        ResultSet rs = ps.executeQuery();

        List<Object[]> theorySubjects = new ArrayList<>();
        List<Object[]> practicalSubjects = new ArrayList<>();
        List<Object[]> appliedSubjects = new ArrayList<>();

        int theoryTotal = 0;
        int practicalTotal = 0;
        int appliedTotal = 0;

        while (rs.next()) {

            String subject = rs.getString("subject_name");
            String type = rs.getString("type");

            int max = rs.getInt("max_mark");
            int pass = rs.getInt("pass_mark");
            int mark = rs.getInt("obtained_mark");

            if (type == null) {
                continue;
            }

            type = type.trim();

            Object[] data = {subject, max, pass, mark};

            if (type.contains("نظري")) {

                theorySubjects.add(data);
                theoryTotal += mark;

            } else if (type.contains("عملي")) {

                practicalSubjects.add(data);
                practicalTotal += mark;

            } else if (type.contains("تطبيقي")) {

                appliedSubjects.add(data);
                appliedTotal += mark;
            }
        }

        // المواد النظري

        if (theorySubjects.size() > 0) {

            Object[] s = theorySubjects.get(0);

            sub1Lbl.setText(s[0].toString());
            max1Lbl.setText(s[1].toString());
            pass1Lbl.setText(s[2].toString());
            mark1Lbl.setText(s[3].toString());
        }

        if (theorySubjects.size() > 1) {

            Object[] s = theorySubjects.get(1);

            sub2Lbl.setText(s[0].toString());
            max2Lbl.setText(s[1].toString());
            pass2Lbl.setText(s[2].toString());
            mark2Lbl.setText(s[3].toString());
        }

        if (theorySubjects.size() > 2) {

            Object[] s = theorySubjects.get(2);

            sub3Lbl.setText(s[0].toString());
            max3Lbl.setText(s[1].toString());
            pass3Lbl.setText(s[2].toString());
            mark3Lbl.setText(s[3].toString());
        }

        if (theorySubjects.size() > 3) {

            Object[] s = theorySubjects.get(3);

            sub4Lbl.setText(s[0].toString());
            max4Lbl.setText(s[1].toString());
            pass4Lbl.setText(s[2].toString());
            mark4Lbl.setText(s[3].toString());
        }

        // العملي

        if (practicalSubjects.size() > 0) {

            Object[] p = practicalSubjects.get(0);

            amalymax.setText(p[1].toString());
            amalyPass.setText(p[2].toString());
            studgra.setText(p[3].toString());
        }

        // التطبيقي

        if (appliedSubjects.size() >0) {

            Object[] p = appliedSubjects.get(0);

            tatbecMax.setText(p[1].toString());
            tatbecPass.setText(p[2].toString());
            studeTa.setText(p[3].toString());
        }

        int finalTotal = theoryTotal + practicalTotal + appliedTotal;
        int finalTa = practicalTotal+appliedTotal;
        
        String finalTotalArabic = numberToArabicWords(finalTotal);
jLabel41.setText(finalTotalArabic);
        ee.setText(String.valueOf(theoryTotal));
        practicalTotalLbl.setText(String.valueOf(finalTa));
        eed.setText(String.valueOf(finalTotal));

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public void printSuccessForms(List<String> seatNumbers, List<String> nationalIds) {

    try {

        File mainFolder = new File("تقارير");

        if (!mainFolder.exists()) {
            mainFolder.mkdir();
        }

        File formFolder = new File(mainFolder, "استمارة نجاح");

        if (!formFolder.exists()) {
            formFolder.mkdir();
        }

        String allPath = formFolder.getAbsolutePath() + "/all_success_forms.pdf";

        Document allDoc = new Document(PageSize.A4);

        PdfWriter.getInstance(allDoc, new FileOutputStream(allPath));

        allDoc.open();

        for (int i = 0; i < seatNumbers.size(); i++) {

            String seatNo = seatNumbers.get(i);
            String nationalId = nationalIds.get(i);

            loadStudentInfo(seatNo);
            loadStudentSubjects(seatNo);
            loadStudentImage(nationalId);

           int width = 845;   // عرض A4 بالبكسل
int height = 1212; // ارتفاع A4 بالبكسل

jPanel1.setSize(width, height);
jPanel1.doLayout();

BufferedImage image = new BufferedImage(
        width,
        height,
        BufferedImage.TYPE_INT_RGB
);
            Graphics2D g2 = image.createGraphics();

            jPanel1.printAll(g2);

            g2.dispose();

            Image img = Image.getInstance(image, null);

            img.scaleAbsolute(
                    PageSize.A4.getWidth(),
                    PageSize.A4.getHeight()
            );

            img.setAbsolutePosition(0, 0);

            allDoc.add(img);

            allDoc.newPage();
        }

        allDoc.close();

        Desktop.getDesktop().open(new File(allPath));

        JOptionPane.showMessageDialog(this, "تم إنشاء الاستمارات");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        studentImageLbl = new javax.swing.JLabel();
        seatNoLbl = new javax.swing.JLabel();
        coordinationLbl = new javax.swing.JLabel();
        nationalIdLbl = new javax.swing.JLabel();
        groupLbl = new javax.swing.JLabel();
        roundLbl = new javax.swing.JLabel();
        centerLbl = new javax.swing.JLabel();
        govLbl = new javax.swing.JLabel();
        sub1Lbl = new javax.swing.JLabel();
        sub2Lbl = new javax.swing.JLabel();
        sub3Lbl = new javax.swing.JLabel();
        sub4Lbl = new javax.swing.JLabel();
        max1Lbl = new javax.swing.JLabel();
        max2Lbl = new javax.swing.JLabel();
        max3Lbl = new javax.swing.JLabel();
        max4Lbl = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        amalymax = new javax.swing.JLabel();
        tatbecMax = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        pass1Lbl = new javax.swing.JLabel();
        pass2Lbl = new javax.swing.JLabel();
        pass3Lbl = new javax.swing.JLabel();
        pass4Lbl = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        amalyPass = new javax.swing.JLabel();
        tatbecPass = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        mark1Lbl = new javax.swing.JLabel();
        mark2Lbl = new javax.swing.JLabel();
        mark3Lbl = new javax.swing.JLabel();
        mark4Lbl = new javax.swing.JLabel();
        ee = new javax.swing.JLabel();
        studgra = new javax.swing.JLabel();
        studeTa = new javax.swing.JLabel();
        practicalTotalLbl = new javax.swing.JLabel();
        eed = new javax.swing.JLabel();
        nameLbl = new javax.swing.JLabel();
        specLbl = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        gradeLbl = new javax.swing.JLabel();
        percentLbl = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);
        jPanel1.add(studentImageLbl);
        studentImageLbl.setBounds(20, 0, 220, 270);
        jPanel1.add(seatNoLbl);
        seatNoLbl.setBounds(467, 290, 200, 20);
        jPanel1.add(coordinationLbl);
        coordinationLbl.setBounds(467, 320, 200, 20);
        jPanel1.add(nationalIdLbl);
        nationalIdLbl.setBounds(467, 350, 200, 20);
        jPanel1.add(groupLbl);
        groupLbl.setBounds(127, 310, 130, 20);
        jPanel1.add(roundLbl);
        roundLbl.setBounds(280, 340, 30, 20);
        jPanel1.add(centerLbl);
        centerLbl.setBounds(537, 440, 140, 20);
        jPanel1.add(govLbl);
        govLbl.setBounds(217, 440, 130, 20);
        jPanel1.add(sub1Lbl);
        sub1Lbl.setBounds(530, 530, 50, 50);
        jPanel1.add(sub2Lbl);
        sub2Lbl.setBounds(480, 530, 50, 50);
        jPanel1.add(sub3Lbl);
        sub3Lbl.setBounds(430, 530, 50, 50);
        jPanel1.add(sub4Lbl);
        sub4Lbl.setBounds(370, 530, 60, 50);
        jPanel1.add(max1Lbl);
        max1Lbl.setBounds(530, 580, 50, 40);
        jPanel1.add(max2Lbl);
        max2Lbl.setBounds(480, 580, 50, 40);
        jPanel1.add(max3Lbl);
        max3Lbl.setBounds(430, 580, 50, 40);
        jPanel1.add(max4Lbl);
        max4Lbl.setBounds(370, 580, 60, 40);

        jLabel18.setText("300");
        jPanel1.add(jLabel18);
        jLabel18.setBounds(323, 580, 50, 40);
        jPanel1.add(amalymax);
        amalymax.setBounds(270, 580, 50, 40);
        jPanel1.add(tatbecMax);
        tatbecMax.setBounds(220, 580, 50, 40);

        jLabel21.setText("300");
        jPanel1.add(jLabel21);
        jLabel21.setBounds(160, 580, 50, 40);

        jLabel22.setText("600");
        jPanel1.add(jLabel22);
        jLabel22.setBounds(100, 580, 60, 40);
        jPanel1.add(pass1Lbl);
        pass1Lbl.setBounds(530, 620, 50, 40);
        jPanel1.add(pass2Lbl);
        pass2Lbl.setBounds(480, 620, 50, 40);
        jPanel1.add(pass3Lbl);
        pass3Lbl.setBounds(430, 620, 50, 40);
        jPanel1.add(pass4Lbl);
        pass4Lbl.setBounds(373, 620, 50, 40);

        jLabel27.setText("150");
        jPanel1.add(jLabel27);
        jLabel27.setBounds(320, 620, 50, 40);
        jPanel1.add(amalyPass);
        amalyPass.setBounds(270, 620, 50, 40);
        jPanel1.add(tatbecPass);
        tatbecPass.setBounds(220, 620, 50, 40);

        jLabel30.setText("170");
        jPanel1.add(jLabel30);
        jLabel30.setBounds(160, 620, 50, 40);

        jLabel31.setText("320");
        jPanel1.add(jLabel31);
        jLabel31.setBounds(100, 620, 60, 40);
        jPanel1.add(mark1Lbl);
        mark1Lbl.setBounds(530, 660, 60, 80);
        jPanel1.add(mark2Lbl);
        mark2Lbl.setBounds(480, 660, 50, 80);
        jPanel1.add(mark3Lbl);
        mark3Lbl.setBounds(430, 660, 50, 80);
        jPanel1.add(mark4Lbl);
        mark4Lbl.setBounds(380, 660, 50, 80);
        jPanel1.add(ee);
        ee.setBounds(325, 660, 50, 80);
        jPanel1.add(studgra);
        studgra.setBounds(270, 660, 50, 80);
        jPanel1.add(studeTa);
        studeTa.setBounds(220, 660, 50, 80);
        jPanel1.add(practicalTotalLbl);
        practicalTotalLbl.setBounds(160, 660, 50, 80);
        jPanel1.add(eed);
        eed.setBounds(100, 666, 60, 70);
        jPanel1.add(nameLbl);
        nameLbl.setBounds(473, 380, 150, 20);
        jPanel1.add(specLbl);
        specLbl.setBounds(533, 410, 140, 20);
        jPanel1.add(jLabel41);
        jLabel41.setBounds(243, 770, 270, 20);
        jPanel1.add(gradeLbl);
        gradeLbl.setBounds(533, 810, 110, 20);
        jPanel1.add(percentLbl);
        percentLbl.setBounds(223, 810, 160, 20);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/certificate_1.jpg"))); // NOI18N
        jPanel1.add(jLabel2);
        jLabel2.setBounds(6, 6, 790, 1200);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 792, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1181, Short.MAX_VALUE)
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
        java.awt.EventQueue.invokeLater(() -> new sucsseccFromPage().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel amalyPass;
    private javax.swing.JLabel amalymax;
    private javax.swing.JLabel centerLbl;
    private javax.swing.JLabel coordinationLbl;
    private javax.swing.JLabel ee;
    private javax.swing.JLabel eed;
    private javax.swing.JLabel govLbl;
    private javax.swing.JLabel gradeLbl;
    private javax.swing.JLabel groupLbl;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel mark1Lbl;
    private javax.swing.JLabel mark2Lbl;
    private javax.swing.JLabel mark3Lbl;
    private javax.swing.JLabel mark4Lbl;
    private javax.swing.JLabel max1Lbl;
    private javax.swing.JLabel max2Lbl;
    private javax.swing.JLabel max3Lbl;
    private javax.swing.JLabel max4Lbl;
    private javax.swing.JLabel nameLbl;
    private javax.swing.JLabel nationalIdLbl;
    private javax.swing.JLabel pass1Lbl;
    private javax.swing.JLabel pass2Lbl;
    private javax.swing.JLabel pass3Lbl;
    private javax.swing.JLabel pass4Lbl;
    private javax.swing.JLabel percentLbl;
    private javax.swing.JLabel practicalTotalLbl;
    private javax.swing.JLabel roundLbl;
    private javax.swing.JLabel seatNoLbl;
    private javax.swing.JLabel specLbl;
    private javax.swing.JLabel studeTa;
    private javax.swing.JLabel studentImageLbl;
    private javax.swing.JLabel studgra;
    private javax.swing.JLabel sub1Lbl;
    private javax.swing.JLabel sub2Lbl;
    private javax.swing.JLabel sub3Lbl;
    private javax.swing.JLabel sub4Lbl;
    private javax.swing.JLabel tatbecMax;
    private javax.swing.JLabel tatbecPass;
    // End of variables declaration//GEN-END:variables
}
