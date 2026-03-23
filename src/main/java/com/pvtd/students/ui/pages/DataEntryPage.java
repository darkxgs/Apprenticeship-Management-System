package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.GradeCalculationService;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class DataEntryPage extends JPanel {

    private final AppFrame parentFrame;
    private JComboBox<String> centerCombo;
    private JTextField secretNoField;
    private JLabel studentNameLabel;

    // Grade inputs mapped by Subject ID
    private final Map<Integer, JTextField> gradeFieldsMap = new HashMap<>();
    private List<Subject> currentSubjects = new ArrayList<>();
    private Student currentStudent = null;

    // Totals & Status Display (Read-Only)
    private JTextField theoryTotalField;
    private JTextField practicalTotalField;
    private JTextField appliedTotalField;
    private JTextField grandTotalField;
    
    private JTextArea failedSubjectsArea;
    private JLabel statusLabel;
    private JTextField ratingField;
    private JTextField pracPlusApplField;

    // Data lists for navigation
    private List<Student> centerStudents = new ArrayList<>();
    private int currentIndex = -1;
    
    // Unsaved changes tracker
    private boolean isDirty = false;

    public DataEntryPage(AppFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(0xF1F5F9)); // Standard modern background

        // Title Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0A192F));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        JLabel title = new JLabel("النظام التخصصي - إدخال الدرجات", SwingConstants.RIGHT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.EAST);

        // Legend strip for special codes
        JLabel legend = new JLabel(
            "غائب = -1  │  محروم = -2  │  مفصول = -3  │  معتذر = -4  │  مؤجل = -5",
            SwingConstants.CENTER);
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        legend.setForeground(new Color(0xFDE68A));   // warm yellow
        legend.setBorder(new EmptyBorder(0, 0, 4, 0));
        header.add(legend, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Main Layout wrapper
        JPanel scrollWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        scrollWrapper.setOpaque(false);
        scrollWrapper.add(buildMainCard());
        
        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Navigation Footer
        add(buildFooterActions(), BorderLayout.SOUTH);

        // Load distinct centers initially
        loadCenters();
    }

    private JPanel buildMainCard() {
        JPanel card = new JPanel(new BorderLayout(20, 20));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xE2E8F0), 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));
        card.setPreferredSize(new Dimension(1000, 600));

        // -- TOP PANEL (Center Select & Secret No) --
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        topPanel.setOpaque(false);
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel centerLbl = new JLabel("رقم المركز:");
        centerLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        centerLbl.setOpaque(true);
        centerLbl.setBackground(Color.BLACK);
        centerLbl.setForeground(Color.WHITE);
        centerLbl.setBorder(new EmptyBorder(5, 10, 5, 10));

        centerCombo = new JComboBox<>();
        centerCombo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        centerCombo.setPreferredSize(new Dimension(150, 36));
        centerCombo.addActionListener(e -> onCenterSelected());

        JLabel secretLbl = new JLabel("الرقم السري:");
        secretLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        secretLbl.setOpaque(true);
        secretLbl.setBackground(Color.BLACK);
        secretLbl.setForeground(Color.WHITE);
        secretLbl.setBorder(new EmptyBorder(5, 10, 5, 10));

        secretNoField = new JTextField();
        secretNoField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        secretNoField.setHorizontalAlignment(JTextField.CENTER);
        secretNoField.setPreferredSize(new Dimension(120, 36));
        secretNoField.setEditable(false);
        secretNoField.setBackground(new Color(0xF8FAFC));

        JButton btnSearchSecret = new JButton("بحث بالرقم السري");
        btnSearchSecret.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearchSecret.setBackground(Color.BLACK);
        btnSearchSecret.setForeground(Color.WHITE);
        btnSearchSecret.addActionListener(e -> searchBySecret());

        topPanel.add(secretLbl);
        topPanel.add(secretNoField);
        topPanel.add(centerLbl);
        topPanel.add(centerCombo);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(btnSearchSecret);

        // -- CENTER PANELS (Split into Right: Subjects, Middle: Totals, Left: Status) --
        JPanel centerWrapper = new JPanel(new GridLayout(1, 3, 20, 0));
        centerWrapper.setOpaque(false);
        centerWrapper.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        centerWrapper.add(buildSubjectsPanel()); // Right
        centerWrapper.add(buildTotalsPanel());   // Middle
        centerWrapper.add(buildStatusPanel());   // Left

        // Student name label (hidden - not shown in this view)
        studentNameLabel = new JLabel(" ", SwingConstants.RIGHT);
        studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        studentNameLabel.setForeground(UITheme.PRIMARY);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(topPanel, BorderLayout.NORTH);
        // studentNameLabel intentionally NOT added — name is not shown in data entry
        contentPanel.add(centerWrapper, BorderLayout.CENTER);

        card.add(contentPanel, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildSubjectsPanel() {
        // Container that will hold the dynamic subject input fields
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // This inner panel gets replaced when profession changes
        subjectsContainer = new JPanel();
        subjectsContainer.setLayout(new BoxLayout(subjectsContainer, BoxLayout.Y_AXIS));
        subjectsContainer.setOpaque(false);
        
        panel.add(subjectsContainer, BorderLayout.NORTH);
        return panel;
    }

    private JPanel subjectsContainer;
    private List<JTextField> orderedGradeFields = new ArrayList<>();

    private JPanel buildTotalsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        theoryTotalField = createReadOnlyCalcField();
        practicalTotalField = createReadOnlyCalcField();
        appliedTotalField = createReadOnlyCalcField();
        grandTotalField = createReadOnlyCalcField();
        ratingField = createReadOnlyCalcField();
        pracPlusApplField = createReadOnlyCalcField();

        int row = 0;
        addLabeledCalcField(panel, gbc, row++, "مجموع النظري",        theoryTotalField,    new Color(132, 204, 22));
        addLabeledCalcField(panel, gbc, row++, "درجات العملي",        practicalTotalField, new Color(132, 204, 22));
        addLabeledCalcField(panel, gbc, row++, "درجات التطبيقي",      appliedTotalField,   new Color(132, 204, 22));
        addLabeledCalcField(panel, gbc, row++, "مجموع عملي + تطبيقي", pracPlusApplField,   new Color(163, 230, 53));

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel grandLbl = new JLabel("المجموع الكلى", SwingConstants.CENTER);
        grandLbl.setOpaque(true);
        grandLbl.setBackground(Color.BLACK);
        grandLbl.setForeground(Color.WHITE);
        grandLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(grandLbl, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(grandTotalField, gbc);

        gbc.gridy = row++;
        addLabeledCalcField(panel, gbc, row++, "التقدير", ratingField, new Color(163, 230, 53)); // Lighter green

        return panel;
    }

    private void addLabeledCalcField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field, Color bgColor) {
        gbc.gridy = row;
        
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(bgColor);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setBorder(new LineBorder(Color.BLACK, 1));
        lbl.setPreferredSize(new Dimension(150, 36));

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        panel.add(lbl, gbc);

        gbc.gridx = 0;
        gbc.weightx = 0.2;
        field.setPreferredSize(new Dimension(60, 36));
        field.setBorder(new LineBorder(Color.BLACK, 2));
        panel.add(field, gbc);
    }

    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Failed Subjects Area
        JPanel failedPanel = new JPanel(new BorderLayout());
        failedPanel.setOpaque(false);
        JLabel failedLbl = new JLabel("مواد الرسوب والإعادة", SwingConstants.CENTER);
        failedLbl.setOpaque(true);
        failedLbl.setBackground(Color.BLACK);
        failedLbl.setForeground(Color.WHITE);
        failedLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        failedSubjectsArea = new JTextArea(5, 15);
        failedSubjectsArea.setEditable(false);
        failedSubjectsArea.setFont(new Font("Segoe UI", Font.BOLD, 14));
        failedSubjectsArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        failedSubjectsArea.setBorder(new LineBorder(Color.GRAY, 1));
        
        failedPanel.add(failedLbl, BorderLayout.NORTH);
        failedPanel.add(failedSubjectsArea, BorderLayout.CENTER);

        // Status Label Box
        JPanel statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        JLabel statusTitle = new JLabel("حالة التلميذ", SwingConstants.CENTER);
        statusTitle.setOpaque(true);
        statusTitle.setBackground(Color.BLACK);
        statusTitle.setForeground(Color.WHITE);
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        statusLabel.setBorder(new LineBorder(Color.GRAY, 1));
        statusLabel.setPreferredSize(new Dimension(0, 60));

        statusBox.add(statusTitle, BorderLayout.NORTH);
        statusBox.add(statusLabel, BorderLayout.CENTER);

        panel.add(failedPanel, BorderLayout.NORTH);
        panel.add(statusBox, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildFooterActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(new Color(0xE2E8F0));
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JButton btnSave = createActionButton("حفظ", new Color(0xDC2626), Color.WHITE);
        btnSave.addActionListener(e -> saveCurrentGrades());

        JButton btnNext = createActionButton("التالى", Color.WHITE, UITheme.PRIMARY);
        btnNext.addActionListener(e -> navigate(1));

        JButton btnPrev = createActionButton("السابق", Color.WHITE, new Color(0x16A34A));
        btnPrev.addActionListener(e -> navigate(-1));

        JButton btnFirst = createActionButton("الأول", Color.WHITE, new Color(0xD97706));
        btnFirst.addActionListener(e -> {
            if (centerStudents.size() > 0) { navigateTo(0); }
        });

        JButton btnLast = createActionButton("الأخير", Color.WHITE, new Color(0xDC2626));
        btnLast.addActionListener(e -> {
            if (centerStudents.size() > 0) { navigateTo(centerStudents.size() - 1); }
        });

        panel.add(btnFirst);
        panel.add(btnPrev);
        panel.add(btnNext);
        panel.add(btnLast);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnSave);

        return panel;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 36));
        return btn;
    }

    private JTextField createReadOnlyCalcField() {
        JTextField tf = new JTextField();
        tf.setEditable(false);
        tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setBackground(Color.WHITE);
        return tf;
    }

    // ─── Logic ───────────────────────────────────────────

    private void loadCenters() {
        centerCombo.removeAllItems();
        centerCombo.addItem("اختر المركز...");
        for (String c : StudentService.getDistinctCenters()) {
            centerCombo.addItem(c);
        }
    }

    private void onCenterSelected() {
        if (checkDirty()) return; // Abort if unsaved changes and user says cancel
        
        if (centerCombo.getSelectedIndex() <= 0) {
            clearUI();
            return;
        }
        
        String center = (String) centerCombo.getSelectedItem();
        centerStudents = StudentService.searchStudents("", "", "الكل", "الكل", "الكل", center);
        
        if (centerStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لا يوجد طلاب مسجلين في هذا المركز.", "معلومة", JOptionPane.INFORMATION_MESSAGE);
            clearUI();
            return;
        }

        navigateTo(0); // Load first student
    }

    private void searchBySecret() {
        String num = JOptionPane.showInputDialog(this, "أدخل الرقم السري للبحث:", "بحث بالرقم السري", JOptionPane.QUESTION_MESSAGE);
        if (num == null || num.trim().isEmpty()) return;
        
        // Find across all or just current list? Usually search means globally find it.
        // But for speed we just scan the loaded list. If it must be global, we use searchStudents
        for (int i = 0; i < centerStudents.size(); i++) {
            if (num.equals(centerStudents.get(i).getSecretNo())) {
                if (!checkDirty()) { // proceed
                    centerCombo.setSelectedItem(centerStudents.get(i).getCenterName());
                    navigateTo(i);
                }
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "الرقم السري غير موجود في المركز الحالي.", "خطأ", JOptionPane.ERROR_MESSAGE);
    }

    private void navigate(int offset) {
        if (centerStudents.isEmpty()) return;
        int nextIdx = currentIndex + offset;
        if (nextIdx >= 0 && nextIdx < centerStudents.size()) {
            if (!checkDirty()) {
                navigateTo(nextIdx);
            }
        }
    }

    private void navigateTo(int index) {
        currentIndex = index;
        currentStudent = centerStudents.get(index);
        
        // Load UI
        secretNoField.setText(currentStudent.getSecretNo() != null ? currentStudent.getSecretNo() : "غير محدد");
        studentNameLabel.setText("الاسم: " + currentStudent.getName() + " | المهنة: " + currentStudent.getProfession());
        
        isDirty = false;
        renderSubjectsForProfession();
        populateGradesIntoUI();
        recalculate(); // Trigger initial calc
    }

    private void renderSubjectsForProfession() {
        subjectsContainer.removeAll();
        gradeFieldsMap.clear();
        orderedGradeFields.clear();

        if (currentStudent == null || currentStudent.getProfession() == null) return;

        currentSubjects = SubjectService.getSubjectsByProfession(currentStudent.getProfession());

        // Group by type
        List<Subject> theorySubjects   = new ArrayList<>();
        List<Subject> practicalSubjects = new ArrayList<>();
        List<Subject> appliedSubjects   = new ArrayList<>();

        for (Subject sub : currentSubjects) {
            String type = sub.getType() != null ? sub.getType() : "نظري";
            if (type.equals("تطبيقي")) {
                appliedSubjects.add(sub);
            } else if (type.equals("عملي")) {
                practicalSubjects.add(sub);
            } else {
                theorySubjects.add(sub);
            }
        }

        // --- Render نظري section ---
        if (!theorySubjects.isEmpty()) {
            subjectsContainer.add(buildSectionHeader("نظري", new Color(0x1D4ED8)));
            for (Subject sub : theorySubjects) {
                subjectsContainer.add(buildSubjectRow(sub, new Color(132, 204, 22)));
            }
        }

        // --- Render عملي section ---
        if (!practicalSubjects.isEmpty()) {
            subjectsContainer.add(buildSectionHeader("عملي", new Color(0x7C3AED)));
            for (Subject sub : practicalSubjects) {
                subjectsContainer.add(buildSubjectRow(sub, new Color(167, 139, 250)));
            }
        }

        // --- Render تطبيقي section ---
        if (!appliedSubjects.isEmpty()) {
            subjectsContainer.add(buildSectionHeader("تطبيقي", new Color(0xEA580C)));
            for (Subject sub : appliedSubjects) {
                subjectsContainer.add(buildSubjectRow(sub, new Color(253, 186, 116)));
            }
        }

        subjectsContainer.revalidate();
        subjectsContainer.repaint();
    }

    /** Builds a colored section header label */
    private JLabel buildSectionHeader(String title, Color bgColor) {
        JLabel header = new JLabel("— " + title + " —", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(bgColor);
        header.setForeground(Color.WHITE);
        header.setBorder(new EmptyBorder(4, 8, 4, 8));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        return header;
    }

    /** Builds a single subject row with label + grade input */
    private JPanel buildSubjectRow(Subject sub, Color labelBg) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subject name + max mark hint
        JLabel lbl = new JLabel(sub.getName() + " /" + sub.getMaxMark(), SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setOpaque(true);
        lbl.setBackground(labelBg);
        lbl.setForeground(Color.BLACK);
        lbl.setBorder(new LineBorder(Color.BLACK, 1));
        lbl.setPreferredSize(new Dimension(150, 36));
        lbl.setToolTipText("درجة النجاح: " + sub.getPassMark() + " | الدرجة العظمى: " + sub.getMaxMark());

        JTextField gradeInput = new JTextField();
        gradeInput.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gradeInput.setHorizontalAlignment(JTextField.CENTER);
        gradeInput.setPreferredSize(new Dimension(60, 36));
        gradeInput.setBorder(new LineBorder(Color.BLACK, 2));

        setupGradeInput(gradeInput, sub.getMaxMark());
        gradeFieldsMap.put(sub.getId(), gradeInput);
        orderedGradeFields.add(gradeInput);

        row.add(lbl, BorderLayout.CENTER);
        row.add(gradeInput, BorderLayout.EAST);
        return row;
    }

    private void setupGradeInput(JTextField tf, int maxMark) {
        final LineBorder normalBorder = new LineBorder(Color.BLACK, 2);
        final LineBorder errorBorder  = new LineBorder(new Color(0xDC2626), 3);

        // Allow positive numbers AND a leading minus (for special codes: -1 غائب, -2 محروم, ...)
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                // Build what the resulting string would look like
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result  = current.substring(0, offset) + text + current.substring(offset + length);
                // Accept if it matches an optional leading minus followed by digits
                if (result.matches("-?[0-9]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { triggerCalc(); }
            public void removeUpdate(DocumentEvent e)  { triggerCalc(); }
            public void changedUpdate(DocumentEvent e) { triggerCalc(); }
            private void triggerCalc() {
                isDirty = true;
                recalculate();
                // Validate max mark
                SwingUtilities.invokeLater(() -> {
                    try {
                        String txt = tf.getText().trim();
                        if (!txt.isEmpty() && !txt.equals("-")) {
                            int val = Integer.parseInt(txt);
                            // Negative values are special codes — skip max-mark check
                            if (val > maxMark) {
                                tf.setBorder(errorBorder);
                                tf.setBackground(new Color(0xFEF2F2));
                                tf.setToolTipText("⚠️ الدرجة تتجاوز الحد الأقصى (" + maxMark + ")");
                                return;
                            }
                        }
                        tf.setBorder(normalBorder);
                        tf.setBackground(Color.WHITE);
                        tf.setToolTipText(null);
                    } catch (NumberFormatException ignored) {}
                });
            }
        });

        // Enter = Move to next field, or Save & go to next student if it's the last field
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Find current field index
                    int idx = orderedGradeFields.indexOf(tf);
                    if (idx != -1 && idx < orderedGradeFields.size() - 1) {
                        // Move focus to next field
                        orderedGradeFields.get(idx + 1).requestFocusInWindow();
                    } else {
                        // Last field: Block navigation if any field exceeds max mark
                        boolean hasError = gradeFieldsMap.values().stream()
                            .anyMatch(f -> f.getBackground().equals(new Color(0xFEF2F2)));
                        if (hasError) {
                            JOptionPane.showMessageDialog(DataEntryPage.this,
                                "توجد درجة تتجاوز الحد الأقصى. يرجى التصحيح أولا\u064b.",
                                "خطأ في الدرجات", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        DataEntryPage.this.saveAndGoNext();
                    }
                }
            }
        });

        // Select all text when focused for fast replacing
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.selectAll(); }
        });
    }

    private void populateGradesIntoUI() {
        if (currentStudent == null || currentStudent.getGrades() == null) return;
        Map<Integer, Integer> grades = currentStudent.getGrades();
        
        for (Subject sub : currentSubjects) {
            JTextField tf = gradeFieldsMap.get(sub.getId());
            if (tf != null && grades.containsKey(sub.getId())) {
                tf.setText(String.valueOf(grades.get(sub.getId())));
            }
        }
        isDirty = false;
    }

    private Map<Integer, Integer> extractGradesFromUI() {
        Map<Integer, Integer> grades = new HashMap<>();
        for (Map.Entry<Integer, JTextField> entry : gradeFieldsMap.entrySet()) {
            try {
                String text = entry.getValue().getText().trim();
                if (!text.isEmpty()) {
                    grades.put(entry.getKey(), Integer.parseInt(text));
                }
            } catch (NumberFormatException ignored) {}
        }
        return grades;
    }

    private void recalculate() {
        if (currentStudent == null) return;
        
        Map<Integer, Integer> grades = extractGradesFromUI();
        
        int thTotal = GradeCalculationService.calculateTheoryTotal(currentSubjects, grades);
        int prTotal = GradeCalculationService.calculatePracticalTotal(currentSubjects, grades);
        int apTotal = GradeCalculationService.calculateAppliedTotal(currentSubjects, grades);
        int grandTotal = GradeCalculationService.calculateGrandTotal(thTotal, prTotal, apTotal);
        int maxTotal = GradeCalculationService.calculateMaxPossibleTotal(currentSubjects);
        
        theoryTotalField.setText(String.valueOf(thTotal));
        practicalTotalField.setText(String.valueOf(prTotal));
        appliedTotalField.setText(String.valueOf(apTotal));
        pracPlusApplField.setText(String.valueOf(prTotal + apTotal));
        grandTotalField.setText(String.valueOf(grandTotal));
        
        ratingField.setText(GradeCalculationService.calculateRating(grandTotal, maxTotal));
        
        String failedText = GradeCalculationService.getFailedSubjectsText(currentSubjects, grades);
        failedSubjectsArea.setText(failedText);
        
        String status = StudentService.calculateStatus(currentStudent.getProfession(), grades);
        statusLabel.setText(status);
        
        // Coloring status
        if ("ناجح".equals(status)) {
            statusLabel.setForeground(UITheme.SUCCESS);
        } else if ("دور ثاني".equals(status)) {
            statusLabel.setForeground(UITheme.WARNING);
        } else if ("راسب".equals(status)) {
            statusLabel.setForeground(UITheme.DANGER);
        } else {
            statusLabel.setForeground(UITheme.TEXT_PRIMARY);
        }
    }

    private void saveCurrentGrades() {
        if (currentStudent == null) return;
        
        Map<Integer, Integer> grades = extractGradesFromUI();
        boolean ok = StudentService.updateStudentGrades(currentStudent.getId(), grades);
        
        if (ok) {
            // Use the clean calculated status — NOT the label text (which may have toast suffix)
            String status = StudentService.calculateStatus(currentStudent.getProfession(), grades);
            StudentService.updateStudentStatusDirectly(currentStudent.getId(), status);
            currentStudent.setGrades(grades);
            currentStudent.setStatus(status);
            isDirty = false;
            
            // Toast flash effect on label only — does NOT affect the saved status
            statusLabel.setText(status + " (تم الحفظ ✓)");
            Timer t = new Timer(1500, e -> statusLabel.setText(status));
            t.setRepeats(false);
            t.start();
        } else {
            JOptionPane.showMessageDialog(this, "حدث خطأ أثناء حفظ الدرجات.", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves current grades then navigates to next student directly,
     * bypassing checkDirty to avoid false-positive dialogs after saving.
     */
    private void saveAndGoNext() {
        saveCurrentGrades();
        // Navigate directly without checkDirty – we just saved
        if (centerStudents.isEmpty()) return;
        int nextIdx = currentIndex + 1;
        if (nextIdx < centerStudents.size()) {
            isDirty = false; // clear before navigateTo triggers recalculate
            navigateTo(nextIdx);
        }
    }

    /**
     * @return true if action should ABORT due to unsaved changes.
     */
    private boolean checkDirty() {
        if (isDirty) {
            int res = JOptionPane.showConfirmDialog(this, 
                "لديك تعديلات غير محفوظة، هل تريد الاستمرار بدون الحفظ؟", 
                "تنبيه", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            return res != JOptionPane.YES_OPTION;
        }
        return false;
    }

    private void clearUI() {
        currentStudent = null;
        currentIndex = -1;
        centerStudents.clear();
        secretNoField.setText("");
        studentNameLabel.setText(" ");
        subjectsContainer.removeAll();
        subjectsContainer.revalidate();
        subjectsContainer.repaint();
        gradeFieldsMap.clear();
        
        theoryTotalField.setText("");
        practicalTotalField.setText("");
        appliedTotalField.setText("");
        grandTotalField.setText("");
        ratingField.setText("");
        failedSubjectsArea.setText("");
        statusLabel.setText("");
        isDirty = false;
    }
}
