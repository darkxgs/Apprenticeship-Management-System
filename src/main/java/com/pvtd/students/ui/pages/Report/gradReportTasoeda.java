package com.pvtd.students.ui.pages.Report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;

/**
 * Report: تسويدة رصد الدرجات (30/70)
 * Supports splitting composite subjects into 30/70 columns.
 */
public class gradReportTasoeda extends JFrame {

    private final String profession;
    private final String center;
    private final String region;
    private final List<Student> students;
    private final List<Subject> allSubjects;
    private final List<Subject> displayColumns;
    private boolean is3070;
    private final String examMonth;
    private final String examYear;
    private String centerCode = "  ";
    private int dynamicRowHeight = 140; // auto-calculated to fill A3 page

    public gradReportTasoeda(String profession, String center, String region, List<Student> students, boolean is3070,
            String examMonth, String examYear) {
        this.profession = profession;
        this.center = center;
        this.region = region;
        this.students = students;
        this.examMonth = (examMonth != null) ? examMonth : "........";
        this.examYear = (examYear != null) ? examYear : "........";

        // Fetch center code from database
        try (Connection conn = com.pvtd.students.db.DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT code FROM centers WHERE name = ?")) {
            stmt.setString(1, center);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    this.centerCode = rs.getString("code");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.allSubjects = com.pvtd.students.services.SubjectService.getSubjectsByProfession(profession);
        this.displayColumns = calculateDisplayColumns();

        setTitle("تسويدة رصد الدرجات - " + profession);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setBackground(Color.WHITE);
        initUI();
        pack();
        setLocationRelativeTo(null);
    }

    private List<Subject> calculateDisplayColumns() {
        List<Subject> cols = new ArrayList<>();
        for (Subject s : allSubjects) {
            if (s.getParentSubjectId() == null) {
                List<Subject> children = allSubjects.stream()
                        .filter(c -> c.getParentSubjectId() != null && c.getParentSubjectId().equals(s.getId()))
                        .collect(java.util.stream.Collectors.toList());
                if (children.isEmpty()) {
                    cols.add(s);
                } else {
                    cols.addAll(children);
                }
            }
        }
        return cols;
    }

    private void initUI() {
        JPanel uiRootPanel = new JPanel();
        uiRootPanel.setLayout(new BoxLayout(uiRootPanel, BoxLayout.Y_AXIS));
        uiRootPanel.setBackground(Color.WHITE);
        uiRootPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        int totalPages = (int) Math.ceil(students.size() / 10.0);
        if (totalPages == 0)
            totalPages = 1;

        uiRootPanel.add(buildHeader(1, totalPages));
        uiRootPanel.add(buildTable(students.subList(0, Math.min(10, students.size()))));
        uiRootPanel.add(buildFooter());

        JScrollPane mainScroll = new JScrollPane(uiRootPanel);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(mainScroll);
    }

    private JPanel buildHeader(int pageNum, int totalPages) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 30, 20, 30));
        p.setPreferredSize(new Dimension(2800, 380));

        Font fontBold = new Font("Arial", Font.BOLD, 32);
        Font fontTitle = new Font("Arial", Font.BOLD, 42);

        // --- RIGHT PANEL (Page Box & Special Table) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // Page indicator box - Matches the image layout [Total / Page]
        JPanel pageBox = new JPanel();
        pageBox.setOpaque(false);
        pageBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        pageBox.setLayout(new BoxLayout(pageBox, BoxLayout.Y_AXIS));
        pageBox.setPreferredSize(new Dimension(120, 100));
        pageBox.setMaximumSize(new Dimension(120, 100));

        JLabel pTot = new JLabel("رقم المركز", SwingConstants.CENTER);
        JLabel pLine = new JLabel("-------", SwingConstants.CENTER);
        JLabel pCur = new JLabel(centerCode, SwingConstants.CENTER);
        pTot.setFont(new Font("Arial", Font.BOLD, 22));
        pLine.setFont(new Font("Arial", Font.BOLD, 15));
        pCur.setFont(new Font("Arial", Font.BOLD, 30));
        pTot.setAlignmentX(Component.CENTER_ALIGNMENT);
        pLine.setAlignmentX(Component.CENTER_ALIGNMENT);
        pCur.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageBox.add(Box.createVerticalGlue());
        pageBox.add(pTot);
        pageBox.add(pLine);
        pageBox.add(pCur);
        pageBox.add(Box.createVerticalGlue());
        pageBox.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(pageBox);
        rightPanel.add(Box.createVerticalStrut(15));

        // Small 6-row table on the right
        JPanel smallTable = new JPanel(new GridBagLayout());
        smallTable.setOpaque(false);
        smallTable.setAlignmentX(Component.RIGHT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        String[] labels = { "ناجحون", "راسبون", "دور ثاني", "محرومون", "غائبون", "توقيع" };
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;

            // Value cell (Empty box with border)
            gbc.gridx = 0;
            gbc.weightx = 0.6;
            JPanel valCell = new JPanel();
            valCell.setBackground(Color.WHITE);
            valCell.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            valCell.setPreferredSize(new Dimension(280, 40));
            smallTable.add(valCell, gbc);

            // Label cell
            gbc.gridx = 1;
            gbc.weightx = 0.4;
            JLabel label = new JLabel(labels[i] + "  ", SwingConstants.RIGHT);
            label.setFont(new Font("Arial", Font.BOLD, 24));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            label.setPreferredSize(new Dimension(160, 40));
            smallTable.add(label, gbc);
        }
        rightPanel.add(smallTable);

        // --- CENTER PANEL (Multi-line titles) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(Box.createVerticalStrut(20));

        String[] centerLines = {
                " مسودة نتائج الصف الثالث ",
                " دبلوم التلمذة الصناعية ",
                " دفعة قبول أكتوبر لسنة ٢٠٢٣ وما قبلها ",
                " المنعقد في يوليو لسنة ٢٠٢٦ "
        };

        for (String line : centerLines) {
            JLabel lbl = new JLabel(line, SwingConstants.CENTER);
            lbl.setFont(fontTitle);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(lbl);
            centerPanel.add(Box.createVerticalStrut(12));
        }

        // --- LEFT PANEL (Center Name) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        leftPanel.add(Box.createVerticalStrut(100)); // Push down slightly

        JLabel centerLbl = new JLabel(" ");
        centerLbl.setFont(fontBold);
        centerLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        leftPanel.add(centerLbl);

        p.add(rightPanel, BorderLayout.EAST);
        p.add(centerPanel, BorderLayout.CENTER);
        p.add(leftPanel, BorderLayout.WEST);

        return p;
    }

    private JLabel createLabel(String text, Font f) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return l;
    }

    private JPanel buildTable(List<Student> chunk) {
        List<Subject> theoryCols = displayColumns.stream()
                .filter(s -> "نظري".equals(s.getType()))
                .collect(Collectors.toList());
        int theoryCount = theoryCols.size();

        // New Column Order:
        // م, الاسم, الحرفة, رقم التسجيل, الرقم السري 1, " ", الرقم السري 2, [Theory
        // Subjects], مجموع النظري, العملي, تطبيقي, مجموع عملي وتطبيقي, المجموع الكلي,
        // ملاحظات, التقدير
        int totalCols = 7 + theoryCount + 7;
        String[] cols = new String[totalCols];
        int i = 0;
        cols[i++] = "م";
        cols[i++] = "الاسم";
        cols[i++] = "الحرفة";
        cols[i++] = "رقم التسجيل";
        cols[i++] = "الرقم السري 1";
        cols[i++] = " ";
        cols[i++] = "الرقم السري 2";

        for (Subject s : theoryCols) {
            String displayName = s.getName();
            if (displayName != null && displayName.length() > 10) {
                cols[i++] = "<html><center>" + displayName.replace(" ", "<br/>") + "</center></html>";
            } else {
                cols[i++] = (displayName != null ? displayName : "");
            }
        }

        cols[i++] = "<html><center>مجموع<br/>النظري</center></html>";
        cols[i++] = "";
        cols[i++] = "";
        cols[i++] = "<html><center>مجموع عملي<br/>وتطبيقي</center></html>";
        cols[i++] = "<html><center>المجموع<br/>الكلي</center></html>";
        cols[i++] = "ملاحظات";
        cols[i++] = "حالة";

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // Pre-calculate total Max
        int theoryMaxSum = 0;
        for (Subject s : theoryCols) {
            List<Subject> children = allSubjects.stream()
                    .filter(c -> c.getParentSubjectId() != null && c.getParentSubjectId().equals(s.getId()))
                    .collect(Collectors.toList());
            theoryMaxSum += (children != null && !children.isEmpty())
                    ? children.stream().mapToInt(Subject::getMaxMark).sum()
                    : s.getMaxMark();
        }

        Object[] maxRow = new Object[totalCols];
        maxRow[1] = "النهاية العظمى";
        int subIdx = 7;
        for (Subject s : theoryCols) {
            List<Subject> children = allSubjects.stream()
                    .filter(c -> c.getParentSubjectId() != null && c.getParentSubjectId().equals(s.getId()))
                    .collect(Collectors.toList());
            maxRow[subIdx++] = (children != null && !children.isEmpty())
                    ? children.stream().mapToInt(Subject::getMaxMark).sum()
                    : s.getMaxMark();
        }
        maxRow[subIdx++] = theoryMaxSum;
        maxRow[subIdx++] = " "; // العملي
        maxRow[subIdx++] = " "; // تطبيقي
        maxRow[subIdx++] = "300"; // مجموع عملي وتطبيقي
        maxRow[subIdx++] = "600"; // المجموع الكلي
        maxRow[subIdx++] = " "; // ملاحظات
        maxRow[subIdx] = " "; // التقدير
        model.addRow(maxRow);

        Object[] minRow = new Object[totalCols];
        minRow[1] = "النهاية الصغرى";
        subIdx = 7;
        int theoryPassSum = 0;
        for (Subject s : theoryCols) {
            List<Subject> children = allSubjects.stream()
                    .filter(c -> c.getParentSubjectId() != null && c.getParentSubjectId().equals(s.getId()))
                    .collect(Collectors.toList());
            int curPass = (children != null && !children.isEmpty())
                    ? children.stream().mapToInt(Subject::getPassMark).sum()
                    : s.getPassMark();
            minRow[subIdx++] = curPass;
            theoryPassSum += curPass;
        }
        minRow[subIdx++] = theoryPassSum;
        minRow[subIdx++] = " "; // العملي
        minRow[subIdx++] = " "; // تطبيقي
        minRow[subIdx++] = "180 "; // مجموع عملي وتطبيقي
        minRow[subIdx++] = "330"; // المجموع الكلي
        minRow[subIdx++] = " "; // ملاحظات
        minRow[subIdx] = " "; // التقدير
        model.addRow(minRow);

        for (int i_chunk = 0; i_chunk < 10; i_chunk++) {
            if (i_chunk < chunk.size()) {
                Student st = chunk.get(i_chunk);
                Object[] row = new Object[totalCols];
                int colIdx = 0;
                row[colIdx++] = students.indexOf(st) + 1;
                row[colIdx++] = st.getName();
                row[colIdx++] = "<html><center>" + (st.getProfession() != null ? st.getProfession() : "")
                        + "</center></html>";
                row[colIdx++] = st.getRegistrationNo();
                row[colIdx++] = st.getSecretNo();
                row[colIdx++] = " "; // Empty column
                row[colIdx++] = st.getSecretNo();

                for (int j = 0; j < theoryCount; j++) {
                    row[colIdx++] = " ";
                }
                row[colIdx++] = " "; // مجموع النظري
                row[colIdx++] = " "; // العملي
                row[colIdx++] = " "; // تطبيقي
                row[colIdx++] = " "; // مجموع عملي وتطبيقي
                row[colIdx++] = " "; // المجموع الكلي
                row[colIdx++] = " "; // ملاحظات
                row[colIdx] = " "; // التقدير
                model.addRow(row);
            } else {
                Object[] row = new Object[totalCols];
                for (int c = 0; c < totalCols; c++) {
                    row[c] = " ";
                }
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        styleTable(table);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row,
                    int col) {
                String text = val == null ? "" : val.toString();
                // Use HTML for right alignment and consistent padding
                String htmlVal = text.toLowerCase().startsWith("<html>")
                        ? text
                        : "<html><div align='right' style='padding-right:10px;'>" + text + "</div></html>";
                Component c = super.getTableCellRendererComponent(t, htmlVal, sel, foc, row, col);
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
                c.setFont(new Font("Arial", Font.PLAIN, 24));
                setHorizontalAlignment(SwingConstants.RIGHT);
                setVerticalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                return c;
            }
        });

        JPanel tableCont = new JPanel(new BorderLayout());
        tableCont.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCont.add(table, BorderLayout.CENTER);
        return tableCont;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(dynamicRowHeight);
        table.setFont(new Font("Arial", Font.PLAIN, 24));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 22));
        table.getTableHeader().setBackground(new Color(204, 255, 255));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 220));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(300);
        }

        try {
            table.getColumn("م").setPreferredWidth(80);
        } catch (Exception e) {
        }
        try {
            table.getColumn("الاسم").setPreferredWidth(1000);
        } catch (Exception e) {
        }
        try {
            table.getColumn("الحرفة").setPreferredWidth(900);
        } catch (Exception e) {
        }
        try {
            table.getColumn("رقم التسجيل").setPreferredWidth(350);
        } catch (Exception e) {
        }
        try {
            table.getColumn("الرقم السري 1").setPreferredWidth(300);
        } catch (Exception e) {
        }
        try {
            table.getColumnModel().getColumn(5).setPreferredWidth(100);
        } catch (Exception e) {
        } // The empty column
        try {
            table.getColumn("الرقم السري 2").setPreferredWidth(300);
        } catch (Exception e) {
        }
        try {
            table.getColumn("العملي").setPreferredWidth(300);
        } catch (Exception e) {
        }
        try {
            table.getColumn("تطبيقي").setPreferredWidth(300);
        } catch (Exception e) {
        }
        try {
            table.getColumn("<html><center>مجموع عملي<br/>وتطبيقي</center></html>").setPreferredWidth(380);
        } catch (Exception e) {
        }
        try {
            table.getColumn("<html><center>المجموع<br/>الكلي</center></html>").setPreferredWidth(350);
        } catch (Exception e) {
        }
        try {
            table.getColumn("ملاحظات").setPreferredWidth(400);
        } catch (Exception e) {
        }
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Color.BLACK));
        p.setPreferredSize(new Dimension(2000, 220));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        String[] sigs = { "رئيس لجنة النظام والمراقبة", "راجع المراجعة", "راجع الدوائر الحمراء والرصد",
                "رصد ووضع الدوائر الحمراء", "راجع الاملاء", "راجعه", "كتبه" };
        for (int c = 0; c < sigs.length; c++) {
            gbc.gridx = c;
            p.add(sigBlock(sigs[c]), gbc);
        }
        return p;
    }

    private JPanel sigBlock(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Arial", Font.BOLD, 30));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line = new JLabel(".............................................", SwingConstants.CENTER);
        line.setFont(new Font("Arial", Font.PLAIN, 26));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(20));
        p.add(t);
        p.add(line);
        return p;
    }

    public void createPDF(com.itextpdf.text.Document combinedDoc) {
        try {
            // إنشاء الفولدرات
            File mainFolder = new File("التقارير");
            if (!mainFolder.exists()) mainFolder.mkdirs();

            File tasoedaFolder = new File(mainFolder, "التسويدة");
            if (!tasoedaFolder.exists()) tasoedaFolder.mkdirs();

            int pageSizeCount = 10;
            int totalCount = students.size();
            int totalPages = (int) Math.ceil(totalCount / (double) pageSizeCount);
            if (totalPages == 0) totalPages = 1;

            String sanitizedProfession = profession.replace("/", "_").replace("\\", "_").replace(":", "_");
            String typeSuffix = is3070 ? "_30_70" : "";

            for (int pIdx = 0; pIdx < totalPages; pIdx++) {
                int start = pIdx * pageSizeCount;
                int end = Math.min(start + pageSizeCount, totalCount);
                List<Student> chunk = students.subList(start, end);

                String pageSuffix = (totalPages > 1) ? "_صفحة_" + (pIdx + 1) : "";
                String fn = tasoedaFolder.getAbsolutePath() + "/" + sanitizedProfession + typeSuffix + pageSuffix + ".pdf";

                // 1. Create individual page file
                Document doc = new Document(PageSize.A3.rotate());
                PdfWriter.getInstance(doc, new FileOutputStream(fn));
                doc.open();

                JPanel pagePanel = buildPagePanel(chunk, pIdx + 1, totalPages);
                
                int scale = 2; // High resolution
                BufferedImage img = new BufferedImage(pagePanel.getWidth() * scale, pagePanel.getHeight() * scale, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.scale(scale, scale);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(Color.WHITE);
                g2.fillRect(0, 0, pagePanel.getWidth(), pagePanel.getHeight());
                pagePanel.printAll(g2);
                g2.dispose();

                Image pImg = Image.getInstance(img, null);
                pImg.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                pImg.setAbsolutePosition(0, 0);
                doc.add(pImg);
                doc.close();

                // 2. Add to combined document if provided
                if (combinedDoc != null) {
                    combinedDoc.setPageSize(PageSize.A3.rotate());
                    combinedDoc.newPage();
                    Image combinedImg = Image.getInstance(img, null);
                    combinedImg.scaleAbsolute(combinedDoc.getPageSize().getWidth(), combinedDoc.getPageSize().getHeight());
                    combinedImg.setAbsolutePosition(0, 0);
                    combinedDoc.add(combinedImg);
                }
                
                pagePanel.removeNotify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPDF() {
        createPDF(null);
    }

    private JPanel buildPagePanel(List<Student> chunk, int pageNum, int totalPages) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // Fixed A3 landscape dimensions at high resolution
        int panelWidth = 2800;
        int panelHeight = (int) (panelWidth / 1.4142); // ~1980px

        // Calculate row height to fill available space dynamically
        int headerH = 380, footerH = 220, tableHeaderH = 220;
        int available = panelHeight - headerH - footerH - tableHeaderH - 50; // 50px safety margin for borders
        int totalRows = 10 + 2; // +2 for النهاية العظمى / الصغرى rows
        dynamicRowHeight = Math.max(50, available / totalRows);

        page.add(buildHeader(pageNum, totalPages));
        JPanel tablePanel = buildTable(chunk);
        page.add(tablePanel);
        page.add(buildFooter());

        page.setSize(new Dimension(panelWidth, panelHeight));
        page.addNotify();
        page.validate();
        return page;
    }
}
