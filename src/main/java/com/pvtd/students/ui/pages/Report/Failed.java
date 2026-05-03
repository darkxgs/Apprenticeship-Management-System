/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
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

/**
 *
 * @author Seif
 */
public class Failed extends javax.swing.JFrame {
        private String manualExamMonth = null;
        private String manualAdmissionMonth = null;
        public boolean isCancelled = false;


        private static final java.util.logging.Logger logger = java.util.logging.Logger
                        .getLogger(Failed.class.getName());

        /**
         * Creates new form Failed
         */
        public Failed() {
                initComponents();
                initTableLogic();
        }

        public Failed(String examMonth, String admissionMonth) {
                this.manualExamMonth = examMonth;
                this.manualAdmissionMonth = admissionMonth;
                initComponents();
                initTableLogic();
        }

        private void initTableLogic() {

                // احسب الارتفاع الحقيقي

                jTable2.setFillsViewportHeight(true);

                this.setBackground(Color.WHITE);
                this.setSize(1200, 800);
                jScrollPane2.setPreferredSize(new Dimension(500, 900));
                jTable2.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                jTable2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

                jTable2.setShowGrid(true);
                jTable2.setGridColor(Color.GRAY);

                jTable2.setRowHeight(38); // ارتفاع الصف ليتسع للأسماء الكبيرة

                jTable2.setShowHorizontalLines(true);
                jTable2.setShowVerticalLines(true);

                jTable2.setShowVerticalLines(true);

                // 7-column model
                DefaultTableModel model7 = new DefaultTableModel(
                    new String[] { "مواد الدور الثاني", "حالة التلميذ", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م" }, 0
                );
                jTable2.setModel(model7);

                jTable2.getColumnModel().getColumn(0).setHeaderValue("<html><center>مواد الدور<br>الثاني</center></html>");
                jTable2.getColumnModel().getColumn(1).setHeaderValue("<html><center>حالة<br>التلميذ</center></html>");
                jTable2.getColumnModel().getColumn(2).setHeaderValue("<html><center>رقم<br>الجلوس</center></html>");
                jTable2.getColumnModel().getColumn(3).setHeaderValue("<html><center>رقم<br>التسجيل</center></html>");

                jTable2.getColumnModel().getColumn(0).setPreferredWidth(500); // مواد الدور الثاني
                jTable2.getColumnModel().getColumn(1).setPreferredWidth(90);  // حالة التلميذ
                jTable2.getColumnModel().getColumn(2).setPreferredWidth(120); // رقم الجلوس
                jTable2.getColumnModel().getColumn(3).setPreferredWidth(120); // رقم التسجيل
                jTable2.getColumnModel().getColumn(4).setPreferredWidth(210); // المهنة
                jTable2.getColumnModel().getColumn(5).setPreferredWidth(270); // الاسم
                jTable2.getColumnModel().getColumn(6).setPreferredWidth(60);  // م

                javax.swing.table.DefaultTableCellRenderer centerCellRenderer = new javax.swing.table.DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus,
                                        int row, int column) {
                                String txt = (value == null) ? "" : value.toString();
                                if (!txt.toLowerCase().startsWith("<html>") && txt.length() > 0) {
                                    txt = "<html><div align='right' style='padding-right:10px;'>" + txt + "</div></html>";
                                }
                                Component c = super.getTableCellRendererComponent(table, txt, isSelected, hasFocus,
                                                row, column);
                                c.setBackground(java.awt.Color.WHITE);
                                setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                                // col 0 = مواد الدور الثاني: scale font by content length
                                if (column == 0) {
                                    int lineCount = txt.split("<br/>|<br>").length;
                                    if (lineCount > 5) {
                                        c.setFont(new Font("Tahoma", Font.BOLD, 16));
                                    } else if (lineCount > 3) {
                                        c.setFont(new Font("Tahoma", Font.BOLD, 19));
                                    } else {
                                        c.setFont(new Font("Tahoma", Font.BOLD, 23));
                                    }
                                } else {
                                    c.setFont(new Font("Tahoma", Font.PLAIN, 21));
                                }
                                return c;
                        }
                };

                for (int col = 0; col < jTable2.getColumnCount(); col++) {
                        jTable2.getColumnModel().getColumn(col).setCellRenderer(centerCellRenderer);
                }

