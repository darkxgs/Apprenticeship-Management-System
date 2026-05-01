package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Report: تلاميذ راسبون — dynamic subject columns + max/min rows
 */
public class gradReport extends JFrame {

    private final String profession;
    private final List<Student> students;
    private List<Subject> subjects;

    public gradReport(String profession, List<Student> students) {
        this.profession = profession;
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
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTable(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 20, 10, 20));
        p.setPreferredSize(new Dimension(0, 160));

        // Right block: ministry info
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        right.add(label("وزارة الصناعة", 14, true));
        right.add(label("مصلحة الكفاية الانتاجية والتدريب المهنى", 14, true));
        right.add(label("لجنة النظام والمراقبة", 14, true));
        p.add(right, BorderLayout.EAST);

        // Center block: title
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = new JLabel("نتائج امتحان دبلوم التلمذة الصناعية", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(61, 59, 110));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("تلاميذ راسبون", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sub.setForeground(new Color(51, 51, 255));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel profLabel = new JLabel("الحرفة: " + profession, SwingConstants.CENTER);
        profLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profLabel.setForeground(new Color(61, 59, 110));
        profLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(Box.createVerticalStrut(15));
        center.add(title);
        center.add(sub);
        center.add(profLabel);
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        // Build column names dynamically
        // Fixed columns (RTL order — rightmost first): م, الاسم, كود الحرفه, الحرفه, الرقم القومي, رقم الجلوس, الرقم السري,
        //   [subject1..subjectN], مجموع النظري, درجات العملي, درجات التطبيقي,
        //   مجموع العملي والتطبيقي, المجموع الكلي, حاله التلميذ, مواد الرسوب
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
        String[] cols = new String[6 + subCount + 7];
        int i = 0;
        cols[i++] = "م";
        cols[i++] = "الاسم";
        cols[i++] = "كود الحرفه";
        cols[i++] = "الحرفه";
        cols[i++] = "الرقم القومي";
        cols[i++] = "رقم الجلوس";
        // subject columns in order
        for (Subject s : parentSubjects) {
            cols[i++] = s.getName();
        }
        cols[i++] = "مجموع النظري";
        cols[i++] = "درجات العملي";
        cols[i++] = "درجات التطبيقي";
        cols[i++] = "مجموع العملي والتطبيقي";
        cols[i++] = "المجموع الكلي";
        cols[i++] = "حاله التلميذ";
        cols[i++] = "مواد الرسوب";

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Row 1: النهاية العظمى
        Object[] maxRow = new Object[cols.length];
        maxRow[0] = "النهاية العظمى";
        for (int j = 0; j < parentSubjects.size(); j++) {
            Subject s = parentSubjects.get(j);
            List<Subject> children = childrenMap.get(s.getId());
            int m = (children != null && !children.isEmpty()) ? 0 : s.getMaxMark(); 
            if (children != null) {
                for (Subject c : children) m += c.getMaxMark();
            }
            maxRow[6 + j] = m;
        }
        model.addRow(maxRow);

        // Row 2: النهاية الصغرى
        Object[] minRow = new Object[cols.length];
        minRow[0] = "النهاية الصغرى";
        for (int j = 0; j < parentSubjects.size(); j++) {
            Subject s = parentSubjects.get(j);
            List<Subject> children = childrenMap.get(s.getId());
            int p = (children != null && !children.isEmpty()) ? 0 : s.getPassMark();
            if (children != null) {
                for (Subject c : children) p += c.getPassMark();
            }
            minRow[6 + j] = p;
        }
        model.addRow(minRow);

        // Student rows
        int seq = 1;
        for (Student st : students) {
            Object[] row = new Object[cols.length];
            row[0] = seq++;
            row[1] = st.getName();
            row[2] = st.getRegistrationNo();  // كود الحرفه — يستخدم رقم التسجيل
            row[3] = st.getProfession();
            row[4] = st.getNationalId();
            row[5] = st.getSeatNo();
            // subject grades
            int theorySum = 0;
            int practicalMark = 0;
            int appliedMark = 0;
            StringBuilder failedSubjects = new StringBuilder();
            int colIdx = 6;
            for (Subject s : parentSubjects) {
                List<Subject> children = childrenMap.get(s.getId());
                // Skip parent grade if children exist to avoid double-counting
                int mark = (children != null && !children.isEmpty()) ? 0 : (st.getGrades() != null ? st.getGrades().getOrDefault(s.getId(), 0) : 0);
                if (children != null) {
                    for (Subject c : children) mark += (st.getGrades() != null ? st.getGrades().getOrDefault(c.getId(), 0) : 0);
                }
                row[colIdx++] = mark != 0 ? mark : "";
                
                if ("نظري".equals(s.getType())) {
                    theorySum += (mark > 0 ? mark : 0);
                } else if ("تطبيقي".equals(s.getType())) {
                    appliedMark = (mark > 0 ? mark : 0);
                } else {
                    practicalMark = (mark > 0 ? mark : 0);
                }

                // Check for failure in parental context (summed)
                int passMarkSum = s.getPassMark();
                if (children != null) {
                    for (Subject c : children) passMarkSum += c.getPassMark();
                }
                if (mark < passMarkSum) {
                    if (failedSubjects.length() > 0) failedSubjects.append(" - ");
                    failedSubjects.append(s.getName());
                }
            }
            row[colIdx++] = theorySum > 0 ? theorySum : "";
            row[colIdx++] = practicalMark > 0 ? practicalMark : "";
            row[colIdx++] = appliedMark > 0 ? appliedMark : "";
            row[colIdx++] = (practicalMark + appliedMark) > 0 ? (practicalMark + appliedMark) : "";
            row[colIdx++] = (theorySum + practicalMark + appliedMark) > 0 ? (theorySum + practicalMark + appliedMark) : "";
            row[colIdx++] = st.getStatus();
            row[colIdx]   = failedSubjects.toString();
            model.addRow(row);
        }

        JTable table = new JTable(model);
        styleTable(table);
        // Highlight the first 2 rows (max/min)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (row == 0) {
                    c.setBackground(new Color(0xDCFCE7));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (row == 1) {
                    c.setBackground(new Color(0xFEF9C3));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Color.WHITE);
        return scroll;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(255, 102, 0)));
        p.add(sigBlock("رئيس لجنة النظام والمراقبة"));
        p.add(sigBlock("راجع المراجعة"));
        p.add(sigBlock("راجع الدوائر الحمراء والرصد"));
        p.add(sigBlock("رصد ووضع الدوائر الحمراء"));
        p.add(sigBlock("راجع الاملاء"));
        p.add(sigBlock("راجع الكتابة"));
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel label(String text, int size, boolean bold) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(new Color(61, 59, 110));
        l.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return l;
    }

    private JPanel sigBlock(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(new Color(61, 59, 110));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel line = new JLabel("_______________________", SwingConstants.CENTER);
        line.setForeground(new Color(61, 59, 110));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t);
        p.add(line);
        return p;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(204, 255, 255));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(110);
        }
        table.getColumn("م").setPreferredWidth(50);
        table.getColumn("الاسم").setPreferredWidth(180);
        table.getColumn("مواد الرسوب").setPreferredWidth(200);
    }
}
