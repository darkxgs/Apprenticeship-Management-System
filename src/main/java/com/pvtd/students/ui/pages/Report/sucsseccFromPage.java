
package com.pvtd.students.ui.pages.Report;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;

public class sucsseccFromPage extends javax.swing.JFrame {
    private String currentImagePath;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(sucsseccFromPage.class.getName());

    
    public sucsseccFromPage() {
        initComponents();
        nameLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
groupLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
centerLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
govLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
specLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
percentLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
gradeLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        sub1Lbl.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
sub2Lbl.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
sub3Lbl.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
sub4Lbl.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
        int year = LocalDate.now().getYear();
        DateL.setText(toArabicNumbers(String.valueOf(year)));
        roundLbl.setText(getArabicMonth());
        String j18 = jLabel18.getText();
        jLabel18.setText(toArabicNumbers(j18));
        
     String j127=jLabel27.getText();
      jLabel27.setText(toArabicNumbers(j127));
      
      String j21=jLabel21.getText();
      jLabel21.setText(toArabicNumbers(j21));
     
      String j30=jLabel30.getText();
      jLabel30.setText(toArabicNumbers(j30));
     
      String j22=jLabel22.getText();
      jLabel22.setText(toArabicNumbers(j22));
     
      String j31=jLabel31.getText();
      jLabel31.setText(toArabicNumbers(j31));
     
    }
    
