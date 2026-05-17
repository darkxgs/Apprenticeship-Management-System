package com.pvtd.students.ui.pages.Report;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;

/**
 * كشف الراسبين بالدرجات (مختلط الحرف) — A3 Landscape
 * يعرض جميع الطلاب مرتبين برقم الجلوس بدون تقسيم على الحرفة.
 * الأعمدة: م، الاسم، رقم التسجيل، الحرفة، المجموعة المهنية،
 *           الرقم القومي، رقم الجلوس، الرقم السري،
 *           مجموع النظري، العملي، التطبيقي، مجموع(ع+ت)، المجموع الكلي،
 *           حالة التلميذ، مواد الدور الثاني
 */
public class gradReportFailMixed extends JFrame {

    private static final int PAGE_SIZE = 5; // Reduced from 8 to increase row height for subject visibility

    private final String center;
    private final String region;
    private final List<Student> students;
    private String selectedMonth;
    private String admissionMonth;

    // Cache: profession -> subjects
    private final Map<String, List<Subject>> subjectCache = new java.util.concurrent.ConcurrentHashMap<>();

    private int dynamicRowHeight = 300;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public gradReportFailMixed(String center, String region, List<Student> students,
                               String selectedMonth, String admissionMonth) {
        this.center        = center;
        this.region        = region;
        this.students      = students;
        this.selectedMonth = selectedMonth;
        this.admissionMonth = admissionMonth;

        setTitle("كشف الراسبين بالدرجات - " + region);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setBackground(Color.WHITE);
    }

    // ─── Subject helpers ───────────────────────────────────────────────────────

    private List<Subject> subjectsFor(String profession) {
        return subjectCache.computeIfAbsent(
            profession == null ? "" : profession,
            k -> SubjectService.getSubjectsByProfession(k)
        );
    }

    /** حساب مجاميع الطالب: [نظري, عملي, تطبيقي] */
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
                ? ch.stream().mapToInt(c -> {
                      int val = grades != null ? grades.getOrDefault(c.getId(), 0) : 0;
                      return val < 0 ? 0 : val;
                  }).sum()
                : (grades != null ? grades.getOrDefault(s.getId(), 0) : 0);
            
            if (mark < 0) mark = 0;

