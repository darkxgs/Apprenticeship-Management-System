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
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;

/**
 * Report: كشف الأرقام السرية — Minimal header with dual secret number columns.
 */
public class gradSecretReport extends JFrame {

    private final String profession;
    private final String center;
    private final String region;
    private final String system;
    private final List<Student> students;
    private List<Subject> subjects;
    
    private JPanel uiRootPanel;
    private int dynamicRowHeight = 140; // auto-calculated to fill A3 page

    public gradSecretReport(String profession, String center, String region, String system, List<Student> students) {
        this.profession = profession;
        this.center = center;
        this.region = region;
        this.system = system;
        this.students = students;
        this.subjects = SubjectService.getSubjectsByProfession(profession);
        
        setTitle("كشف الأرقام السرية - " + profession);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setBackground(Color.WHITE);
        initUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        uiRootPanel = new JPanel();
        uiRootPanel.setLayout(new BoxLayout(uiRootPanel, BoxLayout.Y_AXIS));
        uiRootPanel.setBackground(Color.WHITE);
        uiRootPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        int totalPages = (int) Math.ceil(students.size() / 10.0);
        if (totalPages == 0) totalPages = 1;
        
        uiRootPanel.add(buildHeader(1, totalPages));
        uiRootPanel.add(buildTable(students.subList(0, Math.min(10, students.size()))));
        uiRootPanel.add(buildFooter());

        JScrollPane mainScroll = new JScrollPane(uiRootPanel);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(mainScroll);
    }

