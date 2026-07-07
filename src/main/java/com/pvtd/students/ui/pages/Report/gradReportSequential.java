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

    private static final int PAGE_SIZE = 15; // Increased to 15 students per page
    private final Map<String, List<Subject>> subjectCache = new java.util.concurrent.ConcurrentHashMap<>();
    private int dynamicRowHeight = 300;

    public gradReportSequential(String statusTitle, Color titleColor, String center, String region, List<Student> students,
                                String selectedMonth, String admissionMonth) {
        this.statusTitle   = statusTitle;
        this.titleColor    = titleColor;
        this.center        = center;
        this.region        = region;
        this.students      = new java.util.ArrayList<>(students);
        this.students.sort((s1, s2) -> {
            String sn1 = s1.getSeatNo() != null ? s1.getSeatNo().trim() : "";
            String sn2 = s2.getSeatNo() != null ? s2.getSeatNo().trim() : "";
            String sn1Norm = sn1.replace("٠", "0").replace("١", "1").replace("٢", "2").replace("٣", "3").replace("٤", "4")
                                .replace("٥", "5").replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9");
            String sn2Norm = sn2.replace("٠", "0").replace("١", "1").replace("٢", "2").replace("٣", "3").replace("٤", "4")
                                .replace("٥", "5").replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9");
            String sn1Clean = sn1Norm.replaceAll("\\D", "");
            String sn2Clean = sn2Norm.replaceAll("\\D", "");
            if (!sn1Clean.isEmpty() && !sn2Clean.isEmpty()) {
                try {
                    return Long.compare(Long.parseLong(sn1Clean), Long.parseLong(sn2Clean));
                } catch (Exception ex) {}
            }
            return sn1Norm.compareTo(sn2Norm);
        });
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
            // Use Thread Pool to speed up generating pages
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

    private JPanel buildPage(List<Student> chunk, int pageNum, int totalPages) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        int panelWidth = 13000;
        int panelHeight = 9192;

        int headerH = 1100, footerH = 800, tableHeaderH = 600; // Match actual preferred heights
        int available = panelHeight - headerH - footerH - tableHeaderH - 100;
        int totalRows = 15 + 2; 
        int calculatedH = available / totalRows; 
        this.dynamicRowHeight = Math.min(550, Math.max(300, calculatedH));

        page.add(buildHeader(chunk, pageNum, totalPages));
        JPanel tablePanel = buildTable(chunk, pageNum);
        tablePanel.setPreferredSize(new Dimension(panelWidth, available + tableHeaderH));
        tablePanel.setMaximumSize(new Dimension(panelWidth, available + tableHeaderH));
        page.add(tablePanel);
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

        JLabel sub = label(statusTitle, 200, true);
        sub.setForeground(titleColor);
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
        } catch (Exception e) {
        }
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
        if (shouldHideSecondRoundColumn()) {
            List<String> list = new ArrayList<>(Arrays.asList(COLS));
            list.remove("مواد الدور الثاني");
            effectiveCols = list.toArray(new String[0]);
        }

        DefaultTableModel model = new DefaultTableModel(effectiveCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

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
                row[c++] = st.getStatus() != null ? st.getStatus() : "";
                if (!shouldHideSecondRoundColumn()) {
                    row[c++] = getFailedSubjectsArray(st); // مواد الدور الثاني
                }
                row[c]   = ""; // الملاحظات
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
                        failedLabels[i].setBorder(BorderFactory.createMatteBorder(0, 0, 0, 5, Color.BLACK));
                    }
                    failedSubjectsPanel.add(failedLabels[i]);
                }
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String colName = t.getColumnName(col);
                if ("مواد الرسوب".equals(colName) || "مواد الدور الثاني".equals(colName)) {
                    String[] failedArr = val instanceof String[] ? (String[]) val : new String[6];

                    // Calculate the width available per sub-cell (total column width / 6)
                    int totalColWidth = t.getColumnModel().getColumn(col).getWidth();
                    int subCellWidth = totalColWidth > 0 ? (totalColWidth / 6) - 4 : 200;

                    for (int i = 0; i < 6; i++) {
                        String text = (failedArr.length > i && failedArr[i] != null) ? failedArr[i] : "";

                        // Improved Auto Font Size for Failed Subjects Labels
                        int fontSize = 75;
                        int minFontSize = 42;
                        if (!text.isEmpty() && subCellWidth > 0) {
                            String plainText = text.replaceAll("<[^>]*>", ""); // strip HTML tags for measurement
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
                        // Generous padding and width to prevent touching borders
                        int adjustedWidth = subCellWidth - 40; 
                        failedLabels[i].setText("<html><div align='center' style='padding: 15px; width: " + adjustedWidth + "px;'>" + text + "</div></html>");
                        failedLabels[i].setVerticalAlignment(SwingConstants.CENTER);
                        failedLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
                        failedLabels[i].setBackground(Color.WHITE);
                        failedLabels[i].setForeground(Color.BLACK);
                    }
                    failedSubjectsPanel.setBackground(Color.WHITE);
                    failedSubjectsPanel.setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, Color.BLACK));
                    return failedSubjectsPanel;
                }

                String rawTxt = (val == null) ? "" : toAr(val);
                String txt = rawTxt;
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div dir='rtl' align='center' style='padding:20px 15px;'>" + txt + "</div></html>";
                }
                Component comp = super.getTableCellRendererComponent(t, txt, false, false, row, col);
                
                // Auto Font Size Logic
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
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, Color.BLACK));
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
        try { table.getColumn("الملاحظات").setPreferredWidth(600); } catch (Exception ignored) {}
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 102, 0)));
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
        if ("كتبه".equals(title)) {
            t.setFont(new Font("Arial", Font.BOLD, 120));
        } else {
            t.setFont(new Font("Arial", Font.PLAIN, 100));
        }
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line = new JLabel("..........................................", SwingConstants.CENTER);
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(12));
        p.add(t);
        p.add(line);
        return p;
    }

    private JLabel lbl(String t, int size, boolean bold) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
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
