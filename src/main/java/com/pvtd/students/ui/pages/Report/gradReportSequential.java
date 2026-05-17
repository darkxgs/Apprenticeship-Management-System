package com.pvtd.students.ui.pages.Report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
 * Generic Sequential Report — Handles mixed professions by showing a fixed set
 * of total columns (Theory, Practical, Applied, etc.) without individual subject columns.
 * This maintains the exact selection order from the table.
 */
public class gradReportSequential extends JFrame {

    private final String statusTitle;
    private final Color titleColor;
    private final String center;
    private final String region;
    private final List<Student> students;
    private String selectedMonth;
    private String admissionMonth;

    private static final int PAGE_SIZE = 10; // Standard sequential page size
    private final Map<String, List<Subject>> subjectCache = new java.util.concurrent.ConcurrentHashMap<>();

    public gradReportSequential(String statusTitle, Color titleColor, String center, String region, List<Student> students,
                                String selectedMonth, String admissionMonth) {
        this.statusTitle   = statusTitle;
        this.titleColor    = titleColor;
        this.center        = center;
        this.region        = region;
        this.students      = students;
        this.selectedMonth = selectedMonth;
        this.admissionMonth = admissionMonth;

        setTitle(statusTitle + " - " + region);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setBackground(Color.WHITE);
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

    private List<Subject> subjectsFor(String profession) {
        return subjectCache.computeIfAbsent(
            profession == null ? "" : profession,
            k -> SubjectService.getSubjectsByProfession(k)
        );
    }

    private int[] calcTotals(Student st) {
        String status = st.getStatus();
        boolean isAllowed = "ناجح".equals(status) || "راسب".equals(status) || "دور ثاني".equals(status);
        if (!isAllowed) return new int[]{0, 0, 0};

        List<Subject> allSubs = subjectsFor(st.getProfession());
        Map<Integer, List<Subject>> childMap = new HashMap<>();
        List<Subject> parents = new ArrayList<>();
        for (Subject s : allSubs) {
            if (s.getParentSubjectId() == null) parents.add(s);
            else childMap.computeIfAbsent(s.getParentSubjectId(), k -> new ArrayList<>()).add(s);
        }

        int theory = 0, practical = 0, applied = 0;
        Map<Integer, Integer> grades = st.getGrades();
        for (Subject s : parents) {
            List<Subject> ch = childMap.get(s.getId());
            int mark = (ch != null && !ch.isEmpty())
                ? ch.stream().mapToInt(c -> grades != null ? grades.getOrDefault(c.getId(), 0) : 0).sum()
                : (grades != null ? grades.getOrDefault(s.getId(), 0) : 0);
            if ("نظري".equals(s.getType()))       theory     += mark;
            else if ("تطبيقي".equals(s.getType())) applied    += mark;
            else                                    practical  += mark;
        }
        return new int[]{theory, practical, applied};
    }

    private String failedSubjects(Student st) {
        if (!"راسب".equals(st.getStatus()) && !"دور ثاني".equals(st.getStatus())) return "";
        List<Subject> allSubs = subjectsFor(st.getProfession());
        Map<Integer, List<Subject>> childMap = new HashMap<>();
        List<Subject> parents = new ArrayList<>();
        for (Subject s : allSubs) {
            if (s.getParentSubjectId() == null) parents.add(s);
            else childMap.computeIfAbsent(s.getParentSubjectId(), k -> new ArrayList<>()).add(s);
        }

        List<String> failed = new ArrayList<>();
        Map<Integer, Integer> grades = st.getGrades();
        for (Subject s : parents) {
            List<Subject> ch = childMap.get(s.getId());
            int mark = (ch != null && !ch.isEmpty())
                ? ch.stream().mapToInt(c -> grades != null ? grades.getOrDefault(c.getId(), 0) : 0).sum()
                : (grades != null ? grades.getOrDefault(s.getId(), 0) : 0);
            int pass = (ch != null && !ch.isEmpty())
                ? ch.stream().mapToInt(Subject::getPassMark).sum()
                : s.getPassMark();
            
            boolean isFailed = (mark < pass && mark >= 0);
            Integer srCode = getSecondRoundCode();
            if (srCode != null && mark == srCode) isFailed = true;

            if (isFailed && s.getName() != null && !s.getName().isBlank())
                failed.add(s.getName());
        }
        return failed.isEmpty() ? "" : String.join("<br/>", failed);
    }

    private String[] getFailedSubjectsArray(Student st) {
        List<Subject> allSubs = subjectsFor(st.getProfession());
        Map<Integer, List<Subject>> childMap = new HashMap<>();
        List<Subject> parents = new ArrayList<>();
        for (Subject s : allSubs) {
            if (s.getParentSubjectId() == null) parents.add(s);
            else childMap.computeIfAbsent(s.getParentSubjectId(), k -> new ArrayList<>()).add(s);
        }

        List<String> theoryFailed = new ArrayList<>();
        boolean failedPractical = false;
        boolean failedApplied = false;

        Map<Integer, Integer> grades = st.getGrades();
        for (Subject s : parents) {
            List<Subject> ch = childMap.get(s.getId());
            int mark = (ch != null && !ch.isEmpty())
                ? ch.stream().mapToInt(c -> grades != null ? grades.getOrDefault(c.getId(), 0) : 0).sum()
                : (grades != null ? grades.getOrDefault(s.getId(), 0) : 0);
            int pass = (ch != null && !ch.isEmpty())
                ? ch.stream().mapToInt(Subject::getPassMark).sum()
                : s.getPassMark();
            
            boolean isFailed = (mark < pass && mark >= 0);
            Integer srCode = getSecondRoundCode();
            if (srCode != null && mark == srCode) isFailed = true;

            if (isFailed && s.getName() != null && !s.getName().isBlank()) {
                if ("نظري".equals(s.getType())) theoryFailed.add(s.getName());
                else if ("تطبيقي".equals(s.getType())) failedApplied = true;
                else failedPractical = true;
            }
        }

        String[] res = new String[6];
        java.util.Arrays.fill(res, "");
        
        java.util.List<String> allFailed = new java.util.ArrayList<>();
        allFailed.addAll(theoryFailed);
        if (failedPractical) allFailed.add("عملي");
        if (failedApplied)   allFailed.add("تطبيقي");

        for (int i = 0; i < 6 && i < allFailed.size(); i++) {
            res[i] = allFailed.get(i);
        }
        if (allFailed.size() > 6) {
            res[5] = String.join("/", allFailed.subList(5, allFailed.size()));
        }

        return res;
    }

    public void appendToDocument(Document doc) {
        try {
            int total = students.size();
            if (total == 0) return;
            int pages = (int) Math.ceil(total / (double) PAGE_SIZE);

            int pagesAdded = 0;
            for (int p = 0; p < pages; p++) {
                final int pIdx = p;
                final int start = p * PAGE_SIZE;
                final int end = Math.min(start + PAGE_SIZE, total);
                final List<Student> chunk = students.subList(start, end);
                final int totalP = pages;

                try {
                    JPanel page = buildPage(chunk, pIdx + 1, totalP);
                    double scale = 0.15; // مقياس موفر للذاكرة لضمان الاستقرار
                    int imgW = (int) (26000 * scale);
                    int imgH = (int) (18385 * scale);
                    
                    BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = img.createGraphics();
                    g2.scale(scale, scale);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setPaint(Color.WHITE);
                    g2.fillRect(0, 0, 26000, 18385);
                    page.printAll(g2);
                    g2.dispose();

                    Image pImg = Image.getInstance(img, null);
                    doc.setPageSize(com.itextpdf.text.PageSize.A3.rotate());
                    doc.newPage();
                    pImg.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                    pImg.setAbsolutePosition(0, 0);
                    doc.add(pImg);
                    img.flush();
                    pagesAdded++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // إذا لم يتم إضافة أي صفحات بسبب خطأ ما، نضيف صفحة فارغة لتجنب الـ iText Exception
            if (pagesAdded == 0) {
                doc.newPage();
                doc.add(new com.itextpdf.text.Paragraph("لا توجد بيانات أو حدث خطأ أثناء التوليد"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel buildPage(List<Student> chunk, int pageNum, int totalPages) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        int panelWidth = 26000;
        int panelHeight = (int) (panelWidth / 1.4142);

        page.add(buildHeader(chunk, pageNum, totalPages));
        page.add(buildTable(chunk, pageNum));
        page.add(buildFooter());

        page.setSize(new Dimension(panelWidth, panelHeight));
        page.addNotify();
        page.validate();
        return page;
    }

    private JPanel buildHeader(List<Student> chunk, int pageNum, int totalPages) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 50, 40, 50));
        p.setPreferredSize(new Dimension(26000, 3800)); // Increased height for the extra line
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 15, 5, 15);

        // RIGHT
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.33;
        JPanel right = new JPanel(new GridLayout(5, 1, 0, 10)); // Changed from 4 to 5
        right.setOpaque(false);
        right.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        right.add(lbl("وزارة الصناعة", 250, true));
        right.add(lbl("مصلحة الكفاية الإنتاجية والتدريب المهني", 250, true));
        right.add(lbl("المنطقة / " + (region != null ? region : ""), 250, true));
        right.add(lbl("مركز / " + (center != null ? center : ""), 250, true));
        
        // جلب النظام من جدول المهن بناءً على اسم مهنة أول طالب
        String systemStr = "";
        if (chunk != null && !chunk.isEmpty()) {
            String profName = chunk.get(0).getProfession();
            if (profName != null && !profName.trim().isEmpty()) {
                try (Connection con = com.pvtd.students.db.DatabaseConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("SELECT exam_system FROM professions WHERE name = ?")) {
                    ps.setString(1, profName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            systemStr = rs.getString("exam_system");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // fallback if not found in professions table
            if (systemStr == null || systemStr.trim().isEmpty()) {
                systemStr = chunk.get(0).getExamSystem();
            }
        }
        
        if (systemStr == null) systemStr = "";
        right.add(lbl("النظام / " + systemStr, 250, true));
        p.add(right, gbc);

        // CENTER
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.34;
        JPanel ctr = new JPanel();
        ctr.setLayout(new BoxLayout(ctr, BoxLayout.Y_AXIS));
        ctr.setOpaque(false);
        JLabel t1 = lbl("نتائج أمتحان دبلوم التلمذة الصناعية", 350, true); t1.setAlignmentX(Component.CENTER_ALIGNMENT); ctr.add(t1);
        JLabel t2 = lbl(statusTitle, 450, true); t2.setForeground(titleColor); t2.setAlignmentX(Component.CENTER_ALIGNMENT); ctr.add(t2);
        ctr.add(Box.createVerticalStrut(25));
        JLabel t3 = lbl("دفعة قبول : " + admissionMonth + " وما قبلها", 250, true); t3.setAlignmentX(Component.CENTER_ALIGNMENT); ctr.add(t3);
        JLabel t4 = lbl("المنعقد في : " + selectedMonth, 250, true); t4.setAlignmentX(Component.CENTER_ALIGNMENT); ctr.add(t4);
        p.add(ctr, gbc);

        // LEFT
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.33;
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        left.add(lbl("صفحة " + pageNum + " من " + totalPages, 200, true));
        p.add(left, gbc);

        return p;
    }

    private static final String[] COLS = {
            "م", "الاسم", "رقم التسجيل", "الحرفة", "المجموعة المهنية", "الرقم القومي", "رقم الجلوس", "الرقم السري",
            "تكنولوجيا", "رسم", "ميكانيكا عامة", "لغة انجليزية",
            "مجموع النظري", "العملي", "التطبيقي", "مجموع عملي وتطبيقي", "المجموع الكلي", "حالة التلميذ", "مواد الدور الثاني", "الملاحظات"
    };

    private String getTheorySubjectMark(Student st, int subjectIndex) {
        // subjectIndex: 0=تكنولوجيا, 1=رسم, 2=ميكانيكا عامة, 3=لغة انجليزية
        List<Subject> allSubs = subjectsFor(st.getProfession());
        List<Subject> theoryParents = new java.util.ArrayList<>();
        Map<Integer, List<Subject>> childMap = new HashMap<>();
        for (Subject s : allSubs) {
            if (s.getParentSubjectId() == null) {
                if ("نظري".equals(s.getType())) theoryParents.add(s);
            } else {
                childMap.computeIfAbsent(s.getParentSubjectId(), k -> new java.util.ArrayList<>()).add(s);
            }
        }
        if (subjectIndex >= theoryParents.size()) return "0";
        Subject sub = theoryParents.get(subjectIndex);
        List<Subject> ch = childMap.get(sub.getId());
        Map<Integer, Integer> grades = st.getGrades();
        
        int mark;
        if (ch != null && !ch.isEmpty()) {
            mark = ch.stream().mapToInt(c2 -> grades != null ? grades.getOrDefault(c2.getId(), 0) : 0).sum();
        } else {
            mark = grades != null ? grades.getOrDefault(sub.getId(), 0) : 0;
        }

        // إذا كانت الدرجة صفر ولكن حالة الطالب العامة لها كود معين (للحالات التي لم ترصد درجاتها بعد)
        if (mark == 0 && st.getStatus() != null) {
            String s = st.getStatus();
            if (s.contains("غائب")) mark = -1;
            else if (s.contains("محروم")) mark = -2;
            else if (s.contains("مفصول")) mark = -3;
            else if (s.contains("معتذر")) mark = -4;
            else if (s.contains("مؤجل")) mark = -5;
        }

        if (mark < 0) {
            return "0";
        }
        
        String status = st.getStatus();
        boolean isAllowed = "ناجح".equals(status) || "راسب".equals(status) || "دور ثاني".equals(status);
        if (!isAllowed) return "0";

        return String.valueOf(mark);
    }

    private boolean isSuccessReport() {
        return "تلاميذ ناجحون".equals(statusTitle);
    }

    private boolean shouldHideSecondRoundColumn() {
        return !"تلاميذ راسبون ولهم حق دخول الدور الثاني".equals(statusTitle);
    }

    private JPanel buildTable(List<Student> chunk, int pageNum) {
        String[] effectiveCols = COLS;
        int[] widths = { 500, 5000, 2000, 5000, 4000, 3500, 1800, 1800, 1500, 1500, 1500, 1500, 1800, 1500, 1500, 1800, 1800, 1800, 4500, 2000 };
        
        if (shouldHideSecondRoundColumn()) {
            List<String> list = new ArrayList<>(Arrays.asList(COLS));
            list.remove("مواد الدور الثاني");
            effectiveCols = list.toArray(new String[0]);
            
            int[] filteredWidths = new int[widths.length - 1];
            int j = 0;
            for (int i = 0; i < widths.length; i++) {
                if (i == 18) continue; // index of "مواد الدور الثاني"
                filteredWidths[j++] = widths[i];
            }
            widths = filteredWidths;
        }

        DefaultTableModel model = new DefaultTableModel(effectiveCols, 0);
        for (int i = 0; i < PAGE_SIZE; i++) {
            Object[] row = new Object[effectiveCols.length];
            Arrays.fill(row, " ");
            if (i < chunk.size()) {
                Student st = chunk.get(i);
                int[] totals = calcTotals(st);
                int theory = totals[0], prac = totals[1], appl = totals[2];
                int c = 0;
                row[c++] = ((pageNum - 1) * PAGE_SIZE) + i + 1;
                row[c++] = st.getName();
                row[c++] = st.getRegistrationNo();
                row[c++] = st.getProfession();        // الحرفة
                row[c++] = st.getProfessionalGroup(); // المجموعة المهنية
                row[c++] = st.getNationalId();
                row[c++] = st.getSeatNo();
                row[c++] = st.getSecretNo();
                // المواد النظرية الأربعة
                row[c++] = getTheorySubjectMark(st, 0); // تكنولوجيا
                row[c++] = getTheorySubjectMark(st, 1); // رسم
                row[c++] = getTheorySubjectMark(st, 2); // ميكانيكا عامة
                row[c++] = getTheorySubjectMark(st, 3); // لغة انجليزية
                row[c++] = theory;
                row[c++] = prac;
                row[c++] = appl;
                row[c++] = (prac + appl);
                row[c++] = theory + prac + appl;
                row[c++] = st.getStatus();
                if (!shouldHideSecondRoundColumn()) {
                    row[c++] = getFailedSubjectsArray(st); // مواد الدور الثاني
                }
                row[c]   = ""; // الملاحظات
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // م، الاسم، رقم التسجيل، الحرفة، المجموعة المهنية، الرقم القومي، رقم الجلوس، الرقم السري،
        // تكنولوجيا، رسم، ميكانيكا عامة، لغة انجليزية،
        // مجموع النظري، العملي، التطبيقي، مجموع عملي وتطبيقي، المجموع الكلي، حالة التلميذ، مواد الدور الثاني، الملاحظات
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setRowHeight(900);
        table.setFont(new Font("Tahoma", Font.PLAIN, 180));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                if ("مواد الدور الثاني".equals(t.getColumnName(col))) {
                    String[] arr = val instanceof String[] ? (String[]) val : new String[0];
                    java.util.List<String> list = new java.util.ArrayList<>();
                    if (arr != null) {
                        for (String s : arr) {
                            if (s != null && !s.trim().isEmpty()) {
                                String cleaned = s.replaceAll("\\(اضغط للتعديل\\)", "").trim();
                                if (!cleaned.isEmpty()) list.add(cleaned);
                            }
                        }
                    }
                    int count = Math.max(1, list.size());
                    JPanel dynamicPanel = new JPanel(new java.awt.GridLayout(1, count, 0, 0));
                    dynamicPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    dynamicPanel.setBackground(Color.WHITE);
                    for (int i = 0; i < count; i++) {
                        JLabel lbl = new JLabel();
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        lbl.setFont(new Font("Tahoma", Font.BOLD, 140));
                        lbl.setOpaque(true);
                        lbl.setBackground(Color.WHITE);
                        lbl.setForeground(Color.BLACK);
                        String text = (i < list.size()) ? list.get(i) : "";
                        lbl.setText("<html><center>" + text + "</center></html>");
                        
                        // Pixel-perfect borders for each label inside the grid
                        int right = (i == 0) ? 5 : 0;
                        lbl.setBorder(BorderFactory.createMatteBorder(5, 5, 5, right, Color.BLACK));
                        
                        dynamicPanel.add(lbl);
                    }
                    dynamicPanel.setBorder(null);
                    return dynamicPanel;
                }
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div align='center' style='padding:10px;'>" + txt + "</div></html>";
                }
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div align='center' style='padding:10px 5px;'><b>" + txt + "</b></div></html>";
                }
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                c.setBackground(new Color(240, 248, 255));
                c.setForeground(new Color(10, 30, 60));
                c.setFont(new Font("Tahoma", Font.BOLD, 180));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
                return c;
            }
        });

        JPanel tableCont = new JPanel(new BorderLayout());
        tableCont.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCont.add(table, BorderLayout.CENTER);
        return tableCont;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel(new GridLayout(1, 7, 30, 0));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(26000, 2000));
        f.setBorder(new EmptyBorder(50, 100, 50, 100));
        f.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        String[] titles = {
            "كتبه",
            "راجعه",
            "راجع الاملاء",
            "رصد ووضع الدوائر الحمراء",
            "راجع الدوائر الحمراء والرصد",
            "راجع المراجعة",
            "رئيس لجنة النظام والمراقبة"
        };

        for (String t : titles) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);
            
            JLabel lTitle = lbl(t, 220, true);
            lTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel lLine = lbl("...........................", 200, false);
            lLine.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            p.add(lTitle);
            p.add(Box.createVerticalStrut(30));
            p.add(lLine);
            f.add(p);
        }

        return f;
    }

    private JLabel lbl(String t, int size, boolean bold) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }
}
