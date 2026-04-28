package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class successful extends javax.swing.JFrame {
    public boolean isCancelled = false;

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(successful.class.getName());

    public successful() {
        initComponents();

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

        // تطبيق المنسق وعرض الأعمدة على كافة خلايا الجدول
        for (int col = 0; col < jTable2.getColumnCount(); col++) {
            jTable2.getColumnModel().getColumn(col).setCellRenderer(centerCellRenderer);
        }

        if (jTable2.getColumnCount() >= 6) {
            jTable2.getColumnModel().getColumn(0).setHeaderValue("<html><center>حالة<br>التلميذ</center></html>");
            jTable2.getColumnModel().getColumn(1).setHeaderValue("<html><center>رقم<br>الجلوس</center></html>");
            jTable2.getColumnModel().getColumn(2).setHeaderValue("<html><center>رقم<br>التسجيل</center></html>");

            jTable2.getColumnModel().getColumn(0).setPreferredWidth(80); // حالة التلميذ
            jTable2.getColumnModel().getColumn(1).setPreferredWidth(140); // رقم الجلوس
            jTable2.getColumnModel().getColumn(2).setPreferredWidth(150); // رقم التسجيل
            jTable2.getColumnModel().getColumn(3).setPreferredWidth(520); // المهنة
            jTable2.getColumnModel().getColumn(4).setPreferredWidth(430); // الاسم
            jTable2.getColumnModel().getColumn(5).setPreferredWidth(50); // م
        }

        JTableHeader header = jTable2.getTableHeader();

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(javax.swing.SwingConstants.CENTER); // توسيط الكلام في الهيدر

                c.setFont(new Font("Tahoma", Font.BOLD, 18));
                c.setBackground(new Color(204, 255, 255));
                c.setForeground(Color.BLACK);

                return c;
            }
        });

        String month = chooseMonth();

        if (month == null) {
            this.isCancelled = true;
            return;
        }

        int year = java.time.Year.now().getValue();

        String arabicYear = toArabicNumbers(String.valueOf(year));

        jLabel10.setText("دفعة قبول : " + "اكتوبر" + " " + "لسنة " + toArabicNumbers("2023") + " وما قبلها");
        jLabel11.setText("المنعقد في : " + "يوليو" + " " + "لسنة " + toArabicNumbers("2026"));

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

                model.addRow(new Object[] {
                        "ناجح", // 0: حالة التلميذ (تظهر في اليسار)
                        rs.getString("seat_no"), // 1: رقم الجلوس
                        rs.getString("registration_no"), // 2: رقم التسجيل
                        htmlProf, // 3: المهنة
                        htmlName, // 4: الاسم
                        i++ // 5: م (يظهر في اليمين)
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
        jPanel1.setLayout(null); // Force absolute layout to prevent GroupLayout clipping
        jPanel1.setPreferredSize(new java.awt.Dimension(1400, 1980));
        jPanel1.setSize(1400, 1980);
        jPanel1.setBackground(java.awt.Color.WHITE);

        // Logo (Top Left)
        jLabel15.setBounds(50, 20, 100, 100);

        // Page Info (Left under logo)
        jLabel12.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel13.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel12.setBounds(30, 130, 250, 25); // استمارة 15
        jLabel13.setBounds(30, 160, 250, 25); // الصفحة

        // Ministry Info (Right Top)
        int rAlign = 1000;
        jLabel1.setFont(new Font("Tahoma", Font.BOLD, 18));
        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 18));
        jLabel3.setFont(new Font("Tahoma", Font.BOLD, 18));
        jLabel4.setFont(new Font("Tahoma", Font.BOLD, 18));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jLabel1.setBounds(rAlign, 10, 390, 25);
        jLabel2.setBounds(rAlign, 35, 390, 25);
        jLabel3.setBounds(rAlign, 60, 390, 25);
        jLabel4.setBounds(rAlign, 85, 390, 25);

        // Region, Center, System below ministry
        jLabel7.setFont(new Font("Tahoma", Font.BOLD, 18));
        jLabel8.setFont(new Font("Tahoma", Font.BOLD, 18));
        jLabel9.setFont(new Font("Tahoma", Font.BOLD, 18));
        regoin.setFont(new Font("Tahoma", Font.BOLD, 18));
        cent.setFont(new Font("Tahoma", Font.BOLD, 18));
        system.setFont(new Font("Tahoma", Font.BOLD, 18));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        regoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        system.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        int y = 120;
        jLabel7.setBounds(rAlign + 290, y, 100, 25);
        regoin.setBounds(rAlign - 10, y, 300, 25);
        y += 25;
        jLabel8.setBounds(rAlign + 290, y, 100, 25);
        cent.setBounds(rAlign - 10, y, 300, 25);
        y += 25;
        jLabel9.setBounds(rAlign + 290, y, 100, 25);
        system.setBounds(rAlign - 10, y, 300, 25);

        // Center Title
        jLabel5.setFont(new Font("Tahoma", Font.BOLD, 22)); // نتائج امتحان...
        jLabel6.setFont(new Font("Tahoma", Font.BOLD, 36)); // تلاميذ ناجحون
        jLabel6.setForeground(new Color(0, 102, 204)); // Blue color like gradReportGeneric

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        jLabel5.setBounds(400, 40, 600, 35);
        jLabel6.setBounds(400, 80, 600, 50);

        jLabel10.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel11.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setBounds(400, 140, 600, 25); // دفعة قبول
        jLabel11.setBounds(400, 170, 600, 25); // المنعقد في

        // Table Content
        int tableY = 220;
        int rowH = 42;
        jTable2.setRowHeight(rowH);

        // Fixed full-page height for 30 rows + header (42*30 + 60 = 1320)
        int tableHeight = 1320;
        jScrollPane2.setBounds(20, tableY, 1260, tableHeight);

        // Footer positioned safely at bottom
        int separatorY = tableY + tableHeight + 20;
        int labelsY = separatorY + 20;

        jSeparator1.setBounds(20, separatorY, 1260, 10);

        jLabel16.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel17.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel18.setFont(new Font("Tahoma", Font.BOLD, 16));
        jLabel14.setFont(new Font("Tahoma", Font.BOLD, 22)); // كتبه bigger

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // RTL positioning: Right to Left
        jLabel14.setBounds(1150, labelsY, 200, 40); // كتبه (right)
        jLabel18.setBounds(850, labelsY, 200, 40); // املاه
        jLabel17.setBounds(550, labelsY, 200, 40); // راجعه
        jLabel16.setBounds(50, labelsY, 300, 40);  // رئيس اللجنة (left)

        jPanel1.doLayout();
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    public void createPDF() {
        try {
            int rowsPerPage = 30;
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
                    row.set(0, "ناجح");
                    row.set(5, globalIndex++);
                    model.addRow(row);
                }
                while (model.getRowCount() < 30) {
                    model.addRow(new Object[] { "", "", "", "", "", "" });
                }

                jLabel13.setText("صفحة " + toArabicNumbers(String.valueOf(page + 1)) + " من "
                        + toArabicNumbers(String.valueOf(pageCount)));
                buildPagePanel(model.getRowCount());

                int width = 1400, height = 1980;
                jPanel1.setSize(width, height);
                jPanel1.validate();
                jPanel1.doLayout();

                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                jPanel1.printAll(g2d);
                g2d.dispose();

                Image pdfImg = Image.getInstance(img, null);
                pdfImg.scaleAbsolute(document.getPageSize().getWidth(), document.getPageSize().getHeight());
                pdfImg.setAbsolutePosition(0, 0);
                document.add(pdfImg);
                if (page < pageCount - 1)
                    document.newPage();
            }

            model.setRowCount(0);
            for (Vector row : originalData)
                model.addRow(row);
            document.close();
            Desktop.getDesktop().open(new File("report.pdf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPDFGroupedBySystem(
            java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem,
            String centerName, String regionName, boolean isFirstCall) {
        try {
            int rowsPerPage = 30;

            String folderStr = "التقارير/بدون درجات/ناجحين";
            java.io.File folder2 = new java.io.File(folderStr);
            if (!folder2.exists()) folder2.mkdirs();
            String sanitizedRegion2 = (regionName != null && !regionName.trim().isEmpty())
                    ? regionName.replace("/", "_").replace("\\", "_").replace(":", "_")
                    : "غير_محدد";
            String filePath2 = folderStr + "/" + sanitizedRegion2 + ".pdf";

            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, new FileOutputStream(filePath2));
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
                        row.set(0, "ناجح");
                        row.set(5, globalIndex++);
                        model.addRow(row);
                    }
                    while (model.getRowCount() < 30) {
                        model.addRow(new Object[] { "", "", "", "", "", "" });
                    }

                    jLabel13.setText("صفحة " + toArabicNumbers(String.valueOf(page + 1))
                            + " من " + toArabicNumbers(String.valueOf(pageCount)));
                    buildPagePanel(model.getRowCount());

                    int width = 1400, height = 1980;
                    jPanel1.setSize(width, height);
                    jPanel1.validate();
                    jPanel1.doLayout();

                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = img.createGraphics();
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
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

            // تعيين اسم المركز الممرر للدالة
            if (centerName != null) {
                cent.setText(centerName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        regoin = new javax.swing.JLabel();
        cent = new javax.swing.JLabel();
        system = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(null);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(30, 60, 114));
        jLabel1.setText("وزارة الصناعة ");
        jPanel1.add(jLabel1);
        jLabel1.setBounds(850, 10, 140, 20);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(30, 60, 114));
        jLabel2.setText("مصلحة الكفاية االنتاجية والتدريب المهنى");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(790, 30, 250, 20);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(30, 60, 114));
        jLabel3.setText("الرئاسة العامة المتحانات دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel3);
        jLabel3.setBounds(770, 50, 290, 20);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(30, 60, 114));
        jLabel4.setText("لجنة النظام والمراقبة");
        jPanel1.add(jLabel4);
        jLabel4.setBounds(840, 70, 140, 20);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(30, 60, 114));
        jLabel5.setText("نتائج أمتحان دبلوم التلمذة الصناعية");
        jPanel1.add(jLabel5);
        jLabel5.setBounds(390, 60, 220, 20);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(42, 82, 152));
        jLabel6.setText("تلاميذ ناجحون");
        jPanel1.add(jLabel6);
        jLabel6.setBounds(430, 100, 140, 60);

        jLabel7.setText("المنطقة /");
        jPanel1.add(jLabel7);
        jLabel7.setBounds(1000, 90, 70, 20);

        jLabel8.setText("مــــركز/");
        jPanel1.add(jLabel8);
        jLabel8.setBounds(1000, 120, 70, 20);

        jLabel9.setText("النظام /");
        jPanel1.add(jLabel9);
        jLabel9.setBounds(990, 150, 80, 30);

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(30, 60, 114));
        jLabel10.setText("دفعة قبول : أكتوبر لسنة ٢٠٢٣ وما قبلها");
        jPanel1.add(jLabel10);
        jLabel10.setBounds(110, 10, 250, 20);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(30, 60, 114));
        jLabel11.setText("المنعقد فى : يوليو لسنة ٢٠٢٦");
        jPanel1.add(jLabel11);
        jLabel11.setBounds(130, 30, 210, 20);

        jLabel12.setForeground(new java.awt.Color(30, 60, 114));
        jLabel12.setText("إستمارة رقم 15 امتحانات");
        jPanel1.add(jLabel12);
        jLabel12.setBounds(90, 70, 150, 30);

        jLabel13.setForeground(new java.awt.Color(30, 60, 114));
        jLabel13.setText("صفحة 1 من 1 ");
        jPanel1.add(jLabel13);
        jLabel13.setBounds(110, 90, 100, 20);

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/unnamed-removebg-preview (3).png"))); // NOI18N
        jPanel1.add(jLabel15);
        jLabel15.setBounds(30, 20, 60, 60);

        jSeparator1.setForeground(new java.awt.Color(204, 102, 0));
        jPanel1.add(jSeparator1);
        jSeparator1.setBounds(0, 1210, 1170, 10);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {

                },
                new String[] {
                        "حاله التلميذ", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م"
                }));
        jTable2.setPreferredSize(new java.awt.Dimension(450, 100));
        jScrollPane2.setViewportView(jTable2);

        jPanel1.add(jScrollPane2);
        jScrollPane2.setBounds(0, 200, 1130, 980);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(42, 82, 152));
        jLabel14.setText("كتبه                 ");
        jPanel1.add(jLabel14);
        jLabel14.setBounds(780, 1230, 170, 20);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(42, 82, 152));
        jLabel16.setText("رئيس لجنة النظام والمراقبة");
        jPanel1.add(jLabel16);
        jLabel16.setBounds(0, 1230, 170, 20);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(42, 82, 152));
        jLabel17.setText("راجعه                ");
        jPanel1.add(jLabel17);
        jLabel17.setBounds(250, 1230, 160, 20);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(42, 82, 152));
        jLabel18.setText("املاه                ");
        jPanel1.add(jLabel18);
        jLabel18.setBounds(510, 1230, 170, 20);

        regoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        regoin.setText("");
        jPanel1.add(regoin);
        regoin.setBounds(590, 90, 405, 20);

        cent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cent.setText("");
        jPanel1.add(cent);
        cent.setBounds(590, 120, 405, 20);

        system.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        system.setText("");
        jPanel1.add(system);
        system.setBounds(580, 150, 405, 30);

        getContentPane().add(jPanel1);
        jPanel1.setBounds(0, 0, 1600, 1310);

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
        java.awt.EventQueue.invokeLater(() -> new successful().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel cent;
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

    // End of variables declaration//GEN-END:variables
    public void createPDFGroupedBySystem(
            java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem,
            String centerName, String regionName, boolean isFirstCall) {
        try {
            int rowsPerPage = 30;
            int totalPdfPages = 0;
            
            String folderStr = "التقارير/بدون درجات/ناجحين";
            java.io.File folder = new java.io.File(folderStr);
            if (!folder.exists()) folder.mkdirs();

            String sanitizedRegion = (regionName != null && !regionName.trim().isEmpty()) 
                    ? regionName.replace("/", "_").replace("\\", "_").replace(":", "_") 
                    : "غير_محدد";
            String baseFn = folderStr + "/" + sanitizedRegion + ".pdf";

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document,
                    new java.io.FileOutputStream(baseFn));
            document.open();

            boolean isFirstSystem = true;
            for (java.util.Map.Entry<String, java.util.List<java.util.Vector>> entry : bySystem.entrySet()) {
                String systemName = entry.getKey();
                java.util.List<java.util.Vector> systemData = entry.getValue();

                int systemTotalRows = systemData.size();
                int systemPages = (int) Math.ceil((double) systemTotalRows / rowsPerPage);

                for (int p = 0; p < systemPages; p++) {
                    if (!isFirstSystem || p > 0) {
                        document.newPage();
                    }
                    isFirstSystem = false;

                    int start = p * rowsPerPage;
                    int end = Math.min(start + rowsPerPage, systemTotalRows);

                    DefaultTableModel tempModel = (DefaultTableModel) jTable2.getModel();
                    tempModel.setRowCount(0);
                    for (int i = start; i < end; i++) {
                        tempModel.addRow(systemData.get(i));
                    }

                    this.cent.setText(centerName);
                    this.regoin.setText(regionName);
                    this.system.setText(systemName);

                    this.jPanel1.validate();
                    this.jPanel1.repaint();

                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                            jPanel1.getWidth(), jPanel1.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2 = image.createGraphics();
                    jPanel1.paint(g2);
                    g2.dispose();

                    com.itextpdf.text.Image pdfImg = com.itextpdf.text.Image.getInstance(image, null);
                    pdfImg.scaleToFit(document.getPageSize().getWidth() - 20, document.getPageSize().getHeight() - 20);
                    pdfImg.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
                    document.add(pdfImg);
                }
            }

            document.close();
            java.awt.Desktop.getDesktop().open(new java.io.File(folderStr));

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "خطأ في إنشاء PDF: " + e.getMessage());
        }
    }
}