    private JPanel buildHeader(int pageNum, int totalPages) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 30, 10, 30));
        p.setPreferredSize(new Dimension(1450, 150)); // Shorter header
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // --- Logo Block (Only centered logo) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("logo.jpg");
            if (icon.getIconWidth() > 0) {
                int targetHeight = 110; 
                int targetWidth = (icon.getIconWidth() * targetHeight) / icon.getIconHeight();
                java.awt.Image scaled = icon.getImage().getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {}
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        
        JLabel title = new JLabel("كشف الأرقام السرية", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(title);
        
        p.add(centerPanel, gbc);

        return p;
    }

    private JPanel buildTable(List<Student> chunk) {
        int subCount = subjects.size();
        // 10 base columns (including dual secret) + subCount + 6 summary columns
        String[] cols = new String[10 + subCount + 6];
        int i = 0;
        cols[i++] = "م"; cols[i++] = "الاسم"; cols[i++] = "رقم التسجيل";
        cols[i++] = "كود التنسيق";
        cols[i++] = "الحرفة"; cols[i++] = "المجموعة المهنية"; cols[i++] = "الرقم القومي";
        cols[i++] = "رقم الجلوس"; 
        cols[i++] = "الرقم السري 1"; cols[i++] = "الرقم السري 2";
        
        for (Subject s : subjects) {
            String name = s.getName();
            if (name != null && name.length() > 10) {
                cols[i++] = "<html><center>" + name.replace(" ", "<br/>") + "</center></html>";
            } else {
                cols[i++] = (name != null ? name : "");
            }
        }
        cols[i++] = "مجموع النظري"; cols[i++] = "درجات العملي"; cols[i++] = "درجات التطبيقي";
        cols[i++] = "مجموع عملي وتطبيقي"; cols[i++] = "المجموع الكلي"; cols[i] = "التقدير";

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Row 1 & 2: Max/Min
        Object[] maxRow = new Object[cols.length];
        maxRow[1] = "النهاية العظمى";
        int totalMax = 0, theoryMaxSum = 0, practicalMax = 0, appliedMax = 0, subIdx = 10;
        for (Subject s : subjects) {
            maxRow[subIdx++] = s.getMaxMark();
            totalMax += s.getMaxMark();
            if ("نظري".equals(s.getType())) theoryMaxSum += s.getMaxMark();
            else if ("تطبيقي".equals(s.getType())) appliedMax = s.getMaxMark();
            else practicalMax = s.getMaxMark();
        }
        maxRow[subIdx++] = theoryMaxSum; maxRow[subIdx++] = practicalMax;
        maxRow[subIdx++] = appliedMax; maxRow[subIdx++] = (practicalMax + appliedMax);
        maxRow[subIdx++] = totalMax; maxRow[subIdx] = "0";
        model.addRow(maxRow);

        Object[] minRow = new Object[cols.length];
        minRow[1] = "النهاية الصغرى";
        int theoryPassSum = 0, practicalPass = 0, appliedPass = 0;
        subIdx = 10;
        for (Subject s : subjects) {
            minRow[subIdx++] = s.getPassMark();
            if ("نظري".equals(s.getType())) theoryPassSum += s.getPassMark();
            else if ("تطبيقي".equals(s.getType())) appliedPass = s.getPassMark();
            else practicalPass = s.getPassMark();
        }
        minRow[subIdx++] = theoryPassSum; minRow[subIdx++] = practicalPass;
        minRow[subIdx++] = appliedPass; minRow[subIdx++] = (practicalPass + appliedPass);
        minRow[subIdx++] = "0"; minRow[subIdx] = "0";
        model.addRow(minRow);

        for (Student st : chunk) {
            Object[] row = new Object[cols.length];
            int colIdx = 0;
            row[colIdx++] = students.indexOf(st) + 1;
            row[colIdx++] = st.getName(); 
            row[colIdx++] = st.getRegistrationNo();
            row[colIdx++] = st.getCoordinationNo();
            row[colIdx++] = "<html><center>" + (st.getProfession() != null ? st.getProfession() : "") + "</center></html>";
            row[colIdx++] = "<html><center>" + (st.getProfessionalGroup() != null ? st.getProfessionalGroup() : "") + "</center></html>";
            row[colIdx++] = st.getNationalId(); 
            row[colIdx++] = st.getSeatNo();
            
            // Dual secret columns
            row[colIdx++] = st.getSecretNo();
            row[colIdx++] = st.getSecretNo();
            
            int theorySum = 0, practicalMark = 0, appliedMark = 0;
            Map<Integer, Integer> grades = st.getGrades();
            for (Subject sub : subjects) {
                int mark = grades != null ? grades.getOrDefault(sub.getId(), 0) : 0;
                row[colIdx++] = mark > 0 ? mark : "0";
                if ("نظري".equals(sub.getType())) theorySum += (mark > 0 ? mark : 0);
                else if ("تطبيقي".equals(sub.getType())) appliedMark = (mark > 0 ? mark : 0);
                else practicalMark = (mark > 0 ? mark : 0);
            }
            row[colIdx++] = theorySum; row[colIdx++] = practicalMark;
            row[colIdx++] = appliedMark; row[colIdx++] = (practicalMark + appliedMark);
            row[colIdx++] = (theorySum + practicalMark + appliedMark); row[colIdx] = st.getStatus();
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String text = val == null ? "" : val.toString();
                // If already HTML pass through, otherwise wrap for line wrapping
                String htmlVal = text.toLowerCase().startsWith("<html>")
                    ? text
                    : "<html><div style='text-align:center;'>" + text + "</div></html>";
                Component c = super.getTableCellRendererComponent(t, htmlVal, sel, foc, row, col);
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
                c.setFont(new Font("Arial", Font.PLAIN, 24));
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent)c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
        table.getTableHeader().setPreferredSize(new Dimension(0, 180));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(300);
        }
        table.getColumn("م").setPreferredWidth(80);
        table.getColumn("الاسم").setPreferredWidth(450);
        table.getColumn("الحرفة").setPreferredWidth(350);
        table.getColumn("المجموعة المهنية").setPreferredWidth(350);
        try { table.getColumn("الرقم القومي").setPreferredWidth(450); } catch (Exception e) {}
        try { table.getColumn("رقم الجلوس").setPreferredWidth(250); } catch (Exception e) {}
        try { table.getColumn("رقم التسجيل").setPreferredWidth(250); } catch (Exception e) {}
        try { table.getColumn("الرقم السري 1").setPreferredWidth(250); } catch (Exception e) {}
        try { table.getColumn("الرقم السري 2").setPreferredWidth(250); } catch (Exception e) {}
        try { table.getColumn("مجموع عملي وتطبيقي").setPreferredWidth(380); } catch (Exception e) {}
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, Color.BLACK)); 
        p.setPreferredSize(new Dimension(2000, 220));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        String[] sigs = {"كتبه", "راجعه", "راجع الاملاء", "رصد ووضع الدوائر الحمراء", "راجع الدوائر الحمراء والرصد", "راجع المراجعة", "رئيس لجنة النظام والمراقبة"};
        for (int c = 0; c < sigs.length; c++) {
            gbc.gridx = c; p.add(sigBlock(sigs[c]), gbc);
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
        p.add(t); p.add(line);
        return p;
    }

    private void layoutRecipes(java.awt.Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof java.awt.Container) layoutRecipes((java.awt.Container) child);
        }
        c.doLayout();
        if (c instanceof javax.swing.JComponent) ((javax.swing.JComponent) c).revalidate();
    }

    private JPanel buildPagePanel(List<Student> chunk, int pageNum, int totalPages) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Fixed A3 landscape dimensions at high resolution
        int panelWidth = 2800;
        int panelHeight = (int)(panelWidth / 1.4142); // ~1980px
        
        // Calculate row height to fill available space dynamically
        int headerH = 150, footerH = 220, tableHeaderH = 120;
        int available = panelHeight - headerH - footerH - tableHeaderH;
        int totalRows = chunk.size() + 2; // +2 for النهاية العظمى / الصغرى rows
        dynamicRowHeight = Math.max(80, available / totalRows);
        
        page.add(buildHeader(pageNum, totalPages));
        JPanel tablePanel = buildTable(chunk);
        page.add(tablePanel);
        page.add(buildFooter());
        
        page.setSize(new Dimension(panelWidth, panelHeight));
        page.addNotify();
        page.validate();
        layoutRecipes(page);
        return page;
    }

    public void createPDF() {
        try {
            String fn = "Secret_No_Report_" + profession.replace("/", "_").replace("\\", "_") + ".pdf";
            Document doc = new Document(); 
            PdfWriter.getInstance(doc, new FileOutputStream(fn));
            doc.open();
            appendToDocument(doc);
            doc.close();
            Desktop.getDesktop().open(new File(fn));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void appendToDocument(Document doc) {
        try {
            int pageSizeCount = 10; 
            int totalCount = students.size();
            int totalPages = (int) Math.ceil(totalCount / (double) pageSizeCount);
            if (totalPages == 0) totalPages = 1;

            for (int pIdx = 0; pIdx < totalPages; pIdx++) {
                int start = pIdx * pageSizeCount;
                int end = Math.min(start + pageSizeCount, totalCount);
                List<Student> chunk = students.subList(start, end);

                JPanel pagePanel = buildPagePanel(chunk, pIdx + 1, totalPages);
                doc.setPageSize(PageSize.A3.rotate());
                doc.newPage(); 

                int scale = 2; // Increase resolution for clearer text
                BufferedImage img = new BufferedImage(pagePanel.getWidth() * scale, pagePanel.getHeight() * scale, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.scale(scale, scale);
                // Enable anti-aliasing and high quality rendering hints
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_NORMALIZE);
                g2.setPaint(Color.WHITE);
                g2.fillRect(0, 0, pagePanel.getWidth(), pagePanel.getHeight());
                pagePanel.printAll(g2);
                g2.dispose();

                Image pImg = Image.getInstance(img, null);
                // Fill the entire A3 page
                pImg.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                pImg.setAbsolutePosition(0, 0);
                doc.add(pImg);
                pagePanel.removeNotify();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
