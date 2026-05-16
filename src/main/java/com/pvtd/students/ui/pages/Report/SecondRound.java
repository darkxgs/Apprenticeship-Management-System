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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Seif
 */
public class SecondRound extends javax.swing.JFrame {
    public boolean isCancelled = false;

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(SecondRound.class.getName());

    /**
     * Creates new form SecondRound
     */
    public SecondRound() {
        this(null, null);
    }

    public SecondRound(String examMonth, String admissionMonth) {
        initComponents();

        jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable2.setFillsViewportHeight(true);
        JTableHeader header = jTable2.getTableHeader();

        // إنشاء منسق خلايا لضبط النص في المنتصف واختيار الخط مع خلفية بيضاء ثابتة
        javax.swing.table.DefaultTableCellRenderer centerCellRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(java.awt.Color.WHITE); // Make all rows white
                setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                c.setFont(new Font("Tahoma", Font.PLAIN, 22));
                return c;
            }
        };

        for (int col = 0; col < jTable2.getColumnCount(); col++) {
            if (col == 1) continue; // Skip custom renderer column
            jTable2.getColumnModel().getColumn(col).setCellRenderer(centerCellRenderer);
        }

        if (jTable2.getColumnCount() >= 7) {
            jTable2.getColumnModel().getColumn(0).setHeaderValue("<html><center>حالة<br>التلميذ</center></html>");
            jTable2.getColumnModel().getColumn(1).setHeaderValue("<html><center>مواد الدور الثاني</center></html>");
            jTable2.getColumnModel().getColumn(2).setHeaderValue("<html><center>رقم<br>الجلوس</center></html>");
            jTable2.getColumnModel().getColumn(3).setHeaderValue("<html><center>رقم<br>التسجيل</center></html>");

            jTable2.getColumnModel().getColumn(0).setPreferredWidth(100); // حالة التلميذ
            jTable2.getColumnModel().getColumn(1).setPreferredWidth(350); // مواد الدور الثاني
            jTable2.getColumnModel().getColumn(2).setPreferredWidth(150); // رقم الجلوس
            jTable2.getColumnModel().getColumn(3).setPreferredWidth(150); // رقم التسجيل
            jTable2.getColumnModel().getColumn(4).setPreferredWidth(300); // المهنة
            jTable2.getColumnModel().getColumn(5).setPreferredWidth(250); // الاسم
            jTable2.getColumnModel().getColumn(6).setPreferredWidth(50); // م
        }

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                c.setBackground(new Color(204, 255, 255));
                c.setForeground(Color.BLACK); // لون النص
                c.setFont(new Font("Tahoma", Font.BOLD, 18));

                return c;
            }
        });

        if (examMonth == null || admissionMonth == null) {
            String month = chooseMonth();
            if (month == null) {
                this.isCancelled = true;
                return;
            }
            examMonth = month;
            admissionMonth = "اكتوبر"; // Fallback
        }

        jLabel10.setText("دفعة قبول : " + admissionMonth + " وما قبلها");
        jLabel11.setText("المنعقد في : " + examMonth);

        regoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        system.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

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
                "يناير", "فبراير", "مارس", "أبريل",
                "مايو", "يونيو", "يوليو", "أغسطس",
                "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };

        return (String) JOptionPane.showInputDialog(
                this,
                "اختر شهر الشهادة",
                "تاريخ الشهادة",
                JOptionPane.QUESTION_MESSAGE,
                null,
                months,
                months[7]);
    }

    public void loadStudents(String center) {

        try (Connection con = DatabaseConnection.getConnection()) {

            String sql = """
                    SELECT name, profession, registration_no, seat_no
                    FROM students
                    WHERE center_name = ?
                    ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC
                    """;

            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, center);

            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();

            model.setRowCount(0);

            int i = 1;

            while (rs.next()) {
                String name = rs.getString("name");
                String prof = rs.getString("profession");

                name = (name == null) ? "" : name;
                prof = (prof == null) ? "" : prof;

                // إحاطة النصوص بعلامات HTML وتوسيطها للسماح بتعدد الأسطر والتفاف النص
                String htmlName = "<html><center>" + name.trim() + "</center></html>";
                String htmlProf = "<html><center>" + prof.trim() + "</center></html>";

                String seatNo = rs.getString("seat_no");
                String[] failedArr = getFailedSubjectsForSeat(seatNo);

                model.addRow(new Object[] {
                        "دور ثاني", // 0: حالة التلميذ
                        failedArr,   // 1: مواد الدور الثاني
                        seatNo,      // 2: رقم الجلوس
                        rs.getString("registration_no"), // 3: رقم التسجيل
                        htmlProf, // 4: المهنة
                        htmlName, // 5: الاسم
                        i++ // 6: م
                });
            }

            // 👇 مهم جدا
            resizeTable();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resizeTable() {
        // This method is now replaced by buildPagePanel logic for A3 PDF generation
        // but remains for basic UI refresh if needed.
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    /**
     * Builds a standardized A3 Landscape panel (2800x1980) with dynamic row
     * scaling.
     */
    public void buildPagePanel(int rowCount) {
        jPanel1.setLayout(null);
        jPanel1.setPreferredSize(new java.awt.Dimension(1400, 1980));
        jPanel1.setSize(1400, 1980);
        jPanel1.setBackground(java.awt.Color.WHITE);

        // Logo (Top Left)
        jLabel8.setBounds(50, 40, 120, 120);

        // Ministry Info (Right Top) - محاذاة أقصى اليمين لتجنب التداخل
        int rAlignX = 1000; // تم تحريكه لليمين أكثر
        int rWidth = 380;
        jLabel1.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel3.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel4.setFont(new Font("Tahoma", Font.BOLD, 20));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jLabel1.setBounds(rAlignX, 10, rWidth, 30);
        jLabel2.setBounds(rAlignX, 40, rWidth, 30);
        jLabel3.setBounds(rAlignX, 70, rWidth, 30);
        jLabel4.setBounds(rAlignX, 100, rWidth, 30);

        // Region, Center, System below ministry
        jLabel7.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel9.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel12.setFont(new Font("Tahoma", Font.BOLD, 20));
        regoin.setFont(new Font("Tahoma", Font.BOLD, 20));
        cent.setFont(new Font("Tahoma", Font.BOLD, 20));
        system.setFont(new Font("Tahoma", Font.BOLD, 20));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        regoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        system.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        int labelsX = 1250; 
        int dataX = 900;
        jLabel7.setBounds(labelsX, 140, 120, 30);
        regoin.setBounds(dataX, 140, 350, 30);
        jLabel9.setBounds(labelsX, 170, 120, 30);
        cent.setBounds(dataX, 170, 350, 30);
        jLabel12.setBounds(labelsX, 200, 120, 30);
        system.setBounds(dataX, 200, 350, 30);

        // Center Titles - التوسيع والتوسيط بدون تداخل
        jLabel5.setFont(new Font("Tahoma", Font.BOLD, 24));
        jLabel6.setFont(new Font("Tahoma", Font.BOLD, 36));
        jLabel6.setText("تلاميذ راسبون ولهم حق دخول الدور الثاني");
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setBounds(400, 40, 550, 45);
        jLabel6.setBounds(400, 90, 550, 60);

        // Dates
        jLabel10.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel11.setFont(new Font("Tahoma", Font.BOLD, 20));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setBounds(400, 160, 550, 30);
        jLabel11.setBounds(400, 190, 550, 30);

        // Table Content - تفعيل الخطوط (Grid)
        int tableY = 260;
        int tableWidth = 1350;
        jScrollPane1.setBounds(25, tableY, tableWidth, 1550);
        
        jTable2.setShowGrid(true);
        jTable2.setGridColor(new java.awt.Color(150, 150, 150));
        
        // ضبط عرض الأعمدة
        jTable2.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTable2.getColumnModel().getColumn(1).setPreferredWidth(350);
        jTable2.getColumnModel().getColumn(2).setPreferredWidth(150);
        jTable2.getColumnModel().getColumn(3).setPreferredWidth(150);
        jTable2.getColumnModel().getColumn(4).setPreferredWidth(300);
        jTable2.getColumnModel().getColumn(5).setPreferredWidth(250);
        jTable2.getColumnModel().getColumn(6).setPreferredWidth(50);

        // Custom renderer for مواد الدور الثاني column
        jTable2.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String[] arr = val instanceof String[] ? (String[]) val : new String[0];
                java.util.List<String> list = new java.util.ArrayList<>();
                if (arr != null) {
                    for (String s : arr) {
                        if (s != null && !s.trim().isEmpty()) {
                            // Clean up text if it contains UI placeholders
                            String cleaned = s.replaceAll("\\(اضغط للتعديل\\)", "").trim();
                            if (!cleaned.isEmpty()) list.add(cleaned);
                        }
                    }
                }
                
                int count = Math.max(1, list.size());
                JPanel panel = new JPanel(new java.awt.GridLayout(1, count, 0, 0));
                panel.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);
                panel.setBackground(Color.WHITE);
                
                for (int i = 0; i < count; i++) {
                    JLabel lbl = new JLabel();
                    lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    lbl.setFont(new Font("Tahoma", Font.BOLD, 14));
                    lbl.setOpaque(true);
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                    String text = (i < list.size()) ? list.get(i) : "";
                    lbl.setText("<html><center>" + text + "</center></html>");
                    // Add borders between boxes (on the left of each box except the last one in RTL)
                    int leftBorder = (i < count - 1) ? 1 : 0;
                    lbl.setBorder(javax.swing.BorderFactory.createMatteBorder(0, leftBorder, 0, 0, Color.BLACK));
                    panel.add(lbl);
                }
                panel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));
                return panel;
            }
        });

        // Footer
        int footerY = 1850;
        jSeparator1.setBounds(25, footerY - 10, tableWidth, 5);
        
        jLabel14.setFont(new Font("Tahoma", Font.BOLD, 22));
        jLabel18.setFont(new Font("Tahoma", Font.BOLD, 22));
        jLabel17.setFont(new Font("Tahoma", Font.BOLD, 22));
        jLabel16.setFont(new Font("Tahoma", Font.BOLD, 22));

        jLabel14.setBounds(1150, footerY, 200, 40);
        jLabel18.setBounds(850, footerY, 200, 40);
        jLabel17.setBounds(550, footerY, 200, 40);
        jLabel16.setBounds(50, footerY, 350, 40);

        jPanel1.doLayout();
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    public void createPDF() {
        try {
            int rowsPerPage = 32; // Updated limit as requested

            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            Vector<Vector> allData = new Vector<>(model.getDataVector());

            int totalRows = allData.size();
            int pageCount = (int) Math.ceil((double) totalRows / rowsPerPage);

            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, new FileOutputStream("report.pdf"));
            document.open();

            int globalIndex = 1;

            for (int page = 0; page < pageCount; page++) {
                model.setRowCount(0);

                int start = page * rowsPerPage;
                int end = Math.min(start + rowsPerPage, totalRows);

                for (int i = start; i < end; i++) {
                    java.util.Vector row = allData.get(i);
                    String seatNo = String.valueOf(row.get(1)); // index 1 is seat_no in ScoundRoundFramePage model
                    String[] failedArr = getFailedSubjectsForSeat(seatNo);
                    java.util.Vector newRow = new java.util.Vector();
                    newRow.add("دور ثاني"); // 0
                    newRow.add(failedArr);   // 1
                    newRow.add(seatNo);      // 2
                    newRow.add(row.get(2));  // 3: registration_no
                    newRow.add(row.get(3));  // 4: profession
                    newRow.add(row.get(4));  // 5: name
                    newRow.add(globalIndex++); // 6: م
                    model.addRow(newRow);
                }

                while (model.getRowCount() < 32) {
                    model.addRow(new Object[] { "", new String[6], "", "", "", "", "" });
                }

                // Apply A4 standardization
                // jLabel13.setText("صفحة " + toArabicNumbers(String.valueOf(page + 1)) + " من "
                // + toArabicNumbers(String.valueOf(pageCount)));
                buildPagePanel(model.getRowCount());

                int width = 1400;
                int height = 1980;

                jPanel1.setSize(width, height);
                jTable2.setSize(jTable2.getPreferredSize());
                jPanel1.validate();
                jPanel1.doLayout();

                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = img.createGraphics();

                // Enable high quality rendering
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                jPanel1.printAll(g2d);
                g2d.dispose();

                Image pdfImg = Image.getInstance(img, null);
                // Force it to fill the entire A3 page
                pdfImg.scaleAbsolute(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                pdfImg.setAbsolutePosition(0, 0);

                document.add(pdfImg);

                if (page < pageCount - 1) {
                    document.newPage();
                }
            }

            // Restore data
            model.setRowCount(0);
            for (Vector row : allData) {
                model.addRow(row);
            }

            document.close();
            Desktop.getDesktop().open(new File("report.pdf"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCenterData(String centerName) {

        try {

            String sql = """
                    SELECT s.region, NVL(p.exam_system, s.exam_system) as exam_system
                    FROM students s
                    LEFT JOIN professions p ON TRIM(p.name) = TRIM(s.profession)
                    WHERE s.center_name = ?
                    AND ROWNUM = 1
                    """;

            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, centerName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String regio = rs.getString("region");
                String syste = rs.getString("exam_system");

                regoin.setText(regio); // المنطقة
                system.setText(syste); // النظام
            } else {
                regoin.setText("—");
                system.setText("—");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Integer secondRoundCode = null;
    private boolean codeLoaded = false;

    private Integer getSecondRoundCode() {
        if (!codeLoaded) {
            try {
                java.util.Map<String, Integer> codes = com.pvtd.students.services.StatusesService.getAllStatusesWithCodes();
                secondRoundCode = codes.get("دور ثاني");
            } catch (Exception e) {
                e.printStackTrace();
            }
            codeLoaded = true;
        }
        return secondRoundCode;
    }

    private String[] getFailedSubjectsForSeat(String seatNo) {
        String[] res = new String[6];
        java.util.Arrays.fill(res, "");
        if (seatNo == null || seatNo.trim().isEmpty() || seatNo.equals("null")) return res;
        
        Integer srCode = getSecondRoundCode();

        try (Connection con = DatabaseConnection.getConnection()) {
            int studentId = -1;
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM students WHERE seat_no = ?")) {
                ps.setString(1, seatNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) studentId = rs.getInt("id");
                }
            }
            if (studentId == -1) return res;

            java.util.List<String> theoryFailed = new java.util.ArrayList<>();
            boolean failedPractical = false;
            boolean failedApplied = false;

            String sql = """
                SELECT sub.name, sub.type, sub.pass_mark, NVL(sg.obtained_mark, 0) as mark
                FROM subjects sub
                JOIN specializations sp ON sp.id = sub.specialization_id
                JOIN students st ON TRIM(st.profession) = TRIM(sp.name)
                LEFT JOIN student_grades sg ON sg.subject_id = sub.id AND sg.student_id = st.id
                WHERE st.id = ? AND sub.subject_type IS NULL
                ORDER BY sub.id
                """;
            try (PreparedStatement ps2 = con.prepareStatement(sql)) {
                ps2.setInt(1, studentId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) {
                        String sName = rs2.getString("name");
                        String sType = rs2.getString("type");
                        int mark = rs2.getInt("mark");
                        int passMark = rs2.getInt("pass_mark");
                        
                        boolean isSecondRound = (mark < passMark && mark >= 0);
                        if (srCode != null && mark == srCode) isSecondRound = true;

                        if (isSecondRound && sName != null && !sName.isBlank()) {
                            if ("نظري".equals(sType)) theoryFailed.add(sName);
                            else if ("تطبيقي".equals(sType)) failedApplied = true;
                            else failedPractical = true;
                        }
                    }
                }
            }
            java.util.List<String> allFailed = new java.util.ArrayList<>();
            allFailed.addAll(theoryFailed);
            if (failedPractical) allFailed.add("عملي");
            if (failedApplied)   allFailed.add("تطبيقي");

            for (int i = 0; i < 6 && i < allFailed.size(); i++) {
                res[i] = allFailed.get(i);
            }
            // If more than 6, join the rest in the last box
            if (allFailed.size() > 6) {
                res[5] = String.join("/", allFailed.subList(5, allFailed.size()));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }

    public void createPDFGroupedBySystem(
            java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem,
            String centerName, String regionName, boolean isFirstCall) {
        try {
            int rowsPerPage = 32;

            String folderStr = "التقارير/بدون درجات/دور ثاني";
            java.io.File folder = new java.io.File(folderStr);
            if (!folder.exists())
                folder.mkdirs();
            String sanitizedRegion = (regionName != null && !regionName.trim().isEmpty())
                    ? regionName.replace("/", "_").replace("\\", "_").replace(":", "_")
                    : "غير_محدد";
            String filePath = folderStr + "/" + sanitizedRegion + ".pdf";

            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            boolean firstSystem = true;
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();

            for (java.util.Map.Entry<String, java.util.List<java.util.Vector>> sysEntry : bySystem.entrySet()) {
                String sysName = sysEntry.getKey();
                java.util.List<java.util.Vector> rows = sysEntry.getValue();

                system.setText(sysName);
                cent.setText(centerName);
                regoin.setText(regionName);

                int totalRows = rows.size();
                int pageCount = (int) Math.ceil((double) totalRows / rowsPerPage);
                if (pageCount == 0)
                    pageCount = 1;

                int globalIndex = 1;
                for (int page = 0; page < pageCount; page++) {
                    if (!firstSystem || page > 0)
                        document.newPage();
                    firstSystem = false;

                    model.setRowCount(0);
                    int start = page * rowsPerPage;
                    int end = Math.min(start + rowsPerPage, totalRows);
                    for (int i = start; i < end; i++) {
                        java.util.Vector row = new java.util.Vector(rows.get(i));
                        String seatNo = String.valueOf(row.get(1)); // index 1 in ScoundRoundFramePage model
                        String[] failedArr = getFailedSubjectsForSeat(seatNo);
                        
                        java.util.Vector newRow = new java.util.Vector();
                        newRow.add("دور ثاني"); // 0
                        newRow.add(failedArr);   // 1
                        newRow.add(seatNo);      // 2
                        newRow.add(row.get(2));  // 3: registration_no
                        newRow.add(row.get(3));  // 4: profession
                        newRow.add(row.get(4));  // 5: name
                        newRow.add(globalIndex++); // 6: م
                        model.addRow(newRow);
                    }
                    while (model.getRowCount() < 32) {
                        model.addRow(new Object[] { "", new String[6], "", "", "", "", "" });
                    }

                    jLabel13.setText("صفحة " + toArabicNumbers(String.valueOf(page + 1))
                            + " من " + toArabicNumbers(String.valueOf(pageCount)));
                    buildPagePanel(model.getRowCount());

                    int width = 1400, height = 1980;
                    jPanel1.setSize(width, height);
                    jTable2.setSize(jTable2.getPreferredSize());
                    jPanel1.validate();
                    jPanel1.doLayout();

                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = img.createGraphics();
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                    g2d.setColor(java.awt.Color.WHITE);
                    g2d.fillRect(0, 0, width, height);

                    jPanel1.printAll(g2d);
                    g2d.dispose();

                    Image pdfImg = Image.getInstance(img, null);
                    pdfImg.scaleAbsolute(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                    pdfImg.setAbsolutePosition(0, 0);
                    document.add(pdfImg);
                }
            }
            document.close();
            Desktop.getDesktop().open(new java.io.File(folderStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
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
        regoin = new javax.swing.JLabel();
        cent = new javax.swing.JLabel();
        system = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(30, 60, 114));
        jLabel2.setText("مصلحة الكفاية الانتاجية والتدريب المهنى");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(740, 50, 250, 20);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(30, 60, 114));
        jLabel3.setText("الرئاسة العامة للامتحانات لدبلوم التلمذة الصناعية");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(720, 70, 290, 20);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(30, 60, 114));
        jLabel4.setText("لجنة النظام والمراقبة");
        jPanel1.add(jLabel4);
        jLabel4.setBounds(790, 90, 140, 20);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(30, 60, 114));
        jLabel1.setText("وزارة الصناعة");
        jPanel1.add(jLabel1);
        jLabel1.setBounds(820, 30, 90, 20);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(30, 60, 114));
        jLabel5.setText("نتائج أمتحان دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel5);
        jLabel5.setBounds(421, 86, 220, 20);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(42, 82, 152));
        jLabel16.setText("تلاميذ دور ثاني");
        jPanel1.add(jLabel6);
        jLabel6.setBounds(442, 122, 170, 60);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(30, 60, 114));
        jLabel10.setText("دفعة قبول : أكتوبر وما قبلها");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(0, 70, 250, 20);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(30, 60, 114));
        jLabel11.setText("المنعقد فى : مايو");
        jPanel1.add(jLabel11);
        jLabel11.setBounds(20, 90, 210, 20);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N
        jPanel1.add(jLabel8);
        jLabel8.setBounds(11, 6, 60, 58);

        jLabel7.setText("المنطقة /");
        jPanel1.add(jLabel7);
        jLabel7.setBounds(930, 130, 70, 20);

        jLabel9.setText("مــــركز/");
        jPanel1.add(jLabel9);
        jLabel9.setBounds(940, 160, 60, 20);

        jLabel12.setText("النظام /");
        jPanel1.add(jLabel12);
        jLabel12.setBounds(940, 190, 70, 30);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(42, 82, 152));
        jLabel16.setText("رئيس لجنة النظام والمراقبة");
        jPanel1.add(jLabel16);
        jLabel16.setBounds(60, 1230, 180, 30);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(42, 82, 152));
        jLabel17.setText("راجعه                ");
        jPanel1.add(jLabel17);
        jLabel17.setBounds(420, 1230, 140, 30);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(42, 82, 152));
        jLabel14.setText("كتبه                 ");
        jPanel1.add(jLabel14);
        jLabel14.setBounds(990, 1230, 160, 30);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(42, 82, 152));
        jLabel18.setText("املاه                ");
        jPanel1.add(jLabel18);
        jLabel18.setBounds(730, 1230, 160, 30);

        jSeparator1.setForeground(new java.awt.Color(255, 153, 0));
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(0, 1210, 1310, 10);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null }
                },
                new String[] {
                        "حالة التلميذ", "مواد الدور الثاني", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م"
                }));
        jScrollPane1.setViewportView(jTable2);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(0, 240, 1160, 960);

        regoin.setText("jLabel13");
        jPanel1.add(regoin);
        regoin.setBounds(770, 130, 150, 20);

        cent.setText("jLabel15");
        jPanel1.add(cent);
        cent.setBounds(770, 160, 170, 20);

        system.setText("jLabel19");
        jPanel1.add(system);
        system.setBounds(770, 190, 170, 30);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1313,
                                javax.swing.GroupLayout.PREFERRED_SIZE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1313, Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
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
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new SecondRound().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel cent;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    public javax.swing.JTable jTable2;
    private javax.swing.JLabel regoin;
    private javax.swing.JLabel system;
    // End of variables declaration//GEN-END:variables

    public String getRegionText() {
        return regoin.getText();
    }
}