    private String selectedMonth = null;

private String chooseMonth() {

    String[] months = {
        "يناير", "فبراير", "مارس", "أبريل",
        "مايو", "يونيو", "يوليو", "أغسطس",
        "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    };

    String choice = (String) JOptionPane.showInputDialog(
            this,
            "اختار الشهر:",
            "اختيار الشهر",
            JOptionPane.QUESTION_MESSAGE,
            null,
            months,
            months[LocalDate.now().getMonthValue() - 1] // default
    );

    return choice;
}
    
    
private String wrapText(String text, int width) {
    return "<html><div style='width:" + width + "px; text-align:center; direction:rtl;'>"
            + text +
            "</div></html>";
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
    
public void loadStudentImage(String imagePath) {

    try {

        if (imagePath != null && !imagePath.isEmpty()) {

            File imgFile = new File(imagePath);

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
                System.out.println("❌ Image not found: " + imagePath);
            }

        } else {
            studentImageLbl.setIcon(null);
            System.out.println("❌ No image path provided");
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
  public void loadStudentInfo(String seatNo, Connection con) {

    PreparedStatement ps = null;
    ResultSet rs = null;

    try {

        String sql = "SELECT s.name, s.seat_no, s.national_id, s.coordination_no,"
                + " s.professional_group, s.profession, s.center_name, s.governorate,"
                + " s.image_path,"
                + " sp.name specialization,"

                // ✅ النسبة
                + " NVL(ROUND((SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100,2),0) percentage,"

                + " CASE"
                + " WHEN (SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100 >= 85 THEN 'ممتاز'"
                + " WHEN (SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100 >= 75 THEN 'جيد جداً'"
                + " WHEN (SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100 >= 65 THEN 'جيد'"
                + " WHEN (SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100 >= 50 THEN 'مقبول'"
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
                + " s.professional_group,s.profession,s.center_name,s.governorate,sp.name,s.image_path";

        ps = con.prepareStatement(sql);
        ps.setString(1, seatNo);
        rs = ps.executeQuery();

        if (rs.next()) {

            nameLbl.setText(rs.getString("name"));
            seatNoLbl.setText(toArabicNumbers(rs.getString("seat_no")));
            nationalIdLbl.setText(toArabicNumbers(rs.getString("national_id")));
            coordinationLbl.setText(toArabicNumbers(rs.getString("coordination_no")));

            groupLbl.setText(rs.getString("professional_group"));

            centerLbl.setText(rs.getString("center_name"));
            govLbl.setText(rs.getString("governorate"));

            // 🔥🔥 التعديل هنا
            double percent = rs.getDouble("percentage");

            // رقمين بعد العلامة
            String formattedPercent = String.format("%.2f", percent);

            // تحويل لعربي
            String percentArabic = toArabicNumbers(formattedPercent);

            percentLbl.setText(percentArabic + "٪");

            gradeLbl.setText(rs.getString("grade"));

            currentImagePath = rs.getString("image_path");

            String round = rs.getString("exam_round");
        }

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (ps != null) ps.close(); } catch (Exception e) {}
    }
}
    
 public void loadStudentSubjects(String seatNo, Connection con) {

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
WHERE s.seat_no = ? AND sub.id IS NOT NULL
""";

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

            // 🔥 أهم تعديل (منع الكسور)
            int mark = (int) rs.getDouble("obtained_mark");

            if (type == null) continue;

            type = type.trim().toLowerCase();

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

        // ================= المواد =================

        if (theorySubjects.size() > 0) {
            Object[] s = theorySubjects.get(0);
            sub1Lbl.setText(wrapText(s[0].toString(), 40));
            max1Lbl.setText("      "+toArabicNumbers(s[1].toString()));
            pass1Lbl.setText("      "+toArabicNumbers(s[2].toString()));
            mark1Lbl.setText("       "+toArabicNumbers(String.valueOf((int) s[3])));
        }

        if (theorySubjects.size() > 1) {
            Object[] s = theorySubjects.get(1);
            sub2Lbl.setText(wrapText(s[0].toString(), 30));
            max2Lbl.setText("     "+toArabicNumbers(s[1].toString()));
            pass2Lbl.setText("     "+toArabicNumbers(s[2].toString()));
            mark2Lbl.setText("      "+toArabicNumbers(String.valueOf((int) s[3])));
        }

        if (theorySubjects.size() > 2) {
            Object[] s = theorySubjects.get(2);
            sub3Lbl.setText(wrapText(s[0].toString(), 30));
            max3Lbl.setText("     "+toArabicNumbers(s[1].toString()));
            pass3Lbl.setText("     "+toArabicNumbers(s[2].toString()));
            mark3Lbl.setText("     "+toArabicNumbers(String.valueOf((int) s[3])));
        }

        if (theorySubjects.size() > 3) {
            Object[] s = theorySubjects.get(3);
            sub4Lbl.setText(wrapText(s[0].toString(), 30));
            max4Lbl.setText("      "+toArabicNumbers(s[1].toString()));
            pass4Lbl.setText("      "+toArabicNumbers(s[2].toString()));
            mark4Lbl.setText("       "+toArabicNumbers(String.valueOf((int) s[3])));
        }

        // ================= العملي =================

        if (practicalSubjects.size() > 0) {
            Object[] p = practicalSubjects.get(0);
            amalymax.setText("     "+toArabicNumbers(p[1].toString()));
            amalyPass.setText("     "+toArabicNumbers(p[2].toString()));
            studgra.setText("     "+toArabicNumbers(String.valueOf((int) p[3])));
        }

        // ================= التطبيقي =================

        if (appliedSubjects.size() > 0) {
            Object[] p = appliedSubjects.get(0);
            tatbecMax.setText("     "+toArabicNumbers(p[1].toString()));
            tatbecPass.setText("     "+toArabicNumbers(p[2].toString()));
            studeTa.setText("     "+toArabicNumbers(String.valueOf((int) p[3])));
        }

        // ================= المجاميع =================

        int finalTotal = theoryTotal + practicalTotal + appliedTotal;
        int finalTa = practicalTotal + appliedTotal;

        String finalTotalArabic = numberToArabicWords(finalTotal) + " درجة فقط لا غير";
        jLabel41.setText("     "+finalTotalArabic);

        ee.setText("     "+toArabicNumbers(String.valueOf(theoryTotal)));
        practicalTotalLbl.setText("     "+toArabicNumbers(String.valueOf(finalTa)));
        eed.setText("       "+toArabicNumbers(String.valueOf(finalTotal)));

    } catch (Exception e) {
        e.printStackTrace();
    }
}
 
 
 
public void printSuccessForms(List<String[]> studentsData, java.util.function.BiConsumer<Integer, Integer> progressCallback) {

     // 👇 اختار الشهر الأول
    selectedMonth = chooseMonth();

    if (selectedMonth == null) {
        JOptionPane.showMessageDialog(this, "لم يتم اختيار الشهر");
        return;
    }

    // 👇 حط الشهر بدل الحالي
    roundLbl.setText(selectedMonth);

    // باقي الكود زي ما هو...
    
    
    Connection con = null;

    try {

        con = DatabaseConnection.getConnection();

        File mainFolder = new File("تقارير");
        if (!mainFolder.exists()) mainFolder.mkdir();

        File formFolder = new File(mainFolder, "استمارة نجاح");
        if (!formFolder.exists()) formFolder.mkdir();

        String allPath = formFolder.getAbsolutePath() + "/all_success_forms.pdf";

        Document allDoc = new Document(PageSize.A4);
        PdfWriter.getInstance(allDoc, new FileOutputStream(allPath));

        allDoc.open();

        int width = jPanel1.getWidth();
int height = 1300;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        jPanel1.setSize(width, height);
        jPanel1.setDoubleBuffered(false);

        // 🔥 اللوب الجديد
        int total = studentsData.size();
        for (int i = 0; i < total; i++) {
            String[] student = studentsData.get(i);
            if (progressCallback != null) {
                progressCallback.accept(i + 1, total);
            }

            String seatNo = student[0];
            String profession = student[1];

            // 👇 تنظيف البيانات القديمة
            clearLabels();

            // 👇 حط المهنة من الجدول
            specLbl.setText(profession);

            // تحميل باقي البيانات من DB
            loadStudentInfo(seatNo, con);
            loadStudentSubjects(seatNo, con);
            loadStudentImage(currentImagePath);

            // تنظيف الصورة
            g2.clearRect(0, 0, width, height);

            // رسم
            jPanel1.printAll(g2);

            Image img = Image.getInstance(image, null);

            img.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            img.setAbsolutePosition(0, 0);

            allDoc.add(img);

            if (i < total - 1) {
                allDoc.newPage();
            }
        }

        g2.dispose();
        allDoc.close();

        Desktop.getDesktop().open(new File(allPath));

        javax.swing.SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "تم إنشاء كل الاستمارات");
        });

    } catch (Exception e) {
        e.printStackTrace();

    } finally {
        try { if (con != null) con.close(); } catch (Exception e) {}
    }
}

private String getArabicMonth() {

    String[] months = {
        "يناير", "فبراير", "مارس", "أبريل",
        "مايو", "يونيو", "يوليو", "أغسطس",
        "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    };

    int monthIndex = LocalDate.now().getMonthValue() - 1;

    return months[monthIndex];
}

/**
 * تنظيف العناصر من البيانات القديمة
 */
private void clearLabels() {
    nameLbl.setText("");
    seatNoLbl.setText("");
    nationalIdLbl.setText("");
    coordinationLbl.setText("");
    groupLbl.setText("");
    centerLbl.setText("");
    govLbl.setText("");
    percentLbl.setText("");
    gradeLbl.setText("");
    
    // تنظيف المواد النظرية
    sub1Lbl.setText("");
    sub2Lbl.setText("");
    sub3Lbl.setText("");
    sub4Lbl.setText("");
    mark1Lbl.setText("");
    mark2Lbl.setText("");
    mark3Lbl.setText("");
    mark4Lbl.setText("");
    
    // تنظيف العملي والتطبيقي
    studgra.setText("");
    studeTa.setText("");
    practicalTotalLbl.setText("");
    eed.setText("");
    ee.setText("");
    jLabel41.setText("");
    
    studentImageLbl.setIcon(null);
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
        DateL = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);
        jPanel1.add(studentImageLbl);
        studentImageLbl.setBounds(0, 0, 250, 270);

        seatNoLbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(seatNoLbl);
        seatNoLbl.setBounds(537, 290, 130, 20);

        coordinationLbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(coordinationLbl);
        coordinationLbl.setBounds(537, 320, 130, 20);

        nationalIdLbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(nationalIdLbl);
        nationalIdLbl.setBounds(527, 350, 140, 20);

        groupLbl.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        groupLbl.setForeground(new java.awt.Color(61, 59, 110));
        jPanel1.add(groupLbl);
        groupLbl.setBounds(67, 300, 190, 30);

        roundLbl.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        roundLbl.setForeground(new java.awt.Color(61, 59, 110));
        jPanel1.add(roundLbl);
        roundLbl.setBounds(250, 330, 70, 20);

        centerLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        centerLbl.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel1.add(centerLbl);
        centerLbl.setBounds(560, 440, 120, 20);

        govLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel1.add(govLbl);
        govLbl.setBounds(187, 430, 160, 30);

        sub1Lbl.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        jPanel1.add(sub1Lbl);
        sub1Lbl.setBounds(530, 530, 50, 50);

        sub2Lbl.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        jPanel1.add(sub2Lbl);
        sub2Lbl.setBounds(480, 530, 40, 50);

        sub3Lbl.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        jPanel1.add(sub3Lbl);
        sub3Lbl.setBounds(430, 530, 40, 50);

        sub4Lbl.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        jPanel1.add(sub4Lbl);
        sub4Lbl.setBounds(380, 530, 40, 50);

        max1Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        max1Lbl.setText("     100");
        jPanel1.add(max1Lbl);
        max1Lbl.setBounds(530, 580, 50, 40);

        max2Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        max2Lbl.setText("100");
        jPanel1.add(max2Lbl);
        max2Lbl.setBounds(480, 580, 50, 40);

        max3Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        max3Lbl.setText("100");
        jPanel1.add(max3Lbl);
        max3Lbl.setBounds(430, 580, 50, 40);

        max4Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        max4Lbl.setText("100");
        max4Lbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel1.add(max4Lbl);
        max4Lbl.setBounds(370, 580, 60, 40);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("     300");
        jPanel1.add(jLabel18);
        jLabel18.setBounds(323, 580, 50, 40);

        amalymax.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(amalymax);
        amalymax.setBounds(270, 580, 50, 40);

        tatbecMax.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(tatbecMax);
        tatbecMax.setBounds(210, 580, 60, 40);

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("    300");
        jPanel1.add(jLabel21);
        jLabel21.setBounds(160, 580, 50, 40);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setText("      600");
        jPanel1.add(jLabel22);
        jLabel22.setBounds(100, 580, 60, 40);

        pass1Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        pass1Lbl.setText("     100");
        jPanel1.add(pass1Lbl);
        pass1Lbl.setBounds(530, 620, 50, 40);

        pass2Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        pass2Lbl.setText("100");
        jPanel1.add(pass2Lbl);
        pass2Lbl.setBounds(480, 620, 50, 40);

        pass3Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        pass3Lbl.setText("100");
        jPanel1.add(pass3Lbl);
        pass3Lbl.setBounds(430, 620, 50, 40);

        pass4Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        pass4Lbl.setText("100");
        jPanel1.add(pass4Lbl);
        pass4Lbl.setBounds(373, 620, 50, 40);

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel27.setText("      150");
        jPanel1.add(jLabel27);
        jLabel27.setBounds(320, 620, 50, 40);

        amalyPass.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(amalyPass);
        amalyPass.setBounds(270, 620, 50, 40);

        tatbecPass.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(tatbecPass);
        tatbecPass.setBounds(210, 620, 60, 40);

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel30.setText("    170");
        jPanel1.add(jLabel30);
        jLabel30.setBounds(160, 620, 50, 40);

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel31.setText("      320");
        jPanel1.add(jLabel31);
        jLabel31.setBounds(100, 620, 60, 40);

        mark1Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        mark1Lbl.setText("      100");
        jPanel1.add(mark1Lbl);
        mark1Lbl.setBounds(530, 660, 60, 80);

        mark2Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        mark2Lbl.setText("100");
        jPanel1.add(mark2Lbl);
        mark2Lbl.setBounds(480, 660, 50, 80);

        mark3Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        mark3Lbl.setText("100");
        jPanel1.add(mark3Lbl);
        mark3Lbl.setBounds(430, 660, 50, 80);

        mark4Lbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        mark4Lbl.setText("100");
        jPanel1.add(mark4Lbl);
        mark4Lbl.setBounds(370, 660, 60, 80);

        ee.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ee.setText("100");
        jPanel1.add(ee);
        ee.setBounds(325, 660, 50, 80);

        studgra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(studgra);
        studgra.setBounds(270, 660, 50, 80);

        studeTa.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jPanel1.add(studeTa);
        studeTa.setBounds(210, 660, 60, 80);

        practicalTotalLbl.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        practicalTotalLbl.setText("100");
        jPanel1.add(practicalTotalLbl);
        practicalTotalLbl.setBounds(160, 660, 50, 80);

        eed.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        eed.setText("100");
        jPanel1.add(eed);
        eed.setBounds(100, 666, 60, 70);

        nameLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel1.add(nameLbl);
        nameLbl.setBounds(473, 380, 150, 20);

        specLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel1.add(specLbl);
        specLbl.setBounds(523, 410, 150, 20);

        jLabel41.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel1.add(jLabel41);
        jLabel41.setBounds(310, 760, 210, 30);

        gradeLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jPanel1.add(gradeLbl);
        gradeLbl.setBounds(510, 800, 140, 40);

        percentLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        percentLbl.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(percentLbl);
        percentLbl.setBounds(263, 800, 130, 30);

        DateL.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        DateL.setForeground(new java.awt.Color(61, 59, 110));
        DateL.setText("jLabel1");
        jPanel1.add(DateL);
        DateL.setBounds(137, 330, 70, 20);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/images-removebg-preview (2).png"))); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(200, 200));
        jPanel1.add(jLabel1);
        jLabel1.setBounds(540, 30, 150, 150);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(61, 59, 110));
        jLabel3.setText("وزارة الصناعة ");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(560, 180, 120, 25);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(61, 59, 110));
        jLabel4.setText("مصلحة الكفاية الإنتاجية والتدريب المهنى ");
        jPanel1.add(jLabel4);
        jLabel4.setBounds(460, 210, 306, 25);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1222, Short.MAX_VALUE)
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
    private javax.swing.JLabel DateL;
    private javax.swing.JLabel amalyPass;
    private javax.swing.JLabel amalymax;
    private javax.swing.JLabel centerLbl;
    private javax.swing.JLabel coordinationLbl;
    private javax.swing.JLabel ee;
    private javax.swing.JLabel eed;
    private javax.swing.JLabel govLbl;
    private javax.swing.JLabel gradeLbl;
    private javax.swing.JLabel groupLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
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
    public javax.swing.JLabel percentLbl;
    private javax.swing.JLabel practicalTotalLbl;
    private javax.swing.JLabel roundLbl;
    private javax.swing.JLabel seatNoLbl;
    public javax.swing.JLabel specLbl;
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
