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
 * استمارة إدارة الامتحانات — NewJFrame
 */
public class NewJFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(NewJFrame.class.getName());

    private String currentCenterName = "";
    private String currentNationalId = "";

    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
        initComponents();
        setupDynamicLabels();
    }

    private void setupDynamicLabels() {
        // تحميل الخلفية بشكل متزامن لمنع ظهور الصفحة باللون الأبيض نتيجة التحميل غير المتزامن
        try {
            java.net.URL imgUrl = getClass().getResource("/ChatGPT Image Aug 2, 2026, 07_02_00 AM.png");
            if (imgUrl != null) {
                BufferedImage bgImage = javax.imageio.ImageIO.read(imgUrl);
                if (bgImage != null) {
                    jLabel1.setIcon(new javax.swing.ImageIcon(bgImage));
                    int w = bgImage.getWidth();
                    int h = bgImage.getHeight();
                    jLabel1.setBounds(0, 0, w, h);
                    jLabel1.setPreferredSize(new java.awt.Dimension(w, h));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ضبط لون خلفية الـ ContentPane إلى الأبيض لمنع الحواف السوداء
        getContentPane().setBackground(java.awt.Color.WHITE);

        // إرسال jLabel1 (صورة الخلفية) إلى خلف باقي المكونات حتى لا تغطي أي label بيانات
        // في Swing، المكوّن الأخير المُضاف يظهر في المقدمة (Z-order 0)
        // لذلك نضعه في آخر مرتبة (getContentPane().getComponentCount()-1)
        getContentPane().setComponentZOrder(
            jLabel1, getContentPane().getComponentCount() - 1);

        // ضبط محاذاة واتجاه جميع التسميات (عدا الأعمدة الرأسية jLabel6-9, 13, 14)
        javax.swing.JLabel[] allLabels = {
            jLabel2, jLabel3, jLabel5, jLabel10,
            jLabel11, jLabel12, jLabel15, jLabel16, jLabel17,
            jLabel18, jLabel19, jLabel20, jLabel21, jLabel22, jLabel23, jLabel24,
            jLabel25, jLabel26, jLabel27, jLabel28, jLabel29, jLabel30, jLabel31,
            jLabel32, jLabel33, jLabel34, jLabel35, jLabel36, jLabel37, jLabel38,
            jLabel39, jLabel40, jLabel41, jLabel42, jLabel43, jLabel44, jLabel45, jLabel46
        };

        for (javax.swing.JLabel lbl : allLabels) {
            lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lbl.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
        }
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        // ضبط خانات الشرطة (الأعمدة 5 و6 و7): خط عريض وأسود ومحاذاة وسط
        java.awt.Font dashFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20);
        javax.swing.JLabel[] dashLabels = {
            jLabel10, jLabel11, jLabel12, // أسماء المواد
            jLabel19, jLabel20, jLabel21, // الدرجة العظمى
            jLabel28, jLabel29, jLabel30, // الدرجة الصغرى
            jLabel37, jLabel38, jLabel39  // درجة الطالب
        };
        for (javax.swing.JLabel lbl : dashLabels) {
            lbl.setFont(dashFont);
            lbl.setForeground(java.awt.Color.BLACK);
        }
    }

    private String extraPounds = "";
    private String extraDate = "";
    private String extraReceiptNo = "";
    private String extraDestination = "";

    public void setReceiptMetadata(String pounds, String date, String receiptNo, String destination) {
        this.extraPounds = pounds != null ? pounds.trim() : "";
        this.extraDate = date != null ? date.trim() : "";
        this.extraReceiptNo = receiptNo != null ? receiptNo.trim() : "";
        this.extraDestination = destination != null ? destination.trim() : "";

        jLabel43.setText(toArabic(this.extraPounds));
        jLabel44.setText(toArabic(this.extraDate));
        jLabel45.setText(toArabic(this.extraReceiptNo));
        jLabel46.setText(toArabic(this.extraDestination));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SQL QUERIES & DATA BINDING
    // ═══════════════════════════════════════════════════════════════════════════

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
                    jLabel2.setText(name);
                    // jLabel3 = اسم المركز — خانة «التابع لمركز/ "..."» في القالب
                    jLabel3.setText(centerName);
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
            "SELECT sub.id, sub.name AS subject_name, sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession) " +
            "ORDER BY sub.id ASC";

        // أسماء المواد — 4 أعمدة نظرية فقط
        VerticalJLabel[] nameLbls = { jLabel6, jLabel7, jLabel8, jLabel9 };
        // درجات الأعمدة الـ 4 النظرية
        javax.swing.JLabel[] maxLbls  = { jLabel15, jLabel16, jLabel17, jLabel18 };
        javax.swing.JLabel[] passLbls = { jLabel24, jLabel25, jLabel26, jLabel27 };
        javax.swing.JLabel[] markLbls = { jLabel33, jLabel34, jLabel35, jLabel36 };

        // مسح الأعمدة الـ 4 النظرية أولاً
        for (int i = 0; i < 4; i++) {
            nameLbls[i].setText("");
            maxLbls[i].setText("");
            passLbls[i].setText("");
            markLbls[i].setText("");
        }

        // الأعمدة 5 و6 و7: شرطة في جميع خانات الأعمدة الثلاثة (اسم المادة، العظمى، الصغرى، درجة الطالب)
        jLabel10.setText("-"); jLabel11.setText("-"); jLabel12.setText("-");
        jLabel19.setText("-"); jLabel20.setText("-"); jLabel21.setText("-");
        jLabel28.setText("-"); jLabel29.setText("-"); jLabel30.setText("-");
        jLabel37.setText("-"); jLabel38.setText("-"); jLabel39.setText("-");

        // العمود 9 (المجموع الكلي لكافة المواد):
        String grandTotalSql =
            "SELECT sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession)";

        int grandMax = 0;
        int grandPass = 0;
        int grandObtained = 0;
        boolean hasGrandData = false;

        try (PreparedStatement psGrand = con.prepareStatement(grandTotalSql)) {
            psGrand.setString(1, seatNo);
            try (ResultSet rsGrand = psGrand.executeQuery()) {
                while (rsGrand.next()) {
                    hasGrandData = true;
                    grandMax += rsGrand.getInt("max_mark");
                    grandPass += rsGrand.getInt("pass_mark");
                    grandObtained += Math.max(rsGrand.getInt("obtained_mark"), 0);
                }
            }
        }

        jLabel14.setText("المجموع الكلي");
        if (hasGrandData) {
            jLabel23.setText(toArabic(String.valueOf(grandMax)));
            jLabel32.setText(toArabic(String.valueOf(grandPass)));
            jLabel41.setText(toArabic(String.valueOf(grandObtained)));
        } else {
            jLabel23.setText("-");
            jLabel32.setText("-");
            jLabel41.setText("-");
        }

        jLabel42.setText("");

        int idx = 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, seatNo);
            try (ResultSet rs = ps.executeQuery()) {
                // نقرأ فقط أول 4 مواد نظرية
                while (rs.next() && idx < 4) {
                    String subName = orEmpty(rs.getString("subject_name"));
                    int maxMark    = rs.getInt("max_mark");
                    int passMark   = rs.getInt("pass_mark");
                    int obtained   = rs.getInt("obtained_mark");

                    nameLbls[idx].setText(subName.trim());
                    maxLbls[idx].setText(toArabic(String.valueOf(maxMark)));
                    passLbls[idx].setText(toArabic(String.valueOf(passMark)));
                    markLbls[idx].setText(toArabic(String.valueOf(obtained)));

                    idx++;
                }
            }
        }

        // العمود 8 (تجميع التطبيقي والعملي):
        String practicalSql =
            "SELECT sub.name AS subject_name, sub.type, sub.max_mark, sub.pass_mark, sg.obtained_mark " +
            "FROM subjects sub " +
            "CROSS JOIN students s " +
            "LEFT JOIN student_grades sg ON sub.id = sg.subject_id AND sg.student_id = s.id " +
            "WHERE TRIM(s.seat_no) = TRIM(?) AND TRIM(sub.profession) = TRIM(s.profession) " +
            "AND (LOWER(sub.type) LIKE '%practical%' OR LOWER(sub.type) LIKE '%applied%' " +
            "     OR sub.name LIKE '%تطبيقي%' OR sub.name LIKE '%عملي%')";

        int totalMaxPrac = 0;
        int totalPassPrac = 0;
        int totalObtainedPrac = 0;
        boolean hasPracData = false;

        try (PreparedStatement psPrac = con.prepareStatement(practicalSql)) {
            psPrac.setString(1, seatNo);
            try (ResultSet rsPrac = psPrac.executeQuery()) {
                while (rsPrac.next()) {
                    hasPracData = true;
                    totalMaxPrac += rsPrac.getInt("max_mark");
                    totalPassPrac += rsPrac.getInt("pass_mark");
                    totalObtainedPrac += Math.max(rsPrac.getInt("obtained_mark"), 0);
                }
            }
        }

        jLabel13.setText("مجموع درجات\nالتطبيقي\nوالامتحان العملي");
        if (hasPracData) {
            jLabel22.setText(toArabic(String.valueOf(totalMaxPrac)));
            jLabel31.setText(toArabic(String.valueOf(totalPassPrac)));
            jLabel40.setText(toArabic(String.valueOf(totalObtainedPrac)));
        } else {
            jLabel22.setText("-");
            jLabel31.setText("-");
            jLabel40.setText("-");
        }

        jLabel42.setText("");
    }

    private void clearForm() {
        jLabel2.setText("");
        jLabel3.setText("");
        jLabel5.setText("");
        jLabel42.setText("");
        jLabel43.setText(toArabic(extraPounds));
        jLabel44.setText(toArabic(extraDate));
        jLabel45.setText(toArabic(extraReceiptNo));
        jLabel46.setText(toArabic(extraDestination));
        currentCenterName = "";
        currentNationalId = "";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PDF PRINT GENERATION
    // ═══════════════════════════════════════════════════════════════════════════

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
            final int width = 1080;
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
                String[] info = studentsData.get(idx);
                String seatNo = info[0];

                if (progressCallback != null) progressCallback.accept(idx + 1, total);

                clearForm();
                loadStudentInfo(seatNo, con);
                loadStudentGrades(seatNo, con);

                getContentPane().revalidate();
                getContentPane().repaint();
                getContentPane().doLayout();

                // ملء الخلفية بالأبيض لتفادي الحواف أو المنطقة السوداء
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRect(0, 0, width, height);
                getContentPane().printAll(g2);

                String rawCenter = currentCenterName.isEmpty() ? "بدون مركز" : currentCenterName;
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

    // ═══════════════════════════════════════════════════════════════════════════
    // CUSTOM VERTICAL LABEL — يكتب النص بالطول (رأسياً) داخل الخانة
    // ═══════════════════════════════════════════════════════════════════════════
    private static class VerticalJLabel extends javax.swing.JLabel {
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

            java.awt.Font font = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15);
            g2.setFont(font);
            g2.setColor(java.awt.Color.BLACK);

            // إذا كان النص يحتوي على مسافات أو أسطر جديدة، قم بتقسيمه إلى أسطر
            String[] lines = text.contains("\n") ? text.split("\n") : text.split(" ");
            
            // 1) انتقل إلى مركز المكوّن
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);

            // 2) أدر 90 درجة عكس عقارب الساعة — النص يُقرأ رأسيًا من الأسفل للأعلى
            g2.rotate(-Math.PI / 2.0);

            if (lines.length == 1) {
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int drawX = -textWidth / 2;
                int drawY = (fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, drawX, drawY);
            } else {
                java.awt.Font fontMulti = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13);
                g2.setFont(fontMulti);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int lineHeight = fm.getHeight();
                int totalHeight = lines.length * lineHeight;
                
                // تظبيط البداية بحيث تظهر جميع الكلمات تحت بعضها ومُمركزة
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

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new VerticalJLabel();
        jLabel7 = new VerticalJLabel();
        jLabel8 = new VerticalJLabel();
        jLabel9 = new VerticalJLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
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
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("سيف رجب سيد رزق اسماعيل محمد");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(217, 210, 270, 80);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("fghkgfhjafgakjsgfka");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(700, 280, 180, 70);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("/");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(420, 350, 10, 60);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("ميكانيكا سيارات ");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(297, 340, 123, 70);

        // Row 1: Subject Names (Y: 466 to 620 -> Y=466, Height=154)
        getContentPane().add(jLabel6); jLabel6.setBounds(768, 466, 79, 154);
        getContentPane().add(jLabel7); jLabel7.setBounds(693, 466, 75, 154);
        getContentPane().add(jLabel8); jLabel8.setBounds(613, 466, 80, 154);
        getContentPane().add(jLabel9); jLabel9.setBounds(533, 466, 80, 154);
        getContentPane().add(jLabel10); jLabel10.setBounds(467, 466, 66, 154);
        getContentPane().add(jLabel11); jLabel11.setBounds(393, 466, 74, 154);
        getContentPane().add(jLabel12); jLabel12.setBounds(293, 466, 100, 154);
        getContentPane().add(jLabel13); jLabel13.setBounds(177, 466, 116, 154);
        getContentPane().add(jLabel14); jLabel14.setBounds(69, 466, 108, 154);

        // Row 2: Max Marks (Y: 620 to 663 -> Y=620, Height=43)
        getContentPane().add(jLabel15); jLabel15.setBounds(768, 620, 79, 43);
        getContentPane().add(jLabel16); jLabel16.setBounds(693, 620, 75, 43);
        getContentPane().add(jLabel17); jLabel17.setBounds(613, 620, 80, 43);
        getContentPane().add(jLabel18); jLabel18.setBounds(533, 620, 80, 43);
        getContentPane().add(jLabel19); jLabel19.setBounds(467, 620, 66, 43);
        getContentPane().add(jLabel20); jLabel20.setBounds(393, 620, 74, 43);
        getContentPane().add(jLabel21); jLabel21.setBounds(293, 620, 100, 43);
        getContentPane().add(jLabel22); jLabel22.setBounds(177, 620, 116, 43);
        getContentPane().add(jLabel23); jLabel23.setBounds(69, 620, 108, 43);

        // Row 3: Pass Marks (Y: 663 to 707 -> Y=663, Height=44)
        getContentPane().add(jLabel24); jLabel24.setBounds(768, 663, 79, 44);
        getContentPane().add(jLabel25); jLabel25.setBounds(693, 663, 75, 44);
        getContentPane().add(jLabel26); jLabel26.setBounds(613, 663, 80, 44);
        getContentPane().add(jLabel27); jLabel27.setBounds(533, 663, 80, 44);
        getContentPane().add(jLabel28); jLabel28.setBounds(467, 663, 66, 44);
        getContentPane().add(jLabel29); jLabel29.setBounds(393, 663, 74, 44);
        getContentPane().add(jLabel30); jLabel30.setBounds(293, 663, 100, 44);
        getContentPane().add(jLabel31); jLabel31.setBounds(177, 663, 116, 44);
        getContentPane().add(jLabel32); jLabel32.setBounds(69, 663, 108, 44);

        // Row 4: Obtained Marks (Y: 707 to 751 -> Y=707, Height=44)
        getContentPane().add(jLabel33); jLabel33.setBounds(768, 707, 79, 44);
        getContentPane().add(jLabel34); jLabel34.setBounds(693, 707, 75, 44);
        getContentPane().add(jLabel35); jLabel35.setBounds(613, 707, 80, 44);
        getContentPane().add(jLabel36); jLabel36.setBounds(533, 707, 80, 44);
        getContentPane().add(jLabel37); jLabel37.setBounds(467, 707, 66, 44);
        getContentPane().add(jLabel38); jLabel38.setBounds(393, 707, 74, 44);
        getContentPane().add(jLabel39); jLabel39.setBounds(293, 707, 100, 44);
        getContentPane().add(jLabel40); jLabel40.setBounds(177, 707, 116, 44);
        getContentPane().add(jLabel41); jLabel41.setBounds(69, 707, 108, 44);

        jLabel42.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        getContentPane().add(jLabel42);
        jLabel42.setBounds(920, 845, 110, 40);

        // 1. جنيهاً فقط (X=820, Y=847)
        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        getContentPane().add(jLabel43);
        jLabel43.setBounds(820, 847, 160, 40);

        // 2. بموجب قسيمة رقم 33 ع.ح (X=200, Y=847 -> إزاحة يساراً بعد كلمة ع.ح لمنع التداخل)
        jLabel45.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        getContentPane().add(jLabel45);
        jLabel45.setBounds(200, 847, 200, 40);

        // 3. بتاريخ (X=720, Y=895 -> بجوار كلمة بتاريخ)
        jLabel44.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        getContentPane().add(jLabel44);
        jLabel44.setBounds(720, 895, 160, 40);

        // 4. وذلك لتقديمه إلى (X=160, Y=895 -> إزاحة إضافية يساراً لعدم ملامسة كلمة "إلى")
        jLabel46.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        getContentPane().add(jLabel46);
        jLabel46.setBounds(160, 895, 450, 40);

        // 5. تغطية رقم 43 القديم في الخلفية المطبوعة واستبداله برقم 33 (٣٣)
        jLabel47.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabel47.setText(toArabic("33"));
        jLabel47.setOpaque(true);
        jLabel47.setBackground(java.awt.Color.WHITE);
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        getContentPane().add(jLabel47);
        jLabel47.setBounds(547, 847, 45, 30);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ChatGPT Image Aug 2, 2026, 07_02_00 AM.png"))); // NOI18N
        getContentPane().add(jLabel1);
        jLabel1.setBounds(0, 0, 1080, 1480);

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
        java.awt.EventQueue.invokeLater(() -> new NewJFrame().setVisible(true));
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
