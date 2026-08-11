/*
 * Examination Administration Form — NewJFrame1
 */
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * استمارة إدارة الامتحانات — NewJFrame1
 */
public class NewJFrame1 extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(NewJFrame1.class.getName());

    /** الإحداثيات الأصلية لكل Label كما صممها NetBeans — تُستخدم كمرجع ثابت للتغيير الديناميكي */
    private final java.util.Map<javax.swing.JLabel, java.awt.Rectangle> originalBoundsMap = new java.util.HashMap<>();

    private String currentCenterName = "";
    private String currentNationalId = "";

    /**
     * Creates new form NewJFrame1
     */
    public NewJFrame1() {
        initComponents();
        setupDynamicLabels();
    }

    private void setupDynamicLabels() {
        // ضبط لون خلفية ContentPane إلى الأبيض لمنع الحواف السوداء
        getContentPane().setBackground(java.awt.Color.WHITE);

        // إرسال jLabel2 (صورة الخلفية) إلى خلف باقي المكونات حتى لا تغطي أي label بيانات
        // في Swing، المكوّن الأخير المُضاف يظهر في المقدمة (Z-order 0)
        // لذلك نضعه في آخر مرتبة (getContentPane().getComponentCount()-1)
        getContentPane().setComponentZOrder(
            jLabel2, getContentPane().getComponentCount() - 1);

        // محاذاة جميع خلايا الدرجات أفقياً مع رؤوس الأعمدة المقابلة لها (الأعمدة من 1 إلى 9)
        javax.swing.JLabel[] headers = {
            jLabel6,  // العمود 1 (تكنولوجيا)
            jLabel7,  // العمود 2 (رسم)
            jLabel8,  // العمود 3 (ميكانيكا عامة)
            jLabel9,  // العمود 4 (لغة انجليزية)
            jLabel10, // العمود 5
            jLabel11, // العمود 6
            jLabel12, // العمود 7
            jLabel13, // العمود 8 (التطبيقي والعملي)
            jLabel14  // العمود 9 (المجموع الكلي)
        };

        javax.swing.JLabel[] maxCols = {
            jLabel15, jLabel16, jLabel17, jLabel18, jLabel19, jLabel20, jLabel21, jLabel22, jLabel23
        };

        javax.swing.JLabel[] passCols = {
            jLabel24, jLabel25, jLabel26, jLabel27, jLabel28, jLabel29, jLabel30, jLabel31, jLabel32
        };

        javax.swing.JLabel[] obtainedCols = {
            jLabel33, jLabel34, jLabel35, jLabel36, jLabel46, jLabel37, jLabel38, jLabel39, jLabel40
        };

        for (int i = 0; i < 9; i++) {
            javax.swing.JLabel header = headers[i];
            int x = header.getX();
            int w = header.getWidth();
            
            // محاذاة الدرجة العظمى
            javax.swing.JLabel max = maxCols[i];
            max.setBounds(x, max.getY(), w, max.getHeight());
            
            // محاذاة درجة النجاح
            javax.swing.JLabel pass = passCols[i];
            pass.setBounds(x, pass.getY(), w, pass.getHeight());
            
            // محاذاة الدرجة التي حصل عليها الطالب
            javax.swing.JLabel obtained = obtainedCols[i];
            obtained.setBounds(x, obtained.getY(), w, obtained.getHeight());
        }

        // ضبط محاذاة جميع التسميات (عدا الأعمدة الرأسية jLabel6-9, 13, 14)
        javax.swing.JLabel[] allLabels = {
            jLabel1, jLabel3, jLabel5,
            jLabel10, jLabel11, jLabel12,
            jLabel15, jLabel16, jLabel17, jLabel18, jLabel19, jLabel20, jLabel21, jLabel22, jLabel23,
            jLabel24, jLabel25, jLabel26, jLabel27, jLabel28, jLabel29, jLabel30, jLabel31, jLabel32,
            jLabel33, jLabel34, jLabel35, jLabel36, jLabel37, jLabel38, jLabel39, jLabel40, jLabel41,
            jLabel42, jLabel43, jLabel44, jLabel45, jLabel46
        };

        for (javax.swing.JLabel lbl : allLabels) {
            // حفظ الإحداثيات الأصلية قبل أي تعديل ديناميكي
            originalBoundsMap.put(lbl, new java.awt.Rectangle(lbl.getBounds()));
            lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lbl.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
        }

        // بيانات الطالب وبيانات القسيمة: محاذاة من اليمين
        // (النص يبدأ من الحافة اليمنى للخانة وينمو نحو اليسار)
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        // رقم 100 الثابت بالأرقام العربية
        jLabel46.setText("١٠٠");

        // ضبط خانات الشرطة (العمودان 5 و6): خط عريض وأسود ومحاذاة وسط
        // ملاحظة: العمود 7 أصبح «مجموع الدرجات النظرية» والعمود 8 مجموع العملي
        java.awt.Font dashFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20);
        javax.swing.JLabel[] dashLabels = {
            jLabel10, jLabel11, // أسماء المواد (العمودان 5 و6)
            jLabel19, jLabel20, // الدرجة العظمى
            jLabel28, jLabel29, // الدرجة الصغرى
            jLabel46, jLabel37  // درجة الطالب (العمود 5=jLabel46, 6=jLabel37)
        };
        for (javax.swing.JLabel lbl : dashLabels) {
            lbl.setFont(dashFont);
            lbl.setForeground(java.awt.Color.BLACK);
        }

        // ── موضع jLabel45 (جهة التقديم) ─────────────────────────────────────
        // السطر ده هو اللي بيتحكم في مكان الـ label على الاستمارة:
        //   new java.awt.Rectangle( X_يسار , Y_ارتفاع , عرض , ارتفاع_النص )
        //   الحافة اليمنى = X_يسار + عرض  ← لازم تساوي مكان نهاية كلمة "إلى"
        //
        //  لو الكلمة لسه "تحت" إلى:  نقّص Y مثلاً من 920 لـ 905
        //  لو الكلمة بعيدة يسار:     زوّد عرض (مثلاً 580 → 600)
        //  لو الكلمة داخل جملة "وذلك لتقديمها": نقّص عرض (مثلاً 580 → 550)
        java.awt.Rectangle dest45Bounds = new java.awt.Rectangle(50, 908, 560, 25);
        // الحافة اليمنى الحالية = 50 + 560 = 610 ← نهاية كلمة "إلى" على الصورة
        jLabel45.setBounds(dest45Bounds);
        originalBoundsMap.put(jLabel45, dest45Bounds);

        // ── موضع jLabel43 (رقم القسيمة والمجموعة بعد «قسيمة رقم 33 ع .ح») ────
        // نص القالب المعاد رسمه «قسيمة رقم ٣٣ ع .ح» ينتهي عند x≈485
        // فالحافة اليمنى = 80 + 370 = 450 ← مسافة أمان حتى لا يلتصق بالقالب
        java.awt.Rectangle receipt43Bounds = new java.awt.Rectangle(80, 860, 370, 25);
        jLabel43.setBounds(receipt43Bounds);
        originalBoundsMap.put(jLabel43, receipt43Bounds);

        // ── موضع jLabel3 (اسم المركز — خانة «التابع لمركز/ "..."») ───────────
        // جملة «قد نجح...» أُعيد رسمها في القالب حتى x=560، فخانة المركز
        // اتّسعت من x=570 إلى x=877 (~307px) ليظهر الاسم بخط كبير
        java.awt.Rectangle center3Bounds = new java.awt.Rectangle(570, 303, 307, 30);
        jLabel3.setBounds(center3Bounds);
        originalBoundsMap.put(jLabel3, center3Bounds);

        // ── موضع jLabel44 (التاريخ) ──────────────────────────────────────────
        // كلمة «بتاريخ» المطبوعة قاعدتها عند y≈925 — نرفع خانة التاريخ
        // لتحاذيها على نفس السطر بدلاً من النزول تحتها
        java.awt.Rectangle date44Bounds = new java.awt.Rectangle(880, 903, 70, 28);
        jLabel44.setBounds(date44Bounds);
        originalBoundsMap.put(jLabel44, date44Bounds);

        // ── موضع jLabel41 (مجموع درجات التلميذ بالكتابة — التفقيطة) ──────────
        // الكلام المطبوع «مجموع درجات التلميذ بالكتابة :» يمتد من x=746 إلى x=1008
        // (مقاس بالبكسل) — لذا التفقيطة تبدأ من يمين x=738 وتمتد شمالًا حتى لا
        // تركب على الكلام المطبوع، ومحاذاة يمين حتى تلتصق بالنقطتين مباشرة
        java.awt.Rectangle totWords41Bounds = new java.awt.Rectangle(200, 766, 538, 30);
        jLabel41.setBounds(totWords41Bounds);
        originalBoundsMap.put(jLabel41, totWords41Bounds);
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        // ── تكبير خط التفقيطة وبيانات القسيمة (طلب إدارة الامتحانات) ─────────
        java.awt.Font bigFieldFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22);
        jLabel41.setFont(bigFieldFont); // تفقيطة مجموع الدرجات
        jLabel42.setFont(bigFieldFont); // المبلغ كتابة (جنيهاً فقط)
        jLabel43.setFont(bigFieldFont); // رقم القسيمة
        jLabel44.setFont(bigFieldFont); // التاريخ

        // جهة التقديم أُلغيت (طلب إدارة الامتحانات — أُزيلت جملة
        // «م ، وذلك لتقديمها إلى» من القالب فلا يُطبع ما يُكتب في الحقل)
        jLabel45.setVisible(false);
    }

    // ── Receipt metadata fields ─────────────────────────────────────────────

    private String extraPounds    = "";
    private String extraDate      = "";
    private String extraGroup     = "";
    private String extraReceiptNo = "";

    public void setReceiptMetadata(String pounds, String receiptNo, String groupNo, String date) {
        this.extraPounds    = pounds    != null ? pounds.trim()    : "";
        this.extraReceiptNo = receiptNo != null ? receiptNo.trim() : "";
        this.extraGroup     = groupNo   != null ? groupNo.trim()   : "";
        this.extraDate      = date      != null ? date.trim()      : "";

        // jLabel42 = جنيهاً فقط (المبلغ كتابة)
        // jLabel43 = رقم القسيمة + المجموعة (بعد «بموجب قسيمة رقم 33 ع .ح» المطبوعة)
        // jLabel44 = بتاريخ (التاريخ)
        // jLabel45 = جهة التقديم (مخفي — أُلغي من الاستمارة)
        jLabel42.setText(toArabic(this.extraPounds));
        jLabel43.setText(receiptWithGroup());
        jLabel44.setText(formatDate(this.extraDate));
        jLabel45.setText("");
        adjustAllDynamicLabels();
    }

    /** «[رقم القسيمة]  مجموعة / [المجموعة]» — كما في الورقة المؤمنة الرسمية */
    private String receiptWithGroup() {
        String txt = toArabic(extraReceiptNo);
        if (!extraGroup.isEmpty()) {
            txt += "  مجموعة / " + toArabic(extraGroup);
        }
        return txt;
    }

    /**
     * يعرض التاريخ بمجموعات مفصولة داخل سياق يمين→يسار بحيث يُقرأ من اليمين
     * يوم/شهر/سنة كما يُكتب باليد: «١٠ / ٨ / ٢٠٢٦» تُقرأ ١٠ ثم ٨ ثم ٢٠٢٦
     */
    private String formatDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        String[] parts = raw.trim().split("\\s*[/\\\\-]\\s*");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(" / ");
            sb.append(parts[i].trim());
        }
        // ‏RLE‏ لضمان أن اتجاه الفقرة يمين→يسار مهما كان سياق العرض
        return "‫" + toArabic(sb.toString()) + "‬";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SQL QUERIES & DATA BINDING
    // ═══════════════════════════════════════════════════════════════════════

    public void loadStudentInfo(String seatNo, Connection con) throws Exception {
        String sql =
            "SELECT s.name, s.seat_no, s.national_id, s.coordination_no, " +
            "       s.professional_group, s.profession, s.center_name, s.region " +
            "FROM students s " +
            "WHERE TRIM(s.seat_no) = TRIM(?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, seatNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name           = orEmpty(rs.getString("name"));
                    String seat           = orEmpty(rs.getString("seat_no"));
                    String nationalId     = orEmpty(rs.getString("national_id"));
                    String profession     = orEmpty(rs.getString("profession"));
                    String coordinationNo = orEmpty(rs.getString("coordination_no"));
                    String centerName     = orEmpty(rs.getString("center_name"));

                    // --- الحقول الأساسية ---
                    // jLabel1 = اسم الطالب
                    jLabel1.setText(name);
                    // jLabel3 = اسم المركز — خانة «التابع لمركز/ "..."» في القالب
                    jLabel3.setText(centerName);
                    fitLabelToOriginalWidth(jLabel3, 22, 13);
                    // jLabel5 = المهنة / التخصص
                    jLabel5.setText(profession);

                    currentCenterName = centerName;
                    currentNationalId = nationalId;
                }
            }
        }
    }

    public void loadStudentGrades(String seatNo, Connection con) throws Exception {
        // فقط أول 4 أعمدة (مواد نظرية) تُملأ من قاعدة البيانات
        String sql =
            "SELECT sub.id, sub.name AS subject_name, sub.type, sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession) " +
            "AND (LOWER(sub.type) LIKE '%نظري%' OR LOWER(sub.type) LIKE '%theory%') " +
            "ORDER BY sub.display_order ASC, sub.id ASC";

        // أسماء المواد الرأسية — 4 أعمدة نظرية (jLabel6..9)
        VerticalJLabel[] nameLbls = { jLabel6, jLabel7, jLabel8, jLabel9 };
        // درجات الأعمدة الـ4 النظرية
        javax.swing.JLabel[] maxLbls  = { jLabel15, jLabel16, jLabel17, jLabel18 };
        javax.swing.JLabel[] passLbls = { jLabel24, jLabel25, jLabel26, jLabel27 };
        javax.swing.JLabel[] markLbls = { jLabel33, jLabel34, jLabel35, jLabel36 };

        // مسح الأعمدة الـ4 النظرية أولاً
        for (int i = 0; i < 4; i++) {
            nameLbls[i].setText("");
            maxLbls[i].setText("");
            passLbls[i].setText("");
            markLbls[i].setText("");
        }

        // العمودان 5 و6: شرطة في جميع الخانات (اسم المادة، العظمى، الصغرى، درجة الطالب)
        jLabel10.setText("-"); jLabel11.setText("-");
        jLabel19.setText("-"); jLabel20.setText("-");
        jLabel28.setText("-"); jLabel29.setText("-");
        jLabel46.setText("-"); jLabel37.setText("-");

        // ── العمود 7: مجموع الدرجات النظرية (كما في الورقة المؤمنة) ──────────
        String theorySql =
            "SELECT sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession) " +
            "AND (LOWER(sub.type) LIKE '%نظري%' OR LOWER(sub.type) LIKE '%theory%')";

        int theoryMax = 0, theoryPass = 0, theoryObtained = 0;
        boolean hasTheoryData = false;

        try (PreparedStatement psTheory = con.prepareStatement(theorySql)) {
            psTheory.setString(1, seatNo);
            try (ResultSet rsTheory = psTheory.executeQuery()) {
                while (rsTheory.next()) {
                    hasTheoryData = true;
                    theoryMax      += rsTheory.getInt("max_mark");
                    theoryPass     += rsTheory.getInt("pass_mark");
                    theoryObtained += Math.max(rsTheory.getInt("obtained_mark"), 0);
                }
            }
        }

        // jLabel12 = رأس عمود «مجموع الدرجات النظرية» (رأسي، سطران)
        jLabel12.setText("مجموع الدرجات\nالنظرية");

        if (hasTheoryData) {
            jLabel21.setText(toArabic(String.valueOf(theoryMax)));
            jLabel30.setText(toArabic(String.valueOf(theoryPass)));
            jLabel38.setText(toArabic(String.valueOf(theoryObtained)));
        } else {
            jLabel21.setText("-");
            jLabel30.setText("-");
            jLabel38.setText("-");
        }

        // ── العمود 9: المجموع الكلي لكافة المواد ──────────────────────────────
        String grandTotalSql =
            "SELECT sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession)";

        int grandMax = 0, grandPass = 0, grandObtained = 0;
        boolean hasGrandData = false;

        try (PreparedStatement psGrand = con.prepareStatement(grandTotalSql)) {
            psGrand.setString(1, seatNo);
            try (ResultSet rsGrand = psGrand.executeQuery()) {
                while (rsGrand.next()) {
                    hasGrandData = true;
                    grandMax      += rsGrand.getInt("max_mark");
                    grandPass     += rsGrand.getInt("pass_mark");
                    grandObtained += Math.max(rsGrand.getInt("obtained_mark"), 0);
                }
            }
        }

        // jLabel14 = رأس عمود «المجموع الكلي» (رأسي ثابت)
        jLabel14.setText("المجموع الكلي");

        if (hasGrandData) {
            jLabel23.setText(toArabic(String.valueOf(grandMax)));
            jLabel32.setText(toArabic(String.valueOf(grandPass)));
            // العمود 9 داخل الجدول (أرقام)
            jLabel40.setText(toArabic(String.valueOf(grandObtained)));
            // مجموع درجات التلميذ بالكتابة (خارج الجدول — بالحروف)
            jLabel41.setText(numberToArabicWords(grandObtained) + " درجة");
        } else {
            jLabel23.setText("-");
            jLabel32.setText("-");
            jLabel40.setText("-");
            jLabel41.setText("-");
        }

        // ── ملء الأعمدة الـ4 النظرية ─────────────────────────────────────────
        int idx = 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, seatNo);
            try (ResultSet rs = ps.executeQuery()) {
                // نقرأ فقط أول 4 مواد نظرية
                while (rs.next() && idx < 4) {
                    String subName = orEmpty(rs.getString("subject_name"));
                    int maxMark    = rs.getInt("max_mark");
                    int passMark   = rs.getInt("pass_mark");
                    int obtained   = Math.max(rs.getInt("obtained_mark"), 0);

                    nameLbls[idx].setText(subName.trim());
                    maxLbls[idx].setText(toArabic(String.valueOf(maxMark)));
                    passLbls[idx].setText(toArabic(String.valueOf(passMark)));
                    markLbls[idx].setText(toArabic(String.valueOf(obtained)));

                    idx++;
                }
            }
        }

        // ── العمود 8: مجموع مواد العملي والتطبيقي ────────────────────────────
        String practicalSql =
            "SELECT sub.name AS subject_name, sub.type, sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession) " +
            "AND (LOWER(sub.type) LIKE '%عملي%' OR LOWER(sub.type) LIKE '%تطبيقي%' " +
            "     OR LOWER(sub.type) LIKE '%practical%' OR LOWER(sub.type) LIKE '%applied%')";

        int totalMaxPrac = 0, totalPassPrac = 0, totalObtainedPrac = 0;
        boolean hasPracData = false;

        try (PreparedStatement psPrac = con.prepareStatement(practicalSql)) {
            psPrac.setString(1, seatNo);
            try (ResultSet rsPrac = psPrac.executeQuery()) {
                while (rsPrac.next()) {
                    hasPracData = true;
                    totalMaxPrac      += rsPrac.getInt("max_mark");
                    totalPassPrac     += rsPrac.getInt("pass_mark");
                    totalObtainedPrac += Math.max(rsPrac.getInt("obtained_mark"), 0);
                }
            }
        }

        // jLabel13 = رأس عمود «مجموع درجات التطبيقي والامتحان العملي» (رأسي، 3 أسطر)
        jLabel13.setText("مجموع درجات\nالتطبيقي\nوالامتحان العملي");

        if (hasPracData) {
            jLabel22.setText(toArabic(String.valueOf(totalMaxPrac)));
            jLabel31.setText(toArabic(String.valueOf(totalPassPrac)));
            jLabel39.setText(toArabic(String.valueOf(totalObtainedPrac)));
        } else {
            jLabel22.setText("-");
            jLabel31.setText("-");
            jLabel39.setText("-");
        }
    }

    private void adjustLabelSize(javax.swing.JLabel label) {
        if (label == null) return;

        // استخدام الإحداثيات الأصلية المحفوظة كمرجع ثابت
        java.awt.Rectangle orig = originalBoundsMap.get(label);
        if (orig == null) return;

        java.awt.Font font = label.getFont();
        if (font == null) return;

        String text = label.getText();
        if (text == null) text = "";

        java.awt.FontMetrics fm = label.getFontMetrics(font);
        int textWidth = fm.stringWidth(text);

        // هامش أمان (+12px) لمنع قص الحروف العربية في الـ PDF
        int newWidth = Math.max(textWidth + 12, 10);
        int newHeight = orig.height > 0 ? orig.height : (fm.getHeight() + 4);

        // تحديد نقطة X بناءً على المحاذاة الأصلية:
        //   RIGHT  → الحافة اليمنى ثابتة (النص يبدأ من اليمين وينمو يسارًا)
        //   CENTER → مركز المربع ثابت
        //   LEFT   → الحافة اليسرى ثابتة
        int newX;
        int align = label.getHorizontalAlignment();
        if (align == javax.swing.SwingConstants.RIGHT) {
            newX = (orig.x + orig.width) - newWidth;
        } else if (align == javax.swing.SwingConstants.CENTER) {
            newX = (orig.x + orig.width / 2) - newWidth / 2;
        } else {
            newX = orig.x;
        }

        label.setBounds(newX, orig.y, newWidth, newHeight);
    }

    /**
     * يصغّر خط الـ label تدريجياً حتى يتسع النص داخل عرض الخانة الأصلي
     * (يُستخدم لاسم المركز حتى لا يركب على نص القالب المطبوع لو الاسم طويل)
     */
    private void fitLabelToOriginalWidth(javax.swing.JLabel label, int maxSize, int minSize) {
        java.awt.Rectangle orig = originalBoundsMap.get(label);
        if (orig == null) orig = label.getBounds();
        String text = label.getText();
        if (text == null || text.isEmpty()) return;

        java.awt.Font base = label.getFont();
        int size = maxSize;
        while (size > minSize) {
            java.awt.Font f = new java.awt.Font(base.getName(), base.getStyle(), size);
            if (label.getFontMetrics(f).stringWidth(text) <= orig.width) break;
            size--;
        }
        label.setFont(new java.awt.Font(base.getName(), base.getStyle(), size));
    }

    private void adjustAllDynamicLabels() {
        javax.swing.JLabel[] dynamicLabels = {
            jLabel1, jLabel3, jLabel5,
            jLabel10, jLabel11, jLabel12,
            jLabel15, jLabel16, jLabel17, jLabel18,
            jLabel19, jLabel20, jLabel21,
            jLabel22, jLabel23, jLabel24, jLabel25, jLabel26, jLabel27,
            jLabel28, jLabel29, jLabel30, jLabel31, jLabel32,
            jLabel33, jLabel34, jLabel35, jLabel36, jLabel37, jLabel38, jLabel39,
            jLabel40, jLabel41, jLabel42, jLabel43, jLabel44, jLabel45, jLabel46
        };

        for (javax.swing.JLabel lbl : dynamicLabels) {
            adjustLabelSize(lbl);
        }
        
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void clearForm() {
        jLabel1.setText("");
        jLabel3.setText("");
        jLabel5.setText("");
        jLabel41.setText("");
        jLabel42.setText("");
        jLabel43.setText("");
        jLabel44.setText("");
        jLabel45.setText("");
        // إعادة تطبيق بيانات القسيمة إذا تم تعيينها
        jLabel42.setText(toArabic(extraPounds));
        jLabel43.setText(receiptWithGroup());
        jLabel44.setText(formatDate(extraDate));
        jLabel45.setText("");
        // jLabel46 = ١٠٠ (الدرجة العظمى للمجموع الكلي، ثابت)
        jLabel46.setText("١٠٠");
        currentCenterName = "";
        currentNationalId = "";
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PDF PRINT GENERATION
    // ═══════════════════════════════════════════════════════════════════════

    public void printForms(List<String[]> studentsData,
                           java.util.function.BiConsumer<Integer, Integer> progressCallback) {

        try (Connection con = DatabaseConnection.getConnection()) {
            File rootFolder = new File("التقارير" + File.separator + "استمارة إدارة الامتحانات");
            if (!rootFolder.exists()) rootFolder.mkdirs();

            File allFile = new File(rootFolder, "استمارات الطلاب المختارين.pdf");
            Document allDoc = new Document(PageSize.A4, 0f, 0f, 0f, 0f);
            PdfWriter.getInstance(allDoc, new FileOutputStream(allFile));
            allDoc.open();

            java.util.Map<String, Document> centerDocs = new java.util.HashMap<>();
            final int width  = 1080;
            final int height = 1480;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            getContentPane().setSize(width, height);
            if (getContentPane() instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) getContentPane()).setDoubleBuffered(false);
            }

            int total = studentsData.size();

            for (int idx = 0; idx < total; idx++) {
                String[] info   = studentsData.get(idx);
                String seatNo   = info[0];

                if (progressCallback != null) progressCallback.accept(idx + 1, total);

                clearForm();
                loadStudentInfo(seatNo, con);
                loadStudentGrades(seatNo, con);
                adjustAllDynamicLabels();

                getContentPane().revalidate();
                getContentPane().repaint();
                getContentPane().doLayout();

                // ملء الخلفية بالأبيض لتفادي الحواف أو المنطقة السوداء
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRect(0, 0, width, height);
                getContentPane().printAll(g2);

                String rawCenter  = currentCenterName.isEmpty() ? "بدون مركز" : currentCenterName;
                String safeCenter = rawCenter.replaceAll("[\\\\/:*?\"<>|]", "_");
                File centerFolder = new File(rootFolder, safeCenter);
                if (!centerFolder.exists()) centerFolder.mkdirs();

                String rawNationalId = currentNationalId.trim();
                String fileName = rawNationalId.isEmpty()
                        ? "student_" + seatNo.replaceAll("[\\\\/:*?\"<>|]", "")
                        : rawNationalId.replaceAll("[\\\\/:*?\"<>|]", "");
                File singleFile = new File(centerFolder, fileName + ".pdf");

                Document singleDoc = new Document(PageSize.A4);
                PdfWriter.getInstance(singleDoc, new FileOutputStream(singleFile));
                singleDoc.open();
                Image imgSingle = Image.getInstance(image, null);
                imgSingle.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                imgSingle.setAbsolutePosition(0, 0);
                singleDoc.add(imgSingle);
                singleDoc.close();

                if (idx > 0) allDoc.newPage();
                Image imgAll = Image.getInstance(image, null);
                imgAll.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                imgAll.setAbsolutePosition(0, 0);
                allDoc.add(imgAll);

                Document centerDoc = centerDocs.get(safeCenter);
                if (centerDoc == null) {
                    File cFile = new File(centerFolder, "استمارة طلاب مركز " + safeCenter + ".pdf");
                    centerDoc = new Document(PageSize.A4);
                    PdfWriter.getInstance(centerDoc, new FileOutputStream(cFile));
                    centerDoc.open();
                    centerDocs.put(safeCenter, centerDoc);
                } else {
                    centerDoc.newPage();
                }
                Image imgCenter = Image.getInstance(image, null);
                imgCenter.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                imgCenter.setAbsolutePosition(0, 0);
                centerDoc.add(imgCenter);
            }

            g2.dispose();

            for (Document d : centerDocs.values()) {
                if (d.isOpen()) d.close();
            }
            if (allDoc.isOpen()) allDoc.close();

            final File finalRoot = rootFolder;
            final File finalAll  = allFile;
            final int  finalN    = total;

            SwingUtilities.invokeLater(() -> {
                try {
                    if (finalN == 1) {
                        String[] list = finalRoot.list();
                        if (list != null && list.length > 0) {
                            Desktop.getDesktop().open(new File(finalRoot, list[0]));
                        }
                    } else {
                        Desktop.getDesktop().open(finalRoot);
                        Desktop.getDesktop().open(finalAll);
                    }
                    javax.swing.JOptionPane.showMessageDialog(null,
                        "تم إنشاء " + finalN + " استمارة بنجاح",
                        "اكتمال العملية",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() ->
                javax.swing.JOptionPane.showMessageDialog(null,
                    "حدث خطأ أثناء إنشاء الاستمارات:\n" + ex.getMessage(),
                    "خطأ", javax.swing.JOptionPane.ERROR_MESSAGE));
        }
    }

    // ── Helper utilities ────────────────────────────────────────────────────

    private String toArabic(String s) {
        if (s == null) return "";
        return s.replace("0", "٠").replace("1", "١").replace("2", "٢")
                .replace("3", "٣").replace("4", "٤").replace("5", "٥")
                .replace("6", "٦").replace("7", "٧").replace("8", "٨")
                .replace("9", "٩");
    }

    private String orEmpty(String s) {
        return s != null ? s : "";
    }

    /**
     * تحويل العدد إلى كلمات عربية
     * مثال: 500 → خمسمائة، 347 → ثلاثة و أربعون و ثلاثمائة
     * تعمل بشكل صحيح للأرقام بين 0 و 999
     */
    private String numberToArabicWords(int number) {
        if (number == 0) return "صفر";

        String[] ones = {
            "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة",
            "ستة", "سبعة", "ثمانية", "تسعة", "عشرة",
            "إحدى عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر",
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

        java.util.List<String> parts = new java.util.ArrayList<>();

        // الآلاف (نظام النهاية العظمى 1000 وما فوق)
        int th = number / 1000;
        if (th == 1) parts.add("ألف");
        else if (th == 2) parts.add("ألفان");
        else if (th >= 3) parts.add(ones[th] + " آلاف");

        int rem = number % 1000;
        int h = rem / 100;
        int remainder = rem % 100;

        if (h > 0) {
            parts.add(hundreds[h]);
        }

        if (remainder > 0) {
            if (remainder < 20) {
                parts.add(ones[remainder]);
            } else {
                int o  = remainder % 10;
                int tt = remainder / 10;
                if (o > 0) parts.add(ones[o]);
                parts.add(tens[tt]);
            }
        }

        // واو العطف ملتصقة بالكلمة التالية: «مائة وسبعة وثمانون»
        return String.join(" و", parts);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CUSTOM VERTICAL LABEL — يكتب النص بالطول (رأسياً) داخل الخانة
    // ═══════════════════════════════════════════════════════════════════════
    static class VerticalJLabel extends javax.swing.JLabel {
        public VerticalJLabel() { super(); }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            String text = getText();
            if (text == null || text.isEmpty()) return;
            text = text.replaceAll("<[^>]+>", "").trim();
            if (text.isEmpty()) return;

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(java.awt.Color.BLACK);

            // انتقل إلى مركز المكوّن
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            // أدر 90 درجة عكس عقارب الساعة — النص يُقرأ رأسياً من الأسفل للأعلى
            g2.rotate(-Math.PI / 2.0);

            // إذا كان النص يحتوي على مسافات أو أسطر جديدة، قسِّمه إلى أسطر
            String[] lines;
            if (text.contains("\n")) {
                lines = text.split("\n");
            } else {
                lines = new String[]{ text };
            }

            if (lines.length == 1) {
                java.awt.Font font = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15);
                g2.setFont(font);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int drawX = -textWidth / 2;
                int drawY = (fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, drawX, drawY);
            } else {
                java.awt.Font fontMulti = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
                g2.setFont(fontMulti);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int lineHeight   = fm.getHeight();
                int totalHeight  = lines.length * lineHeight;

                // تضبيط البداية بحيث تظهر جميع الكلمات تحت بعضها ومُمركزة
                int startY = -(totalHeight / 2) + fm.getAscent();

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.isEmpty()) continue;
                    int textWidth = fm.stringWidth(line);
                    int drawX = -textWidth / 2;
                    int drawY = startY + (i * lineHeight) - (fm.getDescent() / 2);
                    g2.drawString(line, drawX, drawY);
                }
            }

            g2.dispose();
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

        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new VerticalJLabel();
        jLabel7 = new VerticalJLabel();
        jLabel8 = new VerticalJLabel();
        jLabel9 = new VerticalJLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new VerticalJLabel();
        jLabel13 = new VerticalJLabel();
        jLabel14 = new VerticalJLabel();
        jLabel15 = new javax.swing.JLabel();
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
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("jLabel1");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(37, 250, 450, 25);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("jLabel3");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(707, 320, 170, 25);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("/");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(420, 370, 10, 25);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("jLabel5");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(27, 370, 390, 30);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("jLabel6");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(770, 475, 80, 150);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("jLabel7");
        getContentPane().add(jLabel7);
        jLabel7.setBounds(700, 475, 70, 150);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("jLabel8");
        getContentPane().add(jLabel8);
        jLabel8.setBounds(620, 475, 80, 150);

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setText("jLabel9");
        getContentPane().add(jLabel9);
        jLabel9.setBounds(540, 475, 70, 150);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setText("jLabel10");
        getContentPane().add(jLabel10);
        jLabel10.setBounds(470, 475, 60, 150);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("jLabel11");
        getContentPane().add(jLabel11);
        jLabel11.setBounds(390, 475, 70, 150);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel12.setText("jLabel12");
        getContentPane().add(jLabel12);
        jLabel12.setBounds(300, 475, 90, 150);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setText("jLabel13");
        getContentPane().add(jLabel13);
        jLabel13.setBounds(180, 475, 110, 150);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setText("jLabel14");
        getContentPane().add(jLabel14);
        jLabel14.setBounds(70, 475, 110, 150);

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel15.setText("jLabel15");
        getContentPane().add(jLabel15);
        jLabel15.setBounds(790, 640, 70, 25);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setText("jLabel16");
        getContentPane().add(jLabel16);
        jLabel16.setBounds(710, 640, 70, 25);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel17.setText("jLabel17");
        getContentPane().add(jLabel17);
        jLabel17.setBounds(630, 640, 70, 25);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel18.setText("jLabel18");
        getContentPane().add(jLabel18);
        jLabel18.setBounds(560, 640, 70, 25);

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel19.setText("jLabel19");
        getContentPane().add(jLabel19);
        jLabel19.setBounds(480, 640, 70, 25);

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel20.setText("jLabel20");
        getContentPane().add(jLabel20);
        jLabel20.setBounds(400, 640, 70, 25);

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel21.setText("jLabel21");
        getContentPane().add(jLabel21);
        jLabel21.setBounds(320, 640, 70, 25);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel22.setText("jLabel22");
        getContentPane().add(jLabel22);
        jLabel22.setBounds(210, 640, 70, 25);

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel23.setText("jLabel23");
        getContentPane().add(jLabel23);
        jLabel23.setBounds(100, 640, 70, 25);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setText("jLabel24");
        getContentPane().add(jLabel24);
        jLabel24.setBounds(790, 680, 70, 25);

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel25.setText("jLabel25");
        getContentPane().add(jLabel25);
        jLabel25.setBounds(710, 680, 70, 25);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel26.setText("jLabel26");
        getContentPane().add(jLabel26);
        jLabel26.setBounds(630, 680, 70, 25);

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel27.setText("jLabel27");
        getContentPane().add(jLabel27);
        jLabel27.setBounds(550, 680, 70, 25);

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel28.setText("jLabel28");
        getContentPane().add(jLabel28);
        jLabel28.setBounds(480, 680, 70, 25);

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel29.setText("jLabel29");
        getContentPane().add(jLabel29);
        jLabel29.setBounds(410, 680, 70, 25);

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel30.setText("jLabel30");
        getContentPane().add(jLabel30);
        jLabel30.setBounds(320, 690, 70, 25);

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel31.setText("jLabel31");
        getContentPane().add(jLabel31);
        jLabel31.setBounds(220, 680, 70, 25);

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel32.setText("jLabel32");
        getContentPane().add(jLabel32);
        jLabel32.setBounds(100, 680, 70, 25);

        jLabel33.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel33.setText("jLabel33");
        getContentPane().add(jLabel33);
        jLabel33.setBounds(770, 715, 80, 40);

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel34.setText("jLabel34");
        getContentPane().add(jLabel34);
        jLabel34.setBounds(690, 715, 80, 40);

        jLabel35.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel35.setText("jLabel35");
        getContentPane().add(jLabel35);
        jLabel35.setBounds(610, 715, 90, 40);

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel36.setText("jLabel36");
        getContentPane().add(jLabel36);
        jLabel36.setBounds(530, 715, 80, 40);

        jLabel37.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel37.setText("jLabel37");
        getContentPane().add(jLabel37);
        jLabel37.setBounds(390, 715, 70, 40);

        jLabel38.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel38.setText("jLabel38");
        getContentPane().add(jLabel38);
        jLabel38.setBounds(300, 715, 90, 40);

        jLabel39.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel39.setText("jLabel39");
        getContentPane().add(jLabel39);
        jLabel39.setBounds(180, 715, 110, 40);

        jLabel40.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel40.setText("jLabel40");
        getContentPane().add(jLabel40);
        jLabel40.setBounds(70, 715, 110, 40);

        jLabel41.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel41.setText("jLabel41");
        getContentPane().add(jLabel41);
        jLabel41.setBounds(670, 770, 70, 20);

        jLabel42.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel42.setText("jLabel42");
        getContentPane().add(jLabel42);
        jLabel42.setBounds(930, 860, 70, 25);

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel43.setText("jLabel43");
        getContentPane().add(jLabel43);
        jLabel43.setBounds(430, 860, 70, 25);

        jLabel44.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel44.setText("jLabel44");
        getContentPane().add(jLabel44);
        jLabel44.setBounds(880, 920, 70, 25);

        jLabel45.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel45.setText("jLabel45");
        getContentPane().add(jLabel45);
        jLabel45.setBounds(560, 920, 70, 25);

        jLabel46.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel46.setText("100");
        getContentPane().add(jLabel46);
        jLabel46.setBounds(470, 715, 60, 40);

        jLabel47.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel47.setText("دور /مايو 2026 الميلاديه الفان وسته وعشرون");
        getContentPane().add(jLabel47);
        jLabel47.setBounds(580, 360, 450, 40);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ChatGPT Image Aug 2, 2026, 07_02_00 AM.png"))); // NOI18N
        getContentPane().add(jLabel2);
        jLabel2.setBounds(0, 6, 1080, 1456);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
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
        java.awt.EventQueue.invokeLater(() -> new NewJFrame1().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private VerticalJLabel jLabel13;
    private VerticalJLabel jLabel14;
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
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private VerticalJLabel jLabel6;
    private VerticalJLabel jLabel7;
    private VerticalJLabel jLabel8;
    private VerticalJLabel jLabel9;
    // End of variables declaration//GEN-END:variables
}
