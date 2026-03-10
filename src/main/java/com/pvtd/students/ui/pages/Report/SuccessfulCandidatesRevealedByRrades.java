
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;
import java.awt.Desktop;
import java.awt.Graphics2D;
//import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;


import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;


/**
 *
 * @author Seif
 */
public class SuccessfulCandidatesRevealedByRrades extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SuccessfulCandidatesRevealedByRrades.class.getName());

    
    public SuccessfulCandidatesRevealedByRrades() {
        initComponents();
        int lastColumn = jTable1.getColumnCount() - 1;

Object totalValue = jTable1.getValueAt(0, lastColumn);

String total = java.util.Objects.toString(totalValue, "");
int total2 = Integer.parseInt(totalValue.toString());

String totalWords = numberToArabicWords(total2);

jLabelTotalWords.setText(totalWords + " درجة فقط لا غير");
    }

    public static String numberToArabicWords(int number){

    String[] ones = {
        "", "واحد", "اثنان", "ثلاثة", "أربعة",
        "خمسة", "ستة", "سبعة", "ثمانية", "تسعة"
    };

    String[] tens = {
        "", "عشرة", "عشرون", "ثلاثون",
        "أربعون", "خمسون", "ستون",
        "سبعون", "ثمانون", "تسعون"
    };

    String[] hundreds = {
        "", "مائة", "مائتان", "ثلاثمائة",
        "أربعمائة", "خمسمائة", "ستمائة",
        "سبعمائة", "ثمانمائة", "تسعمائة"
    };

    int h = number / 100;
    int t = (number % 100) / 10;
    int o = number % 10;

    String result = "";

    if(h > 0){
        result += hundreds[h] + " ";
    }

    if(t > 1){
        result += ones[o] + " و " + tens[t];
    }
    else if(t == 1){
        result += "عشرة";
    }
    else{
        result += ones[o];
    }

    return result.trim();
}
    
    
   public void loadStudentData(String seatNo){

    new Thread(() -> {

        try {

            Connection con = DatabaseConnection.getConnection();

            String sql =
            "SELECT s.name , s.national_id , s.seat_no , s.center_name , s.region , " +
            "sp.name specialization , g.name group_name , s.image_path " +
            "FROM students s " +
            "LEFT JOIN specializations sp ON s.specialization_id = sp.id " +
            "LEFT JOIN groups g ON s.group_id = g.id " +
            "WHERE s.seat_no = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, seatNo);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                String name = rs.getString("name");
                String national = rs.getString("national_id");
                String seat = rs.getString("seat_no");
                String center = rs.getString("center_name");
                String region = rs.getString("region");
                String spec = rs.getString("specialization");
                String group = rs.getString("group_name");
                String imgPath = rs.getString("image_path");

                SwingUtilities.invokeLater(() -> {

                    this.name.setText(name);
                    jLabelSeatNo.setText(seat);
                    jLabelNationalID.setText(national);
                    jLabelSpecialization.setText(spec);
                    jLabelGroup.setText(group);
                    jLabelCenter.setText(center);
                    jLabelRegion.setText(region);

                    if(imgPath != null){

                        ImageIcon icon = new ImageIcon(imgPath);

                        java.awt.Image img = icon.getImage().getScaledInstance(
        jLabelPhoto.getWidth(),
        jLabelPhoto.getHeight(),
        java.awt.Image.SCALE_SMOOTH
);

                        jLabelPhoto.setIcon(new ImageIcon(img));
                    }

                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }).start();
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

            String nationalId = jLabelNationalID.getText();

            // إنشاء صورة من الشهادة
            BufferedImage image = new BufferedImage(
                    jPanel1.getWidth(),
                    jPanel1.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2 = image.createGraphics();
            jPanel1.paint(g2);
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
   
   
   public void loadGradesTable(String seatNo){

    new Thread(() -> {

        try {

            Connection con = DatabaseConnection.getConnection();

            DefaultTableModel model = new DefaultTableModel();

            String sql =
            "SELECT sub.name , sub.max_mark , g.obtained_mark , sub.type " +
            "FROM students st " +
            "JOIN subjects sub ON sub.specialization_id = st.specialization_id " +
            "LEFT JOIN student_grades g ON g.subject_id = sub.id AND g.student_id = st.id " +
            "WHERE st.seat_no = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, seatNo);

            ResultSet rs = ps.executeQuery();

            Vector<String> columns = new Vector<>();
            Vector<Object> row = new Vector<>();

            columns.add("الدرجات");

            int theoryTotal = 0;
            int practicalTotal = 0;
            int appliedTotal = 0;

            while(rs.next()){

                String subject = rs.getString("name");
                int mark = rs.getInt("obtained_mark");
                int max = rs.getInt("max_mark");
                String type = rs.getString("type");

                columns.add(subject + " / " + max);

                row.add(mark);

                if("theory".equals(type)){
                    theoryTotal += mark;
                }

                if("practical".equals(type)){
                    practicalTotal += mark;
                }

                if("applied".equals(type)){
                    appliedTotal += mark;
                }

            }

            columns.add("مجموع النظري");
            columns.add("درجات العملي");
            columns.add("درجات التطبيقي");
            columns.add("مجموع العملي والتطبيقي");
            columns.add("المجموع الكلي");

            row.add(theoryTotal);
            row.add(practicalTotal);
            row.add(appliedTotal);
            row.add(practicalTotal + appliedTotal);
            row.add(theoryTotal + practicalTotal + appliedTotal);

            model.setColumnIdentifiers(columns);
            model.addRow(row);

            SwingUtilities.invokeLater(() -> {

                jTable1.setModel(model);
                jTable1.setRowHeight(35);

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }).start();
}
   
   
   
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabelPhoto = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabelGroup = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabelSeatNo = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabelNationalID = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        name = new javax.swing.JLabel();
        jLabelSpecialization = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        ll = new javax.swing.JLabel();
        jLabelCenter = new javax.swing.JLabel();
        jLabelRegion = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabelTotalWords = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);

        jLabelPhoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sief.jpeg"))); // NOI18N
        jLabelPhoto.setText("jLabel1");
        jPanel1.add(jLabelPhoto);
        jLabelPhoto.setBounds(31, 16, 185, 220);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("المجموعة المهنية :");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(280, 280, 120, 20);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("دور أغسطس لسنة 2021");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(150, 320, 150, 20);

        jLabelGroup.setText("jLabel4");
        jPanel1.add(jLabelGroup);
        jLabelGroup.setBounds(117, 280, 160, 30);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("الاداره العامه للاختبارات النمطيه");
        jPanel1.add(jLabel4);
        jLabel4.setBounds(570, 210, 200, 20);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("إستمارة دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel5);
        jLabel5.setBounds(200, 250, 190, 20);

        jLabel6.setText("رقم القومى :");
        jPanel1.add(jLabel6);
        jLabel6.setBounds(720, 320, 70, 16);

        jLabel7.setText("رقم الجلوس :");
        jPanel1.add(jLabel7);
        jLabel7.setBounds(720, 260, 70, 16);

        jLabel8.setText("كود التنسيق :");
        jPanel1.add(jLabel8);
        jLabel8.setBounds(720, 290, 70, 16);

        jLabelSeatNo.setText("jLabel9");
        jPanel1.add(jLabelSeatNo);
        jLabelSeatNo.setBounds(580, 260, 130, 20);

        jLabel10.setText("jLabel10");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(573, 290, 140, 16);

        jLabelNationalID.setText("jLabel11");
        jPanel1.add(jLabelNationalID);
        jLabelNationalID.setBounds(583, 320, 130, 16);

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(570, 340, 220, 10);

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator2);
        jSeparator2.setBounds(570, 250, 220, 10);

        jSeparator3.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setToolTipText("");
        jSeparator3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel1.add(jSeparator3);
        jSeparator3.setBounds(790, 250, 10, 90);

        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));
        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setToolTipText("");
        jSeparator4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jPanel1.add(jSeparator4);
        jSeparator4.setBounds(570, 250, 10, 90);

        jSeparator5.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator5);
        jSeparator5.setBounds(570, 280, 220, 10);

        jSeparator6.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator6);
        jSeparator6.setBounds(570, 310, 220, 10);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("اسم الطالب ولقبه : ");
        jPanel1.add(jLabel12);
        jLabel12.setBounds(670, 360, 120, 20);

        name.setText("jLabel13");
        jPanel1.add(name);
        name.setBounds(513, 360, 150, 20);

        jLabelSpecialization.setText("jLabel13");
        jPanel1.add(jLabelSpecialization);
        jLabelSpecialization.setBounds(560, 390, 150, 20);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("التخصص: ");
        jPanel1.add(jLabel14);
        jLabel14.setBounds(720, 390, 70, 20);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("المنطقة /");
        jPanel1.add(jLabel15);
        jLabel15.setBounds(340, 440, 60, 30);

        ll.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        ll.setText("المركز/");
        jPanel1.add(ll);
        ll.setBounds(690, 440, 50, 20);

        jLabelCenter.setText("jLabel1");
        jPanel1.add(jLabelCenter);
        jLabelCenter.setBounds(577, 440, 110, 16);

        jLabelRegion.setText("jLabel9");
        jPanel1.add(jLabelRegion);
        jLabelRegion.setBounds(187, 446, 150, 20);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(2, 510, 800, 270);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("مجموع درجات الطالب بالحروف :");
        jPanel1.add(jLabel1);
        jLabel1.setBounds(537, 810, 250, 25);

        jLabelTotalWords.setText("jLabel9");
        jPanel1.add(jLabelTotalWords);
        jLabelTotalWords.setBounds(187, 810, 350, 30);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("النسبه المئويه ");
        jPanel1.add(jLabel11);
        jLabel11.setBounds(400, 900, 110, 16);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("التقدير/ ");
        jPanel1.add(jLabel13);
        jLabel13.setBounds(730, 900, 60, 20);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("كتبه /");
        jPanel1.add(jLabel16);
        jLabel16.setBounds(730, 960, 50, 20);

        jLabel17.setText("--------------------");
        jPanel1.add(jLabel17);
        jLabel17.setBounds(593, 960, 140, 16);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("املاة");
        jPanel1.add(jLabel18);
        jLabel18.setBounds(520, 960, 50, 20);

        jLabel19.setText("--------------------");
        jPanel1.add(jLabel19);
        jLabel19.setBounds(380, 960, 140, 16);

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("رئيس لجنة النظام والمراقبة ");
        jPanel1.add(jLabel20);
        jLabel20.setBounds(10, 990, 170, 20);

        jLabel21.setText("--------------------");
        jPanel1.add(jLabel21);
        jLabel21.setBounds(50, 1020, 100, 16);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("راجعة");
        jPanel1.add(jLabel22);
        jLabel22.setBounds(290, 960, 50, 20);

        jLabel23.setText("--------------------");
        jPanel1.add(jLabel23);
        jLabel23.setBounds(150, 960, 140, 16);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(51, 0, 204));
        jLabel24.setText("ـــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــــ");
        jPanel1.add(jLabel24);
        jLabel24.setBounds(20, 1030, 760, 25);

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setText("شئون الطلبة");
        jPanel1.add(jLabel25);
        jLabel25.setBounds(680, 1080, 70, 16);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel26.setText("------------------ ");
        jPanel1.add(jLabel26);
        jLabel26.setBounds(670, 1100, 90, 16);

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel27.setText("إدارة التسجيل");
        jPanel1.add(jLabel27);
        jLabel27.setBounds(500, 1080, 70, 16);

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel28.setText("------------------ ");
        jPanel1.add(jLabel28);
        jLabel28.setBounds(490, 1100, 90, 16);

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel29.setText("مدير المركز");
        jPanel1.add(jLabel29);
        jLabel29.setBounds(310, 1080, 70, 16);

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel30.setText("------------------ ");
        jPanel1.add(jLabel30);
        jLabel30.setBounds(300, 1100, 90, 16);

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel31.setText("خاتم الشعار");
        jPanel1.add(jLabel31);
        jLabel31.setBounds(100, 1060, 70, 16);

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel32.setText("روجعت البيانات بمعرفة :");
        jPanel1.add(jLabel32);
        jLabel32.setBounds(650, 1050, 130, 16);

        jLabel33.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel33.setText("الوزارى لوزارة التربية والتعليم رقم ٩٢ الصادر فى ١٧/٦/١٩٦٨ وتم تعديله بالقرار رقم ٥٧ لسنة");
        jPanel1.add(jLabel33);
        jLabel33.setBounds(250, 1150, 550, 20);

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel34.setText("وهى معادله لشهادة دبلوم المدارس الصناعية ومناظرة لها بجمهورية مصر العربية وذلك طبقا للقرار ");
        jPanel1.add(jLabel34);
        jLabel34.setBounds(200, 1130, 600, 20);

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel35.setText("١٩٦٩");
        jPanel1.add(jLabel35);
        jLabel35.setBounds(750, 1170, 40, 20);

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel36.setText("-أي كشط أو تعديل في هذا األخطار يعتبر الغيا . ");
        jPanel1.add(jLabel36);
        jLabel36.setBounds(480, 1250, 290, 20);

        jLabel37.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel37.setText("تنبيه هام : هذا إخطار بنجاح التلميذ متى كان مصدقا عليه");
        jPanel1.add(jLabel37);
        jLabel37.setBounds(453, 1210, 340, 20);

        jLabel38.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel38.setText("ومختوم بختم شعار الجمهوريه.");
        jPanel1.add(jLabel38);
        jLabel38.setBounds(540, 1230, 190, 20);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
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
        java.awt.EventQueue.invokeLater(() -> new SuccessfulCandidatesRevealedByRrades().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelCenter;
    private javax.swing.JLabel jLabelGroup;
    private javax.swing.JLabel jLabelNationalID;
    private javax.swing.JLabel jLabelPhoto;
    private javax.swing.JLabel jLabelRegion;
    private javax.swing.JLabel jLabelSeatNo;
    private javax.swing.JLabel jLabelSpecialization;
    private javax.swing.JLabel jLabelTotalWords;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel ll;
    private javax.swing.JLabel name;
    // End of variables declaration//GEN-END:variables
}
