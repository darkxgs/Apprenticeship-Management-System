package com.pvtd.students.ui.pages.Report;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.DictionaryService;
import com.pvtd.students.services.GradeCalculationService;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.ui.utils.ReportWorker;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * صفحة أوائل الطلاب:
 * فلترة الطلاب الناجحين بالمجموع النهائي (من/إلى) أو أعلى عدد معين،
 * ثم استخراج شهادة أوائل (صفحة لكل طالب) بنفس شكل نموذج الوزارة.
 */
public class TopStudentsFramePage extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(TopStudentsFramePage.class.getName());

    private com.pvtd.students.ui.components.Combobox<String> cmbRegion;
    private com.pvtd.students.ui.components.Combobox<String> cmbCenter;
    private com.pvtd.students.ui.components.Combobox<String> cmbProfession;
    private JTextField txtMinTotal;
    private JTextField txtMaxTotal;
    private JTextField txtMaxPossible;
    private JSpinner spnTopN;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblStatTotal;
    private JLabel lblStatSelected;

    private static final String ALL_REGIONS = "كل المناطق";
    private static final String ALL_CENTERS = "كل المراكز";
    private static final String ALL_PROFESSIONS = "كل التخصصات";

    /** بيانات الصفوف المعروضة حالياً بنفس ترتيب الجدول */
    private final List<RowData> displayedRows = new ArrayList<>();

    private static class RowData {
        int id;
        String name = "", seatNo = "", nationalId = "", profession = "", examSystem = "",
                professionalGroup = "", centerName = "", region = "", phone = "", imagePath = "";
        int total, maxTotal, rank;
        double percent;
    }

    public TopStudentsFramePage() {
        setTitle("أوائل الطلاب");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUi();
        loadRegions();
        loadProfessions();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // ================== UI ==================

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_LIGHT);

        root.add(buildHeader(), BorderLayout.PAGE_START);

        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(UITheme.BG_LIGHT);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainContent.add(createStatsPanel(), gbc);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainContent.add(createTableCard(), gbc);

        root.add(mainContent, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setSize(1200, 750);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(new Color(0, 102, 51));
        header.setPreferredSize(new Dimension(1100, 110));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);

        JLabel title = new JLabel("🏆 أوائل الطلاب");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        g.gridx = 6; g.gridy = 0; g.gridheight = 2;
        g.anchor = GridBagConstraints.EAST;
        header.add(title, g);
        g.gridheight = 1;

        cmbRegion = new com.pvtd.students.ui.components.Combobox<>();
        cmbRegion.setLabeText("المنطقة");
        cmbRegion.addActionListener(e -> onRegionChanged());
        g.gridx = 5; g.gridy = 0; g.ipadx = 120;
        header.add(cmbRegion, g);

        cmbCenter = new com.pvtd.students.ui.components.Combobox<>();
        cmbCenter.setLabeText("المركز");
        g.gridx = 4; g.gridy = 0; g.ipadx = 120;
        header.add(cmbCenter, g);

        cmbProfession = new com.pvtd.students.ui.components.Combobox<>();
        cmbProfession.setLabeText("التخصص");
        g.gridx = 3; g.gridy = 0; g.ipadx = 140;
        header.add(cmbProfession, g);
        g.ipadx = 0;

        // فلتر المجموع النهائي
        JPanel totalFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        totalFilter.setOpaque(false);
        txtMaxTotal = new JTextField(5);
        txtMinTotal = new JTextField(5);
        txtMaxPossible = new JTextField(5);
        spnTopN = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        ((JSpinner.DefaultEditor) spnTopN.getEditor()).getTextField().setColumns(3);

        totalFilter.add(whiteLabel("النهاية العظمى:"));
        totalFilter.add(txtMaxPossible);
        totalFilter.add(whiteLabel("المجموع من:"));
        totalFilter.add(txtMinTotal);
        totalFilter.add(whiteLabel("إلى:"));
        totalFilter.add(txtMaxTotal);
        totalFilter.add(whiteLabel("أعلى عدد (٠ = الكل):"));
        totalFilter.add(spnTopN);

        g.gridx = 3; g.gridy = 1; g.gridwidth = 3;
        g.anchor = GridBagConstraints.EAST;
        header.add(totalFilter, g);
        g.gridwidth = 1;

        // الأزرار
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton btnLoad = new JButton("🔄 عرض الطلاب");
        UITheme.styleButton(btnLoad, UITheme.PRIMARY, UITheme.HOVER_PRIMARY, UITheme.HOVER_PRIMARY.darker());
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFont(UITheme.FONT_HEADER);
        btnLoad.addActionListener(e -> loadStudents());
        actions.add(btnLoad);

        JButton btnSelectAll = new JButton("✅ اختيار الكل");
        UITheme.styleButton(btnSelectAll, UITheme.WARNING, UITheme.WARNING.darker(), UITheme.WARNING.darker());
        btnSelectAll.setForeground(Color.WHITE);
        btnSelectAll.setFont(UITheme.FONT_HEADER);
        btnSelectAll.addActionListener(e -> table.selectAll());
        actions.add(btnSelectAll);

        JButton btnPrint = new JButton("🏆 استخراج شهادات الأوائل");
        UITheme.styleButton(btnPrint, UITheme.SUCCESS, UITheme.SUCCESS.darker(), UITheme.SUCCESS.darker());
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(UITheme.FONT_HEADER);
        btnPrint.addActionListener(e -> printCertificates());
        actions.add(btnPrint);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 3; g.gridheight = 2;
        g.anchor = GridBagConstraints.WEST;
        header.add(actions, g);

        return header;
    }

    private JLabel whiteLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(UITheme.FONT_HEADER);
        return l;
    }

    private JPanel createStatsPanel() {
        JPanel statsContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        statsContainer.setBackground(UITheme.BG_LIGHT);

        lblStatTotal = new JLabel("0");
        lblStatSelected = new JLabel("0");

        statsContainer.add(createStatCard("الطلاب المعروضين", lblStatTotal, UITheme.PRIMARY));
        statsContainer.add(createStatCard("الطلاب المحددين", lblStatSelected, UITheme.SUCCESS));

        return statsContainer;
    }

    private JPanel createStatCard(String title, JLabel valLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BODY);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);

        valLabel.setFont(UITheme.FONT_CARD_TITLE);
        valLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);

        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(0, 3));
        bar.setBackground(accentColor);
        card.add(bar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createTableCard() {
        model = new DefaultTableModel(new Object[][] {},
                new String[] { "النسبة", "النهاية العظمى", "المجموع", "المنطقة", "المركز", "التخصص",
                        "رقم الجلوس", "الاسم", "الترتيب", "م" }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(UITheme.FONT_BODY);
        table.setShowGrid(true);
        table.setGridColor(UITheme.TABLE_GRID);
        table.setSelectionBackground(new Color(37, 99, 235, 40));
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.getTableHeader().setFont(UITheme.FONT_HEADER);

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBackground(new Color(241, 245, 249));
                setForeground(UITheme.TEXT_PRIMARY);
                setFont(UITheme.FONT_HEADER);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, UITheme.TABLE_GRID));
                return this;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(37, 99, 235, 40));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                c.setForeground(UITheme.TEXT_PRIMARY);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(
                e -> lblStatSelected.setText(String.valueOf(table.getSelectedRowCount())));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ================== الفلاتر ==================

    private void loadRegions() {
        cmbRegion.removeAllItems();
        cmbRegion.addItem(ALL_REGIONS);
        for (String r : DictionaryService.getCombinedItems(DictionaryService.CAT_REGION)) {
            cmbRegion.addItem(r);
        }
        onRegionChanged();
    }

    private void onRegionChanged() {
        if (cmbCenter == null) return;
        cmbCenter.removeAllItems();
        cmbCenter.addItem(ALL_CENTERS);
        Object sel = cmbRegion.getSelectedItem();
        if (sel == null || ALL_REGIONS.equals(sel.toString())) {
            for (String c : DictionaryService.getCombinedItems(DictionaryService.CAT_CENTER)) {
                cmbCenter.addItem(c);
            }
        } else {
            Map<String, String> centers = com.pvtd.students.services.StudentService
                    .getCentersByRegionWithCodes(sel.toString());
            for (String c : centers.keySet()) {
                cmbCenter.addItem(c);
            }
        }
    }

    private void loadProfessions() {
        cmbProfession.removeAllItems();
        cmbProfession.addItem(ALL_PROFESSIONS);
        for (String p : DictionaryService.getCombinedItems(DictionaryService.CAT_PROFESSION)) {
            cmbProfession.addItem(p);
        }
    }

    private Integer parseIntOrNull(String s) {
        try {
            String t = s == null ? "" : s.trim();
            if (t.isEmpty()) return null;
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ================== تحميل الطلاب ==================

    private void loadStudents() {
        final String region = selectedOrNull(cmbRegion, ALL_REGIONS);
        final String center = selectedOrNull(cmbCenter, ALL_CENTERS);
        final String profession = selectedOrNull(cmbProfession, ALL_PROFESSIONS);
        final Integer minTotal = parseIntOrNull(txtMinTotal.getText());
        final Integer maxTotal = parseIntOrNull(txtMaxTotal.getText());
        final Integer maxPossible = parseIntOrNull(txtMaxPossible.getText());
        final int topN = (Integer) spnTopN.getValue();

        new SwingWorker<List<RowData>, Void>() {
            @Override
            protected List<RowData> doInBackground() throws Exception {
                List<RowData> rows = new ArrayList<>();
                try (Connection con = DatabaseConnection.getConnection()) {

                    StringBuilder sql = new StringBuilder(
                            "SELECT s.id, s.name, s.seat_no, s.national_id, s.profession, s.exam_system,"
                                    + " s.professional_group, s.center_name, s.region, s.phone_number, s.image_path"
                                    + " FROM students s"
                                    + " WHERE s.status = 'ناجح'"
                                    + " AND NOT EXISTS (SELECT 1 FROM student_grades sg"
                                    + "                 WHERE sg.student_id = s.id AND sg.obtained_mark < 0)");
                    List<String> params = new ArrayList<>();
                    if (region != null) { sql.append(" AND s.region = ?"); params.add(region); }
                    if (center != null) { sql.append(" AND s.center_name = ?"); params.add(center); }
                    if (profession != null) { sql.append(" AND s.profession = ?"); params.add(profession); }

                    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                        for (int i = 0; i < params.size(); i++) {
                            ps.setString(i + 1, params.get(i));
                        }
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                RowData r = new RowData();
                                r.id = rs.getInt("id");
                                r.name = nz(rs.getString("name"));
                                r.seatNo = nz(rs.getString("seat_no"));
                                r.nationalId = nz(rs.getString("national_id"));
                                r.profession = nz(rs.getString("profession"));
                                r.examSystem = nz(rs.getString("exam_system"));
                                r.professionalGroup = nz(rs.getString("professional_group"));
                                r.centerName = nz(rs.getString("center_name"));
                                r.region = nz(rs.getString("region"));
                                r.phone = nz(rs.getString("phone_number"));
                                r.imagePath = nz(rs.getString("image_path"));
                                rows.add(r);
                            }
                        }
                    }

                    // تحميل الدرجات على دفعات
                    Map<Integer, Map<Integer, Integer>> gradesMap = new HashMap<>();
                    int batchSize = 500;
                    for (int i = 0; i < rows.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, rows.size());
                        List<RowData> batch = rows.subList(i, end);
                        StringBuilder sbG = new StringBuilder(
                                "SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN (");
                        for (int j = 0; j < batch.size(); j++) {
                            sbG.append("?");
                            if (j < batch.size() - 1) sbG.append(",");
                        }
                        sbG.append(")");
                        try (PreparedStatement psG = con.prepareStatement(sbG.toString())) {
                            for (int j = 0; j < batch.size(); j++) {
                                psG.setInt(j + 1, batch.get(j).id);
                            }
                            try (ResultSet rsG = psG.executeQuery()) {
                                while (rsG.next()) {
                                    gradesMap.computeIfAbsent(rsG.getInt("student_id"), k -> new HashMap<>())
                                            .put(rsG.getInt("subject_id"), rsG.getInt("obtained_mark"));
                                }
                            }
                        }
                    }

                    // حساب المجموع والنهاية العظمى بنفس منطق النظام (المواد المركبة + استبعاد الدين من العظمى)
                    Map<String, List<Subject>> subjectsCache = new HashMap<>();
                    for (RowData r : rows) {
                        List<Subject> subjects = subjectsCache.computeIfAbsent(r.profession,
                                SubjectService::getSubjectsByProfession);
                        Map<Integer, Integer> grades = gradesMap.getOrDefault(r.id, new HashMap<>());
                        int theory = GradeCalculationService.calculateTheoryTotal(subjects, grades);
                        int practical = GradeCalculationService.calculatePracticalTotal(subjects, grades);
                        int applied = GradeCalculationService.calculateAppliedTotal(subjects, grades);
                        r.total = GradeCalculationService.calculateGrandTotal(theory, practical, applied);
                        r.maxTotal = GradeCalculationService.calculateMaxPossibleTotal(subjects);
                        r.percent = r.maxTotal > 0 ? (r.total * 100.0 / r.maxTotal) : 0;
                    }
                }

                // فلتر النهاية العظمى وفلتر المجموع
                rows.removeIf(r -> (maxPossible != null && r.maxTotal != maxPossible)
                        || (minTotal != null && r.total < minTotal)
                        || (maxTotal != null && r.total > maxTotal));

                // ترتيب تنازلي بالمجموع
                rows.sort((a, b) -> Integer.compare(b.total, a.total));

                // أعلى N
                if (topN > 0 && rows.size() > topN) {
                    rows = new ArrayList<>(rows.subList(0, topN));
                }

                // الترتيب (الأول، الثاني...) — نفس المجموع = نفس الترتيب
                int rank = 0;
                int prevTotal = Integer.MIN_VALUE;
                for (RowData r : rows) {
                    if (r.total != prevTotal) {
                        rank++;
                        prevTotal = r.total;
                    }
                    r.rank = rank;
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<RowData> rows = get();
                    displayedRows.clear();
                    displayedRows.addAll(rows);
                    model.setRowCount(0);
                    int i = 1;
                    for (RowData r : rows) {
                        model.addRow(new Object[] {
                                String.format("%.2f", r.percent) + "%",
                                r.maxTotal,
                                r.total,
                                r.region,
                                r.centerName,
                                r.profession,
                                r.seatNo,
                                r.name,
                                rankWord(r.rank),
                                i++
                        });
                    }
                    lblStatTotal.setText(String.valueOf(rows.size()));
                    lblStatSelected.setText("0");
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TopStudentsFramePage.this,
                            "حدث خطأ أثناء تحميل الطلاب: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String selectedOrNull(JComboBox<String> combo, String allValue) {
        Object sel = combo.getSelectedItem();
        if (sel == null || allValue.equals(sel.toString())) return null;
        return sel.toString();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // ================== الطباعة ==================

    private void printCertificates() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int year = LocalDate.now().getYear();
        String defaultTitle = "أوائل شهادات الدبلومات الفنية " + toArabicNumbers(String.valueOf(year));
        String mainTitle = (String) JOptionPane.showInputDialog(this, "عنوان الشهادة:", "عنوان الشهادة",
                JOptionPane.QUESTION_MESSAGE, null, null, defaultTitle);
        if (mainTitle == null) return;

        final List<RowData> selected = new ArrayList<>();
        for (int row : selectedRows) {
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < displayedRows.size()) {
                selected.add(displayedRows.get(modelRow));
            }
        }

        final String title = mainTitle;
        ReportWorker worker = new ReportWorker(this, "شهادات أوائل الطلاب", null) {
            @Override
            protected Void doInBackground() throws Exception {

                File mainFolder = new File("التقارير");
                if (!mainFolder.exists()) mainFolder.mkdirs();
                File certFolder = new File(mainFolder, "الأوائل");
                if (!certFolder.exists()) certFolder.mkdirs();

                Document allDoc = new Document(PageSize.A4);
                String allPath = certFolder.getAbsolutePath() + File.separator + "شهادات الأوائل.pdf";
                PdfWriter.getInstance(allDoc, new FileOutputStream(allPath));
                allDoc.open();

                CertificatePanel panel = new CertificatePanel();
                panel.setSize(CertificatePanel.PAGE_W, CertificatePanel.PAGE_H);
                panel.setDoubleBuffered(false);

                BufferedImage image = new BufferedImage(CertificatePanel.PAGE_W, CertificatePanel.PAGE_H,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();

                int total = selected.size();
                for (int i = 0; i < total; i++) {
                    RowData r = selected.get(i);
                    updateStatus(i + 1, total, "جاري إنشاء شهادة: " + r.name);

                    panel.setData(title, r, rankWord(r.rank));

                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, CertificatePanel.PAGE_W, CertificatePanel.PAGE_H);
                    panel.printAll(g2);

                    // ملف لكل طالب داخل مجلد المركز
                    String centerName = r.centerName.isEmpty() ? "بدون مركز" : r.centerName;
                    centerName = centerName.replaceAll("[\\\\/:*?\"<>|]", "_");
                    File centerFolder = new File(certFolder, centerName);
                    if (!centerFolder.exists()) centerFolder.mkdirs();

                    String fileName = !r.nationalId.isEmpty() ? r.nationalId : "student_" + r.seatNo;
                    Document singleDoc = new Document(PageSize.A4);
                    PdfWriter.getInstance(singleDoc, new FileOutputStream(
                            centerFolder.getAbsolutePath() + File.separator + fileName + ".pdf"));
                    singleDoc.open();
                    Image imgSingle = Image.getInstance(image, null);
                    imgSingle.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                    imgSingle.setAbsolutePosition(0, 0);
                    singleDoc.add(imgSingle);
                    singleDoc.close();

                    // إضافة للملف المجمع
                    if (i > 0) allDoc.newPage();
                    Image imgAll = Image.getInstance(image, null);
                    imgAll.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                    imgAll.setAbsolutePosition(0, 0);
                    allDoc.add(imgAll);
                }

                g2.dispose();
                if (allDoc.isOpen()) allDoc.close();

                SwingUtilities.invokeLater(() -> {
                    try {
                        Desktop.getDesktop().open(certFolder);
                        Desktop.getDesktop().open(new File(allPath));
                        JOptionPane.showMessageDialog(TopStudentsFramePage.this, "تم إنشاء شهادات الأوائل بنجاح");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                return null;
            }
        };
        worker.start();
    }

    // ================== أدوات مساعدة ==================

    private static final String[] RANK_WORDS = {
            "الأول", "الثاني", "الثالث", "الرابع", "الخامس",
            "السادس", "السابع", "الثامن", "التاسع", "العاشر",
            "الحادي عشر", "الثاني عشر", "الثالث عشر", "الرابع عشر", "الخامس عشر",
            "السادس عشر", "السابع عشر", "الثامن عشر", "التاسع عشر", "العشرون"
    };

    private static String rankWord(int rank) {
        if (rank >= 1 && rank <= RANK_WORDS.length) {
            return RANK_WORDS[rank - 1];
        }
        return "المركز " + toArabicNumbers(String.valueOf(rank));
    }

    private static String toArabicNumbers(String number) {
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

    // ================== لوحة الشهادة (نفس شكل نموذج الوزارة) ==================

    private static class CertificatePanel extends JPanel {

        static final int PAGE_W = 1000;
        static final int PAGE_H = 1414;

        private static final Color TITLE_GREEN = new Color(0x2E, 0x8B, 0x2E);
        private static final Color RED = new Color(0xC0, 0x00, 0x00);
        private static final Color BAR_GREEN = new Color(146, 208, 80);
        private static final Color LABEL_GREEN = new Color(0xC6, 0xE0, 0xB4);
        private static final Color VALUE_BLUE = new Color(0xDE, 0xEA, 0xF6);
        private static final Color DARK_BLUE = new Color(0x1F, 0x38, 0x64);
        private static final Color MAROON = new Color(0x9C, 0x00, 0x06);
        private static final Color GREEN_TEXT = new Color(0x2E, 0x7D, 0x32);

        private String mainTitle = "", subTitle1 = "", subTitle2 = "", rankText = "";
        private String[] values = new String[9];
        private Color[] valueColors = new Color[9];
        private java.awt.Image photo;
        private final java.awt.Image logo;

        private static final String[] LABELS = {
                "الاســــــــم :", "رقم الجلـوس :", "المجمــوع :", "النهاية العظمى :", "النســـبة :",
                "التخصص :", "المدرســـة :", "المديريــــة :", "المحمـــول :"
        };

        CertificatePanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(PAGE_W, PAGE_H));
            java.awt.Image l = null;
            try {
                java.net.URL url = getClass().getResource("/icons/images-removebg-preview (2).png");
                if (url != null) l = new ImageIcon(url).getImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logo = l;
        }

        void setData(String title, RowData r, String rank) {
            this.mainTitle = title;
            this.subTitle1 = r.professionalGroup;
            this.subTitle2 = r.examSystem;
            this.rankText = rank;

            values[0] = r.name;
            values[1] = toArabicNumbers(r.seatNo);
            values[2] = toArabicNumbers(String.valueOf(r.total));
            values[3] = toArabicNumbers(String.valueOf(r.maxTotal));
            values[4] = toArabicNumbers(String.format("%.2f", r.percent)) + "%";
            values[5] = r.profession;
            values[6] = r.centerName;
            values[7] = r.region;
            values[8] = toArabicNumbers(r.phone);

            valueColors[0] = DARK_BLUE;   // الاسم
            valueColors[1] = MAROON;      // رقم الجلوس
            valueColors[2] = DARK_BLUE;   // المجموع
            valueColors[3] = DARK_BLUE;   // النهاية العظمى
            valueColors[4] = GREEN_TEXT;  // النسبة
            valueColors[5] = RED;         // التخصص
            valueColors[6] = DARK_BLUE;   // المدرسة/المركز
            valueColors[7] = GREEN_TEXT;  // المديرية/المنطقة
            valueColors[8] = DARK_BLUE;   // المحمول

            photo = null;
            try {
                if (r.imagePath != null && !r.imagePath.isEmpty()) {
                    File f = new File(r.imagePath);
                    if (f.exists()) {
                        photo = new ImageIcon(f.getAbsolutePath()).getImage();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, PAGE_W, PAGE_H);

            // شعار أعلى اليمين
            if (logo != null) {
                g2.drawImage(logo, PAGE_W - 200, 35, 150, 150, null);
            }

            // العنوان الرئيسي (أخضر)
            drawCentered(g2, mainTitle, PAGE_W / 2, 255, 840,
                    new Font("Segoe UI", Font.BOLD, 38), TITLE_GREEN, false);

            // المجموعة المهنية (أحمر بخط تحته)
            int y = 320;
            if (subTitle1 != null && !subTitle1.trim().isEmpty()) {
                drawCentered(g2, subTitle1, PAGE_W / 2, y, 800,
                        new Font("Segoe UI", Font.BOLD, 30), RED, true);
                y += 62;
            }
            // النظام (أحمر)
            if (subTitle2 != null && !subTitle2.trim().isEmpty()) {
                drawCentered(g2, subTitle2, PAGE_W / 2, y, 820,
                        new Font("Segoe UI", Font.BOLD, 26), RED, false);
                y += 58;
            }

            // البلوك الرئيسي
            int bx = 70;
            int bw = PAGE_W - 140;
            int by = Math.max(y + 20, 440);
            int barH = 58;
            int rowH = 66;
            int rowsCount = LABELS.length;
            int bodyH = rowH * rowsCount;
            int photoW = 270;
            int labelW = 195;

            // شريط الترتيب الأخضر
            g2.setColor(BAR_GREEN);
            g2.fillRect(bx, by, bw, barH);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(bx, by, bw, barH);
            drawCentered(g2, rankText, bx + bw / 2, by + barH / 2 + 12, bw - 40,
                    new Font("Segoe UI", Font.BOLD, 30), Color.BLACK, false);

            int bodyY = by + barH;
            int valueX = bx + photoW;
            int valueW = bw - photoW - labelW;
            int labelX = bx + bw - labelW;

            // الصفوف
            for (int i = 0; i < rowsCount; i++) {
                int ry = bodyY + i * rowH;

                // خلية القيمة
                g2.setColor(i % 2 == 1 ? VALUE_BLUE : Color.WHITE);
                g2.fillRect(valueX, ry, valueW, rowH);
                // خلية التسمية
                g2.setColor(i % 2 == 1 ? LABEL_GREEN : Color.WHITE);
                g2.fillRect(labelX, ry, labelW, rowH);

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(valueX, ry, valueW, rowH);
                g2.drawRect(labelX, ry, labelW, rowH);

                drawCentered(g2, LABELS[i], labelX + labelW / 2, ry + rowH / 2 + 9, labelW - 14,
                        new Font("Segoe UI", Font.BOLD, 23), DARK_BLUE, false);
                String v = values[i] != null ? values[i] : "";
                Color vc = valueColors[i] != null ? valueColors[i] : DARK_BLUE;
                drawCentered(g2, v, valueX + valueW / 2, ry + rowH / 2 + 9, valueW - 24,
                        new Font("Segoe UI", Font.BOLD, 26), vc, false);
            }

            // خلية الصورة (بارتفاع كل الصفوف)
            g2.setColor(Color.WHITE);
            g2.fillRect(bx, bodyY, photoW, bodyH);
            g2.setColor(Color.BLACK);
            g2.drawRect(bx, bodyY, photoW, bodyH);

            int phW = 220, phH = 300;
            int phX = bx + (photoW - phW) / 2;
            int phY = bodyY + 25;
            if (photo != null) {
                g2.drawImage(photo, phX, phY, phW, phH, null);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(phX, phY, phW, phH);
            } else {
                g2.setColor(new Color(240, 240, 240));
                g2.fillRect(phX, phY, phW, phH);
                g2.setColor(Color.GRAY);
                g2.drawRect(phX, phY, phW, phH);
                drawCentered(g2, "لا توجد صورة", phX + phW / 2, phY + phH / 2, phW - 10,
                        new Font("Segoe UI", Font.BOLD, 18), Color.GRAY, false);
            }

            // الإطار الخارجي للبلوك
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(bx, by, bw, barH + bodyH);
        }

        /** يرسم نصاً في المنتصف مع تصغير الخط تلقائياً ليناسب العرض المتاح */
        private void drawCentered(Graphics2D g2, String text, int cx, int baselineY, int maxW,
                Font font, Color color, boolean underline) {
            if (text == null || text.isEmpty()) return;
            Font f = font;
            FontMetrics fm = g2.getFontMetrics(f);
            while (fm.stringWidth(text) > maxW && f.getSize() > 10) {
                f = f.deriveFont((float) (f.getSize() - 1));
                fm = g2.getFontMetrics(f);
            }
            g2.setFont(f);
            g2.setColor(color);
            int w = fm.stringWidth(text);
            int x = cx - w / 2;
            g2.drawString(text, x, baselineY);
            if (underline) {
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(x, baselineY + 6, x + w, baselineY + 6);
            }
        }
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        EventQueue.invokeLater(() -> new TopStudentsFramePage().setVisible(true));
    }
}
