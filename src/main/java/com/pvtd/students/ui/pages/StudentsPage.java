package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.services.ReportService;
import com.pvtd.students.services.StatusesService;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.pages.Report.CertificateOfSuccess1;
import com.pvtd.students.ui.pages.Report.SuccessfulCandidatesRevealedByRrades;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.DropShadowBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StudentsPage extends JPanel {

    private AppFrame parentFrame;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private List<Student> currentStudentsList;

    // Filters
    private JTextField searchKeyField, searchSeatField;
    private JComboBox<String> govCombo, profCombo, statusCombo;

    // Live Stats
    private JLabel countLabel, passedLabel, failedLabel;

    // Column definitions: [header, width]
    private static final Object[][] COLS = {
            { "✓", 44 },
            { "م", 52 },
            { "الاسم", 220 },
            { "رقم التسجيل", 130 },
            { "الرقم القومي", 150 },
            { "المنطقة", 120 },
            { "المركز", 160 },
            { "المهنة", 140 },
            { "النظام", 110 },
            { "رقم الجلوس", 120 },
            { "الرقم السري", 110 },
            { "المجموعة المهنية", 160 },
            { "رقم التنسيق", 120 },
            { "تاريخ الميلاد", 130 },
            { "النوع", 90 },
            { "حي / قرية", 140 },
            { "المحافظة", 120 },
            { "الديانة", 100 },
            { "الجنسية", 100 },
            { "العنوان", 200 },
            { "الحالة", 110 },
            { "أخرى", 140 },
    };

    public StudentsPage(AppFrame frame) {
        this.parentFrame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_LIGHT);

        // ─── Build sections ───────────────────────────────────────────────────
        add(buildTopSection(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        loadStudentData();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOP SECTION (title + filters + stats)
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildTopSection() {
        JPanel top = new JPanel(new BorderLayout(0, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(24, 24, 14, 24));

        // Title Row ─────────────────────────────────────────────────────────
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("إدارة الطلاب", SwingConstants.RIGHT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Stat chips
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        statsRow.setOpaque(false);

        countLabel = makeStatChip("--", "إجمالي", new Color(0xDBEAFE), new Color(0x1D4ED8));
        passedLabel = makeStatChip("--", "ناجح", new Color(0xDCFCE7), new Color(0x15803D));
        failedLabel = makeStatChip("--", "راسب", new Color(0xFEE2E2), new Color(0xDC2626));

        statsRow.add(failedLabel);
        statsRow.add(passedLabel);
        statsRow.add(countLabel);

        titleRow.add(title, BorderLayout.EAST);
        titleRow.add(statsRow, BorderLayout.WEST);
        top.add(titleRow, BorderLayout.NORTH);

        // Filter Bar ────────────────────────────────────────────────────────
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        filterCard.setOpaque(true);
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 4, 0.05f, 14, UITheme.BG_LIGHT),
                new EmptyBorder(2, 12, 2, 12)));
        filterCard.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        searchKeyField = new JTextField(14);
        searchKeyField.setFont(UITheme.FONT_BODY);
        searchKeyField.putClientProperty("JTextField.placeholderText", "الاسم / الرقم القومي");
        searchKeyField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchKeyField.setPreferredSize(new Dimension(170, 36));

        searchSeatField = new JTextField(8);
        searchSeatField.setFont(UITheme.FONT_BODY);
        searchSeatField.putClientProperty("JTextField.placeholderText", "رقم الجلوس");
        searchSeatField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchSeatField.setPreferredSize(new Dimension(120, 36));

        govCombo = makeCombo(); // will add items manually
        govCombo.addItem("الكل");
        for (String g : StudentService.getDistinctGovernorates()) {
            govCombo.addItem(g);
        }

        profCombo = makeCombo();
        profCombo.addItem("الكل");
        for (String p : StudentService.getDistinctProfessions()) {
            profCombo.addItem(p);
        }
        statusCombo = makeCombo(); // will add items manually
        statusCombo.addItem("الكل");
        for (String s : StatusesService.getAllStatuses()) {
            statusCombo.addItem(s);
        }

        JButton btnSearch = new JButton("بحث");
        btnSearch.setFont(UITheme.FONT_HEADER);
        btnSearch.setBackground(UITheme.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.putClientProperty("JButton.buttonType", "roundRect");
        btnSearch.setPreferredSize(new Dimension(90, 36));
        btnSearch.addActionListener(e -> executeSearch());

        JButton btnReset = new JButton("مسح");
        btnReset.setFont(UITheme.FONT_BODY);
        btnReset.setForeground(UITheme.TEXT_SECONDARY);
        btnReset.putClientProperty("JButton.buttonType", "borderless");
        btnReset.addActionListener(e -> {
            searchKeyField.setText("");
            searchSeatField.setText("");
            govCombo.setSelectedIndex(0);
            profCombo.setSelectedIndex(0);
            statusCombo.setSelectedIndex(0);
            loadStudentData();
        });

        // RTL layout: add in reverse visual order (right→left)
        // Each filter group: label then combo (label appears on the right in RTL)
        filterCard.add(btnReset);
        filterCard.add(btnSearch);
        filterCard.add(labelFor("الحالة:"));
        filterCard.add(statusCombo);
        filterCard.add(labelFor("طبيعة العمل:"));
        filterCard.add(profCombo);
        filterCard.add(labelFor("محافظة:"));
        filterCard.add(govCombo);
        filterCard.add(searchSeatField);
        filterCard.add(labelFor("رقم الجلوس:"));
        filterCard.add(searchKeyField);
        filterCard.add(labelFor("البحث:"));

        top.add(filterCard, BorderLayout.SOUTH);
        return top;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TABLE SECTION
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildTableSection() {
        // Column headers
        String[] headers = new String[COLS.length];
        for (int i = 0; i < COLS.length; i++) {
            headers[i] = (String) COLS[i][0];
        }

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : Object.class;
            }
        };

        studentsTable = new JTable(tableModel);
        studentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        studentsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        studentsTable.setRowHeight(42);
        studentsTable.setFont(UITheme.FONT_BODY);
        studentsTable.setShowGrid(false);
        studentsTable.setIntercellSpacing(new Dimension(0, 0));
        studentsTable.setSelectionBackground(new Color(0xDBEAFE));
        studentsTable.setSelectionForeground(UITheme.TEXT_PRIMARY);
        studentsTable.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = studentsTable.getTableHeader();
        header.setFont(UITheme.FONT_HEADER);
        header.setBackground(new Color(0xF1F5F9));
        header.setForeground(new Color(0x475569));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xE2E8F0)));
        header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        header.setPreferredSize(new Dimension(0, 46));
        header.setReorderingAllowed(false);

        // Column widths
        for (int i = 0; i < COLS.length; i++) {
            int w = (int) COLS[i][1];
            studentsTable.getColumnModel().getColumn(i).setPreferredWidth(w);
            studentsTable.getColumnModel().getColumn(i).setMinWidth(w);
        }

        // Row renderer — striped rows, right-aligned text, center-aligned checkbox col
        studentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color CHECKED_BG = new Color(0xDBEAFE); // blue-100
            private final Color CHECKED_FG = new Color(0x1E40AF); // blue-800
            private final Color STRIPE_ODD = new Color(0xF8FAFC);

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);

                // Check if this row's checkbox (col 0) is ticked
                Boolean checked = (Boolean) t.getModel().getValueAt(row, 0);
                boolean isChecked = Boolean.TRUE.equals(checked);

                if (isChecked) {
                    c.setBackground(CHECKED_BG);
                    c.setForeground(CHECKED_FG);
                } else if (sel) {
                    c.setBackground(t.getSelectionBackground());
                    c.setForeground(t.getSelectionForeground());
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : STRIPE_ODD);
                    c.setForeground(UITheme.TEXT_PRIMARY);
                }

                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setFont(UITheme.FONT_BODY);
                if (value == null || value.toString().isEmpty()) {
                    setText("—");
                    setForeground(new Color(0xCBD5E1));
                }
                return c;
            }
        });

        // Status column — colored badge via existing renderer
        studentsTable.getColumnModel().getColumn(20)
                .setCellRenderer(new com.pvtd.students.ui.utils.StatusBadgeRenderer());

        // ── Wrap in card ──────────────────────────────────────────────────────
        JScrollPane scrollPane = new JScrollPane(studentsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);

        // ── Scroll to right edge on load (RTL start) ──
        SwingUtilities.invokeLater(() -> {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            hBar.setValue(hBar.getMaximum());
        });

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 6, 0.07f, 20, UITheme.CARD_BG),
                new EmptyBorder(0, 0, 0, 0)));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 24, 0, 24));
        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ACTION BAR
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(12, 24, 18, 24));

        JButton btnAdd = actionBtn("إضافة طالب", UITheme.SUCCESS, Color.WHITE, true);
        JButton btnEdit = actionBtn("تعديل", new Color(0xEFF6FF), new Color(0x1D4ED8), false);
        JButton btnDelete = actionBtn("حذف", new Color(0xFEF2F2), UITheme.DANGER, false);
        JButton btnSelectAll = actionBtn("تحديد الكل", new Color(0xF1F5F9), UITheme.TEXT_PRIMARY, false);
        JButton btnPdf = actionBtn("شهادة نجاح", new Color(0xF0FDF4), new Color(0x15803D), false);
        JButton btnForm = actionBtn("استمارة طالب", new Color(0xFEF3C7), new Color(0xB45309), false);
        JButton btnIdCard = actionBtn("عرض الهوية", new Color(0xFAFAF9), UITheme.TEXT_SECONDARY, false);

        // ── Actions ───────────────────────────────────────────────────────────
        btnSelectAll.addActionListener(e -> {
            boolean allSel = true;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Boolean v = (Boolean) tableModel.getValueAt(i, 0);
                if (v == null || !v) {
                    allSel = false;
                    break;
                }
            }
            boolean next = !allSel;
            for (int i = 0; i < tableModel.getRowCount(); i++)
                tableModel.setValueAt(next, i, 0);
        });

        btnAdd.addActionListener(e -> parentFrame.showPage(new StudentFormPage(parentFrame, null)));

        btnEdit.addActionListener(e -> {
            List<Student> sel = getSelectedStudents();
            if (sel.size() == 1)
                parentFrame.showPage(new StudentFormPage(parentFrame, sel.get(0)));
            else if (sel.size() > 1)
                warn("يرجى تحديد طالب واحد فقط للتعديل.");
            else
                warn("يرجى تحديد طالب من الجدول للتعديل.");
        });

        btnDelete.addActionListener(e -> {
            List<Student> sel = getSelectedStudents();
            if (sel.isEmpty()) {
                warn("يرجى تحديد طالب على الأقل للحذف.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "حذف " + sel.size() + " طلاب؟", "تأكيد الحذف",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                for (Student s : sel)
                    StudentService.deleteStudent(s.getId());
                executeSearch();
            }
        });

        btnPdf.addActionListener(e -> {
           
            
             
    List<Student> selectedStudents = new ArrayList<>();

    int[] selectedRows = studentsTable.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(this, "اختار طالب واحد على الأقل");
        return;
    }

    for (int row : selectedRows) {

        String seatNo = studentsTable.getValueAt(row, 9).toString(); // عمود رقم الجلوس

        Student s = new Student();
        s.setSeatNo(seatNo);

        selectedStudents.add(s);
    }

    CertificateOfSuccess1 cert = new CertificateOfSuccess1();

    cert.printCertificates(selectedStudents);

            
        });

        btnForm.addActionListener(e -> {
         String seatNo = null;
int[] selectedRows = studentsTable.getSelectedRows();

if (selectedRows.length == 0) {
    JOptionPane.showMessageDialog(this, "اختر الطلاب أولاً");
    return;
}

List<Student> students = new ArrayList<>();

for (int row : selectedRows) {

    Object value = studentsTable.getValueAt(row, 9);

    if (value != null) {
        seatNo = value.toString();
    } else {
        seatNo = ""; // يخليها فاضية بدل ما يعمل Exception
    }

    Student s = new Student();
    s.setSeatNo(seatNo);

    students.add(s);
    }

    // إنشاء نسخة من كلاس الاستمارة
    SuccessfulCandidatesRevealedByRrades form =
            new SuccessfulCandidatesRevealedByRrades();
    form.loadStudentData(seatNo);
    // استدعاء الميثود
    form.printCertificates(students);   
            
        });

        btnIdCard.addActionListener(e -> {
            List<Student> sel = getSelectedStudents();
            if (sel.isEmpty() || sel.size() > 1) {
                warn("يرجى تحديد طالب واحد فقط لعرض هويته.");
                return;
            }
            Student s = sel.get(0);
            if (s.getImagePath() != null && !s.getImagePath().isEmpty()) {
                File imgFile = new File(s.getImagePath());
                if (imgFile.exists()) {
                    JDialog dlg = new JDialog(parentFrame, "صورة الهوية: " + s.getName(), true);
                    dlg.setSize(600, 420);
                    dlg.setLocationRelativeTo(this);
                    ImageIcon icon = new ImageIcon(new ImageIcon(imgFile.getAbsolutePath())
                            .getImage().getScaledInstance(580, 380, Image.SCALE_SMOOTH));
                    dlg.add(new JScrollPane(new JLabel(icon)));
                    dlg.setVisible(true);
                } else {
                    warn("ملف الصورة غير موجود: " + s.getImagePath());
                }
            } else {
                JOptionPane.showMessageDialog(this, "هذا الطالب لا يمتلك صورة بطاقة مسجلة.", "معلومة",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        bar.add(btnSelectAll);
        bar.add(btnIdCard);
        bar.add(btnForm);
        bar.add(btnPdf);
        bar.add(btnDelete);
        bar.add(btnEdit);
        bar.add(btnAdd);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA LOADING
    // ═══════════════════════════════════════════════════════════════════════════
    public void loadStudentData() {
        currentStudentsList = StudentService.getAllStudents();
        populateTable();
    }

    private void executeSearch() {
        currentStudentsList = StudentService.searchStudents(
                searchKeyField.getText(),
                searchSeatField.getText(),
                (String) govCombo.getSelectedItem(),
                (String) profCombo.getSelectedItem(),
                (String) statusCombo.getSelectedItem());
        populateTable();
    }

    private void populateTable() {
        if (tableModel == null)
            return;
        tableModel.setRowCount(0);

        long passed = 0, failed = 0;
        for (Student s : currentStudentsList) {
            String stat = s.getStatus() != null ? s.getStatus() : "غير محدد";
            if ("ناجح".equals(stat))
                passed++;
            if ("راسب".equals(stat))
                failed++;

            tableModel.addRow(new Object[] {
                    false,
                    s.getSerial(),
                    s.getName(),
                    s.getRegistrationNo(),
                    s.getNationalId(),
                    s.getRegion(),
                    s.getCenterName(),
                    s.getProfession(),
                    s.getExamSystem(),
                    s.getSeatNo(),
                    s.getSecretNo(),
                    s.getProfessionalGroup(),
                    s.getCoordinationNo(),
                    s.getDobDay() + "/" + s.getDobMonth() + "/" + s.getDobYear(),
                    s.getGender(),
                    s.getNeighborhood(),
                    s.getGovernorate(),
                    s.getReligion(),
                    s.getNationality(),
                    s.getAddress(),
                    stat,
                    s.getOtherNotes()
            });
        }

        int total = currentStudentsList.size();
        countLabel.setText(String.valueOf(total));
        passedLabel.setText(String.valueOf(passed));
        failedLabel.setText(String.valueOf(failed));
    }

    private List<Student> getSelectedStudents() {
        List<Student> list = new ArrayList<>();
        if (currentStudentsList == null)
            return list;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
            if (checked != null && checked)
                list.add(currentStudentsList.get(i));
        }
        if (list.isEmpty() && studentsTable.getSelectedRow() >= 0) {
            list.add(currentStudentsList.get(studentsTable.getSelectedRow()));
        }
        return list;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════════
    /** Coloured stat chip in title row */
    private JLabel makeStatChip(String count, String label, Color bg, Color fg) {
        JLabel lbl = new JLabel("  " + count + "  " + label + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lbl.setOpaque(false);
        lbl.setForeground(fg);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
        return lbl;
    }

    private JButton actionBtn(String text, Color bg, Color fg, boolean filled) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        if (filled)
            btn.putClientProperty("JButton.arc", 10);
        return btn;
    }

    private JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UITheme.FONT_BODY);
        cb.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        cb.setPreferredSize(new Dimension(140, 36));
        return cb;
    }

    private JLabel labelFor(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.RIGHT);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        return lbl;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
}