            if ("نظري".equals(s.getType()))       theory     += mark;
            else if ("تطبيقي".equals(s.getType())) applied    += mark;
            else                                    practical  += mark;
        }
        return new int[]{theory, practical, applied};
    }

    /** مواد الدور الثاني للطالب */
    private String failedSubjects(Student st) {
        String status = st.getStatus();
        boolean isAllowed = "ناجح".equals(status) || "راسب".equals(status) || "دور ثاني".equals(status);
        if (!isAllowed) return "";

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
            if (mark < pass && s.getName() != null && !s.getName().isBlank())
                failed.add(s.getName());
        }
        return failed.isEmpty() ? "" : String.join("<br/>", failed);
    }

    private String[] getFailedSubjectsArray(Student st) {
        String status = st.getStatus();
        boolean isAllowed = "ناجح".equals(status) || "راسب".equals(status) || "دور ثاني".equals(status);
        if (!isAllowed) {
            String[] empty = new String[6];
            Arrays.fill(empty, "");
            return empty;
        }

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
            
            if (mark < pass && s.getName() != null && !s.getName().isBlank()) {
                if ("نظري".equals(s.getType())) {
                    theoryFailed.add(s.getName());
                } else if ("تطبيقي".equals(s.getType())) {
                    failedApplied = true;
                } else {
                    failedPractical = true;
                }
            }
        }
        
        String[] res = new String[6];
        Arrays.fill(res, "");
        for (int i = 0; i < 4 && i < theoryFailed.size(); i++) {
            res[i] = theoryFailed.get(i);
        }
        if (theoryFailed.size() > 4) {
            res[3] = String.join("<br>", theoryFailed.subList(3, theoryFailed.size()));
        }
        if (failedPractical) res[4] = "عملي";
        if (failedApplied) res[5] = "تطبيقي";
        
        return res;
    }

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

        // Handle specific status codes
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
        return String.valueOf(mark);
    }

    // ─── PDF public API ────────────────────────────────────────────────────────

    public void appendToDocument(Document doc) {
        try {
            int total  = students.size();
            int pages  = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));

            for (int p = 0; p < pages; p++) {
                int start = p * PAGE_SIZE;
                int end   = Math.min(start + PAGE_SIZE, total);
                List<Student> chunk = students.subList(start, end);

                JPanel page = buildPage(chunk, p + 1, pages);
                doc.setPageSize(com.itextpdf.text.PageSize.A3.rotate());
                doc.newPage();

                BufferedImage img = new BufferedImage(page.getWidth(), page.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
                g2.setPaint(Color.WHITE);
                g2.fillRect(0, 0, img.getWidth(), img.getHeight());
                page.printAll(g2);
                g2.dispose();

                Image pImg = Image.getInstance(img, null);
                pImg.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                pImg.setAbsolutePosition(0, 0);
                doc.add(pImg);
                page.removeNotify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Page builder ──────────────────────────────────────────────────────────

    private JPanel buildPage(List<Student> chunk, int pageNum, int totalPages) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        int W = 13000;
        int H = (int) (W / 1.4142);

        int headerH = 2500, footerH = 1000, tableHdrH = 700;
        int avail   = H - headerH - footerH - tableHdrH - 200;
        int rowCnt  = PAGE_SIZE + 0; // no max/min rows in mixed mode
        this.dynamicRowHeight = Math.min(1800, Math.max(250, avail / rowCnt));

        page.add(buildHeader(chunk, pageNum, totalPages));
        page.add(buildTable(chunk));
        page.add(buildFooter());

        page.setSize(new Dimension(W, H));
        page.addNotify();
        page.validate();
        doLayout(page);
        return page;
    }

    private void doLayout(Container c) {
        for (Component ch : c.getComponents())
            if (ch instanceof Container) doLayout((Container) ch);
        c.doLayout();
        if (c instanceof JComponent) ((JComponent) c).revalidate();
    }

    // ─── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader(List<Student> chunk, int pageNum, int totalPages) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 50, 40, 50));
        p.setPreferredSize(new Dimension(2800, 2500));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 15, 5, 15);

        // RIGHT — ministry
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.33;
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for (String t : new String[]{
            "وزارة الصناعة",
            "مصلحة الكفاية الإنتاجية والتدريب المهني",
            "لجنة النظام والمراقبة"}) {
            JLabel l = lbl(t, 150, true); l.setAlignmentX(Component.RIGHT_ALIGNMENT); right.add(l);
        }
        right.add(Box.createVerticalStrut(30));
        
        String systemStr = "";
        if (chunk != null && !chunk.isEmpty()) {
            String profName = chunk.get(0).getProfession();
            if (profName != null && !profName.trim().isEmpty()) {
                try (java.sql.Connection con = com.pvtd.students.db.DatabaseConnection.getConnection();
                     java.sql.PreparedStatement ps = con.prepareStatement("SELECT exam_system FROM professions WHERE name = ?")) {
                    ps.setString(1, profName);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            systemStr = rs.getString("exam_system");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (systemStr == null || systemStr.trim().isEmpty()) {
                systemStr = chunk.get(0).getExamSystem();
            }
        }
        if (systemStr == null) systemStr = "";

        for (String t : new String[]{
            "المنطقة /" + (region != null ? region.trim() : ""),
            "مركز /"   + (center != null ? center.trim() : ""),
            "النظام /" + systemStr}) {
            JLabel l = lbl(t, 150, true); l.setAlignmentX(Component.RIGHT_ALIGNMENT); right.add(l);
        }
        p.add(right, gbc);

        // CENTER — title
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.34;
        JPanel ctr = new JPanel();
        ctr.setLayout(new BoxLayout(ctr, BoxLayout.Y_AXIS));
        ctr.setOpaque(false);
        JLabel t1 = lbl("نتائج أمتحان دبلوم التلمذة الصناعية", 220, true);
        t1.setAlignmentX(Component.CENTER_ALIGNMENT);
        t1.setHorizontalAlignment(SwingConstants.CENTER);
        ctr.add(t1);
        JLabel t2 = lbl("تلاميذ راسبون", 300, true);
        t2.setForeground(new Color(200, 50, 50));
        t2.setAlignmentX(Component.CENTER_ALIGNMENT);
        t2.setHorizontalAlignment(SwingConstants.CENTER);
        ctr.add(t2);
        ctr.add(Box.createVerticalStrut(25));
        JLabel t3 = lbl("دفعة قبول : " + admissionMonth + " وما قبلها", 150, true);
        t3.setAlignmentX(Component.CENTER_ALIGNMENT); t3.setHorizontalAlignment(SwingConstants.CENTER);
        ctr.add(t3);
        JLabel t4 = lbl("المنعقد في : " + selectedMonth, 150, true);
        t4.setAlignmentX(Component.CENTER_ALIGNMENT); t4.setHorizontalAlignment(SwingConstants.CENTER);
        ctr.add(t4);
        p.add(ctr, gbc);

        // LEFT — logo + page
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.33;
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JLabel logo = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("logo.jpg");
            if (icon.getIconWidth() > 0) {
                int h = 500, w = (icon.getIconWidth() * h) / icon.getIconHeight();
                logo.setIcon(new ImageIcon(icon.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {}
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(logo);
        left.add(Box.createVerticalStrut(40));
        JLabel pg = lbl("صفحة " + pageNum + " من " + totalPages, 140, true);
        pg.setHorizontalAlignment(SwingConstants.LEFT); pg.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(pg);
        p.add(left, gbc);
        return p;
    }

    // ─── Table ─────────────────────────────────────────────────────────────────

    private static final String[] COLS = {
        "م", "الاسم", "رقم التسجيل", "الحرفة", "المجموعة المهنية",
        "الرقم القومي", "رقم الجلوس", "الرقم السري",
        "تكنولوجيا", "رسم", "ميكانيكا عامة", "لغة انجليزية",
        "مجموع النظري", "العملي", "التطبيقي", "مجموع عملي وتطبيقي",
        "المجموع الكلي", "حالة التلميذ", "مواد الرسوب"
    };

    private JPanel buildTable(List<Student> chunk) {
        DefaultTableModel model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = 0; i < PAGE_SIZE; i++) {
            Object[] row = new Object[COLS.length];
            Arrays.fill(row, " ");
            if (i < chunk.size()) {
                Student st = chunk.get(i);
                int[] totals = calcTotals(st);          // [theory, practical, applied]
                int theory = totals[0], prac = totals[1], appl = totals[2];
                int total  = theory + prac + appl;
                String failed = failedSubjects(st);

                int c = 0;
                row[c++] = students.indexOf(st) + 1;
                row[c++] = st.getName();
                row[c++] = st.getRegistrationNo();
                row[c++] = st.getProfession();
                row[c++] = st.getProfessionalGroup();
                row[c++] = st.getNationalId();
                row[c++] = st.getSeatNo();
                row[c++] = st.getSecretNo();
                row[c++] = getTheorySubjectMark(st, 0); // تكنولوجيا
                row[c++] = getTheorySubjectMark(st, 1); // رسم
                row[c++] = getTheorySubjectMark(st, 2); // ميكانيكا عامة
                row[c++] = getTheorySubjectMark(st, 3); // لغة انجليزية
                row[c++] = theory;
                row[c++] = prac;
                row[c++] = appl;
                row[c++] = prac + appl;
                row[c++] = total;
                row[c++] = "راسب";
                row[c++] = getFailedSubjectsArray(st);
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table, dynamicRowHeight);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private JPanel failedSubjectsPanel;
            private JLabel[] failedLabels;

            {
                failedSubjectsPanel = new JPanel(new java.awt.GridLayout(1, 6, 0, 0));
                failedSubjectsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                failedLabels = new JLabel[6];
                for (int i = 0; i < 6; i++) {
                    failedLabels[i] = new JLabel();
                    failedLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
                    failedLabels[i].setFont(new Font("Tahoma", Font.BOLD, 65));
                    failedLabels[i].setOpaque(true);
                    if (i > 0) {
                        failedLabels[i].setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, Color.BLACK));
                    }
                    failedSubjectsPanel.add(failedLabels[i]);
                }
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                
                if ("مواد الرسوب".equals(t.getColumnName(col))) {
                    String[] failedArr = val instanceof String[] ? (String[]) val : new String[6];
                    for (int i = 0; i < 6; i++) {
                        String text = (failedArr.length > i && failedArr[i] != null) ? failedArr[i] : "";
                        failedLabels[i].setText("<html><center style='padding:2px;'>" + text + "</center></html>");
                        failedLabels[i].setBackground(Color.WHITE);
                        failedLabels[i].setForeground(Color.BLACK);
                    }
                    failedSubjectsPanel.setBackground(Color.WHITE);
                    failedSubjectsPanel.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
                    return failedSubjectsPanel;
                }

                String txt = val == null ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>"))
                    txt = "<html><div align='right' style='padding:15px 20px;'>" + txt + "</div></html>";
                Component comp = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                comp.setBackground(Color.WHITE);
                comp.setFont(new Font("Tahoma", Font.PLAIN, 100));
                comp.setForeground(Color.BLACK);
                setHorizontalAlignment(SwingConstants.RIGHT);
                ((JComponent) comp).setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
                return comp;
            }
        });
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(table.getTableHeader(), BorderLayout.NORTH);
        wrap.add(table, BorderLayout.CENTER);
        return wrap;
    }

    private void styleTable(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setFont(new Font("Tahoma", Font.PLAIN, 65));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 600));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                String txt = val == null ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>"))
                    txt = "<html><div align='center' style='padding:10px 5px;'><b>" + txt + "</b></div></html>";
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                c.setBackground(new Color(204, 255, 255)); // Light cyan
                c.setForeground(new Color(10, 30, 60));
                c.setFont(new Font("Tahoma", Font.BOLD, 65));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
                return c;
            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(600);
        // Fine-tune widths
        table.getColumn("م").setPreferredWidth(150);
        table.getColumn("الاسم").setPreferredWidth(1800);
        table.getColumn("رقم التسجيل").setPreferredWidth(1100);
        table.getColumn("الحرفة").setPreferredWidth(2000);
        table.getColumn("المجموعة المهنية").setPreferredWidth(1100);
        table.getColumn("الرقم القومي").setPreferredWidth(1200);
        table.getColumn("رقم الجلوس").setPreferredWidth(800);
        table.getColumn("الرقم السري").setPreferredWidth(700);
        table.getColumn("تكنولوجيا").setPreferredWidth(500);
        table.getColumn("رسم").setPreferredWidth(450);
        table.getColumn("ميكانيكا عامة").setPreferredWidth(600);
        table.getColumn("لغة انجليزية").setPreferredWidth(600);
        table.getColumn("مجموع النظري").setPreferredWidth(600);
        table.getColumn("العملي").setPreferredWidth(500);
        table.getColumn("التطبيقي").setPreferredWidth(500);
        try { table.getColumn("مجموع عملي وتطبيقي").setPreferredWidth(550); } catch (Exception ignored) {}
        table.getColumn("المجموع الكلي").setPreferredWidth(600);
        table.getColumn("حالة التلميذ").setPreferredWidth(700);
        table.getColumn("مواد الرسوب").setPreferredWidth(2400);
    }

    // ─── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 102, 0)));
        p.setPreferredSize(new Dimension(1450, 1000));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        String[] sigs = {"كتبه","راجعه","راجع الاملاء","رصد ووضع الدوائر الحمراء",
                         "راجع الدوائر الحمراء والرصد","راجع المراجعة","رئيس لجنة النظام والمراقبة"};
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
        t.setFont(new Font("Arial", "كتبه".equals(title) ? Font.BOLD : Font.PLAIN, 100));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line = new JLabel("..........................................", SwingConstants.CENTER);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(12));
        p.add(t);
        p.add(line);
        return p;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private JLabel lbl(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(30, 60, 114));
        return l;
    }
}
