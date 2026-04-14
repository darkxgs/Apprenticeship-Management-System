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
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;

/**
 * Report: تلاميذ راسبون — Precise Layout synchronized with successful report style (A3 Landscape).
 */
public class gradReportFail extends JFrame {

    private final String profession;
    private final String center;
    private final String region;
    private final String system;
    private final List<Student> students;
    private List<Subject> subjects;
    
    private JPanel uiRootPanel;
    private int dynamicRowHeight = 40;

    public gradReportFail(String profession, String center, String region, String system, List<Student> students) {
        this.profession = profession;
        this.center = center;
        this.region = region;
        this.system = system;
        this.students = students;
        this.subjects = SubjectService.getSubjectsByProfession(profession);
        
        setTitle("كشف الراسبين - " + profession);
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

        // Standardized to 50 rows per page like in successful report
        int totalPages = (int) Math.ceil(students.size() / 50.0);
        if (totalPages == 0) totalPages = 1;
        
        uiRootPanel.add(buildHeader(1, totalPages));
        uiRootPanel.add(buildTable(students.subList(0, Math.min(50, students.size()))));
        uiRootPanel.add(buildFooter());

        JScrollPane mainScroll = new JScrollPane(uiRootPanel);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(mainScroll);
    }

    private JPanel buildHeader(int pageNum, int totalPages) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 30, 20, 30));
        p.setPreferredSize(new Dimension(1450, 220)); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 15, 5, 15);

        // --- RIGHT BLOCK (Ministry & Location) ---
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.33;
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JLabel l1 = label("وزارة الصناعة", 20, true);
        JLabel l2 = label("مصلحة الكفاية الإنتاجية والتدريب المهني", 20, true);
        JLabel l3 = label("لجنة النظام والمراقبة", 20, true);
        
        l1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l3.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightPanel.add(l1);
        rightPanel.add(l2);
        rightPanel.add(l3);
        rightPanel.add(Box.createVerticalStrut(15));
        
        JLabel l4 = label("المنطقة /" + (region != null ? region.trim() : ""), 15, true);
        JLabel l5 = label("مركز /" + (center != null ? center.trim() : ""), 15, true);
        JLabel l6 = label("النظام /" + (system != null ? system.trim() : ""), 15, true);

        l1.setHorizontalAlignment(SwingConstants.RIGHT);
        l2.setHorizontalAlignment(SwingConstants.RIGHT);
        l3.setHorizontalAlignment(SwingConstants.RIGHT);
        l4.setHorizontalAlignment(SwingConstants.RIGHT);
        l5.setHorizontalAlignment(SwingConstants.RIGHT);
        l6.setHorizontalAlignment(SwingConstants.RIGHT);
        
        l1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l3.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l4.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l5.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l6.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightPanel.add(l1);
        rightPanel.add(l2);
        rightPanel.add(l3);
        rightPanel.add(Box.createVerticalStrut(15));
        
        rightPanel.add(l4);
        rightPanel.add(l5);
        rightPanel.add(l6);
        p.add(rightPanel, gbc);

        // --- CENTER BLOCK (Exam Title & Batch Info) ---
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.34;
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        JLabel mainTitle = label("نتائج أمتحان دبلوم التلمذة الصناعية", 18, true);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(mainTitle);

        JLabel sub = label("تلاميذ راسبون", 24, true);
        sub.setForeground(new Color(220, 38, 38)); // Maintain Red for "Failed" for distinction
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(sub);

        centerPanel.add(Box.createVerticalStrut(15));

        JLabel batchInfo = label("دفعة قبول : أكتوبر لسنة ٢٠١٩ وما قبلها", 13, true);
        batchInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        batchInfo.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(batchInfo);

        JLabel examDate = label("المنعقد في : أغسطس لسنة ٢٠٢٢", 13, true);
        examDate.setAlignmentX(Component.CENTER_ALIGNMENT);
        examDate.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(examDate);
        p.add(centerPanel, gbc);

        // --- LEFT BLOCK (Logo & Page Info) ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.33;
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("logo.jpg");
            if (icon.getIconWidth() > 0) {
                int targetHeight = 85; 
                int targetWidth = (icon.getIconWidth() * targetHeight) / icon.getIconHeight();
                java.awt.Image scaled = icon.getImage().getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {}
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalStrut(15));
        
        JLabel l7 = label("إستمارة رقم ١٥ امتحانات", 11, false);
        JLabel l8 = label("صفحة " + pageNum + " من " + totalPages, 11, false);
        
        l7.setHorizontalAlignment(SwingConstants.LEFT);
        l8.setHorizontalAlignment(SwingConstants.LEFT);
        l7.setAlignmentX(Component.LEFT_ALIGNMENT);
        l8.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftPanel.add(l7);
        leftPanel.add(l8);
        p.add(leftPanel, gbc);

        return p;
    }

    private JPanel buildTable(List<Student> chunk) {
        int subCount = subjects.size();
        String[] cols = new String[9 + subCount + 6];
        int i = 0;
        cols[i++] = "م"; cols[i++] = "الاسم"; cols[i++] = "رقم التسجيل";
        cols[i++] = "كود التنسيق";
        cols[i++] = "الحرفة"; cols[i++] = "المجموعة المهنية"; cols[i++] = "الرقم القومي";
        cols[i++] = "رقم الجلوس"; cols[i++] = "الرقم السري";
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
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Row 1 & 2: Max/Min
        Object[] maxRow = new Object[cols.length];
        maxRow[1] = "النهاية العظمى";
        int totalMax = 0, theoryMaxSum = 0, practicalMax = 0, appliedMax = 0, subIdx = 9;
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
        subIdx = 9;
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
            row[colIdx++] = st.getNationalId(); row[colIdx++] = st.getSeatNo();
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
        styleTable(table, dynamicRowHeight);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><center>" + txt + "</center></html>";
                }
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                if (row == 0) c.setBackground(new Color(220, 252, 231)); 
                else if (row == 1) c.setBackground(new Color(254, 249, 195));
                else c.setBackground(Color.WHITE);
                c.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent)c).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                return c;
            }
        });

        JPanel tableCont = new JPanel(new BorderLayout());
        tableCont.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCont.add(table, BorderLayout.CENTER);
        return tableCont;
    }

    private void styleTable(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 22));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 180));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><center>" + txt + "</center></html>";
                }
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                c.setBackground(new Color(204, 255, 255));
                c.setForeground(Color.BLACK);
                c.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 22));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent)c).setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
                return c;
            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(300);
        }
        table.getColumn("م").setPreferredWidth(80);
        table.getColumn("الاسم").setPreferredWidth(450);
        table.getColumn("رقم التسجيل").setPreferredWidth(250);
        table.getColumn("كود التنسيق").setPreferredWidth(250);
        table.getColumn("رقم الجلوس").setPreferredWidth(250);
        table.getColumn("الحرفة").setPreferredWidth(350);
        table.getColumn("المجموعة المهنية").setPreferredWidth(350);
        table.getColumn("الرقم القومي").setPreferredWidth(450);
        table.getColumn("الرقم السري").setPreferredWidth(250);
        try { table.getColumn("الرقم القومي").setPreferredWidth(450); } catch (Exception e) {}
        try { table.getColumn("رقم الجلوس").setPreferredWidth(250); } catch (Exception e) {}
        try { table.getColumn("رقم التسجيل").setPreferredWidth(250); } catch (Exception e) {}
        try { table.getColumn("مجموع عملي وتطبيقي").setPreferredWidth(380); } catch (Exception e) {}
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 102, 0))); // Thick orange line like in reference image
        p.setPreferredSize(new Dimension(1450, 110));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        String[] sigs = {"كتبه", "راجعه", "راجع الاملاء", "رصد ووضع الدوائر الحمراء", "راجع الدوائر الحمراء والرصد", "راجع المراجعة", "رئيس لجنة النظام والمراقبة"};
        for (int c = 0; c < sigs.length; c++) {
            gbc.gridx = c; p.add(sigBlock(sigs[c]), gbc);
        }
        return p;
    }

    private JLabel label(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(30, 60, 114));
        return l;
    }

    private JPanel sigBlock(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Arial", Font.PLAIN, 18));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line = new JLabel("..........................................", SwingConstants.CENTER);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(12));
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
        
        int panelWidth = 2800;
        int panelHeight = (int) (panelWidth / 1.4142); 
        
        int headerH = 220, footerH = 110, tableHeaderH = 150;
        int available = panelHeight - headerH - footerH - tableHeaderH - 20; 
        int totalRows = chunk.size() + 2; 
        this.dynamicRowHeight = Math.max(35, available / totalRows);

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
            String fn = "Detailed_Failed_Report_" + profession.replace("/", "_").replace("\\", "_") + ".pdf";
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
            int pageSizeCount = 50; 
            int totalCount = students.size();
            int totalPages = (int) Math.ceil(totalCount / (double) pageSizeCount);
            if (totalPages == 0) totalPages = 1;

            for (int pIdx = 0; pIdx < totalPages; pIdx++) {
                int start = pIdx * pageSizeCount;
                int end = Math.min(start + pageSizeCount, totalCount);
                List<Student> chunk = students.subList(start, end);

                JPanel pagePanel = buildPagePanel(chunk, pIdx + 1, totalPages);
                doc.setPageSize(com.itextpdf.text.PageSize.A3.rotate());
                doc.newPage(); 

                BufferedImage img = new BufferedImage(pagePanel.getWidth(), pagePanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2 = img.createGraphics();
                
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                
                g2.setPaint(Color.WHITE);
                g2.fillRect(0, 0, img.getWidth(), img.getHeight());
                pagePanel.printAll(g2);
                g2.dispose();

                Image pImg = Image.getInstance(img, null);
                pImg.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                pImg.setAbsolutePosition(0, 0);
                doc.add(pImg);
                
                pagePanel.removeNotify();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