                JTableHeader header = jTable2.getTableHeader();
                header.setDefaultRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus,
                                        int row, int column) {
                                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                                c.setFont(new Font("Tahoma", Font.BOLD, 18));
                                c.setBackground(new Color(204, 255, 255));
                                c.setForeground(Color.BLACK);
                                return c;
                        }
                });

                String examMonth = (manualExamMonth != null) ? manualExamMonth : chooseMonth("اختر المنعقد فيه", "يوليو");
                String admissionMonth = (manualAdmissionMonth != null) ? manualAdmissionMonth : chooseMonth("اختر شهر دفعة القبول", "أكتوبر");

                if (examMonth == null || admissionMonth == null) {
                        this.isCancelled = true;
                        return;
                }

                int year = java.time.Year.now().getValue();
                String arabicYear = toArabicNumbers(String.valueOf(year));

                jLabel6.setText("تلاميذ راسبون ولهم حق دخول الدور الثاني");
                jLabel6.setForeground(new Color(42, 82, 152)); // Blue color from image

                jLabel10.setText("دفعة قبول : " + admissionMonth + " لسنة ٢٠١٩ وما قبلها"); // Fixed as per image
                jLabel11.setText("المنعقد في : " + examMonth + " لسنة " + arabicYear);

                regoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                cent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                system.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                
                jLabel1.setText("وزارة التجارة والصناعة");
                jLabel2.setText("مصلحة الكفاية الانتاجية والتدريب المهني");
                jLabel3.setText("الرئاسة العامة للامتحانات لدبلوم التلمذة الصناعية");
                jLabel4.setText("لجنة النظام والمراقبة");

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

        private String chooseMonth(String title, String defaultMonth) {

                String[] months = {
                                "يناير", "فبراير", "مارس", "أبريل",
                                "مايو", "يونيو", "يوليو", "أغسطس",
                                "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
                };

                return (String) JOptionPane.showInputDialog(
                                this,
                                title,
                                "تحديد الموعد",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                months,
                                defaultMonth);
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
                                String htmlName = "<html><RIGHT>" + name.trim() + "</RIGHT></html>";
                                String htmlProf = "<html><RIGHT>" + prof.trim() + "</RIGHT></html>";

                                model.addRow(new Object[] {
                                                "",                              // 0: مواد الدور الثاني (فارغ)
                                                "راسب",                         // 1: حالة التلميذ
                                                rs.getString("seat_no"),        // 2: رقم الجلوس
                                                rs.getString("registration_no"), // 3: رقم التسجيل
                                                htmlProf,                        // 4: المهنة
                                                htmlName,                        // 5: الاسم
                                                i++                              // 6: م
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

        public void buildPagePanel(int rowCount) {
                // A4 portrait canvas: 1400 x 1980 px
                final int PANEL_W = 1400;
                final int PANEL_H = 1980;
                final int HEADER_H = 220;   // space for ministry + title + dates
                final int FOOTER_H = 80;    // separator + signature labels
                final int MARGIN   = 15;

                jPanel1.setLayout(null);
                jPanel1.setPreferredSize(new java.awt.Dimension(PANEL_W, PANEL_H));
                jPanel1.setSize(PANEL_W, PANEL_H);
                jPanel1.setBackground(java.awt.Color.WHITE);

                // ── Logo (top-left) ──────────────────────────────────────
                jLabel8.setBounds(50, 20, 100, 100);

                // ── Page counter (left, below logo) ─────────────────────
                jLabel13.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel13.setBounds(30, 160, 250, 25);

                // ── Ministry info (top-right) ────────────────────────────
                jLabel1.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel2.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel3.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel4.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel1.setBounds(1000, 10,  390, 25);
                jLabel2.setBounds(1000, 35,  390, 25);
                jLabel3.setBounds(1000, 60,  390, 25);
                jLabel4.setBounds(1000, 85,  390, 25);

                // ── Region / Center / System (right block) ───────────────
                jLabel7.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel9.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel12.setFont(new Font("Tahoma", Font.BOLD, 18));
                regoin.setFont(new Font("Tahoma", Font.BOLD, 18));
                cent.setFont(new Font("Tahoma", Font.BOLD, 18));
                system.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                regoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                cent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                system.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                jLabel7.setBounds(1250, 120, 120, 30);  regoin.setBounds(900, 120, 350, 30);
                jLabel9.setBounds(1250, 150, 120, 30);  cent.setBounds(900, 150, 350, 30);
                jLabel12.setBounds(1250, 180, 120, 30); system.setBounds(900, 180, 350, 30);

                // ── Center titles ────────────────────────────────────────
                jLabel5.setFont(new Font("Tahoma", Font.BOLD, 18));
                jLabel6.setFont(new Font("Tahoma", Font.BOLD, 28));
                jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel5.setBounds(400, 40,  600, 35);
                jLabel6.setBounds(400, 80,  600, 50);

                // ── Dates ────────────────────────────────────────────────
                jLabel10.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel11.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                jLabel10.setBounds(400, 140, 600, 25);
                jLabel11.setBounds(400, 170, 600, 25);

                // ── Table: fills space between header and footer ──────────
                int tableY        = HEADER_H;
                int tableHeight   = PANEL_H - HEADER_H - FOOTER_H - MARGIN * 2;
                int tableHeaderH  = 45; // JTable header row height
                // rowH = remaining space after header, divided by 10 rows
                int rowH = (tableHeight - tableHeaderH) / 10;
                jTable2.setRowHeight(rowH);
                jScrollPane2.setBounds(MARGIN, tableY, PANEL_W - MARGIN * 2, tableHeight);

                // ── Footer ───────────────────────────────────────────────
                int separatorY = PANEL_H - FOOTER_H - MARGIN;
                jSeparator1.setBounds(MARGIN, separatorY, PANEL_W - MARGIN * 2, 3);
                int labelsY = separatorY + 8;
                jLabel16.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel17.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel18.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel14.setFont(new Font("Tahoma", Font.BOLD, 16));
                jLabel14.setBounds(1150, labelsY, 200, 40);
                jLabel18.setBounds(850,  labelsY, 200, 40);
                jLabel17.setBounds(550,  labelsY, 200, 40);
                jLabel16.setBounds(50,   labelsY, 300, 40);

                jPanel1.doLayout();
                jPanel1.revalidate();
                jPanel1.repaint();
        }

        public void createPDF() {
                try {

                        int rowsPerPage = 10; // Limited to 10 students per page as requested for better spacing

                        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
                        Vector<Vector> originalData = new Vector<>(model.getDataVector());

                        int totalRows = originalData.size();
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
                                        Vector row = new Vector(originalData.get(i));
                                        row.set(1, "راسب"); // 1: حالة التلميذ
                                        row.set(6, globalIndex++); // 6: م
                                        model.addRow(row);
                                }

                                // Fill remaining rows to 10 so table always fills the page
                                while (model.getRowCount() < 10) {
                                        model.addRow(new Object[] { "", "", "", "", "", "", "" });
                                }

                                // Apply A4 standardization
                                jLabel13.setText("صفحة " + toArabicNumbers(String.valueOf(page + 1)) + " من "
                                                + toArabicNumbers(String.valueOf(pageCount)));
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
                                g2d.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                                                java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                                g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                                                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                                g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                                                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                                jPanel1.printAll(g2d);
                                g2d.dispose();

                                Image pdfImg = Image.getInstance(img, null);
                                // Force it to fill the entire A3 page
                                pdfImg.scaleAbsolute(document.getPageSize().getWidth(),
                                                document.getPageSize().getHeight());
                                pdfImg.setAbsolutePosition(0, 0);

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

        /**
         * Generates a combined PDF with separate pages per exam system.
         * rows: LinkedHashMap<systemName, list-of-row-vectors-from-jTable1>
         * Each system group gets its own pages.
         * The combined PDF is opened at the end.
         */
        public void createPDFGroupedBySystem(
                        java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem,
                        String centerName, String regionName, boolean isFirstCall) {
                try {
                        int rowsPerPage = 10; // Limited to 10 students per page for better clarity

                        String folderStr = "التقارير/بدون درجات/راسبين";
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

                        for (java.util.Map.Entry<String, java.util.List<java.util.Vector>> sysEntry : bySystem
                                        .entrySet()) {
                                String sysName = sysEntry.getKey();
                                java.util.List<java.util.Vector> rows = sysEntry.getValue();

                                // Set system label on report
                                system.setText(sysName);
                                cent.setText(centerName);
                                regoin.setText(regionName);

                                int totalRows = rows.size();
                                int pageCount = (int) Math.ceil((double) totalRows / rowsPerPage);
                                if (pageCount == 0)
                                        pageCount = 1;

                                int globalIndex = 1;

                                for (int page = 0; page < pageCount; page++) {
                                        if (!firstSystem || page > 0) {
                                                document.newPage();
                                        }
                                        firstSystem = false;

                                        model.setRowCount(0);
                                        int start = page * rowsPerPage;
                                        int end = Math.min(start + rowsPerPage, totalRows);

                                        for (int i = start; i < end; i++) {
                                                java.util.Vector row = new java.util.Vector(rows.get(i));
                                                // Ensure 7 columns
                                                while (row.size() < 7) row.add("");
                                                if (row.size() > 7) row.setSize(7);
                                                row.set(1, "راسب");      // 1: حالة التلميذ
                                                row.set(6, globalIndex++); // 6: م
                                                model.addRow(row);
                                        }
                                        while (model.getRowCount() < 10) {
                                                model.addRow(new Object[] { "", "", "", "", "", "", "" });
                                        }

                                        jLabel13.setText("صفحة " + toArabicNumbers(String.valueOf(page + 1))
                                                        + " من " + toArabicNumbers(String.valueOf(pageCount)));
                                        buildPagePanel(model.getRowCount());

                                        int width = 1400, height = 1980;
                                        jPanel1.setSize(width, height);
                                        jPanel1.validate();
                                        jPanel1.doLayout();

                                        BufferedImage img = new BufferedImage(width, height,
                                                        BufferedImage.TYPE_INT_RGB);
                                        Graphics2D g2d = img.createGraphics();
                                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                                                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                                                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                                        jPanel1.printAll(g2d);
                                        g2d.dispose();

                                        Image pdfImg = Image.getInstance(img, null);
                                        pdfImg.scaleAbsolute(document.getPageSize().getWidth(),
                                                        document.getPageSize().getHeight());
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

        public void loadCenterData(String centerName) {
                try (Connection con = DatabaseConnection.getConnection()) {
                        String sql = """
                                        SELECT DISTINCT s.region, NVL(p.exam_system, 'نظامي') as exam_system
                                        FROM students s
                                        LEFT JOIN professions p ON TRIM(p.name) = TRIM(s.profession)
                                        WHERE s.center_name = ?
                                        AND ROWNUM = 1
                                        """;

                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setString(1, centerName);
                                try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                                String regio = rs.getString("region");
                                                String syste = rs.getString("exam_system");

                                                regoin.setText(regio != null ? regio : "—");
                                                system.setText(syste != null ? syste : "نظامي");
                                        } else {
                                                regoin.setText("—");
                                                system.setText("نظامي");
                                        }
                                }
                        }

                        if (centerName != null) {
                                cent.setText(centerName);
                        }
                } catch (Exception e) {
                        logger.log(java.util.logging.Level.SEVERE, "Error loading center data", e);
                }
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated
        // Code">//GEN-BEGIN:initComponents
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
                cent = new javax.swing.JLabel();
                system = new javax.swing.JLabel();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

                jPanel1.setBackground(new java.awt.Color(255, 255, 255));

                jTable2.setModel(new javax.swing.table.DefaultTableModel(
                                new Object[][] {
                                                { null, null, null, null, null, null },
                                                { null, null, null, null, null, null },
                                                { null, null, null, null, null, null },
                                                { null, null, null, null, null, null }
                                },
                                new String[] {
                                                "حاله التلميذ", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م"
                                }));
                jScrollPane2.setViewportView(jTable2);

                jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
                jLabel6.setForeground(new java.awt.Color(42, 82, 152));
                jLabel6.setText("تلاميذ راسبون");

                jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel1.setForeground(new java.awt.Color(30, 60, 114));
                jLabel1.setText("وزارة الصناعة");

                jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel2.setForeground(new java.awt.Color(30, 60, 114));
                jLabel2.setText("مصلحة الكفاية الانتاجية والتدريب المهنى");

                jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel3.setForeground(new java.awt.Color(30, 60, 114));
                jLabel3.setText("الرئاسة العامه لامتحنات دبلوم التلمذة الصناعيه");

                jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel4.setForeground(new java.awt.Color(30, 60, 114));
                jLabel4.setText("لجنة النظام والمراقبة");

                jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel5.setForeground(new java.awt.Color(30, 60, 114));
                jLabel5.setText("نتائج أمتحان دبلوم التلمذة الصناعية");

                jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel10.setForeground(new java.awt.Color(30, 60, 114));
                jLabel10.setText("دفعة قبول : أكتوبر لسنة ٢٠٢٣ وما قبلها");

                jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel11.setForeground(new java.awt.Color(30, 60, 114));
                jLabel11.setText("المنعقد فى : يوليو لسنة ٢٠٢٦");

                jLabel8.setIcon(new javax.swing.ImageIcon(
                                getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N

                jLabel7.setText("المنطقة /");

                jLabel9.setText("مــــركز/");

                jLabel12.setText("النظام /");

                jLabel13.setForeground(new java.awt.Color(30, 60, 114));
                jLabel13.setText("صفحة 1 من 1 ");

                jSeparator1.setForeground(new java.awt.Color(255, 102, 0));

                jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel16.setForeground(new java.awt.Color(42, 82, 152));
                jLabel16.setText("رئيس لجنة النظام والمراقبة");

                jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel17.setForeground(new java.awt.Color(42, 82, 152));
                jLabel17.setText("راجعه                ");

                jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel14.setForeground(new java.awt.Color(42, 82, 152));
                jLabel14.setText("كتبه                 ");

                jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
                jLabel18.setForeground(new java.awt.Color(42, 82, 152));
                jLabel18.setText("املاه                ");

                regoin.setText("jLabel15");

                cent.setText("jLabel19");

                system.setText("jLabel20");

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addContainerGap()
                                                                                                .addComponent(jLabel13,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                90,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(21, 21, 21)
                                                                                                                                .addComponent(jLabel8))
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                .addComponent(jLabel10,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                250,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addGap(20, 20, 20)
                                                                                                                                                                .addComponent(jLabel11,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                210,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(jLabel5,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                220,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(50, 50, 50)
                                                                                                                                .addComponent(jLabel6,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                170,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                .addGap(269, 269, 269)
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(80, 80, 80)
                                                                                                                                .addComponent(jLabel1,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                140,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(20, 20, 20)
                                                                                                                                .addComponent(jLabel2,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                250,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                .addComponent(jLabel3,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                290,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(70, 70, 70)
                                                                                                                                .addComponent(jLabel4,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                140,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                                false)
                                                                                                                                                .addComponent(cent,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                                .addComponent(system,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                                .addComponent(regoin,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                220,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                                .addPreferredGap(
                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                .addComponent(jLabel7,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                70,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                .addComponent(jLabel9,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                60,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                .addComponent(jLabel12,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                70,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))))
                                                                .addContainerGap())
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout
                                                                .createSequentialGroup()
                                                                .addGap(93, 93, 93)
                                                                .addComponent(jLabel16)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                189,
                                                                                Short.MAX_VALUE)
                                                                .addComponent(jLabel17,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                160,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(112, 112, 112)
                                                                .addComponent(jLabel18,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                170,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(139, 139, 139)
                                                                .addComponent(jLabel14,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                170,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(120, 120, 120))
                                                .addComponent(jSeparator1)
                                                .addComponent(jScrollPane2));
                jPanel1Layout.setVerticalGroup(
                                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addGap(18, 18, 18)
                                                                                                .addComponent(jLabel8,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                58,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addGap(26, 26, 26)
                                                                                                .addComponent(jLabel10)
                                                                                                .addGap(0, 0, 0)
                                                                                                .addComponent(jLabel11))
                                                                                .addGroup(jPanel1Layout
                                                                                                .createSequentialGroup()
                                                                                                .addContainerGap()
                                                                                                .addComponent(jLabel1)
                                                                                                .addGap(0, 0, 0)
                                                                                                .addComponent(jLabel2)
                                                                                                .addGap(0, 0, 0)
                                                                                                .addComponent(jLabel3)
                                                                                                .addGap(0, 0, 0)
                                                                                                .addComponent(jLabel4)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addGroup(jPanel1Layout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                false)
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addComponent(jLabel7,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                20,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                .addComponent(jLabel9,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                20,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                .addComponent(jLabel12,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                30,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                .addGroup(jPanel1Layout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addComponent(regoin,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                30,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addGap(0, 0, 0)
                                                                                                                                .addComponent(cent,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                30,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                .addComponent(system))))
                                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                jPanel1Layout.createSequentialGroup()
                                                                                                                .addContainerGap()
                                                                                                                .addComponent(jLabel5)
                                                                                                                .addGap(22, 22, 22)
                                                                                                                .addComponent(jLabel6,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                60,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel13)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jScrollPane2,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                847, Short.MAX_VALUE)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jSeparator1,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                10,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel1Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel18)
                                                                                .addComponent(jLabel14)
                                                                                .addComponent(jLabel17)
                                                                                .addComponent(jLabel16))
                                                                .addGap(62, 62, 62)));

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
                layout.setVerticalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

                pack();
        }// </editor-fold>//GEN-END:initComponents

        /**
         * @param args the command line arguments
         */
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
                        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                                        .getInstalledLookAndFeels()) {
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
                java.awt.EventQueue.invokeLater(() -> new Failed().setVisible(true));
        }

        // Variables declaration - do not modify
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
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JSeparator jSeparator1;
        public javax.swing.JTable jTable2;
        private javax.swing.JLabel regoin;
        private javax.swing.JLabel system;
}
