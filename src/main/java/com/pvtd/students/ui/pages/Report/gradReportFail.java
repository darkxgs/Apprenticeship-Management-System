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
 * Report: تلاميذ راسبون — Precise Layout synchronized with successful report
 * style (A3 Landscape).
 */
public class gradReportFail extends JFrame {

    private final String profession;
    private final String center;
    private final String region;
    private final String system;
    private final List<Student> students;
    private List<Subject> subjects;
    private String selectedMonth;
    private String admissionMonth;

    private JPanel uiRootPanel;
    private int dynamicRowHeight = 200;

    public gradReportFail(String profession, String center, String region, String system, List<Student> students) {
        this(profession, center, region, system, students, null, null);
    }

    public gradReportFail(String profession, String center, String region, String system, List<Student> students,
            String selectedMonth, String admissionMonth) {
        this.profession = profession;
        this.center = center;
        this.region = region;
        this.system = system;
        this.students = students;
        this.subjects = SubjectService.getSubjectsByProfession(profession);

        if (selectedMonth != null && admissionMonth != null) {
            this.selectedMonth = selectedMonth;
            this.admissionMonth = admissionMonth;
        } else {
            this.selectedMonth = chooseMonth("اختر المنعقد فيه", "يوليو");
            this.admissionMonth = chooseMonth("اختر شهر دفعة القبول", "أكتوبر");
        }

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

        // Standardized to 4 rows per page to allow more height for failed subjects
        int pageSize = 3; // Reduced from 4 to increase row height for subject visibility
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
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 50, 40, 50));
        p.setPreferredSize(new Dimension(13000, 2500));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 15, 5, 15);

        // --- RIGHT BLOCK (Ministry & Location) ---
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.33;
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel l1 = label("وزارة الصناعة", 400, true);
        JLabel l2 = label("مصلحة الكفاية الإنتاجية والتدريب المهني", 400, true);
        JLabel l2_5 = label("الرئاسة العامة للامتحانات لدبلوم التلمذة الصناعية", 400, true);
        JLabel l3 = label("لجنة النظام والمراقبة", 400, true);

        JLabel l4 = label("المنطقة / " + (region != null ? region.trim() : ""), 400, true);
        JLabel l5 = label("مركز / " + (center != null ? center.trim() : ""), 400, true);
        JLabel l6 = label("النظام / " + (system != null ? system.trim() : ""), 400, true);

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
        rightPanel.add(Box.createVerticalStrut(30));
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

        JLabel mainTitle = label("نتائج أمتحان دبلوم التلمذة الصناعية", 350, true);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(mainTitle);

        JLabel sub = label("تلاميذ راسبون", 400, true);
        sub.setForeground(new Color(200, 50, 50));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(sub);

        centerPanel.add(Box.createVerticalStrut(25));

        JLabel batchInfo = label("دفعة قبول : " + admissionMonth + " وما قبلها",
                280, true);

        batchInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        batchInfo.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(batchInfo);

        JLabel examDate = label("المنعقد في : " + selectedMonth, 280, true);

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
        // Base (8) + subjects + theorySum + Practical + Applied + Combined + Total + Status + FailedSubjects
        String[] cols = new String[8 + subCount + 1 + 1 + 1 + 1 + 1 + 1 + 1];
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
        cols[i++] = "حالة التلميذ";
        cols[i] = "مواد الرسوب";

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
            int m = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getMaxMark).sum()
                    : s.getMaxMark();
            int p = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getPassMark).sum()
                    : s.getPassMark();

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
            int m = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getMaxMark).sum()
                    : s.getMaxMark();
            maxRow[colIdx++] = m;
            if (s.getName() != null && s.getName().contains("لغة انجليزية")) {
                maxRow[colIdx++] = theoryMaxSum;
                tMaxAdded = true;
            }
        }
        if (!tMaxAdded)
            maxRow[colIdx++] = theoryMaxSum;
        maxRow[colIdx++] = practicalMax;
        maxRow[colIdx++] = appliedMax;
        maxRow[colIdx++] = (practicalMax + appliedMax);
        maxRow[colIdx++] = (theoryMaxSum + practicalMax + appliedMax);
        maxRow[colIdx++] = " "; // حالة
        maxRow[colIdx] = " "; // مواد الرسوب
        model.addRow(maxRow);

        // 2. Min Row
        Object[] minRow = new Object[cols.length];
        minRow[1] = "النهاية الصغرى";
        colIdx = 8;
        boolean tPassAdded = false;
        for (Subject s : parentSubjects) {
            List<Subject> children = childrenMap.get(s.getId());
            int p = (children != null && !children.isEmpty()) ? children.stream().mapToInt(Subject::getPassMark).sum()
                    : s.getPassMark();
            minRow[colIdx++] = p;
            if (s.getName() != null && s.getName().contains("لغة انجليزية")) {
                minRow[colIdx++] = theoryPassSum;
                tPassAdded = true;
            }
        }
        if (!tPassAdded)
            minRow[colIdx++] = theoryPassSum;
        minRow[colIdx++] = (int) Math.ceil(practicalMax * 0.5);
        minRow[colIdx++] = (int) Math.ceil(appliedMax * 0.5);
        minRow[colIdx++] = (int) Math.ceil((practicalMax + appliedMax) * 0.5);
        minRow[colIdx++] = (int) Math.ceil((theoryMaxSum + practicalMax + appliedMax) * 0.5);
        minRow[colIdx++] = " "; // حالة
        minRow[colIdx] = " "; // مواد الرسوب
        model.addRow(minRow);

        // 3. Students Rows
        for (int i_chunk = 0; i_chunk < 4; i_chunk++) {
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

                boolean tRowAdded = false;
                java.util.List<String> theoryFailed = new java.util.ArrayList<>();
                boolean failedPractical = false;
                boolean failedApplied = false;

                String status = st.getStatus();
                boolean isAllowed = "ناجح".equals(status) || "راسب".equals(status) || "دور ثاني".equals(status);

                for (Subject s : parentSubjects) {
                    List<Subject> children = childrenMap.get(s.getId());
                    int mark = (children != null && !children.isEmpty())
                            ? children.stream()
                                    .mapToInt(child -> grades != null ? grades.getOrDefault(child.getId(), 0) : 0).sum()
                            : (grades != null ? grades.getOrDefault(s.getId(), 0) : 0);

                    if (!isAllowed || mark < 0) mark = 0;

                    int passMark = (children != null && !children.isEmpty())
                            ? children.stream().mapToInt(Subject::getPassMark).sum()
                            : s.getPassMark();

                    row[c++] = mark;
                    if ("نظري".equals(s.getType())) {
                        theorySum += mark;
                        if (isAllowed && mark < passMark && s.getName() != null && !s.getName().isBlank()) {
                            theoryFailed.add(s.getName());
                        }
                    }
                    else if ("تطبيقي".equals(s.getType())) {
                        appliedMark += mark;
                        if (isAllowed && mark < passMark) failedApplied = true;
                    }
                    else {
                        practicalMark += mark;
                        if (isAllowed && mark < passMark) failedPractical = true;
                    }

                    if (s.getName() != null && s.getName().contains("لغة انجليزية")) {
                        row[c++] = theorySum;
                        tRowAdded = true;
                    }
                }
                if (!tRowAdded)
                    row[c++] = theorySum;

                row[c++] = practicalMark;
                row[c++] = appliedMark;
                row[c++] = (practicalMark + appliedMark);
                row[c++] = (theorySum + practicalMark + appliedMark);
                row[c++] = "راسب"; // حالة
                
                String[] failedArr = new String[6];
                java.util.Arrays.fill(failedArr, "");
                for (int k = 0; k < 4 && k < theoryFailed.size(); k++) {
                    failedArr[k] = theoryFailed.get(k);
                }
                if (theoryFailed.size() > 4) {
                    failedArr[3] = String.join("<br>", theoryFailed.subList(3, theoryFailed.size()));
                }
                if (failedPractical) failedArr[4] = "عملي";
                if (failedApplied) failedArr[5] = "تطبيقي";
                
                row[c] = failedArr; // مواد الرسوب
                model.addRow(row);
            } else {
                Object[] row = new Object[cols.length];
                for (int x = 0; x < cols.length; x++)
                    row[x] = " ";
                model.addRow(row);
            }
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
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row,
                    int col) {
                
                if ("مواد الرسوب".equals(t.getColumnName(col))) {
                    String[] failedArr = val instanceof String[] ? (String[]) val : new String[6];
                    for (int i = 0; i < 6; i++) {
                        String text = (failedArr.length > i && failedArr[i] != null) ? failedArr[i] : "";
                        failedLabels[i].setText("<html><center style='padding:2px;'>" + text + "</center></html>");
                        if (row == 0) failedLabels[i].setBackground(new Color(230, 255, 240)); 
                        else if (row == 1) failedLabels[i].setBackground(new Color(255, 253, 220)); 
                        else failedLabels[i].setBackground(Color.WHITE);
                        failedLabels[i].setForeground(Color.BLACK);
                    }
                    if (row == 0) failedSubjectsPanel.setBackground(new Color(230, 255, 240)); 
                    else if (row == 1) failedSubjectsPanel.setBackground(new Color(255, 253, 220)); 
                    else failedSubjectsPanel.setBackground(Color.WHITE);
                    failedSubjectsPanel.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
                    return failedSubjectsPanel;
                }

                String txt = (val == null) ? "" : val.toString();
                // Wrap long text (like حالة) with HTML word-wrap
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div align='right' style='padding:15px 20px; word-wrap:break-word;'>" + txt
                            + "</div></html>";
                }
                Component comp = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                if (row == 0)
                    comp.setBackground(new Color(230, 255, 240)); // Lighter green
                else if (row == 1)
                    comp.setBackground(new Color(255, 253, 220)); // Lighter yellow
                else
                    comp.setBackground(Color.WHITE);

                comp.setFont(new Font("Tahoma", Font.PLAIN, 100));
                comp.setForeground(Color.BLACK);
                setHorizontalAlignment(SwingConstants.RIGHT);
                ((javax.swing.JComponent) comp).setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
                return comp;
            }
        });
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);

        JPanel tableCont = new JPanel(new BorderLayout());
        tableCont.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCont.add(table, BorderLayout.CENTER);
        return tableCont;
    }

    private void styleTable(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setFont(new java.awt.Font("Tahoma", Font.PLAIN, 65));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 600));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row,
                    int col) {
                String txt = (val == null) ? "" : val.toString();
                if (!txt.toLowerCase().startsWith("<html>")) {
                    txt = "<html><div align='center' style='padding:10px 5px;'><b>" + txt + "</b></div></html>";
                }
                Component c = super.getTableCellRendererComponent(t, txt, sel, foc, row, col);
                c.setBackground(new Color(204, 255, 255)); // Light cyan
                c.setForeground(new Color(10, 30, 60)); // Dark navy/black
                c.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 65));
                setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
                return c;
            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(600);
        }
        table.getColumn("م").setPreferredWidth(150);
        table.getColumn("الاسم").setPreferredWidth(1800);
        table.getColumn("رقم التسجيل").setPreferredWidth(1200);
        table.getColumn("رقم الجلوس").setPreferredWidth(800);
        table.getColumn("الحرفة").setPreferredWidth(1500);
        table.getColumn("المجموعة المهنية").setPreferredWidth(1200);
        table.getColumn("الرقم القومي").setPreferredWidth(1200);
        table.getColumn("الرقم السري").setPreferredWidth(700);
        try {
            table.getColumn("مجموع عملي وتطبيقي").setPreferredWidth(500);
        } catch (Exception e) {
        }
        try {
            table.getColumn("حالة التلميذ").setPreferredWidth(700);
        } catch (Exception e) {
        }
        try {
            table.getColumn("مواد الرسوب").setPreferredWidth(2400);
        } catch (Exception e) {
        }
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(255, 102, 0))); // Thick orange line like in
                                                                                          // reference image
        p.setPreferredSize(new Dimension(13000, 2000));
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

        int headerH = 2500, footerH = 1000, tableHeaderH = 800;
        int available = panelHeight - headerH - footerH - tableHeaderH - 200;
        int totalRows = 4 + 2; // 4 student rows + 2 header rows (max/min)
        int calculatedH = available / totalRows;
        this.dynamicRowHeight = Math.min(1500, Math.max(250, calculatedH));

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendToDocument(Document doc) {
        try {
            int pageSizeCount = 4;
            int totalCount = students.size();
            int totalPages = (int) Math.ceil(totalCount / (double) pageSizeCount);
            if (totalPages == 0)
                totalPages = 1;

            // استخدام Thread Pool لتسريع عملية التوليد عبر استغلال كافة الأنوية
            int cores = Runtime.getRuntime().availableProcessors();
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                    .newFixedThreadPool(Math.max(2, cores / 2));
            java.util.List<java.util.concurrent.Future<BufferedImage>> futures = new java.util.ArrayList<>();

            for (int pIdx = 0; pIdx < totalPages; pIdx++) {
                final int pageIndex = pIdx;
                final int totalP = totalPages;
                int start = pIdx * pageSizeCount;
                int end = Math.min(start + pageSizeCount, totalCount);
                final List<Student> chunk = students.subList(start, end);

                futures.add(executor.submit(() -> {
                    JPanel pagePanel = buildPagePanel(chunk, pageIndex + 1, totalP);

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

            int pagesAdded = 0;
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
}
