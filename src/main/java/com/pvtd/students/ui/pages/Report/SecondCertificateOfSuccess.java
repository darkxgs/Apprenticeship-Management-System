/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pvtd.students.ui.pages.Report;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.swing.SwingUtilities;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;

/**
 *
 * @author siefr
 */
public class SecondCertificateOfSuccess extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SecondCertificateOfSuccess.class.getName());

    /**
     * Creates new form SecondCertificateOfSuccess
     */
    /** أبعاد صفحة الشهادة الثابتة */
    private static final int PAGE_W = 934;
    private static final int PAGE_H = 686;
    /** الحافة اليمنى لكل السطور الرئيسية */
    private static final int RIGHT_EDGE = 894;
    /** الحافة اليمنى لعمود (مركز / تخصص / بتقدير) — قريبة من نصوص اليمين */
    private static final int LEFT_COL_RIGHT_EDGE = 420;

    public SecondCertificateOfSuccess() {
        initComponents();
        applyExactLayout();
    }

    private String receiptText = "";
    private String groupText = "";

    /**
     * حقلا الإدخال الوحيدان في الشهادة (طلب إدارة الامتحانات):
     * رقم القسيمة الحكومية + المجموعة — يُبنى بهما سطر الرسوم كاملاً.
     */
    public void setCustomFields(String receiptNo, String groupNo) {
        this.receiptText = receiptNo != null ? receiptNo.trim() : "";
        this.groupText   = groupNo   != null ? groupNo.trim()   : "";
        // أرقام إنجليزية كما في النموذج الرسمي
        jLabel20.setText(
                "وقد سدد الرسوم وقدرها ستون جنيها لاغير بموجب قسيمة رقم 33 ع .ج الحكومية رقم / "
                + this.receiptText + "   مجموعة " + this.groupText);
        applyExactLayout();
    }

    /** يجبر التاريخ على الظهور بترتيب كتابته يوم/شهر/سنة دون قلب من الـ bidi */
    private String ltrText(String s) {
        if (s == null || s.isEmpty()) return "";
        return "‭" + s + "‬";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // الرص الثابت طبقاً لنموذج شهادة إدارة الامتحانات الرسمي:
    //   كل بيانات اليمين تبدأ من حافة يمنى ثابتة، وعمود (مركز/تخصص/بتقدير)
    //   على يسار الصفحة كلٌ على نفس سطر نظيره الأيمن
    // ═══════════════════════════════════════════════════════════════════════

    /** يضع الـ label بحيث تكون حافته اليمنى عند rightX (نمو النص لليسار) */
    private void placeRight(javax.swing.JLabel l, int rightX, int y) {
        java.awt.FontMetrics fm = l.getFontMetrics(l.getFont());
        int w = fm.stringWidth(l.getText() == null ? "" : l.getText()) + 8;
        int h = fm.getHeight() + 2;
        l.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        l.setBounds(rightX - w, y, w, h);
    }

    /** يضع الـ label ممركزاً حول centerX */
    private void placeCenter(javax.swing.JLabel l, int centerX, int y) {
        java.awt.FontMetrics fm = l.getFontMetrics(l.getFont());
        int w = fm.stringWidth(l.getText() == null ? "" : l.getText()) + 8;
        int h = fm.getHeight() + 2;
        l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        l.setBounds(centerX - w / 2, y, w, h);
    }

    private void applyExactLayout() {

        // أحجام الخطوط (طلب إدارة الامتحانات): عنوان أكبر + متن أكبر درجة
        java.awt.Font titleFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24);
        java.awt.Font bodyFont  = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16);
        java.awt.Font longFont  = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15);

        jLabel4.setFont(titleFont);
        jLabel5.setFont(titleFont);
        javax.swing.JLabel[] bodyLbls = {
            jLabel6, الاسم, jLabel8, الرقم_القومي, jLabel10, المركز,
            jLabel12, تخصص, jLabel14, تقدير, jLabel16, jLabel20,
            jLabel24, jLabel25, jLabel26, jLabel27, jLabel28, jLabel29,
            jLabel30, jLabel31
        };
        for (javax.swing.JLabel l : bodyLbls) l.setFont(bodyFont);
        jLabel21.setFont(longFont);
        jLabel22.setFont(longFont);
        jLabel23.setFont(longFont);

        // نصوص ثابتة مصححة طبقاً للنموذج الرسمي — بأرقام إنجليزية مثله تماماً
        jLabel4.setText("شهـــــادة");
        jLabel12.setText("تخصص /");
        jLabel16.setText("دور / مايو عام 2026 الميلادية / الفان وسته وعشرون ،،،");
        jLabel21.setText("وهي معادلة لشهادة دبلوم المدارس الصناعية بوزارة التربية والتعليم بجمهورية مصر العربية وذلك طبقا للقرار الوزاري للتربية و التعليم");
        // النقطة الأخيرة مفصولة عن «م» بمسافة حتى لا تلتصق بها
        jLabel22.setText("رقم ( 92 ) الصادر في " + ltrText("17/ 6/ 1968") + " وتم تعديله بالقرار رقم 57 لسنه 1969 م .");
        jLabel23.setText("مصلحة الكفاية الانتاجية و التدريب المهني حاصلة علي نظام ادارة الجودة ISO 9001");
        jLabel24.setText("عضو الامتحانات المسؤول");

        // «تحريراً في» بتاريخ اليوم — يُقرأ من اليمين يوم/شهر/سنة (١١ ثم ٨ ثم ٢٠٢٦)
        java.time.LocalDate today = java.time.LocalDate.now();
        String dateStr = today.getDayOfMonth() + " / " + today.getMonthValue() + " / " + today.getYear();
        jLabel30.setText("تحريراً في  " + "‫" + dateStr + "‬");

        // سطر «وقد منحت له هذه الشهادة... لتقديمها الي» أُلغي طبقاً للنموذج
        jLabel17.setVisible(false);
        jLabel19.setVisible(false);
        المركز1.setVisible(false);

        jPanel1.setLayout(null);
        jPanel1.setPreferredSize(new Dimension(PAGE_W, PAGE_H));

        // ── ترويسة يمين ──────────────────────────────────────────────────
        placeRight(jLabel1, RIGHT_EDGE - 60, 26);
        placeRight(jLabel2, RIGHT_EDGE - 8, 44);
        placeRight(jLabel3, RIGHT_EDGE - 28, 62);
        jSeparator1.setBounds(RIGHT_EDGE - 200, 82, 175, 4);

        // ── الشعار أعلى الشمال ───────────────────────────────────────────
        Dimension logoSize = المركز2.getPreferredSize();
        المركز2.setBounds(60, 24, logoSize.width, logoSize.height);

        // ── العنوان في المنتصف ───────────────────────────────────────────
        placeCenter(jLabel4, PAGE_W / 2, 112);
        placeCenter(jLabel5, PAGE_W / 2, 138);

        // ── سطور البيانات ────────────────────────────────────────────────
        int y1 = 200, y2 = 234, y3 = 268, y4 = 302, lh = 0;

        // السطر 1: تشهد وزارة التجارة والصناعة بان السيد / [الاسم]
        placeRight(jLabel6, RIGHT_EDGE, y1);
        placeRight(الاسم, jLabel6.getX() - 4, y1);

        // السطر 2: الرقم القومي / [الرقم]   |   مركز / "[المركز]"
        placeRight(jLabel8, RIGHT_EDGE, y2);
        placeRight(الرقم_القومي, jLabel8.getX() - 4, y2);
        placeRight(jLabel10, LEFT_COL_RIGHT_EDGE, y2);
        placeRight(المركز, jLabel10.getX() - 4, y2);

        // السطر 3: قد نجح في امتحان... |   تخصص / "[التخصص]"
        placeRight(jLabel18, RIGHT_EDGE, y3);
        placeRight(jLabel12, LEFT_COL_RIGHT_EDGE, y3);
        placeRight(تخصص, jLabel12.getX() - 4, y3);

        // السطر 4: دور / ... الميلادية / ... |   بتقدير / "[التقدير]"
        placeRight(jLabel16, RIGHT_EDGE, y4);
        placeRight(jLabel14, LEFT_COL_RIGHT_EDGE, y4);
        placeRight(تقدير, jLabel14.getX() - 4, y4);

        // ── سطر الرسوم ثم المعادلة ثم القرار ثم الأيزو ───────────────────
        placeRight(jLabel20, RIGHT_EDGE, 344);
        placeRight(jLabel21, RIGHT_EDGE, 380);
        placeRight(jLabel22, RIGHT_EDGE, 410);
        placeRight(jLabel23, RIGHT_EDGE, 440);

        // ── التوقيعات ───────────────────────────────────────────────────
        placeRight(jLabel24, RIGHT_EDGE, 482);              // عضو الامتحانات المسؤول
        placeCenter(jLabel25, RIGHT_EDGE - 90, 510);        // نقاط عضو الامتحانات
        placeCenter(jLabel26, PAGE_W / 2, 482);             // المراجع المسؤول
        placeCenter(jLabel27, PAGE_W / 2, 510);             // نقاط المراجع
        placeCenter(jLabel28, 190, 490);                    // يعتمد ،
        placeCenter(jLabel29, 190, 514);                    // مدير الادارة

        // ── أسفل الشهادة ────────────────────────────────────────────────
        placeRight(jLabel30, RIGHT_EDGE, 592);              // تحريراً في [التاريخ]
        placeCenter(jLabel31, PAGE_W / 2, 592);             // خاتم شعار الجمهورية

        jPanel1.revalidate();
        jPanel1.repaint();
    }

    /**
     * جلب بيانات الطالب من قاعدة البيانات بنفس آلية شهادة النجاح العادية.
     */
    public void loadStudentData(String seatNo) {

        String sql = "SELECT s.id, s.name, s.national_id, s.center_name, "
                + "s.profession AS specialization, "
                + "s.professional_group, s.region, s.phone_number "
                + "FROM students s "
                + "WHERE TRIM(s.seat_no) = TRIM(?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, seatNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                double percentage = getStudentPercentage(seatNo);

                // ملء حقول الشهادة — القيم بين علامتي تنصيص كما في النموذج الرسمي
                الاسم.setText(rs.getString("name"));
                // الرقم القومي بأرقام عربية ليكون مقروءاً
                String nid = rs.getString("national_id");
                الرقم_القومي.setText(toArabicNumerals(nid != null ? nid.trim() : ""));
                String centerFromDB = rs.getString("center_name");
                المركز.setText("\" " + (centerFromDB != null ? centerFromDB : "") + " \"");

                String specializationFromDB = rs.getString("specialization");
                if (specializationFromDB != null) {
                    تخصص.setText("\" " + specializationFromDB + " \"");
                }

                // تقدير: نص التقدير بناءً على النسبة المئوية (كما في sucsseccFromPage)
                String gradeText;
                if (percentage >= 85) {
                    gradeText = "ممتاز";
                } else if (percentage >= 75) {
                    gradeText = "جيد جداً";
                } else if (percentage >= 65) {
                    gradeText = "جيد";
                } else if (percentage >= 50) {
                    gradeText = "مقبول";
                } else {
                    gradeText = "ناجح";
                }
                تقدير.setText("\" " + gradeText + " \"");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * حساب النسبة المئوية للطالب بنفس استعلام شهادة النجاح العادية.
     */
    public double getStudentPercentage(String seatNo) {

        double percentage = 0;

        // النسبة = المجموع ÷ مجموع النهايات العظمى لكل مواد مهنة الطالب
        // (نفس طريقة استمارة إدارة الامتحانات — وليس فقط المواد التي لها درجات مسجلة)
        String sql = "SELECT NVL(ROUND((SUM(NVL(sg.obtained_mark,0)) / NULLIF(SUM(NVL(sub.max_mark,0)),0)) * 100,2),0) AS percentage "
                   + "FROM subjects sub "
                   + "CROSS JOIN students s "
                   + "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id "
                   + "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession)";

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

    /**
     * تحويل الأرقام الإنجليزية (0-9) إلى أرقام عربية (٠-٩).
     */
    private String toArabicNumerals(String input) {
        if (input == null) return "";
        return input
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

    /**
     * إنشاء ملفات PDF لشهادات إدارة الامتحانات.
     * الهيكل: التقارير/شهادة إدارة الامتحانات/{اسم المركز}/{رقم قومي}.pdf
     */
    public void printCertificates(List<Student> students,
            java.util.function.BiConsumer<Integer, Integer> progressCallback) {

        try {
            // ─── مجلد الجذر ────────────────────────────────────────────────
            File certFolder = new File("التقارير" + File.separator + "شهادة إدارة الامتحانات");
            if (!certFolder.exists()) certFolder.mkdirs();

            com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(934, 686);
            int total = students.size();

            for (int i = 0; i < total; i++) {
                Student s = students.get(i);
                if (progressCallback != null) progressCallback.accept(i + 1, total);

                // ─── تحميل بيانات الطالب ثم إعادة الرص الثابت طبقاً للنموذج ──
                loadStudentData(s.getSeatNo());
                applyExactLayout();

                int contentWidth = PAGE_W;
                int contentHeight = PAGE_H;
                jPanel1.setSize(contentWidth, contentHeight);
                jPanel1.doLayout();

                BufferedImage image = new BufferedImage(contentWidth, contentHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);

                // ملء الخلفية بالأبيض لتفادي المنطقة السوداء
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRect(0, 0, contentWidth, contentHeight);
                jPanel1.printAll(g2);
                g2.dispose();

                // ─── مجلد المركز ──────────────────────────────────────────
                String centerName = المركز.getText() != null ? المركز.getText().trim() : "";
                centerName = centerName.replaceAll("[\\\\/:*?\"<>|]", "_");
                if (centerName.isEmpty()) centerName = "بدون مركز";

                File centerFolder = new File(certFolder, centerName);
                if (!centerFolder.exists()) centerFolder.mkdirs();

                // ─── اسم الملف = الرقم القومي ──────────────────────────────
                String nationalId = الرقم_القومي.getText();
                String fileName = (nationalId != null && !nationalId.trim().isEmpty())
                        ? nationalId.trim().replaceAll("[\\\\/:*?\"<>|]", "")
                        : "student_" + s.getSeatNo().replaceAll("[\\\\/:*?\"<>|]", "");
                String filePath = centerFolder.getAbsolutePath() + File.separator + fileName + ".pdf";

                // ─── حفظ ملف PDF بحجم يتطابق مع المحتوى أو يعمل scaling مناسب دون قص ───
                com.itextpdf.text.Rectangle dynamicPageSize = new com.itextpdf.text.Rectangle(contentWidth, contentHeight);
                Document doc = new Document(dynamicPageSize, 0f, 0f, 0f, 0f);
                PdfWriter.getInstance(doc, new FileOutputStream(filePath));
                doc.open();
                Image img = Image.getInstance(image, null);
                img.scaleAbsolute(contentWidth, contentHeight);
                img.setAbsolutePosition(0, 0);
                doc.add(img);
                doc.close();
            }

            // ─── فتح مجلد النتيجة ───────────────────────────────────────────
            SwingUtilities.invokeLater(() -> {
                try {
                    Desktop.getDesktop().open(certFolder);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        الاسم = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        الرقم_القومي = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        المركز = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        تخصص = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        تقدير = new javax.swing.JLabel();
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
        المركز1 = new javax.swing.JLabel();
        المركز2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("وزاره الصناعه");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("مصلحه الكفايه الانتاجية والتدريب المهني");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("الادارة العامة اللاختبارات النمطية");

        jSeparator1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jSeparator1.setPreferredSize(new java.awt.Dimension(200, 6));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("شهادة ");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("دبلوم التلمذة الصناعية");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("تشهد وزارة التجارة والصناعة بان السيد /");

        الاسم.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        الاسم.setText("jLabel7");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("الرقم القومي /");

        الرقم_القومي.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        الرقم_القومي.setText("jLabel9");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("مركز /");

        المركز.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        المركز.setText("jLabel11");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("نخصص /");

        تخصص.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        تخصص.setText("jLabel11");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("بتقدير /");

        تقدير.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        تقدير.setText("jLabel11");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("دور / مايو عام 2026الميلادية / الفان وسته وعشرون");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("وقد منحت له هذه الشهادة بناء علي طلبه لتقديمها الي :");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("قد نجح في امتحان دبلوم التلمذة الصناعية بنظام الثلاث سنوات ");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("jLabel19");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("وقد سدد الرسوم بموجب قسيمة رقم 33 ع.ح الحكوميه رقم / ");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel21.setText("وهي معادلة لشهادة دبلوم المدارس الصناعية بوزارة التربية والتعليم بجموهرية مصر العربية وذلك طبقا للقرار الوزاري للتربية و التعليم");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setText("رقم (92) الصادر في 17/6/1968 وتم تعديله بقراير رقم 57 لسنه 1969.");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setText("مصلحة الكفايه الانتاجية و التدريب المهني حاصلة علي نظام ادارة الجودة 9001 iso ");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setText("عضو الامتحنات المسؤول");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setText("..................................");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("المراجع المسؤول");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel27.setText("..................................");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel28.setText("يعتمد ،");

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel29.setText("مدير الادارة");

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel30.setText("تحرير في ");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel31.setText("خاتم شعار الجمهورية");

        المركز1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        المركز2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        المركز2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(150, 150, 150)
                                .addComponent(المركز)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(146, 146, 146)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(تقدير, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel14))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(تخصص)
                                        .addGap(4, 4, 4)
                                        .addComponent(jLabel12)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(الرقم_القومي)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8))
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(156, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(الاسم)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel31)
                                .addGap(196, 196, 196)
                                .addComponent(jLabel30))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(57, 57, 57)
                                        .addComponent(jLabel28)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel26)
                                        .addGap(235, 235, 235))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(49, 49, 49)
                                        .addComponent(jLabel29)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(216, 216, 216)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel24)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addGap(12, 12, 12))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(المركز1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(المركز2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(54, 54, 54)
                                        .addComponent(jLabel4))
                                    .addComponent(jLabel5))
                                .addGap(204, 204, 204)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(14, 14, 14)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3)
                                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(64, 64, 64)
                                .addComponent(jLabel1)
                                .addGap(64, 64, 64)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(30, 30, 30)
                            .addComponent(jLabel1)
                            .addGap(0, 0, 0)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(0, 0, 0)
                                    .addComponent(jLabel3)
                                    .addGap(0, 0, 0)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(64, 64, 64)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(الاسم)
                                        .addComponent(jLabel6)))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addGap(1, 1, 1)
                                    .addComponent(jLabel5))))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addComponent(المركز1)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(المركز2)
                        .addGap(101, 101, 101)))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(الرقم_القومي)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel18)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel16))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(المركز)
                            .addComponent(jLabel10))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(تخصص)
                            .addComponent(jLabel12))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(تقدير)
                            .addComponent(jLabel14))))
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jLabel19))
                .addGap(10, 10, 10)
                .addComponent(jLabel20)
                .addGap(10, 10, 10)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addGap(10, 10, 10)
                .addComponent(jLabel23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(jLabel26)
                            .addComponent(jLabel28))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jLabel29)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel27)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30)
                    .addComponent(jLabel31))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        java.awt.EventQueue.invokeLater(() -> new SecondCertificateOfSuccess().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel الاسم;
    private javax.swing.JLabel الرقم_القومي;
    private javax.swing.JLabel المركز;
    private javax.swing.JLabel المركز1;
    private javax.swing.JLabel المركز2;
    private javax.swing.JLabel تخصص;
    private javax.swing.JLabel تقدير;
    // End of variables declaration//GEN-END:variables
}
