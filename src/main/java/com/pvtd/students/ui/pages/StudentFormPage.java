package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.models.Specialization;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SpecializationService;
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
import java.util.UUID;

public class StudentFormPage extends JPanel {

    private AppFrame parentFrame;
    private Student student;
    private boolean isEditMode;

    // Academic Main Fields
    private JTextField nameField, seatNoField, nationalIdField, serialField, registrationNoField, centerNameField,
            schoolField, academicYearField;
    private JComboBox<SpecializationItem> specCombo;
    private JComboBox<String> statusCombo;

    // Academic Secondary Fields
    private JTextField regionField, professionField, examSystemField, secretNoField, profGroupField,
            coordinationNoField, otherNotesField;

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
            if (specCombo.getItemCount() > 0) {
                renderDynamicSubjects(((SpecializationItem) specCombo.getSelectedItem()).id);
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
        centerNameField = addLabeledField(grid, "اسم المركز");

        // Specialization Combo
        grid.add(createLabel("التخصص"));
        specCombo = new JComboBox<>();
        specCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Department 1 represents the root
        for (Specialization sp : SpecializationService.getSpecializationsByDepartment(1)) {
            specCombo.addItem(new SpecializationItem(sp.getId(), sp.getName()));
        }
        specCombo.addActionListener(e -> {
            SpecializationItem sel = (SpecializationItem) specCombo.getSelectedItem();
            if (sel != null)
                renderDynamicSubjects(sel.id);
        });
        grid.add(specCombo);

        schoolField = addLabeledField(grid, "المدرسة");
        academicYearField = addLabeledField(grid, "العام الدراسي");

        // Status Override Combo
        grid.add(createLabel("الحالة الإدارية (تجاوز)"));
        statusCombo = new JComboBox<>();
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusCombo.addItem("تلقائي (حسب الدرجات)");
        for (String s : StatusesService.getAllStatuses()) {
            statusCombo.addItem(s);
        }
        grid.add(statusCombo);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSecondaryAcademicCard() {
        JPanel card = createCardContainer("بيانات إضافية ونظام الامتحان");
        JPanel grid = new JPanel(new GridLayout(0, 4, 20, 20));
        grid.setOpaque(false);
        grid.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        regionField = addLabeledField(grid, "المنطقة");
        professionField = addLabeledField(grid, "المهنة");
        examSystemField = addLabeledField(grid, "نظام الامتحان");
        secretNoField = addLabeledField(grid, "الرقم السري");
        profGroupField = addLabeledField(grid, "المجموعة المهنية");
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

        govCombo = new JComboBox<>(new String[] { "القاهرة", "الجيزة", "الإسكندرية", "بورسعيد", "السويس", "أخرى" });
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

    private JPanel buildGradesCard() {
        JPanel card = createCardContainer("الدرجات الديناميكية (تُحسب تلقائياً)");
        gradesPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        gradesPanel.setOpaque(false);
        gradesPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        card.add(gradesPanel, BorderLayout.CENTER);

        // Add calc preview button
        JPanel bottomAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomAction.setOpaque(false);
        JButton calcBtn = new JButton("حساب وتقييم الحالة التلقائية");
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcBtn.addActionListener(e -> {
            try {
                SpecializationItem sel = (SpecializationItem) specCombo.getSelectedItem();
                Map<Integer, Integer> currentGrades = collectGrades();
                String result = StudentService.calculateStatus(sel.id, currentGrades);
                JOptionPane.showMessageDialog(this, "الحالة المتوقعة بناءً على الدرجات هي: " + result, "معاينة",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "تأكد من إدخال الأرقام فقط في الدرجات.", "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
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

    private void renderDynamicSubjects(int specId) {
        gradesPanel.removeAll();
        dynamicGradeFields.clear();

        List<Subject> subjects = SubjectService.getSubjectsBySpecialization(specId);
        if (subjects.isEmpty()) {
            JLabel noSubjLabel = new JLabel("لا توجد مواد مسجلة لهذا التخصص.", SwingConstants.CENTER);
            noSubjLabel.setForeground(Color.RED);
            gradesPanel.add(noSubjLabel);
        } else {
            for (Subject sub : subjects) {
                gradesPanel.add(createLabel(sub.getName() + " (" + sub.getMaxMark() + "):"));
                JTextField field = new JTextField("0");
                field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                field.setHorizontalAlignment(JTextField.CENTER);

                if (isEditMode && student.getGrades() != null && student.getGrades().containsKey(sub.getId())) {
                    field.setText(String.valueOf(student.getGrades().get(sub.getId())));
                }

                gradesPanel.add(field);
                dynamicGradeFields.put(sub.getId(), field);
            }
        }
        gradesPanel.revalidate();
        gradesPanel.repaint();
    }

    private void populateFields() {
        serialField.setText(student.getSerial());
        seatNoField.setText(student.getSeatNo());
        nameField.setText(student.getName());
        registrationNoField.setText(student.getRegistrationNo());
        nationalIdField.setText(student.getNationalId());
        centerNameField.setText(student.getCenterName());
        schoolField.setText(student.getSchool());
        academicYearField.setText(student.getAcademicYear());
        regionField.setText(student.getRegion());
        professionField.setText(student.getProfession());
        examSystemField.setText(student.getExamSystem());
        secretNoField.setText(student.getSecretNo());
        profGroupField.setText(student.getProfessionalGroup());
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

        for (int i = 0; i < specCombo.getItemCount(); i++) {
            if (specCombo.getItemAt(i).id == student.getSpecializationId()) {
                specCombo.setSelectedIndex(i);
                break;
            }
        }

        String st = student.getStatus();
        if (st == null || st.isEmpty() || st.equals("ناجح") || st.equals("راسب") || st.equals("دور ثاني")) {
            statusCombo.setSelectedIndex(0);
        } else {
            statusCombo.setSelectedItem(st);
        }

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

    private String saveImageLocally(String sourcePath) {
        if (sourcePath == null || sourcePath.isEmpty())
            return null;
        File source = new File(sourcePath);
        if (!source.exists() || source.getParentFile().getName().equals("images")) {
            return sourcePath; // already saved locally
        }

        try {
            String userHome = System.getProperty("user.home");
            File installDir = new File(userHome, ".student_mgmt/images");
            if (!installDir.exists())
                installDir.mkdirs();

            String extension = source.getName().substring(source.getName().lastIndexOf("."));
            String newName = UUID.randomUUID().toString() + extension;

            File dest = new File(installDir, newName);
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

        student.setSerial(serialField.getText().trim());
        student.setSeatNo(seatNoField.getText().trim());
        student.setName(nameField.getText().trim());
        student.setRegistrationNo(registrationNoField.getText().trim());
        student.setNationalId(nationalIdField.getText().trim());
        student.setCenterName(centerNameField.getText().trim());

        SpecializationItem selSpec = (SpecializationItem) specCombo.getSelectedItem();
        student.setSpecializationId(selSpec != null ? selSpec.id : 0);

        student.setSchool(schoolField.getText().trim());
        student.setAcademicYear(academicYearField.getText().trim());

        String sStatus = (String) statusCombo.getSelectedItem();
        if ("تلقائي (حسب الدرجات)".equals(sStatus)) {
            student.setStatus(null); // Let the service auto-gen
        } else {
            student.setStatus(sStatus);
        }

        student.setRegion(regionField.getText().trim());
        student.setProfession(professionField.getText().trim());
        student.setExamSystem(examSystemField.getText().trim());
        student.setSecretNo(secretNoField.getText().trim());
        student.setProfessionalGroup(profGroupField.getText().trim());
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

        // Process images
        student.setImagePath(saveImageLocally(currentPicPath));
        student.setIdFrontPath(saveImageLocally(currentIdFrontPath));
        student.setIdBackPath(saveImageLocally(currentIdBackPath));

        // Process Grades
        student.setGrades(collectGrades());

        try {
            if (isEditMode) {
                StudentService.updateStudent(student);
                JOptionPane.showMessageDialog(this, "تم تحديث بيانات الطالب بنجاح!", "نجاح",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                StudentService.addStudent(student);
                JOptionPane.showMessageDialog(this, "تم إضافة الطالب بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            }
            parentFrame.showPage(new StudentsPage(parentFrame));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "حدث خطأ أثناء الحفظ: " + ex.getMessage(), "خطأ",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private static class SpecializationItem {
        int id;
        String name;

        SpecializationItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
