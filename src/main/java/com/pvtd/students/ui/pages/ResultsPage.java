package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.ExcelService;
import com.pvtd.students.services.GradeCalculationService;
import com.pvtd.students.services.StatusesService;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.DropShadowBorder;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.StatusBadgeRenderer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ResultsPage extends JPanel {

    private final AppFrame parentFrame;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;
    private JLabel countLabel;

    // Column definitions: [header, width]
    private static final Object[][] COLS = {
            { "الاسم", 250 },
            { "رقم التسجيل", 120 },
            { "الرقم القومي", 150 },
            { "التخصص", 150 },
            { "المجموعة المهنية", 160 },
            { "المركز", 160 },
            { "المنطقة", 120 },
            { "تك ومقايسات", 110 },
            { "رسم", 90 },
            { "ميكانيكا", 90 },
            { "إنجليزي", 90 },
            { "مجموع نظري", 110 },
            { "العملي", 90 },
            { "التطبيقي", 90 },
            { "مجموع نظري وعملي", 140 },
            { "حالة الطالب", 120 },
            { "النسبة", 100 },
            { "التقدير", 100 }
    };

    public ResultsPage(AppFrame frame) {
        this.parentFrame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_LIGHT);

        add(buildTopSection(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
        
        loadData();
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

        JLabel title = new JLabel("النتيجة", SwingConstants.RIGHT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        // Stat chips
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        statsRow.setOpaque(false);
        
        countLabel = makeStatChip("--", "إجمالي المعروض", new Color(0xDBEAFE), new Color(0x1D4ED8));
        statsRow.add(countLabel);

        titleRow.add(title, BorderLayout.EAST);
        titleRow.add(statsRow, BorderLayout.WEST);
        top.add(titleRow, BorderLayout.NORTH);

        // Filter Bar ────────────────────────────────────────────────────────
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        filterCard.setOpaque(true);
        filterCard.setBackground(UITheme.CARD_BG);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 4, 0.05f, 14, UITheme.BG_LIGHT),
                new EmptyBorder(4, 12, 4, 12)));
        filterCard.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        statusFilterCombo = new JComboBox<>();
        statusFilterCombo.setFont(UITheme.FONT_BODY);
        statusFilterCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        statusFilterCombo.setPreferredSize(new Dimension(180, 36));
        
        statusFilterCombo.addItem("الكل");
        for (String s : StatusesService.getAllStatuses()) {
            statusFilterCombo.addItem(s);
        }

        JButton btnSearch = new JButton("تطبيق الفلتر");
        btnSearch.setFont(UITheme.FONT_HEADER);
        btnSearch.setBackground(UITheme.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSearch.putClientProperty("JButton.buttonType", "roundRect");
        btnSearch.setPreferredSize(new Dimension(110, 36));
        btnSearch.addActionListener(e -> loadData());
        
        JButton btnExport = new JButton("تصدير إكسيل");
        btnExport.setFont(UITheme.FONT_HEADER);
        btnExport.setBackground(new Color(0x10B981)); // Emerald 500
        btnExport.setForeground(Color.WHITE);
        btnExport.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnExport.putClientProperty("JButton.buttonType", "roundRect");
        btnExport.setPreferredSize(new Dimension(130, 36));
        btnExport.addActionListener(e -> exportToExcel());

        JLabel filterLabel = new JLabel("تصفية حسب الحالة:", SwingConstants.RIGHT);
        filterLabel.setFont(UITheme.FONT_BODY);
        filterLabel.setForeground(UITheme.TEXT_SECONDARY);

        // Visual order from right to left
        filterCard.add(btnExport);
        filterCard.add(Box.createHorizontalStrut(15));
        filterCard.add(btnSearch);
        filterCard.add(statusFilterCombo);
        filterCard.add(filterLabel);

        top.add(filterCard, BorderLayout.SOUTH);
        return top;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TABLE SECTION
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildTableSection() {
        String[] headers = new String[COLS.length];
        for (int i = 0; i < COLS.length; i++) {
            headers[i] = (String) COLS[i][0];
        }

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        resultsTable.setRowHeight(42);
        resultsTable.setFont(UITheme.FONT_BODY);
        resultsTable.setShowGrid(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 0));
        resultsTable.setSelectionBackground(new Color(0xDBEAFE));
        resultsTable.setSelectionForeground(UITheme.TEXT_PRIMARY);
        resultsTable.setFillsViewportHeight(true);

        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(UITheme.FONT_HEADER);
        header.setBackground(new Color(0xF1F5F9));
        header.setForeground(new Color(0x475569));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xE2E8F0)));
        header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        header.setPreferredSize(new Dimension(0, 46));
        header.setReorderingAllowed(false);

        for (int i = 0; i < COLS.length; i++) {
            int w = (int) COLS[i][1];
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(w);
            resultsTable.getColumnModel().getColumn(i).setMinWidth(w);
        }

        // Custom row renderer
        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color STRIPE_ODD = new Color(0xF8FAFC);

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, foc, row, col);

                if (sel) {
                    c.setBackground(t.getSelectionBackground());
                    c.setForeground(t.getSelectionForeground());
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : STRIPE_ODD);
                    c.setForeground(UITheme.TEXT_PRIMARY);
                }

                setHorizontalAlignment(SwingConstants.CENTER); // Center data for result marks
                if (col == 0) {
                    setHorizontalAlignment(SwingConstants.RIGHT); // Name should be right-aligned
                }

                setBorder(new EmptyBorder(0, 10, 0, 10));
                setFont(UITheme.FONT_BODY);
                if (value == null || value.toString().isEmpty()) {
                    setText("—");
                    setForeground(new Color(0xCBD5E1));
                }
                return c;
            }
        });
        
        // Status Badge for "حالة الطالب" column (Index 15)
        resultsTable.getColumnModel().getColumn(15).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(resultsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
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
        wrapper.setBorder(new EmptyBorder(10, 24, 24, 24));
        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

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

    private void loadData() {
        tableModel.setRowCount(0);
        
        String selectedStatus = (String) statusFilterCombo.getSelectedItem();
        if (selectedStatus != null && selectedStatus.equals("الكل")) {
            selectedStatus = null;
        }

        List<Student> students = StudentService.searchStudents(null, null, "الكل", "الكل", "الكل", selectedStatus != null ? selectedStatus : "الكل", "الكل");

        for (Student s : students) {
            List<Subject> subjects = SubjectService.getSubjectsByProfession(s.getProfession());
            Map<Integer, Integer> resolvedGrades = GradeCalculationService.resolveCompositeGrades(subjects, s.getGrades());

            String techGrade = "-";
            String drawGrade = "-";
            String mechGrade = "-";
            String engGrade = "-";

            int theoryIdx = 0;

            for (Subject sub : subjects) {
                if (sub.getParentSubjectId() != null) continue; // Only check top level
                
                int gradeVal = resolvedGrades.getOrDefault(sub.getId(), -1);
                String displayGrade = gradeVal >= 0 ? String.valueOf(gradeVal) : (gradeVal == -1 ? "غ" : "-");

                String type = sub.getType() != null ? sub.getType().trim() : "نظري";
                if (!type.equals("عملي") && !type.equals("تطبيقي")) {
                    // It's a theory subject
                    if (theoryIdx == 0) techGrade = displayGrade;
                    else if (theoryIdx == 1) drawGrade = displayGrade;
                    else if (theoryIdx == 2) mechGrade = displayGrade;
                    else if (theoryIdx == 3) engGrade = displayGrade;
                    theoryIdx++;
                }
            }

            int theoryTotal = GradeCalculationService.calculateTheoryTotal(subjects, s.getGrades());
            int pracTotal = GradeCalculationService.calculatePracticalTotal(subjects, s.getGrades());
            int appTotal = GradeCalculationService.calculateAppliedTotal(subjects, s.getGrades());
            int theoPracTotal = theoryTotal + pracTotal;
            int grandTotal = GradeCalculationService.calculateGrandTotal(theoryTotal, pracTotal, appTotal);
            int maxTotal = GradeCalculationService.calculateMaxPossibleTotal(subjects);

            String percentage = "-";
            String rating = "-";

            if (maxTotal > 0 && grandTotal > 0) {
                double perc = ((double) grandTotal / maxTotal) * 100;
                percentage = String.format("%.2f%%", perc);
                rating = GradeCalculationService.calculateRating(grandTotal, maxTotal);
            }

            tableModel.addRow(new Object[]{
                    s.getName() != null ? s.getName() : "",
                    s.getRegistrationNo() != null ? s.getRegistrationNo() : "",
                    s.getNationalId() != null ? s.getNationalId() : "",
                    s.getProfession() != null ? s.getProfession() : "",
                    s.getProfessionalGroup() != null ? s.getProfessionalGroup() : "",
                    s.getCenterName() != null ? s.getCenterName() : "",
                    s.getRegion() != null ? s.getRegion() : "",
                    techGrade,
                    drawGrade,
                    mechGrade,
                    engGrade,
                    theoryTotal > 0 ? String.valueOf(theoryTotal) : "-",
                    pracTotal > 0 ? String.valueOf(pracTotal) : "-",
                    appTotal > 0 ? String.valueOf(appTotal) : "-",
                    theoPracTotal > 0 ? String.valueOf(theoPracTotal) : "-",
                    s.getStatus() != null ? s.getStatus() : "",
                    percentage,
                    rating
            });
        }
        
        countLabel.setText(String.valueOf(students.size()));

        // Scroll to the right edge to show the first RTL columns without manual scrolling
        SwingUtilities.invokeLater(() -> {
            Container parent = resultsTable.getParent();
            if (parent instanceof JViewport) {
                JScrollPane scrollPane = (JScrollPane) parent.getParent();
                JScrollBar hBar = scrollPane.getHorizontalScrollBar();
                hBar.setValue(hBar.getMaximum());
            }
        });
    }

    private void exportToExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "لا يوجد بيانات للتصدير", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("حفظ ملف Excel");
        fileChooser.setSelectedFile(new File("results.xlsx"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("النتيجة");
                sheet.setRightToLeft(true);

                // Create Header Row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < COLS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue((String) COLS[i][0]);
                }

                // Create Data Rows
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = tableModel.getValueAt(i, j);
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    }
                }

                // Auto-size columns
                for (int i = 0; i < COLS.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream out = new FileOutputStream(fileToSave)) {
                    workbook.write(out);
                }

                JOptionPane.showMessageDialog(this, "تم تصدير البيانات بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                Desktop.getDesktop().open(fileToSave);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التصدير: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
