package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.services.DictionaryService;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * صفحة إنشاء ومراجعة الأرقام السرية
 */
public class SecretNumberPage extends JPanel {

    private final AppFrame parentFrame;
    private JComboBox<String> regionCombo, centerCombo;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    private List<Student> currentStudents = new ArrayList<>();
    private Map<String, String> centerCodeToNameMap = new HashMap<>();

    public SecretNumberPage(AppFrame parent) {
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

        JLabel title = new JLabel("إنشاء الرقم السري", SwingConstants.RIGHT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.EAST);

        JLabel sub = new JLabel("مراجعة وإنشاء الأرقام السرية للطلاب", SwingConstants.LEFT);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        header.add(sub, BorderLayout.WEST);

        // Filter bar
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
                String displayLabel = entry.getValue().equals(entry.getKey()) ? entry.getKey() : "كود: " + entry.getValue();
                centerCodeToNameMap.put(displayLabel, entry.getKey());
                centerCombo.addItem(displayLabel);
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
        String[] columns = {"م", "الاسم", "رقم الجلوس", "المنطقة", "المركز", "الرقم السري الحالي", "الرقم السري الجديد"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only new secret number is editable
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

        JButton btnGenerate = new JButton("⚡ توليد تلقائي");
        btnGenerate.setFont(UITheme.FONT_HEADER);
        btnGenerate.setBackground(UITheme.SUCCESS);
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGenerate.putClientProperty("JButton.buttonType", "roundRect");
        btnGenerate.addActionListener(e -> generateSecretNumbers());

        JButton btnSave = new JButton("💾 حفظ الأرقام السرية");
        btnSave.setFont(UITheme.FONT_HEADER);
        btnSave.setBackground(UITheme.PRIMARY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.addActionListener(e -> saveSecretNumbers());

        JButton btnExport = new JButton("📥 تصدير Excel");
        btnExport.setFont(UITheme.FONT_BODY);
        btnExport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExport.putClientProperty("JButton.buttonType", "roundRect");
        btnExport.addActionListener(e -> exportToExcel());

        JButton btnImport = new JButton("📤 استيراد من Excel");
        btnImport.setFont(UITheme.FONT_BODY);
        btnImport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnImport.putClientProperty("JButton.buttonType", "roundRect");
        btnImport.addActionListener(e -> importFromExcel());

        footer.add(btnImport);
        footer.add(btnExport);
        footer.add(btnSave);
        footer.add(btnGenerate);

        return footer;
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        currentStudents.clear();

        String displayLabel = (String) centerCombo.getSelectedItem();
        String center = displayLabel == null || displayLabel.equals("الكل") ? "الكل" : centerCodeToNameMap.getOrDefault(displayLabel, displayLabel);
        String region = (String) regionCombo.getSelectedItem();

        currentStudents = StudentService.searchStudents("", "", "الكل", region, "الكل", "الكل", center);

        // Sort by center then seat number
        currentStudents.sort((a, b) -> {
            int c = String.valueOf(a.getCenterName()).compareTo(String.valueOf(b.getCenterName()));
            if (c != 0) return c;
            return String.valueOf(a.getSeatNo()).compareTo(String.valueOf(b.getSeatNo()));
        });

        int idx = 1;
        for (Student s : currentStudents) {
            tableModel.addRow(new Object[]{
                idx++,
                s.getName(),
                s.getSeatNo(),
                s.getRegion(),
                s.getCenterName(),
                s.getSecretNo() != null ? s.getSecretNo() : "",
                "" // New secret number (to be generated)
            });
        }
    }

    private void generateSecretNumbers() {
        if (currentStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى تحميل الطلاب أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < currentStudents.size(); i++) {
            Student s = currentStudents.get(i);
            String secret = com.pvtd.students.services.SecretNumberService.generateSecretNumber(s.getRegion(), s.getCenterName(), s.getSeatNo());
            tableModel.setValueAt(secret, i, 6);
        }

        JOptionPane.showMessageDialog(this, "تم توليد الأرقام السرية بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveSecretNumbers() {
        if (currentStudents.isEmpty()) return;

        int saved = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE students SET secret_no = ? WHERE id = ?")) {
            for (int i = 0; i < currentStudents.size(); i++) {
                String newSecret = (String) tableModel.getValueAt(i, 6);
                if (newSecret != null && !newSecret.trim().isEmpty()) {
                    ps.setString(1, newSecret.trim());
                    ps.setInt(2, currentStudents.get(i).getId());
                    ps.addBatch();
                    saved++;
                }
            }
            ps.executeBatch();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Log action
        String username = parentFrame != null ? parentFrame.getLoggedInUser().getUsername() : "SYSTEM";
        com.pvtd.students.services.LogService.logAction(username, "GENERATE_SECRET", "تم حفظ " + saved + " رقم سري");

        JOptionPane.showMessageDialog(this, "تم حفظ " + saved + " رقم سري بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
        loadStudents(); // Reload to show updated data
    }

    private void exportToExcel() {
        if (currentStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى تحميل الطلاب أولاً.", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("حفظ ملف Excel");
        chooser.setSelectedFile(new java.io.File("SecretNumbers.xlsx"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.xssf.usermodel.XSSFSheet sheet = wb.createSheet("الأرقام السرية");
                sheet.setRightToLeft(true);

                org.apache.poi.xssf.usermodel.XSSFRow header = sheet.createRow(0);
                String[] headers = {"م", "الاسم", "الرقم القومي", "رقم الجلوس", "المنطقة", "المركز", "الرقم السري"};
                for (int i = 0; i < headers.length; i++) {
                    header.createCell(i).setCellValue(headers[i]);
                }

                int rowIdx = 1;
                for (Student s : currentStudents) {
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rowIdx - 1);
                    row.createCell(1).setCellValue(s.getName() != null ? s.getName() : "");
                    row.createCell(2).setCellValue(s.getNationalId() != null ? s.getNationalId() : "");
                    row.createCell(3).setCellValue(s.getSeatNo() != null ? s.getSeatNo() : "");
                    row.createCell(4).setCellValue(s.getRegion() != null ? s.getRegion() : "");
                    row.createCell(5).setCellValue(s.getCenterName() != null ? s.getCenterName() : "");
                    row.createCell(6).setCellValue(s.getSecretNo() != null ? s.getSecretNo() : "");
                }

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(chooser.getSelectedFile())) {
                    wb.write(fos);
                }
                wb.close();
                JOptionPane.showMessageDialog(this, "تم التصدير بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importFromExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("اختر ملف Excel (الرقم القومي + الرقم السري)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx", "xls"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(chooser.getSelectedFile());
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = wb.getSheetAt(0);

            int updated = 0;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE students SET secret_no = ? WHERE national_id = ?")) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    org.apache.poi.xssf.usermodel.XSSFRow row = sheet.getRow(i);
                    if (row == null) continue;

                    String nationalId = getCellString(row, 0);
                    String secretNo = getCellString(row, 1);

                    if (nationalId != null && !nationalId.isEmpty() && secretNo != null && !secretNo.isEmpty()) {
                        ps.setString(1, secretNo.trim());
                        ps.setString(2, nationalId.trim());
                        ps.addBatch();
                        updated++;
                    }
                }
                ps.executeBatch();
            }
            wb.close();

            String username = parentFrame != null ? parentFrame.getLoggedInUser().getUsername() : "SYSTEM";
            com.pvtd.students.services.LogService.logAction(username, "IMPORT_SECRET", "تم استيراد " + updated + " رقم سري من Excel");

            JOptionPane.showMessageDialog(this, "تم استيراد " + updated + " رقم سري بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            loadStudents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCellString(org.apache.poi.xssf.usermodel.XSSFRow row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }
}
