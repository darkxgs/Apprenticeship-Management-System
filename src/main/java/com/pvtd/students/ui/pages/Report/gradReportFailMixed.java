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

    private static final int PAGE_SIZE = 15;

    private final String center;
    private final String region;
    private final List<Student> students;
    private String selectedMonth;
    private String admissionMonth;

    // Cache: profession -> subjects
    private final Map<String, List<Subject>> subjectCache = new java.util.concurrent.ConcurrentHashMap<>();

    private int dynamicRowHeight = 300;

    private boolean isSecondRound;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public gradReportFailMixed(String center, String region, List<Student> students,
                               String selectedMonth, String admissionMonth) {
        this(center, region, students, selectedMonth, admissionMonth, false);
    }

    public gradReportFailMixed(String center, String region, List<Student> students,
                               String selectedMonth, String admissionMonth, boolean isSecondRound) {
        this.center        = center;
        this.region        = region;
        this.students      = students;
        this.selectedMonth = selectedMonth;
        this.admissionMonth = admissionMonth;
        this.isSecondRound = isSecondRound;

        setTitle((isSecondRound ? "كشف الدور الثاني بالدرجات - " : "كشف الراسبين بالدرجات - ") + region);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setBackground(Color.WHITE);
    }

    // ─── Subject helpers ───────────────────────────────────────────────────────

    private List<Subject> subjectsFor(String profession) {
        return subjectCache.computeIfAbsent(
            profession == null ? "" : profession.trim(),
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

    private boolean isSecondRoundReport() {
        if (isSecondRound) return true;
        if (students == null || students.isEmpty()) return false;
        String s = students.get(0).getStatus();
        return "دور ثاني".equals(s);
    }

    private String getFailedColName() {
        return isSecondRoundReport() ? "مواد الدور الثاني" : "مواد الرسوب";
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
            int total = students.size();
            if (total == 0) return;
            int pages = (int) Math.ceil(total / (double) PAGE_SIZE);

            int pagesAdded = 0;
            int cores = Runtime.getRuntime().availableProcessors();
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                    .newFixedThreadPool(Math.max(2, cores / 2));
            java.util.List<java.util.concurrent.Future<BufferedImage>> futures = new java.util.ArrayList<>();

            for (int pIdx = 0; pIdx < pages; pIdx++) {
                final int pageIndex = pIdx;
                final int totalP = pages;
                int start = pIdx * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, total);
                final List<Student> chunk = students.subList(start, end);

                futures.add(executor.submit(() -> {
                    JPanel pagePanel = buildPage(chunk, pageIndex + 1, totalP);

                    double scale = 0.5;
                    int imgW = (int) (13000 * scale);
                    int imgH = (int) (9192 * scale);

                    BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2 = img.createGraphics();
                    g2.scale(scale, scale);

                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                            java.awt.RenderingHints.VALUE_RENDER_QUALITY);

                    g2.setPaint(Color.WHITE);
                    g2.fillRect(0, 0, 13000, 9192);
                    pagePanel.printAll(g2);
                    g2.dispose();

                    return img;
                }));
            }

            for (java.util.concurrent.Future<BufferedImage> future : futures) {
                try {
                    BufferedImage bimg = future.get();
                    if (bimg != null) {
                        Image pImg = Image.getInstance(bimg, null);
                        doc.setPageSize(com.itextpdf.text.PageSize.A3.rotate());
                        doc.newPage();
                        pImg.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                        pImg.setAbsolutePosition(0, 0);
                        doc.add(pImg);
                        bimg.flush();
                        pagesAdded++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();

            if (pagesAdded == 0) {
                doc.newPage();
                doc.add(new com.itextpdf.text.Paragraph("No data or error during generation"));
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

        int panelWidth = 13000;
        int panelHeight = 9192;

        int headerH = 1100, footerH = 1000, tableHeaderH = 800;
        int available = panelHeight - headerH - footerH - tableHeaderH - 400;
        int totalRows = 15 + 2; 
        int calculatedH = available / totalRows; 
        this.dynamicRowHeight = Math.min(550, Math.max(300, calculatedH));

        page.add(buildHeader(chunk, pageNum, totalPages));
        page.add(buildTable(chunk));
        page.add(buildFooter());

        page.setSize(new Dimension(panelWidth, panelHeight));
        page.addNotify();
        page.validate();
        doLayout(page);
        return page;
    }

    private void doLayout(java.awt.Container c) {
        for (Component ch : c.getComponents()) {
            if (ch instanceof java.awt.Container) {
                doLayout((java.awt.Container) ch);
            }
        }
        c.doLayout();
        if (c instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) c).revalidate();
        }
    }

    // ─── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader(List<Student> chunk, int pageNum, int totalPages) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 50, 40, 50));
        p.setPreferredSize(new Dimension(13000, 1100));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(2, 15, 2, 15);

        // --- RIGHT BLOCK (Ministry & Location) ---
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.33;
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel l1 = label("وزارة الصناعة", 110, true);
        JLabel l2 = label("مصلحة الكفاية الإنتاجية والتدريب المهني", 110, true);
        JLabel l2_5 = label("الرئاسة العامة للامتحانات لدبلوم التلمذة الصناعية", 110, true);
        JLabel l3 = label("لجنة النظام والمراقبة", 110, true);

        String regionVal = (region != null ? region.trim() : "");
        String centerVal = (center != null ? center.trim() : "");
        String systemVal = "";
        if (chunk != null && !chunk.isEmpty()) {
            systemVal = chunk.get(0).getExamSystem();
        }
        if (systemVal == null) systemVal = "";
        systemVal = systemVal.trim();

        JLabel l4 = new JLabel("المنطقة: " + regionVal);
        JLabel l5 = new JLabel("المركز: " + centerVal);
        JLabel l6 = new JLabel("النظام: " + systemVal);

        l4.setFont(new Font("Arial", Font.BOLD, 110));
        l5.setFont(new Font("Arial", Font.BOLD, 110));
        l6.setFont(new Font("Arial", Font.BOLD, 110));

        l4.setForeground(new Color(30, 60, 114));
        l5.setForeground(new Color(30, 60, 114));
        l6.setForeground(new Color(30, 60, 114));

        l1.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l2_5.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l3.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l4.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l5.setAlignmentX(Component.RIGHT_ALIGNMENT);
        l6.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(l1);
        rightPanel.add(l2);
        rightPanel.add(l2_5);
        rightPanel.add(l3);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(l4);
        rightPanel.add(l5);
        rightPanel.add(l6);
        p.add(rightPanel, gbc);

        // --- CENTER BLOCK (Exam Title & Batch Info) ---
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.34;
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel mainTitle = label("نتائج أمتحان دبلوم التلمذة الصناعية", 120, true);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(mainTitle);

        String titleText = isSecondRoundReport() ? "تلاميذ راسبون ولهم حق دخول الدور الثاني" : "تلاميذ راسبون بالدرجات";
        JLabel sub = label(titleText, 200, true);
        sub.setForeground(isSecondRoundReport() ? new Color(255, 102, 0) : new Color(200, 50, 50));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(sub);

        centerPanel.add(Box.createVerticalStrut(15));

        JLabel batchInfo = label("دفعة قبول : " + admissionMonth + " وما قبلها",
                150, true);

        batchInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        batchInfo.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(batchInfo);

        JLabel examDate = label("المنعقد في : " + selectedMonth, 150, true);

        examDate.setAlignmentX(Component.CENTER_ALIGNMENT);
        examDate.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(examDate);
        p.add(centerPanel, gbc);

        // --- LEFT BLOCK (Logo & Page Info) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.33;
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("logo.jpg");
            if (icon.getIconWidth() > 0) {
                int targetHeight = 500;
                int targetWidth = (icon.getIconWidth() * targetHeight) / icon.getIconHeight();
                java.awt.Image scaled = icon.getImage().getScaledInstance(targetWidth, targetHeight,
                        java.awt.Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (Exception e) {}
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalStrut(40));

        JLabel l7 = label("إستمارة رقم ١٥ امتحانات", 60, false);
        JLabel l8 = label("صفحة " + pageNum + " من " + totalPages, 140, true);

        l7.setHorizontalAlignment(SwingConstants.LEFT);
        l8.setHorizontalAlignment(SwingConstants.LEFT);
        l7.setAlignmentX(Component.LEFT_ALIGNMENT);
        l8.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(l7);
        leftPanel.add(l8);
        p.add(leftPanel, gbc);

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
        String failedCol = getFailedColName();
        String[] cols = COLS.clone();
        cols[cols.length - 1] = failedCol;

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (int i = 0; i < PAGE_SIZE; i++) {
            Object[] row = new Object[COLS.length];
            Arrays.fill(row, " ");
            if (i < chunk.size()) {
                Student st = chunk.get(i);
                int[] totals = calcTotals(st);
                int theory = totals[0], prac = totals[1], appl = totals[2];
                int total  = theory + prac + appl;

                int c = 0;
                row[c++] = students.indexOf(st) + 1;
                row[c++] = st.getName();
                row[c++] = st.getRegistrationNo();
                row[c++] = st.getProfession();
                row[c++] = st.getProfessionalGroup();
                row[c++] = st.getNationalId();
                row[c++] = st.getSeatNo();
                row[c++] = st.getSecretNo();
                row[c++] = getTheorySubjectMark(st, 0);
                row[c++] = getTheorySubjectMark(st, 1);
                row[c++] = getTheorySubjectMark(st, 2);
                row[c++] = getTheorySubjectMark(st, 3);
                row[c++] = theory;
                row[c++] = prac;
                row[c++] = appl;
                row[c++] = prac + appl;
                row[c++] = total;
                row[c++] = st.getStatus() != null ? st.getStatus() : "راسب";
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
                    failedLabels[i].setVerticalAlignment(SwingConstants.CENTER);
                    failedLabels[i].setFont(new Font("Tahoma", Font.BOLD, 75));
                    failedLabels[i].setOpaque(true);
                    if (i > 0) {
                        failedLabels[i].setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, Color.BLACK));
                    }
                    failedSubjectsPanel.add(failedLabels[i]);
                }
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                if (failedCol.equals(t.getColumnName(col))) {
                    String[] failedArr = val instanceof String[] ? (String[]) val : new String[6];

                    int totalColWidth = t.getColumnModel().getColumn(col).getWidth();
                    int subCellWidth = totalColWidth > 0 ? (totalColWidth / 6) - 4 : 200;

                    for (int i = 0; i < 6; i++) {
                        String text = (failedArr.length > i && failedArr[i] != null) ? failedArr[i] : "";

                        int fontSize = 75;
                        int minFontSize = 42;
                        if (!text.isEmpty() && subCellWidth > 0) {
                            String plainText = text.replaceAll("<[^>]*>", "");
                            Font testFont = new Font("Tahoma", Font.BOLD, fontSize);
                            java.awt.FontMetrics fm = failedLabels[i].getFontMetrics(testFont);
                            int textWidth = fm.stringWidth(plainText);
                            while (textWidth > subCellWidth - 30 && fontSize > minFontSize) {
                                fontSize--;
                                testFont = new Font("Tahoma", Font.BOLD, fontSize);
                                fm = failedLabels[i].getFontMetrics(testFont);
                                textWidth = fm.stringWidth(plainText);
                            }
                        }

                        failedLabels[i].setFont(new Font("Tahoma", Font.BOLD, fontSize));
                        int adjustedWidth = subCellWidth - 40; 
                        failedLabels[i].setText("<html><div align='center' style='padding: 15px; width: " + adjustedWidth + "px;'>" + text + "</div></html>");
                        failedLabels[i].setVerticalAlignment(SwingConstants.CENTER);
                        failedLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
                        failedLabels[i].setBackground(Color.WHITE);
                        failedLabels[i].setForeground(Color.BLACK);
                    }
                    failedSubjectsPanel.setBackground(Color.WHITE);
                    failedSubjectsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                    return failedSubjectsPanel;
                }

                String rawTxt = (val == null) ? "" : toAr(val);
                String txt = rawTxt;
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div dir='rtl' align='center' style='padding:20px 15px;'>" + txt + "</div></html>";
                }
                Component comp = super.getTableCellRendererComponent(t, txt, false, false, row, col);
                
                int fontSize = 110;
                int minFS = 50;
                int colWidth = t.getColumnModel().getColumn(col).getWidth();
                int availW = colWidth - 60;
                
                if (availW > 0 && !rawTxt.isEmpty() && !rawTxt.contains("<")) {
                    Font testF = new Font("Tahoma", Font.PLAIN, fontSize);
                    java.awt.FontMetrics fm = t.getFontMetrics(testF);
                    int textW = fm.stringWidth(rawTxt);
                    while (textW > availW && fontSize > minFS) {
                        fontSize -= 2;
                        testF = new Font("Tahoma", Font.PLAIN, fontSize);
                        fm = t.getFontMetrics(testF);
                        textW = fm.stringWidth(rawTxt);
                    }
                }
                
                comp.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
                comp.setForeground(Color.BLACK);
                comp.setBackground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.CENTER);
                setVerticalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent) comp).setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
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
        table.setFont(new Font("Tahoma", Font.PLAIN, 85));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 600));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div align='center' style='padding:10px 5px;'><b>" + txt + "</b></div></html>";
                }
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                c.setBackground(new Color(204, 255, 255));
                c.setForeground(new Color(10, 30, 60));
                c.setFont(new Font("Tahoma", Font.BOLD, 85));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                return c;
            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(600);
        }
        table.getColumn("م").setPreferredWidth(150);
        table.getColumn("الاسم").setPreferredWidth(1400); 
        table.getColumn("رقم التسجيل").setPreferredWidth(800); 
        table.getColumn("رقم الجلوس").setPreferredWidth(800);
        table.getColumn("الحرفة").setPreferredWidth(1000); 
        table.getColumn("المجموعة المهنية").setPreferredWidth(900); 
        table.getColumn("الرقم القومي").setPreferredWidth(900); 
        table.getColumn("الرقم السري").setPreferredWidth(500);
        
        try { table.getColumn("تكنولوجيا").setPreferredWidth(500); } catch (Exception ignored) {}
        try { table.getColumn("رسم").setPreferredWidth(450); } catch (Exception ignored) {}
        try { table.getColumn("ميكانيكا عامة").setPreferredWidth(600); } catch (Exception ignored) {}
        try { table.getColumn("لغة انجليزية").setPreferredWidth(600); } catch (Exception ignored) {}
        try { table.getColumn("مجموع النظري").setPreferredWidth(600); } catch (Exception ignored) {}
        
        try { table.getColumn("العملي").setPreferredWidth(500); } catch (Exception ignored) {}
        try { table.getColumn("التطبيقي").setPreferredWidth(500); } catch (Exception ignored) {}
        try { table.getColumn("مجموع عملي وتطبيقي").setPreferredWidth(500); } catch (Exception ignored) {}
        try { table.getColumn("المجموع الكلي").setPreferredWidth(600); } catch (Exception ignored) {}
        try { table.getColumn("حالة التلميذ").setPreferredWidth(700); } catch (Exception ignored) {}
        try { table.getColumn("مواد الرسوب").setPreferredWidth(4200); } catch (Exception ignored) {}
        try { table.getColumn("مواد الدور الثاني").setPreferredWidth(4200); } catch (Exception ignored) {}
    }

    // ─── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 102, 0)));
        p.setPreferredSize(new Dimension(13000, 2000));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        String[] sigs = { "كتبه", "راجعه", "راجع الاملاء", "رصد ووضع الدوائر الحمراء",
                "راجع الدوائر الحمراء والرصد", "راجع المراجعة", "رئيس لجنة النظام والمراقبة" };
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

    /** Used by buildHeader — matches the reference model's helper name. */
    private JLabel label(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(30, 60, 114));
        return l;
    }

    private JLabel lbl(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Tahoma", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(10, 30, 60));
        return l;
    }

    private String toAr(Object val) {
        if (val == null) return "";
        String s = val.toString();
        return s.replace("0","٠").replace("1","١").replace("2","٢")
                 .replace("3","٣").replace("4","٤").replace("5","٥")
                 .replace("6","٦").replace("7","٧").replace("8","٨").replace("9","٩");
    }
}
