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
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataEntryPage extends JPanel {

    private final AppFrame parentFrame;
    private JComboBox<String> regionCombo;
    private JComboBox<String> centerCombo;
    private JTextField secretNoField;

    // Grade inputs mapped by Subject ID
    private final Map<Integer, JTextField> gradeFieldsMap = new HashMap<>();
    private List<Subject> currentSubjects = new ArrayList<>();
    private Student currentStudent = null;

    // Totals & Status Display (Read-Only)
    private JTextField theoryTotalField;
    private JTextField practicalTotalField;
    private JTextField appliedTotalField;
    private JTextField sumPracApplField;
    private JLabel grandTotalLabel;
    
    private JPanel failedSubjectsListPanel;
    private JLabel statusPill;
    private JLabel ratingPill;

    private JPanel subjectsContainer;
    private List<JTextField> orderedGradeFields = new ArrayList<>();

    private List<Student> centerStudents = new ArrayList<>();
    private int currentIndex = -1;
    private java.util.Map<String, String> centerCodeToNameMap = new java.util.LinkedHashMap<>();
    private boolean isDirty = false;

    // Colors matched from screenshot
    private final Color CLR_BG = new Color(0xF1F5F9); // Slightly cleaner tailwind slate-100
    private final Color CLR_DARK_BLUE = new Color(0x182235);
    private final Color CLR_GREEN_BTN = new Color(0x4CAF50);
    private final Color CLR_CYAN_HEADER = new Color(0x22A7F0);
    private final Color CLR_GREEN_LIGHT = new Color(0xC8E6C9);
    private final Color CLR_TEAL_DARK = new Color(0x114B5F);
    private final Color CLR_BLUE_LIGHT = new Color(0xE3F2FD);

    public DataEntryPage(AppFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(CLR_BG);

        // --- Main container with Top Bar & 3 Columns ---
        JPanel mainWrapper = new JPanel(new BorderLayout(15, 15));
        mainWrapper.setOpaque(false);
        mainWrapper.setBorder(new EmptyBorder(25, 25, 25, 25));

        mainWrapper.add(buildTopBar(), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        columnsPanel.setOpaque(false);
        columnsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        columnsPanel.add(buildRightColumn());  // Right (Subjects)
        columnsPanel.add(buildMiddleColumn()); // Middle (Totals & Calcs)
        columnsPanel.add(buildLeftColumn());   // Left (Status & Info)

        JPanel columnsWrapper = new JPanel(new BorderLayout());
        columnsWrapper.setOpaque(false);
        columnsWrapper.add(columnsPanel, BorderLayout.NORTH); // Prevents vertical stretching!

        mainWrapper.add(columnsWrapper, BorderLayout.CENTER);

        // Add to view with Scroll
        JScrollPane scrollPane = new JScrollPane(mainWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        add(buildFooterActions(), BorderLayout.SOUTH);

        loadCenters();
    }

    // ========================================================================
    // UI COMPONENTS BUILDERS
    // ========================================================================

    private JPanel buildTopBar() {
        // The Top Bar is a single white pill with shadow
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        
        RoundedPanel topBar = new RoundedPanel(15, Color.WHITE);
        topBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        topBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // 1. Secret Lock Icon + Label
        JPanel secretPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        secretPanel.setOpaque(false);
        secretPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JLabel lockIcon = new JLabel();
        lockIcon.setIcon(new DrawnIcon(DrawnIcon.Type.LOCK, 20, 20, Color.WHITE));
        lockIcon.setOpaque(true);
        lockIcon.setBackground(CLR_DARK_BLUE);
        lockIcon.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        lockIcon.putClientProperty("FlatLaf.styleClass", "rounded");
        
        JLabel secretLbl = new JLabel("الرقم السري:");
        secretLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        secretLbl.setForeground(Color.DARK_GRAY);
        
        secretNoField = new JTextField();
        secretNoField.setEditable(false); // Can't type in it, it's just for display
        secretNoField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        secretNoField.setHorizontalAlignment(JTextField.CENTER);
        secretNoField.setPreferredSize(new Dimension(140, 36));
        secretNoField.setBackground(new Color(0xF4F7F9));
        secretNoField.setBorder(new LineBorder(new Color(0xE0E0E0), 1, true));
        
        secretPanel.add(lockIcon);
        secretPanel.add(secretLbl);
        secretPanel.add(secretNoField);

        // 1.5 Region Combo
        JPanel regionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        regionPanel.setOpaque(false);
        regionPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JLabel regionLbl = new JLabel("المنطقة:");
        regionLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        regionLbl.setForeground(Color.DARK_GRAY);
        
        regionCombo = new JComboBox<>();
        regionCombo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        regionCombo.setPreferredSize(new Dimension(160, 36));
        regionCombo.setBackground(new Color(0xF4F7F9));
        regionCombo.addActionListener(e -> onRegionSelected());
        
        regionPanel.add(regionLbl);
        regionPanel.add(regionCombo);

        // 2. Center Combo
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        centerPanel.setOpaque(false);
        centerPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JLabel centerLbl = new JLabel("المركز:");
        centerLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        centerLbl.setForeground(Color.DARK_GRAY);
        
        centerCombo = new JComboBox<>();
        centerCombo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        centerCombo.setPreferredSize(new Dimension(160, 36));
        centerCombo.setBackground(new Color(0xF4F7F9));
        centerCombo.addActionListener(e -> onCenterSelected());
        
        centerPanel.add(centerLbl);
        centerPanel.add(centerCombo);

        // 3. Search Button
        RoundedButton btnSearchSecret = new RoundedButton("بحث بالرقم السري", CLR_DARK_BLUE, Color.WHITE);
        btnSearchSecret.setPreferredSize(new Dimension(160, 40));
        
        topBar.add(secretPanel);
        topBar.add(btnSearchSecret);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(centerPanel);
        topBar.add(regionPanel);

        btnSearchSecret.addActionListener(e -> searchBySecret());

        // Wrapping with shadow
        JPanel shadowWrap = new JPanel(new BorderLayout());
        shadowWrap.setOpaque(false);
        shadowWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        shadowWrap.add(topBar, BorderLayout.CENTER);

        return shadowWrap;
    }

    private JPanel buildRightColumn() {
        subjectsContainer = new JPanel();
        subjectsContainer.setLayout(new BoxLayout(subjectsContainer, BoxLayout.Y_AXIS));
        subjectsContainer.setOpaque(false);
        return subjectsContainer;
    }

    private JPanel buildMiddleColumn() {
        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setOpaque(false);

        theoryTotalField = createNumBox();
        practicalTotalField = createNumBox();
        appliedTotalField = createNumBox();
        sumPracApplField = createNumBox();

        middle.add(buildCalcRow("مجموع النظري", theoryTotalField, DrawnIcon.Type.BOOK));
        middle.add(Box.createVerticalStrut(10));
        middle.add(buildCalcRow("درجات العملي", practicalTotalField, DrawnIcon.Type.FLASK));
        middle.add(Box.createVerticalStrut(10));
        middle.add(buildCalcRow("درجات التطبيقي", appliedTotalField, DrawnIcon.Type.MONITOR));
        middle.add(Box.createVerticalStrut(10));
        middle.add(buildCalcRow("مجموع عملي + تطبيقي", sumPracApplField, DrawnIcon.Type.SIGMA));
        middle.add(Box.createVerticalStrut(20));

        // Total Box (Dark Blue Top, Big White Num Bottom)
        RoundedPanel totalBox = new RoundedPanel(12, Color.WHITE);
        totalBox.setLayout(new BorderLayout());
        
        RoundedPanel totalTop = new RoundedPanel(12, CLR_DARK_BLUE);
        totalTop.setLayout(new BorderLayout());
        totalTop.setBorder(new EmptyBorder(8, 15, 8, 15));
        JLabel totalLbl = new JLabel("المجموع الكلي", SwingConstants.CENTER);
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        totalLbl.setForeground(Color.WHITE);
        totalTop.add(totalLbl, BorderLayout.CENTER);
        
        grandTotalLabel = new JLabel("0", SwingConstants.CENTER);
        grandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        grandTotalLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        totalBox.add(totalTop, BorderLayout.NORTH);
        totalBox.add(grandTotalLabel, BorderLayout.CENTER);
        totalBox.setMaximumSize(new Dimension(500, 100));
        
        middle.add(totalBox);
        middle.add(Box.createVerticalStrut(20));

        // Rating Box (Green right, white left)
        JPanel ratingPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        ratingPanel.setOpaque(false);
        ratingPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        ratingPanel.setMaximumSize(new Dimension(500, 45));

        RoundedPanel ratingRight = new RoundedPanel(10, CLR_GREEN_BTN);
        ratingRight.setLayout(new BorderLayout());
        JLabel rateLbl = new JLabel("التقدير", SwingConstants.CENTER);
        rateLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        rateLbl.setForeground(Color.WHITE);
        ratingRight.add(rateLbl, BorderLayout.CENTER);

        RoundedPanel ratingLeft = new RoundedPanel(10, Color.WHITE);
        ratingLeft.setLayout(new BorderLayout());
        ratingPill = new JLabel(" ", SwingConstants.CENTER);
        ratingPill.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ratingPill.setForeground(Color.DARK_GRAY);
        ratingLeft.add(ratingPill, BorderLayout.CENTER);
        
        ratingPanel.add(ratingRight);
        ratingPanel.add(ratingLeft);
        middle.add(ratingPanel);

        return middle;
    }

    private JPanel buildCalcRow(String title, JTextField field, DrawnIcon.Type iconType) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        row.setMaximumSize(new Dimension(500, 50));
        
        // Right Green Icon Box
        RoundedPanel iconBox = new RoundedPanel(10, CLR_GREEN_BTN);
        iconBox.setPreferredSize(new Dimension(50, 50));
        iconBox.setLayout(new BorderLayout());
        JLabel lockIcon = new JLabel(new DrawnIcon(iconType, 24, 24, Color.WHITE));
        lockIcon.setHorizontalAlignment(SwingConstants.CENTER);
        iconBox.add(lockIcon, BorderLayout.CENTER);
        
        // Center Title Box
        RoundedPanel titleBox = new RoundedPanel(8, Color.WHITE);
        titleBox.setLayout(new BorderLayout());
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Color.DARK_GRAY);
        titleBox.add(lbl, BorderLayout.CENTER);

        // Left Field Box
        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setOpaque(false);
        fieldWrapper.setPreferredSize(new Dimension(70, 50));
        field.setPreferredSize(new Dimension(70, 50));
        fieldWrapper.add(field, BorderLayout.CENTER);

        row.add(iconBox, BorderLayout.EAST);
        row.add(titleBox, BorderLayout.CENTER);
        row.add(fieldWrapper, BorderLayout.WEST);

        return row;
    }

    private JPanel buildLeftColumn() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // 1. Failing Subjects
        RoundedPanel failCard = new RoundedPanel(12, Color.WHITE);
        failCard.setLayout(new BorderLayout());
        failCard.setMaximumSize(new Dimension(500, 200));

        RoundedPanel failTop = new RoundedPanel(12, CLR_TEAL_DARK);
        failTop.setLayout(new BorderLayout());
        failTop.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel failLbl = new JLabel("مواد الرسوب والاعادة", SwingConstants.CENTER);
        failLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        failLbl.setForeground(Color.WHITE);
        failTop.add(failLbl, BorderLayout.CENTER);
        failCard.add(failTop, BorderLayout.NORTH);

        failedSubjectsListPanel = new JPanel();
        failedSubjectsListPanel.setLayout(new BoxLayout(failedSubjectsListPanel, BoxLayout.Y_AXIS));
        failedSubjectsListPanel.setOpaque(false);
        failedSubjectsListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        failCard.add(failedSubjectsListPanel, BorderLayout.CENTER);
        
        left.add(failCard);
        left.add(Box.createVerticalStrut(15));

        // 2. Status Codes Legend
        RoundedPanel legendCard = new RoundedPanel(12, Color.WHITE);
        legendCard.setLayout(new BorderLayout());
        legendCard.setMaximumSize(new Dimension(500, 180));

        RoundedPanel legendTop = new RoundedPanel(12, CLR_BLUE_LIGHT);
        legendTop.setLayout(new BorderLayout());
        legendTop.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel legendLbl = new JLabel("أكواد الحالات الخاصة", SwingConstants.RIGHT);
        legendLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        legendLbl.setForeground(new Color(0x1E3A8A));
        legendTop.add(legendLbl, BorderLayout.CENTER);
        legendCard.add(legendTop, BorderLayout.NORTH);

        JPanel legendList = new JPanel();
        legendList.setLayout(new BoxLayout(legendList, BoxLayout.Y_AXIS));
        legendList.setOpaque(false);
        legendList.setBorder(new EmptyBorder(10, 20, 10, 20));
        legendList.add(buildLegendItem("1- : غائب", new Color(0x9CA3AF)));
        legendList.add(Box.createVerticalStrut(5));
        legendList.add(buildLegendItem("2- : محروم", new Color(0xEF4444)));
        legendList.add(Box.createVerticalStrut(5));
        legendList.add(buildLegendItem("3- : مفصول", new Color(0xF59E0B)));
        legendList.add(Box.createVerticalStrut(5));
        legendList.add(buildLegendItem("4- : معتذر", new Color(0x10B981)));
        legendList.add(Box.createVerticalStrut(5));
        legendList.add(buildLegendItem("5- : مؤجل", new Color(0x3B82F6)));
        legendCard.add(legendList, BorderLayout.CENTER);

        left.add(legendCard);
        left.add(Box.createVerticalStrut(15));
        
        // 3. Student Status
        RoundedPanel statusCard = new RoundedPanel(12, Color.WHITE);
        statusCard.setLayout(new BorderLayout());
        statusCard.setMaximumSize(new Dimension(500, 100));

        RoundedPanel statusTop = new RoundedPanel(12, CLR_BLUE_LIGHT);
        statusTop.setLayout(new BorderLayout());
        statusTop.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel statusLbl = new JLabel("حالة التلميذ", SwingConstants.CENTER);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLbl.setForeground(new Color(0x1E3A8A));
        statusTop.add(statusLbl, BorderLayout.CENTER);
        statusCard.add(statusTop, BorderLayout.NORTH);
        
        JPanel pWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pWrapper.setOpaque(false);
        statusPill = new JLabel("غير محدد", SwingConstants.CENTER);
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusPill.setOpaque(true);
        statusPill.setBackground(new Color(0xF1F5F9));
        statusPill.setForeground(CLR_DARK_BLUE);
        statusPill.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xF1F5F9), 8, true),
            new EmptyBorder(0, 30, 0, 30)
        ));
        pWrapper.add(statusPill);
        statusCard.add(pWrapper, BorderLayout.CENTER);

        left.add(statusCard);

        return left;
    }

    private JPanel buildLegendItem(String txt, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(txt, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(color);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JTextField createNumBox() {
        JTextField tf = new JTextField("0");
        tf.setEditable(false);
        tf.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new LineBorder(new Color(0xE0E0E0), 1, true));
        return tf;
    }

    private JPanel buildFooterActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE2E8F0)));

        RoundedButton btnSave = new RoundedButton("حفظ", new Color(0xDC2626), Color.WHITE);
        btnSave.setPreferredSize(new Dimension(100, 36));
        btnSave.addActionListener(e -> saveCurrentGrades());

        RoundedButton btnNext = new RoundedButton("التالى", Color.WHITE, CLR_DARK_BLUE);
        btnNext.setPreferredSize(new Dimension(100, 36));
        btnNext.addActionListener(e -> navigate(1));

        RoundedButton btnPrev = new RoundedButton("السابق", Color.WHITE, CLR_GREEN_BTN);
        btnPrev.setPreferredSize(new Dimension(100, 36));
        btnPrev.addActionListener(e -> navigate(-1));

        RoundedButton btnFirst = new RoundedButton("الأول", Color.WHITE, new Color(0xD97706));
        btnFirst.setPreferredSize(new Dimension(100, 36));
        btnFirst.addActionListener(e -> {
            if (centerStudents.size() > 0) { navigateTo(0); }
        });

        RoundedButton btnLast = new RoundedButton("الأخير", Color.WHITE, new Color(0xDC2626));
        btnLast.setPreferredSize(new Dimension(100, 36));
        btnLast.addActionListener(e -> {
            if (centerStudents.size() > 0) { navigateTo(centerStudents.size() - 1); }
        });

        panel.add(btnFirst);
        panel.add(btnPrev);
        panel.add(btnNext);
        panel.add(btnLast);
        panel.add(Box.createHorizontalStrut(30));
        panel.add(btnSave);

        return panel;
    }

    // ========================================================================
    // LOGIC BINDINGS
    // ========================================================================

    private void loadCenters() {
        regionCombo.removeAllItems();
        regionCombo.addItem("كل المناطق");
        List<String> regions = com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION);
        for(String r : regions) {
            regionCombo.addItem(r);
        }
        onRegionSelected(); // This will trigger loading centers
    }

    private void onRegionSelected() {
        if(centerCombo == null || regionCombo == null) return;
        
        // Remove old listener to avoid double firing
        for (java.awt.event.ActionListener al : centerCombo.getActionListeners()) {
            centerCombo.removeActionListener(al);
        }
        
        centerCombo.removeAllItems();
        centerCombo.addItem("اختر المركز...");
        centerCodeToNameMap.clear();

        String selReg = (String) regionCombo.getSelectedItem();
        java.util.Map<String, String> centersMap;
        if(selReg == null || selReg.equals("كل المناطق")) {
            centersMap = StudentService.getCentersWithCodes();
        } else {
            centersMap = StudentService.getCentersByRegionWithCodes(selReg);
        }
        
        for (java.util.Map.Entry<String, String> entry : centersMap.entrySet()) {
            String displayLabel = entry.getValue().equals(entry.getKey()) ? entry.getKey() : "كود: " + entry.getValue();
            centerCodeToNameMap.put(displayLabel, entry.getKey());
            centerCombo.addItem(displayLabel);
        }
        
        centerCombo.addActionListener(e -> onCenterSelected());
    }

    private void onCenterSelected() {
        if (checkDirty()) return;
        if (centerCombo.getSelectedIndex() <= 0) { clearUI(); return; }
        
        String displayLabel = (String) centerCombo.getSelectedItem();
        String center = centerCodeToNameMap.getOrDefault(displayLabel, displayLabel);
        centerStudents = StudentService.searchStudents("", "", "الكل", "الكل", "الكل", center);
        
        if (centerStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لا يوجد طلاب مسجلين في هذا المركز.", "معلومة", JOptionPane.INFORMATION_MESSAGE);
            clearUI();
            return;
        }
        navigateTo(0);
    }

    private void searchBySecret() {
        String num = JOptionPane.showInputDialog(this, "أدخل الرقم السري للبحث:", "بحث بالرقم السري", JOptionPane.QUESTION_MESSAGE);
        if (num == null || num.trim().isEmpty()) return;
        
        for (int i = 0; i < centerStudents.size(); i++) {
            if (num.equals(centerStudents.get(i).getSecretNo())) {
                if (!checkDirty()) navigateTo(i);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "الرقم السري غير موجود في المركز الحالي.", "خطأ", JOptionPane.ERROR_MESSAGE);
    }

    private void navigate(int offset) {
        if (centerStudents.isEmpty()) return;
        int nextIdx = currentIndex + offset;
        if (nextIdx >= 0 && nextIdx < centerStudents.size()) {
            if (!checkDirty()) navigateTo(nextIdx);
        }
    }

    private void navigateTo(int index) {
        currentIndex = index;
        currentStudent = centerStudents.get(index);
        
        secretNoField.setText(currentStudent.getSecretNo() != null ? currentStudent.getSecretNo() : "غير محدد");
        
        isDirty = false;
        renderSubjectsForProfession();
        populateGradesIntoUI();
        recalculate();
    }

    private void renderSubjectsForProfession() {
        subjectsContainer.removeAll();
        gradeFieldsMap.clear();
        orderedGradeFields.clear();

        if (currentStudent == null || currentStudent.getProfession() == null) return;
        currentSubjects = SubjectService.getSubjectsByProfession(currentStudent.getProfession());

        List<Subject> theorySubjects   = new ArrayList<>();
        List<Subject> practicalSubjects = new ArrayList<>();
        List<Subject> appliedSubjects   = new ArrayList<>();

        for (Subject sub : currentSubjects) {
            String type = sub.getType() != null ? sub.getType() : "نظري";
            if (type.equals("تطبيقي")) appliedSubjects.add(sub);
            else if (type.equals("عملي")) practicalSubjects.add(sub);
            else theorySubjects.add(sub);
        }

        RoundedPanel mainCard = new RoundedPanel(12, Color.WHITE);
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));

        if (!theorySubjects.isEmpty()) renderSubjectGroup(mainCard, "نظري", CLR_CYAN_HEADER, theorySubjects);
        if (!practicalSubjects.isEmpty()) renderSubjectGroup(mainCard, "عملي", CLR_GREEN_BTN, practicalSubjects);
        if (!appliedSubjects.isEmpty()) renderSubjectGroup(mainCard, "تطبيقي", new Color(0xF59E0B), appliedSubjects);

        subjectsContainer.add(mainCard);
        subjectsContainer.revalidate();
        subjectsContainer.repaint();
    }

    private void renderSubjectGroup(JPanel container, String title, Color headerBg, List<Subject> subjects) {
        RoundedPanel groupTop = new RoundedPanel(8, headerBg);
        groupTop.setLayout(new BorderLayout());
        groupTop.setBorder(new EmptyBorder(8, 15, 8, 15));
        JLabel groupLbl = new JLabel("— " + title + " —", SwingConstants.CENTER);
        groupLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        groupLbl.setForeground(Color.WHITE);
        groupTop.add(groupLbl, BorderLayout.CENTER);
        container.add(groupTop);
        container.add(Box.createVerticalStrut(10));

        for (Subject sub : subjects) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            row.setBorder(new EmptyBorder(0, 10, 10, 10));

            // Right Pill (Label)
            RoundedPanel lblPill = new RoundedPanel(10, CLR_GREEN_LIGHT);
            lblPill.setLayout(new BorderLayout());
            lblPill.setPreferredSize(new Dimension(200, 42));
            JLabel lbl = new JLabel(sub.getName() + " /" + sub.getMaxMark() + "/", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(new Color(0x065F46)); // Dark Green Text
            lblPill.add(lbl, BorderLayout.CENTER);

            // Left Input Box
            JTextField gradeInput = new JTextField();
            gradeInput.setFont(new Font("Segoe UI", Font.BOLD, 18));
            gradeInput.setHorizontalAlignment(JTextField.CENTER);
            gradeInput.setPreferredSize(new Dimension(50, 42));
            gradeInput.setBorder(new LineBorder(CLR_TEAL_DARK, 1, true));
            gradeInput.setForeground(CLR_DARK_BLUE);

            setupGradeInput(gradeInput, sub.getMaxMark());
            gradeFieldsMap.put(sub.getId(), gradeInput);
            orderedGradeFields.add(gradeInput);

            row.add(lblPill, BorderLayout.CENTER);
            row.add(gradeInput, BorderLayout.EAST);
            
            container.add(row);
        }
    }

    private void setupGradeInput(JTextField tf, int maxMark) {
        final LineBorder normalBorder = new LineBorder(CLR_TEAL_DARK, 1, true);
        final LineBorder errorBorder  = new LineBorder(new Color(0xDC2626), 2, true);

        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result  = current.substring(0, offset) + text + current.substring(offset + length);
                if (result.matches("-?[0-9]*")) super.replace(fb, offset, length, text, attrs);
            }
        });

        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { triggerCalc(); }
            public void removeUpdate(DocumentEvent e)  { triggerCalc(); }
            public void changedUpdate(DocumentEvent e) { triggerCalc(); }
            private void triggerCalc() {
                isDirty = true;
                recalculate();
                SwingUtilities.invokeLater(() -> {
                    try {
                        String txt = tf.getText().trim();
                        if (!txt.isEmpty() && !txt.equals("-")) {
                            int val = Integer.parseInt(txt);
                            if (val > maxMark) {
                                tf.setBorder(errorBorder);
                                tf.setBackground(new Color(0xFEF2F2));
                                return;
                            }
                        }
                        tf.setBorder(normalBorder);
                        tf.setBackground(Color.WHITE);
                    } catch (NumberFormatException ignored) {}
                });
            }
        });

        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int idx = orderedGradeFields.indexOf(tf);
                    if (idx != -1 && idx < orderedGradeFields.size() - 1) {
                        orderedGradeFields.get(idx + 1).requestFocusInWindow();
                    } else {
                        boolean hasError = gradeFieldsMap.values().stream().anyMatch(f -> f.getBackground().equals(new Color(0xFEF2F2)));
                        if (hasError) {
                            JOptionPane.showMessageDialog(DataEntryPage.this, "توجد درجة تتجاوز الحد الأقصى.", "خطأ", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        saveAndGoNext();
                    }
                }
            }
        });

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
                if (!text.isEmpty()) grades.put(entry.getKey(), Integer.parseInt(text));
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
        sumPracApplField.setText(String.valueOf(prTotal + apTotal));
        grandTotalLabel.setText(String.valueOf(grandTotal));
        
        ratingPill.setText(GradeCalculationService.calculateRating(grandTotal, maxTotal));
        
        // Failing subjects bullets
        failedSubjectsListPanel.removeAll();
        for (Subject sub : currentSubjects) {
            int mark = grades.getOrDefault(sub.getId(), 0);
            if (mark < sub.getPassMark()) {
                JLabel flbl = new JLabel("• " + sub.getName(), SwingConstants.RIGHT);
                flbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                flbl.setForeground(Color.DARK_GRAY);
                flbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                failedSubjectsListPanel.add(flbl);
            }
        }
        failedSubjectsListPanel.revalidate();
        failedSubjectsListPanel.repaint();
        
        // Status calculate
        String status = StudentService.calculateStatus(currentStudent.getProfession(), grades);
        statusPill.setText(status);
        if ("ناجح".equals(status)) {
            statusPill.setBackground(new Color(0xD1FAE5));
            statusPill.setForeground(new Color(0x065F46));
        } else if ("دور ثاني".equals(status) || "راسب".equals(status)) {
            statusPill.setBackground(new Color(0xFEE2E2));
            statusPill.setForeground(new Color(0x991B1B));
        } else {
            statusPill.setBackground(new Color(0xF1F5F9));
            statusPill.setForeground(CLR_DARK_BLUE);
        }
    }

    private void saveCurrentGrades() {
        if (currentStudent == null) return;
        String user = parentFrame != null ? parentFrame.getLoggedInUser().getUsername() : "SYSTEM";
        Map<Integer, Integer> grades = extractGradesFromUI();
        boolean ok = StudentService.updateStudentGrades(currentStudent.getId(), grades, user);
        if (ok) {
            String status = StudentService.calculateStatus(currentStudent.getProfession(), grades);
            StudentService.updateStudentStatusDirectly(currentStudent.getId(), status);
            currentStudent.setGrades(grades);
            currentStudent.setStatus(status);
            isDirty = false;
        } else {
            JOptionPane.showMessageDialog(this, "حدث خطأ أثناء حفظ الدرجات.", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndGoNext() {
        saveCurrentGrades();
        if (centerStudents.isEmpty()) return;
        int nextIdx = currentIndex + 1;
        if (nextIdx < centerStudents.size()) {
            isDirty = false;
            navigateTo(nextIdx);
        }
    }

    private boolean checkDirty() {
        if (isDirty) {
            int res = JOptionPane.showConfirmDialog(this, "تعديلات غير محفوظة، المتابعة والتجاهل؟", "تنبيه", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            return res != JOptionPane.YES_OPTION;
        }
        return false;
    }

    private void clearUI() {
        currentStudent = null; currentIndex = -1; centerStudents.clear(); secretNoField.setText("");
        subjectsContainer.removeAll(); subjectsContainer.revalidate(); subjectsContainer.repaint();
        gradeFieldsMap.clear();
        theoryTotalField.setText("0"); practicalTotalField.setText("0"); appliedTotalField.setText("0"); sumPracApplField.setText("0");
        grandTotalLabel.setText("0"); ratingPill.setText(""); failedSubjectsListPanel.removeAll(); statusPill.setText("غير محدد");
        isDirty = false;
    }

    // ========================================================================
    // CUSTOM SWING COMPONENTS FOR MODERN GRAPHICS
    // ========================================================================

    class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Shadow
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, radius, radius);
            // Main Body
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundedButton extends JButton {
        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(bg);
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? getBackground().darker() : getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class DrawnIcon implements Icon {
        public enum Type { LOCK, BOOK, FLASK, MONITOR, SIGMA }
        private final Type type;
        private final int width, height;
        private final Color color;

        public DrawnIcon(Type type, int width, int height, Color color) {
            this.type = type; this.width = width; this.height = height; this.color = color;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            
            int mx = x + width/2;
            int my = y + height/2;
            
            switch(type) {
                case LOCK:
                    g2.drawRoundRect(mx-6, my-2, 12, 10, 3, 3);
                    g2.drawArc(mx-4, my-8, 8, 8, 0, 180);
                    g2.drawLine(mx, my+2, mx, my+4);
                    break;
                case BOOK:
                    g2.drawRect(mx-8, my-6, 7, 12);
                    g2.drawRect(mx-1, my-6, 7, 12);
                    g2.drawLine(mx-8, my+6, mx-1, my+6);
                    g2.drawLine(mx-1, my+6, mx+6, my+6);
                    break;
                case FLASK:
                    g2.drawLine(mx-3, my-8, mx+3, my-8); // top lip
                    g2.drawLine(mx-2, my-8, mx-2, my-2); // left neck
                    g2.drawLine(mx+2, my-8, mx+2, my-2); // right neck
                    g2.drawLine(mx-2, my-2, mx-8, my+8); // left body
                    g2.drawLine(mx+2, my-2, mx+8, my+8); // right body
                    g2.drawLine(mx-8, my+8, mx+8, my+8); // bottom
                    break;
                case MONITOR:
                    g2.drawRoundRect(mx-10, my-8, 20, 14, 2, 2);
                    g2.drawLine(mx, my+6, mx, my+10);
                    g2.drawLine(mx-4, my+10, mx+4, my+10);
                    break;
                case SIGMA:
                    g2.drawLine(mx-5, my-6, mx+5, my-6);
                    g2.drawLine(mx-5, my-6, mx+2, my);
                    g2.drawLine(mx+2, my, mx-5, my+6);
                    g2.drawLine(mx-5, my+6, mx+5, my+6);
                    break;
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }
}
