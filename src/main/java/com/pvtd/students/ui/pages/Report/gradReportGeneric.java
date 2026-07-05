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
 * Report: Generic Graded Report — Precise Layout synchronized with successful
 * report style (A3 Landscape).
 */
public class gradReportGeneric extends JFrame {

    private final String profession;
    private final String center;
    private final String region;
    private final String system;
    private final List<Student> students;
    private List<Subject> subjects;
    private final String statusTitle;
    private final Color titleColor;
    private final String fileNamePrefix;
    private String selectedMonth;
    private String admissionMonth;



    private JPanel uiRootPanel;
    private int dynamicRowHeight = 40;

    public gradReportGeneric(String statusTitle, String profession, String center, String region, String system, List<Student> students, Color titleColor, String fileNamePrefix) {
        this(statusTitle, profession, center, region, system, students, titleColor, fileNamePrefix, null, null);
    }

    public gradReportGeneric(String statusTitle, String profession, String center, String region, String system, List<Student> students, Color titleColor, String fileNamePrefix, String selectedMonth, String admissionMonth) {
        this.statusTitle = statusTitle;
        this.profession = profession;
        this.center = center;
        this.region = region;
        this.system = system;
        this.students = students;
        this.titleColor = titleColor;
        this.fileNamePrefix = fileNamePrefix;
        this.subjects = SubjectService.getSubjectsByProfession(profession);
        
        if (selectedMonth != null && admissionMonth != null) {
            this.selectedMonth = selectedMonth;
            this.admissionMonth = admissionMonth;
        } else {
            this.selectedMonth = chooseMonth("اختر المنعقد فيه", "يوليو");

            this.admissionMonth = chooseMonth("اختر شهر دفعة القبول", "أكتوبر");
        }




        setTitle(statusTitle + " - " + profession);
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

        // Standardized to 15 rows per page for all A3 reports
        int pageSize = 15;
        int totalPages = (int) Math.ceil(students.size() / (double) pageSize);
        if (totalPages == 0)
            totalPages = 1;

        uiRootPanel.add(buildHeader(1, totalPages));
        uiRootPanel.add(buildTable(students.subList(0, Math.min(pageSize, students.size()))));
        uiRootPanel.add(buildFooter());

        JScrollPane mainScroll = new JScrollPane(uiRootPanel);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(mainScroll);
    }

    private JPanel buildHeader(int pageNum, int totalPages) {
        JPanel p = new JPanel(null);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(13000, 1700));

        double sf = 9.28;
        java.util.function.BiConsumer<JLabel, int[]> setB = (l, b) -> {
            l.setBounds((int)(b[0]*sf), (int)(b[1]*sf), (int)(b[2]*sf), (int)(b[3]*sf));
        };

