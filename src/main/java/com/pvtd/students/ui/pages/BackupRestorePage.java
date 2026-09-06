package com.pvtd.students.ui.pages;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.FlatClientProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BackupRestorePage extends JPanel {
    private JPanel contentPane;
    private CardLayout cardLayout;

    // Segmented control buttons
    private JButton btnTabBackup, btnTabRestore;

    public BackupRestorePage(AppFrame parent) {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(24, 30, 30, 30));

        // 1. Header Area with Title and Segmented Switcher
        JPanel topArea = new JPanel(new BorderLayout(0, 25));
        topArea.setOpaque(false);

        JLabel title = new JLabel("النسخ الاحتياطي والاستعادة", SwingConstants.RIGHT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        topArea.add(title, BorderLayout.NORTH);

        // Segmented Control
        JPanel segmentedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        segmentedPanel.setOpaque(false);

        btnTabBackup = createSegmentBtn("نسخ احتياطي", true);
        btnTabRestore = createSegmentBtn("استعادة البيانات", false);

        segmentedPanel.add(btnTabBackup);
        segmentedPanel.add(btnTabRestore);
        topArea.add(segmentedPanel, BorderLayout.CENTER);

        add(topArea, BorderLayout.NORTH);

        // 2. Content Area (Scrollable & Centered)
        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        contentPane.setOpaque(false);

        // Wrap panels in a container that prevents excessive stretching
        contentPane.add(wrapInCenteringPanel(buildBackupPanel()), "BACKUP");
        contentPane.add(wrapInCenteringPanel(buildRestorePanel()), "RESTORE");

        JScrollPane scrollPane = new JScrollPane(contentPane);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);

        // Default view
        switchTab(true);
    }

    private JPanel wrapInCenteringPanel(JPanel panel) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(30, 0, 30, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE; // Don't fill, stay at preferred size
        wrapper.add(panel, gbc);
        return wrapper;
    }

    private JButton createSegmentBtn(String text, boolean left) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_HEADER);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Fix: Use direct integer property for JButton.arc
        btn.putClientProperty("JButton.arc", 12);
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);

        btn.addActionListener(e -> switchTab(text.contains("نسخ")));
        return btn;
    }

    private void switchTab(boolean isBackup) {
        // Update Backup Tab
        btnTabBackup.setBackground(isBackup ? UITheme.PRIMARY : new Color(0xE2E8F0));
        btnTabBackup.setForeground(isBackup ? Color.WHITE : UITheme.TEXT_SECONDARY);
        FlatSVGIcon backupIcon = new FlatSVGIcon("icons/download.svg", 20, 20);
        backupIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> isBackup ? Color.WHITE : UITheme.TEXT_SECONDARY));
        btnTabBackup.setIcon(backupIcon);
        
        // Update Restore Tab
        btnTabRestore.setBackground(!isBackup ? UITheme.PRIMARY : new Color(0xE2E8F0));
        btnTabRestore.setForeground(!isBackup ? Color.WHITE : UITheme.TEXT_SECONDARY);
        FlatSVGIcon restoreIcon = new FlatSVGIcon("icons/upload.svg", 20, 20);
        restoreIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> !isBackup ? Color.WHITE : UITheme.TEXT_SECONDARY));
        btnTabRestore.setIcon(restoreIcon);
        
        cardLayout.show(contentPane, isBackup ? "BACKUP" : "RESTORE");
    }

    // --- Backup UI ---

    private JCheckBox cbRegions, cbCenters, cbProfGroups, cbProfessions, cbSubjects, cbSpecializations, cbGrades;

    private JPanel buildBackupPanel() {
        JPanel card = createMainCard();
        card.setLayout(new BorderLayout(0, 35));
        card.setPreferredSize(new Dimension(1080, 740));

        // Info text
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        JLabel head = new JLabel("تصدير وقاعدة بيانات النظام", SwingConstants.RIGHT);
        head.setFont(UITheme.FONT_CARD_TITLE);
        JLabel sub = new JLabel("حدد الجداول التي ترغب في تضمينها في ملف النسخة الاحتياطية", SwingConstants.RIGHT);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        infoPanel.add(head, BorderLayout.NORTH);
        infoPanel.add(sub, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.NORTH);

        // Grid of options
        JPanel grid = new JPanel(new GridLayout(0, 3, 25, 25));
        grid.setOpaque(false);

        cbRegions = createOptionCard(grid, "المناطق", "الرموز الجغرافية للمناطق", "map-pin.svg", UITheme.PRIMARY);
        cbCenters = createOptionCard(grid, "المراكز", "مراكز التدريب والمحطات", "home.svg", new Color(0x8B5CF6));
        cbProfGroups = createOptionCard(grid, "المجموعات المهنية", "تصنيفات المهن الرئيسية", "users.svg", new Color(0x10B981));
        cbProfessions = createOptionCard(grid, "المهن", "قائمة المهن المسجلة", "briefcase.svg", new Color(0xF59E0B));
        cbSpecializations = createOptionCard(grid, "التخصصات", "تخصصات الأقسام والمهن", "settings.svg", new Color(0x6366F1));
        cbSubjects = createOptionCard(grid, "المواد الدراسية", "المواد والدرجات والترتيب", "book-open.svg", new Color(0xEC4899));
        cbGrades = createOptionCard(grid, "درجات الطلاب", "درجات الطلاب في كل مادة", "students.svg", new Color(0xEF4444));

        card.add(grid, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        
        JButton btnAll = new JButton("تحديد / إلغاء تحديد الكل");
        btnAll.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnAll.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
        btnAll.setForeground(UITheme.PRIMARY);
        btnAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAll.addActionListener(e -> {
            boolean anyUnselected = !cbRegions.isSelected() || !cbCenters.isSelected() || !cbProfGroups.isSelected() || !cbProfessions.isSelected() || !cbSubjects.isSelected() || !cbSpecializations.isSelected() || !cbGrades.isSelected();
            cbRegions.setSelected(anyUnselected); cbCenters.setSelected(anyUnselected);
            cbProfGroups.setSelected(anyUnselected); cbProfessions.setSelected(anyUnselected);
            cbSubjects.setSelected(anyUnselected); cbSpecializations.setSelected(anyUnselected);
            cbGrades.setSelected(anyUnselected);
        });

        // Dependency Logic
        cbProfessions.addActionListener(e -> { if (cbProfessions.isSelected()) cbProfGroups.setSelected(true); });
        cbCenters.addActionListener(e -> { if (cbCenters.isSelected()) cbRegions.setSelected(true); });
        cbSubjects.addActionListener(e -> {
            if (cbSubjects.isSelected()) { cbProfessions.setSelected(true); cbProfGroups.setSelected(true); }
            else cbGrades.setSelected(false); // الدرجات بلا معنى بدون المواد
        });
        cbSpecializations.addActionListener(e -> { if (cbSpecializations.isSelected()) cbProfGroups.setSelected(true); });
        // الدرجات تتطلب المواد الدراسية دائماً
        cbGrades.addActionListener(e -> { if (cbGrades.isSelected()) { cbSubjects.setSelected(true); cbProfessions.setSelected(true); cbProfGroups.setSelected(true); } });

        JButton btnRun = new JButton("إنشاء نسخة احتياطية (JSON)");
        btnRun.setIcon(new FlatSVGIcon("icons/download.svg", 20, 20));
        btnRun.setFont(UITheme.FONT_HEADER);
        btnRun.setBackground(UITheme.PRIMARY);
        btnRun.setForeground(Color.WHITE);
        btnRun.setPreferredSize(new Dimension(280, 50));
        btnRun.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        btnRun.addActionListener(e -> runBackup());

        footer.add(btnAll, BorderLayout.EAST);
        footer.add(btnRun, BorderLayout.WEST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JCheckBox createOptionCard(JPanel parent, String title, String desc, String iconName, Color accent) {
        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setBackground(new Color(0xF8FAFC));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
            new EmptyBorder(18, 20, 18, 20)
        ));
        p.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        p.setPreferredSize(new Dimension(320, 115));

        // Icon Circle with background protection
        FlatSVGIcon icon = new FlatSVGIcon("icons/" + iconName, 32, 32);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> {
            if (c.getAlpha() == 0) return c;
            return accent;
        }));
        JLabel iconLbl = new JLabel(icon);
        p.add(iconLbl, BorderLayout.EAST);

        // Labels
        JPanel txt = new JPanel(new GridLayout(2, 1, 0, 4));
        txt.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.RIGHT);
        t.setFont(UITheme.FONT_HEADER);
        JLabel d = new JLabel(desc, SwingConstants.RIGHT);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        d.setForeground(UITheme.TEXT_SECONDARY);
        txt.add(t);
        txt.add(d);
        p.add(txt, BorderLayout.CENTER);

        // Checkbox
        JCheckBox cb = new JCheckBox();
        cb.setSelected(true);
        cb.setOpaque(false);
        p.add(cb, BorderLayout.WEST);

        parent.add(p);
        return cb;
    }

    // --- Restore UI ---

    private JPanel buildRestorePanel() {
        JPanel card = createMainCard();
        card.setPreferredSize(new Dimension(1080, 620));
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        // Giant Icon & Title
        FlatSVGIcon topIcon = new FlatSVGIcon("icons/upload.svg", 80, 80);
        topIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> UITheme.PRIMARY));
        JLabel icon = new JLabel(topIcon);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; card.add(icon, gbc);

        JLabel head = new JLabel("استيراد بيانات من نسخة سابقة", SwingConstants.CENTER);
        head.setFont(UITheme.FONT_CARD_TITLE);
        gbc.gridy = 1; gbc.insets = new Insets(20, 0, 10, 0); card.add(head, gbc);

        JLabel sub = new JLabel("اختر ملف .json الذي قمت بتصديره مسبقاً لاستعادة الإعدادات", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 50, 0); card.add(sub, gbc);

        // Warning Box
        JPanel warnBox = new JPanel(new BorderLayout(20, 0));
        warnBox.setBackground(new Color(0xFEF2F2));
        warnBox.setBorder(new EmptyBorder(20, 30, 20, 30));
        warnBox.putClientProperty(FlatClientProperties.STYLE, "arc: 16");
        JLabel warnTxt = new JLabel("<html><div style='text-align:right;'><b>تحذير هام:</b> استعادة البيانات ستمسح كل الإدخالات الحالية في الجداول المستوردة. تأكد من عمل نسخة احتياطية للبيانات الحالية أولاً إذا كنت بحاجة إليها.</div></html>");
        warnTxt.setForeground(UITheme.DANGER);
        warnTxt.setFont(UITheme.FONT_BODY);
        warnBox.add(warnTxt, BorderLayout.CENTER);
        gbc.gridy = 3; gbc.insets = new Insets(0, 80, 50, 80); card.add(warnBox, gbc);

        // Action Button
        JButton btnSelect = new JButton("اختيار ملف النسخة الاحتياطية");
        btnSelect.setFont(UITheme.FONT_HEADER);
        btnSelect.setBackground(UITheme.PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setPreferredSize(new Dimension(0, 55));
        btnSelect.putClientProperty(FlatClientProperties.STYLE, "arc: 14");
        btnSelect.addActionListener(e -> runRestore());
        gbc.gridy = 4; gbc.insets = new Insets(0, 150, 0, 150); card.add(btnSelect, gbc);

        return card;
    }

    private JPanel createMainCard() {
        JPanel c = new JPanel();
        c.setBackground(UITheme.CARD_BG);
        c.setBorder(new EmptyBorder(50, 60, 50, 60));
        c.putClientProperty(FlatClientProperties.STYLE, "arc: 24; [light]background: #ffffff; [dark]background: #1e293b");
        return c;
    }

    // --- Logic ---

    private void runBackup() {
        Map<String, List<Map<String, Object>>> backupData = new HashMap<>();
        try {
            if (cbRegions.isSelected()) backupData.put("regions", fetchTableData("regions"));
            if (cbCenters.isSelected()) backupData.put("centers", fetchTableData("centers"));
            if (cbProfGroups.isSelected()) backupData.put("professional_groups", fetchTableData("professional_groups"));
            if (cbProfessions.isSelected()) backupData.put("professions", fetchTableData("professions"));
            if (cbSpecializations.isSelected()) backupData.put("specializations", fetchTableData("specializations"));
            if (cbSubjects.isSelected()) backupData.put("subjects", fetchTableData("subjects"));
            // درجات الطلاب: تُصدَّر مع مفاتيح تعريف الطالب (رقم الجلوس / الرقم القومي)
            // لأن أرقام الطلاب (id) تختلف من قاعدة بيانات لأخرى.
            if (cbGrades.isSelected() && cbSubjects.isSelected())
                backupData.put("student_grades", fetchQueryData(
                        "SELECT sg.student_id, sg.subject_id, sg.obtained_mark, " +
                        "s.seat_no AS student_seat_no, s.national_id AS student_national_id " +
                        "FROM student_grades sg JOIN students s ON s.id = sg.student_id"));

            if (backupData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى اختيار جدول واحد على الأقل للنسخ.", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            String fileName = "backup_" + new SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".json";
            chooser.setSelectedFile(new File(fileName));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                // serializeNulls: لازم حتى تظهر الأعمدة الفارغة (NULL) في الملف،
                // وإلا يختفي العمود بالكامل عند الاستعادة (مثل parent_subject_id).
                Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile()), "UTF-8")) {
                    gson.toJson(backupData, writer);
                    JOptionPane.showMessageDialog(this, "تم إنشاء النسخة الاحتياطية بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Map<String, Object>> fetchTableData(String table) throws SQLException {
        return fetchQueryData("SELECT * FROM " + table);
    }

    private List<Map<String, Object>> fetchQueryData(String sql) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
                list.add(row);
            }
        }
        return list;
    }

    private void runRestore() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Reader reader = new InputStreamReader(new FileInputStream(chooser.getSelectedFile()), "UTF-8")) {
                Type type = new TypeToken<Map<String, List<Map<String, Object>>>>(){}.getType();
                Map<String, List<Map<String, Object>>> backupData = new Gson().fromJson(reader, type);

                if (backupData == null) return;

                // ── تأكيد يوضّح بالاسم كل جدول سيتم مسحه ──
                StringBuilder msg = new StringBuilder("سيتم حذف كل البيانات الحالية في الجداول التالية نهائياً ثم استبدالها ببيانات الملف:\n\n");
                for (String t : tablesToBeErased(backupData)) msg.append("   • ").append(arabicTableName(t)).append("\n");
                msg.append("\nهل تريد المتابعة؟");
                if (!confirmDangerous(msg.toString(), "تأكيد الاستعادة")) return;

                // ── ملف قديم لا يحتوي على درجات الطلاب: تحذير قاطع قبل مسح الدرجات ──
                if (backupData.containsKey("subjects") && !backupData.containsKey("student_grades")) {
                    String warn = "تحذير خطير: الملف المختار لا يحتوي على قسم \"درجات الطلاب\".\n\n"
                            + "استعادة المواد الدراسية تمسح درجات جميع الطلاب، والمتابعة الآن ستحذف\n"
                            + "درجات كل الطلاب نهائياً ولن تكون هناك أي وسيلة لاستعادتها.\n\n"
                            + "يرجى إنشاء نسخة احتياطية جديدة تشمل \"درجات الطلاب\" قبل المتابعة.\n\n"
                            + "هل تريد المتابعة رغم ذلك ومسح كل الدرجات؟";
                    if (!confirmDangerous(warn, "تحذير: سيتم مسح درجات الطلاب")) return;
                }

                String summary = processRestore(backupData);
                JOptionPane.showMessageDialog(this, "تمت الاستعادة بنجاح!" + summary, "نجاح", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** أسماء الجداول التي سيتم مسحها فعلياً بناءً على محتوى الملف (بنفس ترتيب الحذف). */
    private List<String> tablesToBeErased(Map<String, List<Map<String, Object>>> data) {
        List<String> t = new ArrayList<>();
        // حذف المواد الدراسية يستلزم حذف درجات الطلاب أولاً
        if (data.containsKey("subjects")) { t.add("student_grades"); t.add("subjects"); }
        if (data.containsKey("centers")) t.add("centers");
        if (data.containsKey("professions")) t.add("professions");
        if (data.containsKey("specializations")) t.add("specializations");
        if (data.containsKey("professional_groups")) t.add("professional_groups");
        if (data.containsKey("regions")) t.add("regions");
        return t;
    }

    private String arabicTableName(String table) {
        switch (table) {
            case "regions": return "المناطق";
            case "centers": return "المراكز";
            case "professional_groups": return "المجموعات المهنية";
            case "professions": return "المهن";
            case "specializations": return "التخصصات";
            case "subjects": return "المواد الدراسية";
            case "student_grades": return "درجات الطلاب";
            default: return table;
        }
    }

    /** تأكيد لعملية خطرة، الاختيار الافتراضي هو "لا". */
    private boolean confirmDangerous(String message, String title) {
        Object[] options = { "لا، إلغاء", "نعم، متابعة" };
        int res = JOptionPane.showOptionDialog(this, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        return res == 1;
    }

    private String processRestore(Map<String, List<Map<String, Object>>> data) throws Exception {
        // ── Dependency checks ──
        if (data.containsKey("professions") && !data.containsKey("professional_groups"))
            throw new Exception("لا يمكن استعادة 'المهن' بدون 'المجموعات المهنية'.\nيرجى إنشاء نسخة جديدة تشمل كلا الجدولين.");
        if (data.containsKey("specializations") && !data.containsKey("professional_groups"))
            throw new Exception("لا يمكن استعادة 'التخصصات' بدون 'المجموعات المهنية'.\nيرجى إنشاء نسخة جديدة تشمل كلا الجدولين.");
        if (data.containsKey("centers") && !data.containsKey("regions"))
            throw new Exception("لا يمكن استعادة 'المراكز' بدون 'المناطق'.\nيرجى إنشاء نسخة جديدة تشمل كلا الجدولين.");
        if (data.containsKey("student_grades") && !data.containsKey("subjects"))
            throw new Exception("لا يمكن استعادة 'درجات الطلاب' بدون 'المواد الدراسية'.\nيرجى إنشاء نسخة جديدة تشمل كلا الجدولين.");

        String summary = "";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // ── Delete children first, then parents ──
                if (data.containsKey("subjects")) {
                    exec(conn, "DELETE FROM student_grades");
                    exec(conn, "DELETE FROM subjects");
                }
                if (data.containsKey("centers"))       exec(conn, "DELETE FROM centers");
                if (data.containsKey("professions"))   exec(conn, "DELETE FROM professions");
                if (data.containsKey("specializations")) exec(conn, "DELETE FROM specializations");
                if (data.containsKey("professional_groups")) exec(conn, "DELETE FROM professional_groups");
                if (data.containsKey("regions"))       exec(conn, "DELETE FROM regions");

                // ── ID remapping tables (old backup ID → new DB-generated ID) ──
                Map<Long, Long> regionIdMap        = new HashMap<>();
                Map<Long, Long> profGroupIdMap     = new HashMap<>();
                Map<Long, Long> specializationIdMap = new HashMap<>();
                Map<Long, Long> professionIdMap    = new HashMap<>();

                // ── Insert parents and build ID maps ──
                if (data.containsKey("regions")) {
                    insertWithIdRemap(conn, "regions", data.get("regions"));
                    buildIdMap(conn, "SELECT id, name FROM regions",
                               data.get("regions"), "id", "name", regionIdMap);
                }

                if (data.containsKey("professional_groups")) {
                    insertWithIdRemap(conn, "professional_groups", data.get("professional_groups"));
                    buildIdMap(conn, "SELECT id, name FROM professional_groups",
                               data.get("professional_groups"), "id", "name", profGroupIdMap);
                }

                // ── Insert specializations (references professional_groups) ──
                if (data.containsKey("specializations")) {
                    List<Map<String, Object>> rows = remapFKs(data.get("specializations"),
                        new String[]{"professional_group_id"}, new Map[]{profGroupIdMap});
                    insertWithIdRemap(conn, "specializations", rows);
                    buildIdMap(conn, "SELECT id, name FROM specializations",
                               data.get("specializations"), "id", "name", specializationIdMap);
                }

                // ── Insert professions (references professional_groups) ──
                if (data.containsKey("professions")) {
                    List<Map<String, Object>> rows = remapFKs(data.get("professions"),
                        new String[]{"professional_group_id"}, new Map[]{profGroupIdMap});
                    insertWithIdRemap(conn, "professions", rows);
                    buildIdMap(conn, "SELECT id, name FROM professions",
                               data.get("professions"), "id", "name", professionIdMap);
                }

                // ── Insert centers (references regions) ──
                if (data.containsKey("centers")) {
                    List<Map<String, Object>> rows = remapFKs(data.get("centers"),
                        new String[]{"region_id"}, new Map[]{regionIdMap});
                    insertWithIdRemap(conn, "centers", rows);
                }

                // ── Insert subjects (may reference specializations and/or professions) ──
                Map<Long, Long> subjectIdMap = new HashMap<>();
                if (data.containsKey("subjects")) {
                    List<Map<String, Object>> rows = remapFKs(data.get("subjects"),
                        new String[]{"specialization_id", "profession_id"},
                        new Map[]{specializationIdMap, professionIdMap});
                    insertWithIdRemap(conn, "subjects", rows);
                    buildSubjectIdMap(conn, rows, subjectIdMap);
                    // إعادة ربط المواد المركّبة (30/70) بأرقامها الجديدة
                    remapParentSubjects(conn, rows, subjectIdMap);
                }

                // ── Insert student grades (subject_id remapped, student matched by seat/national no) ──
                if (data.containsKey("student_grades")) {
                    int[] r = restoreStudentGrades(conn, data.get("student_grades"), subjectIdMap);
                    summary = "\n\nتمت استعادة " + r[0] + " درجة.";
                    if (r[1] > 0) summary += "\nتم تجاهل " + r[1] + " درجة (لم يتم العثور على الطالب أو المادة في قاعدة البيانات الحالية).";
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return summary;
    }

    /**
     * بناء خريطة (رقم المادة القديم → الجديد).
     * الجدول تم إفراغه قبل الإدراج، لذا ترتيب الأرقام الجديدة هو نفس ترتيب الإدراج.
     * وإن اختلف العدد لأي سبب نرجع للمطابقة بالاسم (الاسم + الاسم الفرعي + النوع).
     */
    private void buildSubjectIdMap(Connection conn, List<Map<String, Object>> backupRows,
                                    Map<Long, Long> idMap) throws SQLException {
        if (backupRows == null || backupRows.isEmpty()) return;

        List<Long> newIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM subjects ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) newIds.add(rs.getLong(1));
        }

        if (newIds.size() == backupRows.size()) {
            for (int i = 0; i < backupRows.size(); i++) {
                Long oldId = asLong(backupRows.get(i).get("id"));
                if (oldId != null) idMap.put(oldId, newIds.get(i));
            }
            return;
        }

        // Fallback: مطابقة بالمفتاح المركّب
        Map<String, Long> keyToOldId = new HashMap<>();
        for (Map<String, Object> row : backupRows) {
            Long oldId = asLong(row.get("id"));
            if (oldId != null) keyToOldId.put(subjectKey(row), oldId);
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM subjects");
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                Long oldId = keyToOldId.get(subjectKey(row));
                Long newId = asLong(row.get("id"));
                if (oldId != null && newId != null) idMap.put(oldId, newId);
            }
        }
    }

    private String subjectKey(Map<String, Object> row) {
        return norm(row.get("name")) + "|" + norm(row.get("sub_name")) + "|"
             + norm(row.get("type")) + "|" + norm(row.get("specialization_id"));
    }

    /** إعادة ربط parent_subject_id بالأرقام الجديدة (وإفراغه لو المرجع مفقود). */
    private void remapParentSubjects(Connection conn, List<Map<String, Object>> backupRows,
                                      Map<Long, Long> subjectIdMap) throws SQLException {
        if (backupRows == null || backupRows.isEmpty() || subjectIdMap.isEmpty()) return;
        boolean hasCol = false;
        for (Map<String, Object> row : backupRows) {
            if (row.containsKey("parent_subject_id")) { hasCol = true; break; }
        }
        if (!hasCol) return;
        if (!columnTypes(conn, "subjects").containsKey("parent_subject_id")) return;

        try (PreparedStatement ps = conn.prepareStatement("UPDATE subjects SET parent_subject_id = ? WHERE id = ?")) {
            int n = 0;
            for (Map<String, Object> row : backupRows) {
                Long oldParent = asLong(row.get("parent_subject_id"));
                if (oldParent == null) continue;
                Long newChild = subjectIdMap.get(asLong(row.get("id")));
                if (newChild == null) continue;
                Long newParent = subjectIdMap.get(oldParent);
                if (newParent != null) ps.setLong(1, newParent); else ps.setNull(1, Types.NUMERIC);
                ps.setLong(2, newChild);
                ps.addBatch();
                n++;
            }
            if (n > 0) ps.executeBatch();
        }
    }

    /**
     * استعادة درجات الطلاب: يتم تحويل subject_id بالخريطة الجديدة، ويتم التعرف على
     * الطالب برقم الجلوس أو الرقم القومي (جدول الطلاب لا يُحذف ولا يُعاد إدراجه).
     * @return [عدد الدرجات المستعادة, عدد الدرجات المتجاهَلة]
     */
    private int[] restoreStudentGrades(Connection conn, List<Map<String, Object>> gradeRows,
                                        Map<Long, Long> subjectIdMap) throws SQLException {
        if (gradeRows == null || gradeRows.isEmpty()) return new int[]{0, 0};

        // مفاتيح التعرف على الطلاب الحاليين
        Map<String, Long> bySeat = new HashMap<>();
        Map<String, Long> byNational = new HashMap<>();
        Set<String> dupNational = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, seat_no, national_id FROM students");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong(1);
                String seat = rs.getString(2);
                String nat = rs.getString(3);
                if (seat != null && !seat.trim().isEmpty()) bySeat.put(seat.trim(), id);
                if (nat != null && !nat.trim().isEmpty()) {
                    String k = nat.trim();
                    if (byNational.containsKey(k)) { dupNational.add(k); byNational.remove(k); }
                    else if (!dupNational.contains(k)) byNational.put(k, id);
                }
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int skipped = 0;
        for (Map<String, Object> row : gradeRows) {
            Long newSubjectId = subjectIdMap.get(asLong(row.get("subject_id")));
            Long studentId = null;
            String seat = norm(row.get("student_seat_no"));
            String nat = norm(row.get("student_national_id"));
            if (!seat.isEmpty()) studentId = bySeat.get(seat);
            if (studentId == null && !nat.isEmpty()) studentId = byNational.get(nat);

            Object mark = row.get("obtained_mark");
            if (newSubjectId == null || studentId == null || mark == null) { skipped++; continue; }
            if (!seen.add(studentId + "|" + newSubjectId)) { skipped++; continue; }

            Map<String, Object> g = new HashMap<>();
            g.put("student_id", studentId);
            g.put("subject_id", newSubjectId);
            // BigDecimal حفاظاً على الكسور العشرية في الدرجة
            g.put("obtained_mark", mark instanceof Number
                    ? new java.math.BigDecimal(mark.toString())
                    : new java.math.BigDecimal(norm(mark)));
            out.add(g);
        }

        insertWithIdRemap(conn, "student_grades", out);
        return new int[]{ out.size(), skipped };
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return new java.math.BigDecimal(v.toString().trim()).longValue(); }
        catch (Exception e) { return null; }
    }

    /** تحويل القيمة إلى نص موحّد للمقارنة (الأرقام الصحيحة بدون كسور). */
    private String norm(Object v) {
        if (v == null) return "";
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) return Long.toString((long) d);
            return Double.toString(d);
        }
        return v.toString().trim();
    }

    /** أنواع أعمدة الجدول (اسم العمود بحروف صغيرة → java.sql.Types). */
    private Map<String, Integer> columnTypes(Connection conn, String table) {
        Map<String, Integer> types = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE 1=0");
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++)
                types.put(meta.getColumnName(i).toLowerCase(), meta.getColumnType(i));
        } catch (SQLException ignore) {
            // لو تعذّر قراءة الأعمدة نكمل بدون فلترة
        }
        return types;
    }

    /** Execute a simple statement (no params). */
    private void exec(Connection conn, String sql) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement(sql)) { s.executeUpdate(); }
    }

    /**
     * Insert rows WITHOUT the 'id' column — let Oracle's sequence/trigger generate it.
     * @param skipCols column names to exclude from INSERT (e.g., "id")
     */
    private void insertWithIdRemap(Connection conn, String tableName,
                                    List<Map<String, Object>> rows,
                                    String... skipCols) throws SQLException {
        if (rows == null || rows.isEmpty()) return;
        Set<String> skip = new HashSet<>();
        for (String s : skipCols) skip.add(s.toLowerCase());
        // Don't skip "id" — we want Oracle to decide. But we DO skip it here.
        // Actually we skip nothing unless it's "id" (which is not in skipCols param here).
        // Re-use skip for the actual columns to exclude.
        skip.clear();
        skip.add("id"); // always exclude the PK so Oracle generates a fresh one

        // أعمدة الجدول الفعلية في قاعدة البيانات (للتعرف على النوع وتجاهل أي عمود غير موجود)
        Map<String, Integer> dbTypes = columnTypes(conn, tableName);

        // اتحاد أعمدة كل الصفوف — لا يصح الاعتماد على الصف الأول وحده،
        // فقد يكون عموده فارغاً (NULL) أو ناقصاً في ملف قديم.
        Set<String> colSet = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            for (String col : row.keySet()) {
                String lc = col.toLowerCase();
                if (skip.contains(lc)) continue;
                if (!dbTypes.isEmpty() && !dbTypes.containsKey(lc)) continue; // عمود غير موجود بالجدول
                colSet.add(col);
            }
        }
        List<String> cols = new ArrayList<>(colSet);
        if (cols.isEmpty()) return;

        String sql = "INSERT INTO " + tableName + " (" + String.join(",", cols) + ") VALUES (" +
                     String.join(",", Collections.nCopies(cols.size(), "?")) + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, Object> row : rows) {
                int i = 1;
                for (String col : cols) {
                    Object v = row.get(col);
                    if (v == null) {
                        Integer t = dbTypes.get(col.toLowerCase());
                        ps.setNull(i++, t != null ? t.intValue() : Types.VARCHAR);
                    } else if (v instanceof Double) {
                        Double d = (Double) v;
                        String lc = col.toLowerCase();
                        if (lc.contains("id") || lc.contains("order") || lc.contains("mark") || lc.contains("serial")) {
                            ps.setLong(i++, d.longValue());
                        } else ps.setObject(i++, v);
                    } else ps.setObject(i++, v);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * After inserting a parent table, build a map of old backup ID → new DB ID,
     * matching by the 'nameCol' field.
     */
    private void buildIdMap(Connection conn, String selectSql,
                             List<Map<String, Object>> backupRows,
                             String idCol, String nameCol,
                             Map<Long, Long> idMap) throws SQLException {
        // Build name → old_id from backup
        Map<String, Long> nameToOldId = new HashMap<>();
        for (Map<String, Object> row : backupRows) {
            Object idVal = row.get(idCol);
            Object nameVal = row.get(nameCol);
            if (idVal != null && nameVal != null) {
                long oldId = idVal instanceof Double ? ((Double) idVal).longValue() : ((Number) idVal).longValue();
                nameToOldId.put(nameVal.toString(), oldId);
            }
        }
        // Query new IDs from DB and map old → new
        try (PreparedStatement ps = conn.prepareStatement(selectSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long newId = rs.getLong(1);
                String name = rs.getString(2);
                Long oldId = nameToOldId.get(name);
                if (oldId != null) idMap.put(oldId, newId);
            }
        }
    }

    /**
     * Replace FK column values in rows using the provided remapping maps.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> remapFKs(List<Map<String, Object>> rows,
                                                String[] fkCols, Map<Long, Long>[] maps) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> newRow = new HashMap<>(row);
            for (int i = 0; i < fkCols.length; i++) {
                Object v = newRow.get(fkCols[i]);
                if (v != null) {
                    Long oldId = asLong(v);
                    Long newId = oldId != null ? maps[i].get(oldId) : null;
                    // لو لم نجد الرقم الجديد نُفرغ الحقل — الرقم القديم يشير الآن لسجل مختلف تماماً
                    newRow.put(fkCols[i], newId);
                }
            }
            result.add(newRow);
        }
        return result;
    }

    private void insertTable(Connection conn, String tableName, List<Map<String, Object>> rows) throws SQLException {
        insertWithIdRemap(conn, tableName, rows);
    }
}
