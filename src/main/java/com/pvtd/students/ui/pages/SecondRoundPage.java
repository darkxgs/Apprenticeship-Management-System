package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.services.DictionaryService;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * صفحة مستقلة للدور الثاني.
 * تعرض فقط طلاب الدور الثاني والمؤجلين المسموح لهم بالدخول (وكذلك الناجحين في الدور
 * الثاني بعد رصد الدرجات)، مع فلتر حسب المنطقة والمركز.
 */
public class SecondRoundPage extends JPanel {

    private final AppFrame parentFrame;
    private JComboBox<String> regionCombo, centerCombo;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private List<Student> currentStudents = new java.util.ArrayList<>();
    private final Map<String, String> centerCodeToNameMap = new java.util.HashMap<>();

    // الحالات اللي تظهر في صفحة الدور الثاني
    private static final List<String> STATUSES =
            java.util.Arrays.asList("دور ثاني", "مؤجل", "ناجح دور ثاني");

    public SecondRoundPage(AppFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 12));
        header.setBackground(UITheme.CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(18, 30, 18, 30)));

        JLabel title = new JLabel("الدور الثاني", SwingConstants.RIGHT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.EAST);

        JLabel sub = new JLabel("طلاب الدور الثاني والمؤجلين المسموح لهم بالدخول", SwingConstants.LEFT);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        header.add(sub, BorderLayout.WEST);

        // شريط الفلاتر
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        filterBar.setBackground(UITheme.CARD_BG);
        filterBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        regionCombo = new JComboBox<>();
        regionCombo.setFont(UITheme.FONT_BODY);
        regionCombo.setPreferredSize(new Dimension(180, 36));
        regionCombo.addItem("الكل");
        for (String r : DictionaryService.getCombinedItems(DictionaryService.CAT_REGION)) {
            regionCombo.addItem(r);
        }

        centerCombo = new JComboBox<>();
        centerCombo.setFont(UITheme.FONT_BODY);
        centerCombo.setPreferredSize(new Dimension(200, 36));
        centerCombo.addItem("الكل");

        regionCombo.addActionListener(e -> {
            centerCombo.removeAllItems();
            centerCombo.addItem("الكل");
            centerCodeToNameMap.clear();
            String selReg = (String) regionCombo.getSelectedItem();
            Map<String, String> centersMap;
            if (selReg == null || selReg.equals("الكل")) {
                centersMap = StudentService.getCentersWithCodes();
            } else {
                centersMap = StudentService.getCentersByRegionWithCodes(selReg);
            }
            for (Map.Entry<String, String> entry : centersMap.entrySet()) {
                centerCodeToNameMap.put(entry.getKey(), entry.getKey());
                centerCombo.addItem(entry.getKey());
            }
        });

        JButton btnLoad = new JButton("عرض الطلاب");
        btnLoad.setFont(UITheme.FONT_HEADER);
        btnLoad.setBackground(UITheme.PRIMARY);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoad.putClientProperty("JButton.buttonType", "roundRect");
        btnLoad.addActionListener(e -> loadStudents());

        filterBar.add(new JLabel("المنطقة:"));
        filterBar.add(regionCombo);
        filterBar.add(new JLabel("المركز:"));
        filterBar.add(centerCombo);
        filterBar.add(btnLoad);

        JPanel combined = new JPanel(new BorderLayout());
        combined.setOpaque(false);
        combined.add(header, BorderLayout.NORTH);
        combined.add(filterBar, BorderLayout.SOUTH);
        return combined;
    }

    private JScrollPane buildTable() {
        String[] columns = {"م", "الاسم", "رقم الجلوس", "رقم التسجيل", "المهنة", "الحالة", "الرقم السري"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentsTable = new JTable(tableModel);
        studentsTable.setFont(UITheme.FONT_BODY);
        studentsTable.setRowHeight(40);
        studentsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        studentsTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        studentsTable.getTableHeader().setFont(UITheme.FONT_HEADER);
        studentsTable.getTableHeader().setBackground(UITheme.PRIMARY);
        studentsTable.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer rtlRenderer = new DefaultTableCellRenderer();
        rtlRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 0; i < studentsTable.getColumnCount(); i++) {
            studentsTable.getColumnModel().getColumn(i).setCellRenderer(rtlRenderer);
        }

        JScrollPane scroll = new JScrollPane(studentsTable);
        scroll.setBorder(new EmptyBorder(8, 16, 0, 16));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        footer.setBackground(UITheme.CARD_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JLabel countLbl = new JLabel(" ");
        countLbl.setFont(UITheme.FONT_HEADER);
        countLbl.setForeground(UITheme.TEXT_SECONDARY);
        this.countLabel = countLbl;

        JButton btnEntry = new JButton("✏️ إدخال درجات الدور الثاني");
        btnEntry.setFont(UITheme.FONT_HEADER);
        btnEntry.setBackground(new Color(0x7C3AED));
        btnEntry.setForeground(Color.WHITE);
        btnEntry.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEntry.putClientProperty("JButton.buttonType", "roundRect");
        btnEntry.addActionListener(e -> {
            if (parentFrame != null) parentFrame.showPage(new DataEntryPage(parentFrame, true));
        });

        JButton btnReport = new JButton("🖨️ كشف الدور الثاني");
        btnReport.setFont(UITheme.FONT_HEADER);
        btnReport.setBackground(UITheme.PRIMARY);
        btnReport.setForeground(Color.WHITE);
        btnReport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReport.putClientProperty("JButton.buttonType", "roundRect");
        btnReport.addActionListener(e ->
                new com.pvtd.students.ui.pages.Report.ScoundRoundFramePage().setVisible(true));

        footer.add(countLbl);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(btnReport);
        footer.add(btnEntry);
        return footer;
    }

    private JLabel countLabel;

    private void loadStudents() {
        tableModel.setRowCount(0);
        currentStudents.clear();

        String center = (String) centerCombo.getSelectedItem();
        if (center == null || center.equals("الكل")) center = "الكل";
        String region = (String) regionCombo.getSelectedItem();

        currentStudents = StudentService.getStudentsByStatuses(region, center, STATUSES);

        int idx = 1;
        for (Student s : currentStudents) {
            tableModel.addRow(new Object[]{
                    idx++,
                    s.getName(),
                    s.getSeatNo(),
                    s.getRegistrationNo(),
                    s.getProfession(),
                    s.getStatus(),
                    s.getSecretNo() != null ? s.getSecretNo() : ""
            });
        }

        if (countLabel != null) {
            countLabel.setText("عدد الطلاب: " + currentStudents.size());
        }

        if (currentStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "لا يوجد طلاب دور ثاني أو مؤجلين مطابقين للفلتر المحدد.",
                    "معلومة", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