        // ── RIGHT: Ministry lines ──
        JLabel l1 = label("وزارة الصناعة", 105, true);
        setB.accept(l1, new int[]{890, 5, 500, 16});
        l1.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l1);

        JLabel l2 = label("مصلحة الكفاية الإنتاجية والتدريب المهني", 105, true);
        setB.accept(l2, new int[]{890, 21, 500, 16});
        l2.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l2);

        JLabel l3 = label("الرئاسة العامة للامتحانات لدبلوم التلمذة الصناعية", 105, true);
        setB.accept(l3, new int[]{890, 37, 500, 16});
        l3.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l3);

        JLabel l4 = label("لجنة النظام والمراقبة", 105, true);
        setB.accept(l4, new int[]{890, 53, 500, 16});
        l4.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(l4);

        // ── RIGHT: Region / Center / System ──
        String regionStr = (region != null && !region.isEmpty()) ? region.trim() : "—";
        String centerStr = (center != null && !center.isEmpty()) ? center.trim() : "—";
        String systemStr = (system != null && !system.isEmpty()) ? system.trim() : "";
        int labelRight = 1380, labelWidth = 120, valueWidth = 360;

        JLabel l7 = label("المنطقة:", 105, true);
        setB.accept(l7, new int[]{labelRight - labelWidth, 74, labelWidth, 16});
        l7.setHorizontalAlignment(SwingConstants.RIGHT); p.add(l7);
        JLabel reg = label(regionStr, 105, false);
        setB.accept(reg, new int[]{labelRight - labelWidth - valueWidth, 74, valueWidth, 16});
        reg.setHorizontalAlignment(SwingConstants.RIGHT); p.add(reg);

        JLabel l9 = label("المركز:", 105, true);
        setB.accept(l9, new int[]{labelRight - labelWidth, 92, labelWidth, 16});
        l9.setHorizontalAlignment(SwingConstants.RIGHT); p.add(l9);
        JLabel cen = label(centerStr, 105, false);
        setB.accept(cen, new int[]{labelRight - labelWidth - valueWidth, 92, valueWidth, 16});
        cen.setHorizontalAlignment(SwingConstants.RIGHT); p.add(cen);

        JLabel l12 = label("النظام:", 105, true);
        setB.accept(l12, new int[]{labelRight - labelWidth, 110, labelWidth, 16});
        l12.setHorizontalAlignment(SwingConstants.RIGHT); p.add(l12);
        JLabel sys = label(systemStr, 105, false);
        setB.accept(sys, new int[]{labelRight - labelWidth - valueWidth, 110, valueWidth, 16});
        sys.setHorizontalAlignment(SwingConstants.RIGHT); p.add(sys);

        // ── CENTER: Title block ──
        JLabel t1 = label("نتائج أمتحان دبلوم التلمذة الصناعية", 120, true);
        setB.accept(t1, new int[]{400, 20, 600, 28});
        t1.setHorizontalAlignment(SwingConstants.CENTER); p.add(t1);

        JLabel t2 = label(statusTitle, 140, true);
        setB.accept(t2, new int[]{400, 50, 600, 35});
        t2.setForeground(titleColor);
        t2.setHorizontalAlignment(SwingConstants.CENTER); p.add(t2);

        JLabel t3 = label("دفعة قبول : " + admissionMonth + " وما قبلها", 110, true);
        setB.accept(t3, new int[]{400, 88, 600, 20});
        t3.setHorizontalAlignment(SwingConstants.CENTER); p.add(t3);

        JLabel t4 = label("المنعقد في : " + selectedMonth, 110, true);
        setB.accept(t4, new int[]{400, 110, 600, 20});
        t4.setHorizontalAlignment(SwingConstants.CENTER); p.add(t4);

        // ── LEFT: Logo + Page number ──
        try {
            ImageIcon icon = new ImageIcon("logo.jpg");
            if (icon.getIconWidth() > 0) {
                int h = (int)(100*sf), w = (icon.getIconWidth() * h) / icon.getIconHeight();
                JLabel logo = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
                logo.setBounds((int)(50*sf), (int)(10*sf), w, h);
                p.add(logo);
            }
        } catch (Exception ignored) {}

        JLabel pg = label("صفحة " + pageNum + " من " + totalPages, 148, true);
        setB.accept(pg, new int[]{30, 140, 250, 25});
        pg.setHorizontalAlignment(SwingConstants.LEFT); p.add(pg);

        // ── Separator ──
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(new Color(180, 180, 180));
        sep.setBounds(0, (int)(135*sf), 13000, (int)(2*sf));
        p.add(sep);

        return p;
    }

    private JPanel buildTable(List<Student> chunk) {
        // Group subjects before building table
        java.util.List<Subject> parentSubjects = new java.util.ArrayList<>();
        java.util.Map<Integer, java.util.List<Subject>> childrenMap = new java.util.HashMap<>();
        for (Subject s : subjects) {
            if (s.getParentSubjectId() == null) {
                parentSubjects.add(s);
            } else {
                childrenMap.computeIfAbsent(s.getParentSubjectId(), k -> new java.util.ArrayList<>()).add(s);
            }
        }

        int subCount = parentSubjects.size();
        // Base (8) + subjects + theorySum + Practical + Applied + Combined + Total + Status
        String[] cols = new String[8 + subCount + 1 + 1 + 1 + 1 + 1 + 1];
        int i = 0;
        cols[i++] = "م";
        cols[i++] = "الاسم";
        cols[i++] = "رقم التسجيل";
        cols[i++] = "الحرفة";
        cols[i++] = "المجموعة المهنية";
        cols[i++] = "الرقم القومي";
        cols[i++] = "رقم الجلوس";
        cols[i++] = "الرقم السري";
        
        boolean theoryColAdded = false;
        for (Subject s : parentSubjects) {
            String name = s.getName();
            if (name != null && name.length() > 10) {
                cols[i++] = "<html><center>" + name.replace(" ", "<br/>") + "</center></html>";
            } else {
                cols[i++] = (name != null ? name : "");
            }
            if (name != null && name.contains("لغة انجليزية")) {
                cols[i++] = "مجموع النظري";
                theoryColAdded = true;
            }
        }
        if (!theoryColAdded)
            cols[i++] = "مجموع النظري";
            
        cols[i++] = "العملي";
        cols[i++] = "التطبيقي";
        cols[i++] = "مجموع عملي وتطبيقي";
        cols[i++] = "المجموع الكلي";
        cols[i] = "حالة";

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // Pre-calculate totals
        int theoryMaxSum = 0, practicalMax = 0, appliedMax = 0;
        int theoryPassSum = 0, practicalPass = 0, appliedPass = 0;
        
        for (Subject s : parentSubjects) {
            List<Subject> children = childrenMap.get(s.getId());
            int m = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getMaxMark).sum() : s.getMaxMark();
            int p = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getPassMark).sum() : s.getPassMark();
            
            if ("نظري".equals(s.getType())) {
                theoryMaxSum += m;
                theoryPassSum += p;
            } else if ("تطبيقي".equals(s.getType())) {
                appliedMax += m;
                appliedPass += p;
            } else {
                practicalMax += m;
                practicalPass += p;
            }
        }

        // 1. Max Row
        Object[] maxRow = new Object[cols.length];
        maxRow[1] = "النهاية العظمى";
        int colIdx = 8;
        boolean tMaxAdded = false;
        for (Subject s : parentSubjects) {
            List<Subject> children = childrenMap.get(s.getId());
            int m = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getMaxMark).sum() : s.getMaxMark();
            maxRow[colIdx++] = m;
            if (s.getName() != null && s.getName().contains("لغة انجليزية")) {
                maxRow[colIdx++] = theoryMaxSum;
                tMaxAdded = true;
            }
        }
        if (!tMaxAdded) maxRow[colIdx++] = theoryMaxSum;
        maxRow[colIdx++] = practicalMax;
        maxRow[colIdx++] = appliedMax;
        maxRow[colIdx++] = (practicalMax + appliedMax);
        maxRow[colIdx++] = (theoryMaxSum + practicalMax + appliedMax);
        maxRow[colIdx] = " ";
        model.addRow(maxRow);

        // 2. Min Row
        Object[] minRow = new Object[cols.length];
        minRow[1] = "النهاية الصغرى";
        colIdx = 8;
        boolean tPassAdded = false;
        for (Subject s : parentSubjects) {
            List<Subject> children = childrenMap.get(s.getId());
            int p = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getPassMark).sum() : s.getPassMark();
            minRow[colIdx++] = p;
            if (s.getName() != null && s.getName().contains("لغة انجليزية")) {
                minRow[colIdx++] = theoryPassSum;
                tPassAdded = true;
            }
        }
        if (!tPassAdded) minRow[colIdx++] = theoryPassSum;
        minRow[colIdx++] = (int) Math.ceil(practicalMax * 0.5);
        minRow[colIdx++] = (int) Math.ceil(appliedMax * 0.5);
        minRow[colIdx++] = (int) Math.ceil((practicalMax + appliedMax) * 0.5);
        minRow[colIdx++] = (int) Math.ceil((theoryMaxSum + practicalMax + appliedMax) * 0.5);
        minRow[colIdx] = " ";
        model.addRow(minRow);

        // 3. Students Rows
        for (int i_chunk = 0; i_chunk < 15; i_chunk++) {
            if (i_chunk < chunk.size()) {
                Student st = chunk.get(i_chunk);
                Object[] row = new Object[cols.length];
                int c = 0;
                row[c++] = students.indexOf(st) + 1;
                row[c++] = st.getName();
                row[c++] = st.getRegistrationNo();
                row[c++] = st.getProfession();
                row[c++] = st.getProfessionalGroup();
                row[c++] = st.getNationalId();
                row[c++] = st.getSeatNo();
                row[c++] = st.getSecretNo();

                int theorySum = 0, practicalMark = 0, appliedMark = 0;
                Map<Integer, Integer> grades = st.getGrades();
                
                String status = st.getStatus();
                boolean isAllowed = "ناجح".equals(status) || "راسب".equals(status) || "دور ثاني".equals(status);

                boolean tRowAdded = false;
                for (Subject s : parentSubjects) {
                    List<Subject> children = childrenMap.get(s.getId());
                    int mark = (children != null && !children.isEmpty()) 
                        ? children.stream().mapToInt(child -> grades != null ? grades.getOrDefault(child.getId(), 0) : 0).sum()
                        : (grades != null ? grades.getOrDefault(s.getId(), 0) : 0);
                    
                    if (!isAllowed || mark < 0) mark = 0;

                    row[c++] = mark;
                    if ("نظري".equals(s.getType())) theorySum += mark;
                    else if ("تطبيقي".equals(s.getType())) appliedMark += mark;
                    else practicalMark += mark;

                    if (s.getName() != null && s.getName().contains("لغة انجليزية")) {
                        row[c++] = theorySum;
                        tRowAdded = true;
                    }
                }
                if (!tRowAdded) row[c++] = theorySum;
                
                row[c++] = practicalMark;
                row[c++] = appliedMark;
                row[c++] = (practicalMark + appliedMark);
                row[c++] = (theorySum + practicalMark + appliedMark);
                row[c] = st.getStatus();
                model.addRow(row);
            } else {
                Object[] row = new Object[cols.length];
                for (int x = 0; x < cols.length; x++) row[x] = " ";
                model.addRow(row);
            }
        }

        JTable table = new JTable(model);
        styleTable(table, dynamicRowHeight);
        table.setBackground(Color.WHITE);
        table.setOpaque(true);
        table.putClientProperty("Table.alternateRowColor", Color.WHITE);
        table.putClientProperty("FlatLaf.alternateRowColor", false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String arTxt = toAr(val);
                String plain = arTxt.replaceAll("<[^>]*>", "");
                if (!arTxt.toLowerCase().startsWith("<html>")) {
                    arTxt = "<html><div dir='rtl' align='right' style='padding-right:20px;'>" + arTxt + "</div></html>";
                }
                Component comp = super.getTableCellRendererComponent(t, arTxt, false, false, row, col);
                comp.setBackground(Color.WHITE);
                comp.setForeground(Color.BLACK);
                setHorizontalAlignment(SwingConstants.RIGHT);
                // Auto-shrink font
                int baseSize = 28, minSize = 14;
                int colW = t.getColumnModel().getColumn(col).getWidth() - 30;
                Font useFont = new Font("Tahoma", Font.PLAIN, baseSize);
                if (colW > 0 && !plain.isEmpty()) {
                    java.awt.FontMetrics fm = t.getFontMetrics(useFont);
                    int sz = baseSize;
                    while (fm.stringWidth(plain) > colW && sz > minSize) {
                        sz--;
                        useFont = new Font("Tahoma", Font.PLAIN, sz);
                        fm = t.getFontMetrics(useFont);
                    }
                }
                comp.setFont(useFont);
                ((javax.swing.JComponent) comp).setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, Color.BLACK));
                return comp;
            }
        });
        table.setIntercellSpacing(new Dimension(2, 2));
        table.setGridColor(Color.BLACK);
        table.setShowGrid(true);

        JPanel tableCont = new JPanel(new BorderLayout());
        tableCont.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCont.add(table, BorderLayout.CENTER);
        return tableCont;
    }


    private void styleTable(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 28));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 180));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row,
                    int col) {
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><center>" + txt + "</center></html>";
                }
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                c.setBackground(new Color(204, 255, 255));  // Light cyan — matches reference
                c.setForeground(Color.BLACK);
                c.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 95));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, Color.BLACK));
                return c;
            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(300);
        }
        table.getColumn("م").setPreferredWidth(80);
        table.getColumn("الاسم").setPreferredWidth(950);
        table.getColumn("رقم التسجيل").setPreferredWidth(250);
        table.getColumn("رقم الجلوس").setPreferredWidth(250);
        table.getColumn("الحرفة").setPreferredWidth(1200);
        table.getColumn("المجموعة المهنية").setPreferredWidth(700);
        table.getColumn("الرقم القومي").setPreferredWidth(450);
        table.getColumn("الرقم السري").setPreferredWidth(250);
        try {
            table.getColumn("الرقم القومي").setPreferredWidth(450);
        } catch (Exception e) {
        }
        try {
            table.getColumn("رقم الجلوس").setPreferredWidth(250);
        } catch (Exception e) {
        }
        try {
            table.getColumn("رقم التسجيل").setPreferredWidth(250);
        } catch (Exception e) {
        }
        try {
            table.getColumn("مجموع عملي وتطبيقي").setPreferredWidth(380);
        } catch (Exception e) {
        }
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 102, 0))); // Thick orange line like in
                                                                                          // reference image
        p.setPreferredSize(new Dimension(13000, 800));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        String[] sigs = { "كتبه", "راجعه", "راجع الاملاء", "رصد ووضع الدوائر الحمراء", "راجع الدوائر الحمراء والرصد",
                "راجع المراجعة", "رئيس لجنة النظام والمراقبة" };
        for (int c = 0; c < sigs.length; c++) {
            gbc.gridx = c;
            p.add(sigBlock(sigs[c]), gbc);
        }
        return p;
    }

    private String chooseMonth(String title, String defaultMonth) {
        String[] months = {
                "يناير", "فبراير", "مارس", "أبريل",
                "مايو", "يونيو", "يوليو", "أغسطس",
                "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };
        String choice = (String) javax.swing.JOptionPane.showInputDialog(
                this,
                title,
                "تحديد الموعد",
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                months,
                defaultMonth);
        return (choice != null) ? choice : defaultMonth;
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


    private JLabel label(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(30, 60, 114));
        return l;
    }

    private String toAr(Object val) {
        if (val == null) return "";
        String s = val.toString();
        return s.replace("0","٠").replace("1","١").replace("2","٢")
                 .replace("3","٣").replace("4","٤").replace("5","٥")
                 .replace("6","٦").replace("7","٧").replace("8","٨").replace("9","٩");
    }

    private JPanel sigBlock(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        if ("كتبه".equals(title)) {
            t.setFont(new Font("Arial", Font.BOLD, 26));
        } else {
            t.setFont(new Font("Arial", Font.PLAIN, 18));
        }
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line = new JLabel("..........................................", SwingConstants.CENTER);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(12));
        p.add(t);
        p.add(line);
        return p;
    }

    private void layoutRecipes(java.awt.Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof java.awt.Container)
                layoutRecipes((java.awt.Container) child);
        }
        c.doLayout();
        if (c instanceof javax.swing.JComponent)
            ((javax.swing.JComponent) c).revalidate();
    }

    private JPanel buildPagePanel(List<Student> chunk, int pageNum, int totalPages) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        int panelWidth = 13000;
        int panelHeight = (int) (panelWidth / 1.4142);

        int headerH = 1700, footerH = 800, tableHeaderH = 180;
        int available = panelHeight - headerH - footerH - tableHeaderH - 100;
        int totalRows = 17;
        this.dynamicRowHeight = Math.max(200, available / totalRows);

        page.add(buildHeader(pageNum, totalPages));
        JPanel tablePanel = buildTable(chunk);
        tablePanel.setPreferredSize(new Dimension(panelWidth, available + tableHeaderH));
        tablePanel.setMaximumSize(new Dimension(panelWidth, available + tableHeaderH));
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
            String fn = fileNamePrefix + "_" + profession.replace("/", "_").replace("\\", "_") + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(fn));
            doc.open();
            appendToDocument(doc);
            doc.close();
            Desktop.getDesktop().open(new File(fn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendToDocument(Document doc) {
        try {
            int pageSizeCount = 15;
            int totalCount = students.size();
            int totalPages = (int) Math.ceil(totalCount / (double) pageSizeCount);
            if (totalPages == 0)
                totalPages = 1;

            for (int pIdx = 0; pIdx < totalPages; pIdx++) {
                int start = pIdx * pageSizeCount;
                int end = Math.min(start + pageSizeCount, totalCount);
                List<Student> chunk = students.subList(start, end);

                JPanel pagePanel = buildPagePanel(chunk, pIdx + 1, totalPages);
                doc.setPageSize(com.itextpdf.text.PageSize.A3.rotate());
                doc.newPage();

                BufferedImage img = new BufferedImage(pagePanel.getWidth(), pagePanel.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2 = img.createGraphics();

                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
                        java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                        java.awt.RenderingHints.VALUE_RENDER_QUALITY);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
