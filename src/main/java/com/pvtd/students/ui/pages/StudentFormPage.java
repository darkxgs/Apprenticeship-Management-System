package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.services.StatusesService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentFormPage extends JPanel {

    private AppFrame parentFrame;
    private Student student;
    private boolean isEditMode;

    // Academic Main Fields
    private JTextField nameField, seatNoField, nationalIdField, serialField, registrationNoField;
    private JComboBox<String> centerNameCombo;
    private JComboBox<String> professionCombo;
    private JComboBox<String> statusCombo;

    // Academic Secondary Fields
    private JTextField examSystemField, secretNoField, coordinationNoField, otherNotesField;
    private JComboBox<String> regionCombo, profGroupCombo;

    // Personal Info Fields
    private JComboBox<String> dayCombo, monthCombo, yearCombo, genderCombo, govCombo;
    private JTextField neighborhoodField, religionField, nationalityField, addressField;

    // Media Uploads
    private String currentPicPath, currentIdFrontPath, currentIdBackPath;
    private JLabel picLabel, idFrontLabel, idBackLabel;

    // Dynamic Grades
    private JPanel gradesPanel;
    private Map<Integer, JTextField> dynamicGradeFields;

    public StudentFormPage(AppFrame parent, Student student) {
        this.parentFrame = parent;
        this.student = student != null ? student : new Student();
        this.isEditMode = (student != null);
        this.dynamicGradeFields = new HashMap<>();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // 1. Header
        add(buildHeader(), BorderLayout.NORTH);

        // 2. Main Scrollable Form Content
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(UITheme.BG_LIGHT);
        formContainer.setBorder(new EmptyBorder(25, 40, 40, 40));

        formContainer.add(buildMainAcademicCard());
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(buildSecondaryAcademicCard());
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(buildPersonalCard());
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(buildMediaCard());
        formContainer.add(Box.createVerticalStrut(24));
        formContainer.add(buildGradesCard());

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Populate fields if edit mode
        if (isEditMode) {
            populateFields();
        } else {
            if (professionCombo.getItemCount() > 0) {
                renderDynamicSubjects((String) professionCombo.getSelectedItem());
            }
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(15, 30, 15, 30)));

        JLabel title = new JLabel(isEditMode ? "تعديل بيانات الطالب" : "إضافة طالب جديد");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actions.setOpaque(false);

        JButton btnBack = new JButton("إلغاء والعودة");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(UITheme.TEXT_SECONDARY);
        btnBack.addActionListener(e -> parentFrame.showPage(new StudentsPage(parentFrame)));

        JButton btnSave = new JButton("حفظ البيانات");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(UITheme.PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(this::handleSave);

        actions.add(btnBack);
        actions.add(btnSave);

        header.add(title, BorderLayout.EAST);
        header.add(actions, BorderLayout.WEST);
        return header;
    }

    private JPanel createCardContainer(String titleStr) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(25, 25, 25, 25)));

        JLabel title = new JLabel(titleStr, SwingConstants.RIGHT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(title, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildMainAcademicCard() {
        JPanel card = createCardContainer("البيانات الأكاديمية الأساسية");

        JPanel grid = new JPanel(new GridLayout(0, 4, 20, 20));
        grid.setOpaque(false);
        grid.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        serialField = addLabeledField(grid, "مسلسل");
        seatNoField = addLabeledField(grid, "رقم الجلوس");
        nameField = addLabeledField(grid, "الاسم الرباعي");
        registrationNoField = addLabeledField(grid, "رقم التسجيل");
        nationalIdField = addLabeledField(grid, "الرقم القومي");

        // Center Name Combo
        grid.add(createLabel("اسم المركز"));
        centerNameCombo = new JComboBox<>();
        centerNameCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centerNameCombo.setEditable(true); // Allow typinig new centers
        centerNameCombo.addItem(""); // Add empty item for new students
        for (String c : StudentService.getDistinctCenters()) {
            centerNameCombo.addItem(c);
        }
        grid.add(centerNameCombo);

        // Profession Combo
        grid.add(createLabel("المهنة"));
        professionCombo = new JComboBox<>();
        professionCombo.setEditable(true);
        professionCombo.addItem("");
        professionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        for (String p : StudentService.getDistinctProfessions()) {
            professionCombo.addItem(p);
        }
        professionCombo.addActionListener(e -> {
            String sel = (String) professionCombo.getSelectedItem();
            if (sel != null && !sel.trim().isEmpty())
                renderDynamicSubjects(sel);
        });
        grid.add(professionCombo);

        // Status Override Combo REMOVED - Status is now fully auto-calculated

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSecondaryAcademicCard() {
        JPanel card = createCardContainer("بيانات إضافية ونظام الامتحان");
        JPanel grid = new JPanel(new GridLayout(0, 4, 20, 20));
        grid.setOpaque(false);
        grid.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        grid.add(createLabel("المنطقة"));
        regionCombo = new JComboBox<>();
        regionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        regionCombo.setEditable(true);
        regionCombo.addItem("");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            regionCombo.addItem(r);
        }
        grid.add(regionCombo);

        examSystemField = addLabeledField(grid, "نظام الامتحان");
        secretNoField = addLabeledField(grid, "الرقم السري");

        grid.add(createLabel("المجموعة المهنية"));
        profGroupCombo = new JComboBox<>();
        profGroupCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profGroupCombo.setEditable(true);
        profGroupCombo.addItem("");
        for (String pg : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_PROF_GROUP)) {
            profGroupCombo.addItem(pg);
        }
        grid.add(profGroupCombo);

        coordinationNoField = addLabeledField(grid, "رقم التنسيق");
        otherNotesField = addLabeledField(grid, "ملاحظات أخرى");

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPersonalCard() {
        JPanel card = createCardContainer("البيانات الشخصية والعنوان");
        JPanel grid = new JPanel(new GridLayout(0, 4, 20, 20));
        grid.setOpaque(false);
        grid.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // Date of Birth logic
        grid.add(createLabel("تاريخ الميلاد (يوم / شهر / سنة)"));
        JPanel dobPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        dobPanel.setOpaque(false);
        dayCombo = new JComboBox<>();
        for (int i = 1; i <= 31; i++)
            dayCombo.addItem(String.format("%02d", i));
        monthCombo = new JComboBox<>();
        for (int i = 1; i <= 12; i++)
            monthCombo.addItem(String.format("%02d", i));
        yearCombo = new JComboBox<>();
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear; i >= 1950; i--)
            yearCombo.addItem(String.valueOf(i));
        dobPanel.add(dayCombo);
        dobPanel.add(monthCombo);
        dobPanel.add(yearCombo);
        grid.add(dobPanel);

        // Gender
        grid.add(createLabel("النوع"));
        genderCombo = new JComboBox<>(new String[] { "ذكر", "أنثى" });
        grid.add(genderCombo);

        govCombo = new JComboBox<>();
        govCombo.setEditable(true); // Allow typing new governorate
        govCombo.addItem(""); // Add empty item
        for (String g : StudentService.getDistinctGovernorates()) {
            govCombo.addItem(g);
        }
        grid.add(createLabel("محافظة السكن"));
        grid.add(govCombo);

        neighborhoodField = addLabeledField(grid, "الحي / القرية");
        religionField = addLabeledField(grid, "الديانة");
        nationalityField = addLabeledField(grid, "الجنسية");
        addressField = addLabeledField(grid, "العنوان بالتفصيل");

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMediaCard() {
        JPanel card = createCardContainer("الصور والمرفقات (الهوية الشخصية)");
        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 20));
        grid.setOpaque(false);
        grid.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        picLabel = new JLabel();
        idFrontLabel = new JLabel();
        idBackLabel = new JLabel();

        grid.add(createUploadBox("الصورة الشخصية", path -> {
            currentPicPath = path;
            setImagePreview(picLabel, path);
        }, picLabel));

        grid.add(createUploadBox("وجه البطاقة", path -> {
            currentIdFrontPath = path;
            setImagePreview(idFrontLabel, path);
        }, idFrontLabel));

        grid.add(createUploadBox("ظهر البطاقة", path -> {
            currentIdBackPath = path;
            setImagePreview(idBackLabel, path);
        }, idBackLabel));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /** Scales an image from path and sets it on the label */
    private void setImagePreview(JLabel imgLabel, String path) {
        if (path == null || path.isEmpty()) {
            imgLabel.setIcon(null);
            imgLabel.setText("لا توجد صورة");
            return;
        }
        File f = new File(path);
        if (!f.exists()) {
            imgLabel.setText("الملف غير موجود");
            return;
        }
        ImageIcon raw = new ImageIcon(path);
        int maxW = 220, maxH = 160;
        int w = raw.getIconWidth(), h = raw.getIconHeight();
        double scale = Math.min((double) maxW / w, (double) maxH / h);
        Image scaled = raw.getImage().getScaledInstance(
                (int) (w * scale), (int) (h * scale), Image.SCALE_SMOOTH);
        imgLabel.setIcon(new ImageIcon(scaled));
        imgLabel.setText(null);
    }

    private JPanel createUploadBox(String title, java.util.function.Consumer<String> onSelect, JLabel previewLabel) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(0xF8FAFC));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(titleLbl, BorderLayout.NORTH);

        // Image preview area
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setText("لا توجد صورة");
        previewLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setPreferredSize(new Dimension(220, 160));
        panel.add(previewLabel, BorderLayout.CENTER);

        JButton btn = new JButton("اختر صورة...");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(UITheme.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("اختر صورة");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "صور (JPG, PNG, JPEG)", "jpg", "png", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                onSelect.accept(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(btn, BorderLayout.SOUTH);
        return panel;
    }

    // Data Holder for calculations
    private List<Subject> currentSubjectsList;

    private JPanel buildGradesCard() {
        JPanel card = createCardContainer("درجات الطالب (الشهادة)");

        gradesPanel = new JPanel();
        gradesPanel.setLayout(new BoxLayout(gradesPanel, BoxLayout.Y_AXIS));
        gradesPanel.setOpaque(false);
        gradesPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Note Label for negative statuses
        JLabel hintLabel = new JLabel("ملاحظة هامة: أدخل إحدى القيم التالية لتعيين حالة إدارية: (-1) غائب، (-2) محروم، (-3) مفصول، (-4) معتذر، (-5) مؤجل", SwingConstants.RIGHT);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLabel.setForeground(Color.RED);
        hintLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(hintLabel, BorderLayout.NORTH);
        container.add(gradesPanel, BorderLayout.CENTER);

        card.add(container, BorderLayout.CENTER);

        // Add calc preview button
        JPanel bottomAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomAction.setOpaque(false);
        JButton calcBtn = new JButton("حساب وتقييم النتيجة بالتفصيل");
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcBtn.setBackground(UITheme.PRIMARY);
        calcBtn.setForeground(Color.WHITE);
        calcBtn.addActionListener(e -> evaluateDetailedGrades());
        bottomAction.add(calcBtn);
        card.add(bottomAction, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    private JTextField addLabeledField(JPanel parent, String label) {
        parent.add(createLabel(label));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        parent.add(tf);
        return tf;
    }

    private void renderDynamicSubjects(String profession) {
        gradesPanel.removeAll();
        dynamicGradeFields.clear();

        currentSubjectsList = SubjectService.getSubjectsByProfession(profession);
        if (currentSubjectsList == null || currentSubjectsList.isEmpty()) {
            JLabel noSubjLabel = new JLabel("لا توجد مواد مسجلة لهذا التخصص.", SwingConstants.CENTER);
            noSubjLabel.setForeground(UITheme.DANGER);
            noSubjLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            gradesPanel.add(noSubjLabel);
        } else {
            // Header Row
            JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
            headerRow.setBackground(UITheme.BG_SIDEBAR);
            headerRow.setBorder(new EmptyBorder(8, 8, 8, 8));

            JLabel h1 = new JLabel("اسم المادة (النوع)", SwingConstants.CENTER);
            JLabel h2 = new JLabel("النهاية العظمى", SwingConstants.CENTER);
            JLabel h3 = new JLabel("درجة النجاح", SwingConstants.CENTER);
            JLabel h4 = new JLabel("درجة الطالب", SwingConstants.CENTER);

            Font hFont = new Font("Segoe UI", Font.BOLD, 14);
            h1.setFont(hFont);
            h2.setFont(hFont);
            h3.setFont(hFont);
            h4.setFont(hFont);
            h1.setForeground(Color.WHITE);
            h2.setForeground(Color.WHITE);
            h3.setForeground(Color.WHITE);
            h4.setForeground(Color.WHITE);

            headerRow.add(h4);
            headerRow.add(h3);
            headerRow.add(h2);
            headerRow.add(h1);
            gradesPanel.add(headerRow);

            // Data Rows
            int i = 0;
            for (Subject sub : currentSubjectsList) {
                JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
                row.setBackground(i % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                row.setBorder(new EmptyBorder(8, 8, 8, 8));

                String typeStr = sub.getType() != null ? sub.getType() : "نظري";
                JLabel nameL = new JLabel(sub.getName() + " (" + typeStr + ")", SwingConstants.CENTER);
                JLabel maxL = new JLabel(String.valueOf(sub.getMaxMark()), SwingConstants.CENTER);
                JLabel passL = new JLabel(String.valueOf(sub.getPassMark()), SwingConstants.CENTER);

                Font vFont = new Font("Segoe UI", Font.BOLD, 14);
                nameL.setFont(vFont);
                maxL.setFont(vFont);
                passL.setFont(vFont);

                JTextField field = new JTextField("0");
                field.setFont(new Font("Segoe UI", Font.BOLD, 15));
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

                if (isEditMode && student.getGrades() != null && student.getGrades().containsKey(sub.getId())) {
                    field.setText(String.valueOf(student.getGrades().get(sub.getId())));
                }

                row.add(field);
                row.add(passL);
                row.add(maxL);
                row.add(nameL);
                dynamicGradeFields.put(sub.getId(), field);
                gradesPanel.add(row);
                i++;
            }
        }
        gradesPanel.revalidate();
        gradesPanel.repaint();
    }

    private void evaluateDetailedGrades() {
        if (currentSubjectsList == null || currentSubjectsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لا توجد مواد لتقييمها!", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int totalMax = 0, totalAttained = 0;
            int practicalMax = 0, practicalAttained = 0;
            int theoreticalMax = 0, theoreticalAttained = 0;
            int failedCount = 0;
            StringBuilder failedSubjects = new StringBuilder();

            Map<Integer, Integer> currentGrades = collectGrades();

            for (Subject sub : currentSubjectsList) {
                int mark = currentGrades.getOrDefault(sub.getId(), 0);
                totalMax += sub.getMaxMark();
                totalAttained += mark;

                boolean isPractical = sub.getType() != null && sub.getType().contains("عمل");
                if (isPractical) {
                    practicalMax += sub.getMaxMark();
                    practicalAttained += mark;
                } else {
                    theoreticalMax += sub.getMaxMark();
                    theoreticalAttained += mark;
                }

                if (mark < sub.getPassMark()) {
                    failedCount++;
                    failedSubjects.append(" - ").append(sub.getName()).append(" ( جاب ").append(mark)
                            .append(" من ").append(sub.getPassMark()).append(" )<br>");
                }
            }

            String sel = (String) professionCombo.getSelectedItem();
            String overallStatus = StudentService.calculateStatus(sel, currentGrades);

            // Build detailed HTML message
            String color = overallStatus.equals("ناجح") ? "green" : (overallStatus.equals("راسب") ? "red" : "orange");
            StringBuilder msg = new StringBuilder();
            msg.append("<html><div style='text-align:right; font-family:tahoma; font-size:14px; direction:rtl;'>");
            msg.append("<h2>تقييم درجات الطالب بالتفصيل</h2>");
            msg.append("<hr>");
            msg.append("<b>المجموع الكلي:</b> ").append(totalAttained).append(" من ").append(totalMax)
                    .append("<br><br>");
            msg.append("<b>مجموع النظري:</b> ").append(theoreticalAttained).append(" من ").append(theoreticalMax)
                    .append("<br>");
            msg.append("<b>مجموع العملي:</b> ").append(practicalAttained).append(" من ").append(practicalMax)
                    .append("<br><br>");

            msg.append("<b>عدد المواد التي رسب فيها:</b> ").append(failedCount).append("<br>");
            if (failedCount > 0) {
                msg.append("<div style='color:red; margin-top:5px;'>").append(failedSubjects.toString())
                        .append("</div><br>");
            }

            msg.append("<h3 style='color:").append(color).append(";'>الحالة النهائية المتوقعة: ").append(overallStatus)
                    .append("</h3>");
            msg.append("</div></html>");

            JOptionPane.showMessageDialog(this, msg.toString(), "تفاصيل التقييم", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التقييم. تأكد من إدخال الدرجات كأرقام.", "خطأ",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields() {
        serialField.setText(student.getSerial());
        seatNoField.setText(student.getSeatNo());
        nameField.setText(student.getName());
        registrationNoField.setText(student.getRegistrationNo());
        nationalIdField.setText(student.getNationalId());

        if (student.getCenterName() != null) {
            centerNameCombo.setSelectedItem(student.getCenterName());
        }

        if (student.getRegion() != null) {
            regionCombo.setSelectedItem(student.getRegion());
        }
        if (student.getProfession() != null) {
            professionCombo.setSelectedItem(student.getProfession());
        }
        examSystemField.setText(student.getExamSystem());
        secretNoField.setText(student.getSecretNo());
        if (student.getProfessionalGroup() != null) {
            profGroupCombo.setSelectedItem(student.getProfessionalGroup());
        }
        coordinationNoField.setText(student.getCoordinationNo());
        otherNotesField.setText(student.getOtherNotes());
        neighborhoodField.setText(student.getNeighborhood());
        religionField.setText(student.getReligion());
        nationalityField.setText(student.getNationality());
        addressField.setText(student.getAddress());

        // Combos
        if (student.getDobDay() != null)
            dayCombo.setSelectedItem(student.getDobDay());
        if (student.getDobMonth() != null)
            monthCombo.setSelectedItem(student.getDobMonth());
        if (student.getDobYear() != null)
            yearCombo.setSelectedItem(student.getDobYear());
        if (student.getGender() != null)
            genderCombo.setSelectedItem(student.getGender());
        if (student.getGovernorate() != null)
            govCombo.setSelectedItem(student.getGovernorate());

        // Status combo removed

        currentPicPath = student.getImagePath();
        currentIdFrontPath = student.getIdFrontPath();
        currentIdBackPath = student.getIdBackPath();

        if (currentPicPath != null)
            setImagePreview(picLabel, currentPicPath);
        if (currentIdFrontPath != null)
            setImagePreview(idFrontLabel, currentIdFrontPath);
        if (currentIdBackPath != null)
            setImagePreview(idBackLabel, currentIdBackPath);
    }

    private Map<Integer, Integer> collectGrades() {
        Map<Integer, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, JTextField> e : dynamicGradeFields.entrySet()) {
            try {
                int mark = Integer.parseInt(e.getValue().getText().trim());
                map.put(e.getKey(), mark);
            } catch (NumberFormatException ex) {
                map.put(e.getKey(), 0);
            }
        }
        return map;
    }

    private String copyToStudentFolder(String sourcePath, String nationalId, String targetFileName) {
        if (sourcePath == null || sourcePath.isEmpty())
            return null;

        File source = new File(sourcePath);
        if (!source.exists())
            return null;

        // Skip copying if it's already exactly the target file we want
        String safeId = (nationalId != null && !nationalId.trim().isEmpty()) ? nationalId.trim() : "unknown_id";
        // To remove invalid filename chars safely
        safeId = safeId.replaceAll("[^a-zA-Z0-9.-]", "_");

        File studentFolder = new File("students_images", safeId);
        if (!studentFolder.exists()) {
            studentFolder.mkdirs();
        }

        // Keep extension from original file if no explicit target name was forced
        String extension = "";
        int i = source.getName().lastIndexOf('.');
        if (i > 0) {
            extension = source.getName().substring(i);
        }

        File dest = new File(studentFolder, targetFileName + extension);

        // If the source is already the destination, do nothing
        if (source.getAbsolutePath().equals(dest.getAbsolutePath())) {
            return dest.getAbsolutePath();
        }

        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleSave(java.awt.event.ActionEvent e) {
        if (nameField.getText().trim().isEmpty() || seatNoField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال الاسم ورقم الجلوس كحد أدنى!", "خطأ",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String natId = nationalIdField.getText().trim();
        if (natId.isEmpty()) {
            // Warn if national ID is missing, as it's needed for folders
            int confirm = JOptionPane.showConfirmDialog(this,
                    "لم يتم إدخال 'الرقم القومي'. سيؤدي ذلك لإنشاء مجلد ببيانات غير مسماة (unknown_id).\nهل تريد المتابعة على أية حال؟",
                    "تحذير فصيح",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION)
                return;
        }

        student.setSerial(serialField.getText().trim());
        student.setSeatNo(seatNoField.getText().trim());
        student.setName(nameField.getText().trim());
        student.setRegistrationNo(registrationNoField.getText().trim());
        student.setNationalId(natId);

        Object selectedCenter = centerNameCombo.getSelectedItem();
        student.setCenterName(selectedCenter != null ? selectedCenter.toString() : "");

        String sStatus = (statusCombo != null) ? (String) statusCombo.getSelectedItem() : null;
        if (sStatus == null || "تلقائي (حسب الدرجات)".equals(sStatus)) {
            student.setStatus(null); // Let the service auto-gen
        } else {
            student.setStatus(sStatus);
        }

        Object regObj = regionCombo.getSelectedItem();
        student.setRegion(regObj != null ? regObj.toString().trim() : "");
        Object profObj = professionCombo.getSelectedItem();
        student.setProfession(profObj != null ? profObj.toString().trim() : "");
        student.setExamSystem(examSystemField.getText().trim());
        student.setSecretNo(secretNoField.getText().trim());
        Object pgObj = profGroupCombo.getSelectedItem();
        student.setProfessionalGroup(pgObj != null ? pgObj.toString().trim() : "");
        student.setCoordinationNo(coordinationNoField.getText().trim());
        student.setOtherNotes(otherNotesField.getText().trim());

        student.setDobDay((String) dayCombo.getSelectedItem());
        student.setDobMonth((String) monthCombo.getSelectedItem());
        student.setDobYear((String) yearCombo.getSelectedItem());
        student.setGender((String) genderCombo.getSelectedItem());

        Object gvObj = govCombo.getSelectedItem();
        if (gvObj != null)
            student.setGovernorate(gvObj.toString());

        student.setNeighborhood(neighborhoodField.getText().trim());
        student.setReligion(religionField.getText().trim());
        student.setNationality(nationalityField.getText().trim());
        student.setAddress(addressField.getText().trim());

        // Process images dynamically into student's unique folder
        student.setImagePath(copyToStudentFolder(currentPicPath, natId, "profile"));
        student.setIdFrontPath(copyToStudentFolder(currentIdFrontPath, natId, "id_front"));
        student.setIdBackPath(copyToStudentFolder(currentIdBackPath, natId, "id_back"));

        // Process Grades
        student.setGrades(collectGrades());

        try {
            String user = parentFrame != null ? parentFrame.getLoggedInUser().getUsername() : "SYSTEM";
            if (isEditMode) {
                StudentService.updateStudent(student, user);
                JOptionPane.showMessageDialog(this, "تم تحديث بيانات الطالب بنجاح!", "نجاح",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                StudentService.addStudent(student, user);
                JOptionPane.showMessageDialog(this, "تم إضافة الطالب بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            }
            parentFrame.showPage(new StudentsPage(parentFrame));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "حدث خطأ أثناء الحفظ: " + ex.getMessage(), "خطأ",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

}
