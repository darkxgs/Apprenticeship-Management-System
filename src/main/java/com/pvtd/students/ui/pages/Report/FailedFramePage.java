package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.pvtd.students.ui.utils.ReportWorker;
import com.pvtd.students.ui.utils.ReportUtils;

import com.pvtd.students.models.Student;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class FailedFramePage extends javax.swing.JFrame {

    private com.pvtd.students.ui.utils.ReportFilterPanel filterPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FailedFramePage.class.getName());

    /** Caches the exam_system fetched from loadStudents — used by both reports */
    private String loadedSystemName = "";

    private javax.swing.JLabel lblStatTotal;
    private javax.swing.JLabel lblStatCenter;
    private javax.swing.JLabel lblStatRegion;
    private javax.swing.JPanel statsContainer;

    /**
     * "كشف الراسبين بالدرجات" logic.
     * Generates a PDF (gradReportFailMixed) which is A3 Landscape, 
     * showing Theory, Practical, Applied columns plus Failed Subjects.
     */
    private void buttonSecretReportActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalSelected = selectedRows.length;
        
        String[] filters = filterPanel != null ? filterPanel.getSelectedMonths() : new String[]{"", "", "", "", "", ""};
        if (filters == null || filters.length < 6) return;
        final String selMonth = filters[4];
        final String admMonth = filters[5];

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = combobox1.getSelectedItem() != null ? combobox1.getSelectedItem().toString() : "";
        final String systemName = loadedSystemName;

        ReportWorker worker = new ReportWorker(this, "كشف الراسبين بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                // We'll store students grouped by region to matching gradReportSequential logic if needed,
                // or just one list for gradReportFailMixed.
                // Keeping it grouped to create separate PDFs per region in a combined folder.
                java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> allStudentsByRegion = new java.util.LinkedHashMap<>();

                try (Connection con = DatabaseConnection.getConnection()) {
                    // 1. Gather all selected seat numbers
                    java.util.List<String> seatNumbers = new java.util.ArrayList<>();
                    for (int row : selectedRows) {
                        int modelRow = jTable1.convertRowIndexToModel(row);
                        seatNumbers.add(String.valueOf(model1.getValueAt(modelRow, 1)));
                    }

                    if (seatNumbers.isEmpty()) return null;

                    // 2. Batch fetch students
                    java.util.Map<String, com.pvtd.students.models.Student> studentMap = new java.util.HashMap<>();
                    java.util.Map<String, String> studentRegions = new java.util.HashMap<>();
                    java.util.List<Integer> studentIds = new java.util.ArrayList<>();

                    int batchSize = 500;
                    for (int i = 0; i < seatNumbers.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, seatNumbers.size());
                        java.util.List<String> batch = seatNumbers.subList(i, end);
                        
                        StringBuilder sb = new StringBuilder("SELECT s.id, s.name, s.registration_no, s.seat_no, s.status, s.national_id, s.profession, s.professional_group, s.secret_no, s.coordination_no, s.region, s.exam_system " +
                                     "FROM students s " +
                                     "WHERE s.seat_no IN (");
                        for (int j = 0; j < batch.size(); j++) {
                            sb.append("?");
                            if (j < batch.size() - 1) sb.append(",");
                        }
                        sb.append(")");

                        try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
                            for (int j = 0; j < batch.size(); j++) {
                                ps.setString(j + 1, batch.get(j));
                            }
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    com.pvtd.students.models.Student st = new com.pvtd.students.models.Student();
                                    st.setId(rs.getInt("id"));
                                    st.setName(rs.getString("name"));
                                    st.setRegistrationNo(rs.getString("registration_no"));
                                    st.setSeatNo(rs.getString("seat_no"));
                                    st.setStatus(rs.getString("status"));
                                    st.setNationalId(rs.getString("national_id"));
                                    st.setProfessionalGroup(rs.getString("professional_group"));
                                    String sys = rs.getString("exam_system");
                                    if (sys == null) sys = "";
                                    st.setExamSystem(sys);
                                    st.setCoordinationNo(rs.getString("coordination_no"));
                                    st.setSecretNo(rs.getString("secret_no"));
                                    st.setProfession(rs.getString("profession"));
                                    
                                    String r = rs.getString("region");
                                    if (r == null || r.trim().isEmpty()) r = regionName;
                                    st.setRegion(r);
                                    
                                    studentMap.put(st.getSeatNo(), st);
                                    studentRegions.put(st.getSeatNo(), r);
                                    studentIds.add(st.getId());
                                }
                            }
                        }
                    }

                    // 3. Batch fetch grades
                    java.util.Map<Integer, java.util.Map<Integer, Integer>> gradesMap = new java.util.HashMap<>();
                    if (!studentIds.isEmpty()) {
                        for (int i = 0; i < studentIds.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, studentIds.size());
                            java.util.List<Integer> batchIds = studentIds.subList(i, end);
                            
                            StringBuilder sbG = new StringBuilder("SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN (");
                            for (int j = 0; j < batchIds.size(); j++) {
                                sbG.append("?");
                                if (j < batchIds.size() - 1) sbG.append(",");
                            }
                            sbG.append(")");
                            
                            try (PreparedStatement psG = con.prepareStatement(sbG.toString())) {
                                for (int j = 0; j < batchIds.size(); j++) {
                                    psG.setInt(j + 1, batchIds.get(j));
                                }
                                try (ResultSet rsG = psG.executeQuery()) {
                                    while (rsG.next()) {
                                        int sid = rsG.getInt("student_id");
                                        int subid = rsG.getInt("subject_id");
                                        int mark = rsG.getInt("obtained_mark");
                                        gradesMap.computeIfAbsent(sid, k -> new java.util.HashMap<>()).put(subid, mark);
                                    }
                                }
                            }
                        }
                    }

                    // 4. Assemble final list in original selection order, grouped by Region and Exam System
                    // Map structure: regionName -> (systemName -> List<Student>)
                    java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>>> studentsByRegionAndSystem = new java.util.LinkedHashMap<>();

                    int total = seatNumbers.size();
                    for (int i = 0; i < total; i++) {
                        if (i % 50 == 0 || i == total - 1) {
                            updateStatus((i + 1) * 20 / total, 100, "جاري تحضير البيانات: " + (i + 1) + "/" + total);
                        }
                        
                        String seatNo = seatNumbers.get(i);
                        com.pvtd.students.models.Student st = studentMap.get(seatNo);
                        if (st != null) {
                            st.setGrades(gradesMap.getOrDefault(st.getId(), new java.util.HashMap<>()));
                            String region = studentRegions.get(seatNo);
                            String system = st.getExamSystem();
                            if (system == null) system = "";

                            studentsByRegionAndSystem
                                .computeIfAbsent(region, k -> new java.util.LinkedHashMap<>())
                                .computeIfAbsent(system, k -> new java.util.ArrayList<>())
                                .add(st);
                        }
                    }

                    // Generate files (20-100%)
                    generatePdfFiles(studentsByRegionAndSystem, centerName, selMonth, admMonth, "التقارير/تبييضة/راسبين_درجات", "جاري إنشاء التقارير", 20, 100);
                }

                java.io.File folder = new java.io.File("التقارير/تبييضة/راسبين_درجات");
                java.awt.Desktop.getDesktop().open(folder);
                return null;
            }

            private void generatePdfFiles(java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>>> data, 
                                        String centerName, String selM, String admM, String folderPath,
                                        String label, int startPct, int endPct) throws Exception {
                java.io.File folder = new java.io.File(folderPath);
                if (!folder.exists()) folder.mkdirs();

                if (centerName == null) centerName = "مركز_غير_محدد";
                String sanitizedCenter = centerName.replace("/", "_").replace("\\", "_").replace(":", "_");
                if (sanitizedCenter.isEmpty()) sanitizedCenter = "مركز_غير_محدد";
                String combinedFn = folderPath + "/" + sanitizedCenter + ".pdf";

                com.itextpdf.text.Document combinedDoc = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
                combinedDoc.open();

                // 1. Aggregating all students into one center-wide list
                java.util.List<com.pvtd.students.models.Student> fullCenterStudents = new java.util.ArrayList<>();
                String mainRegion = "";

                // Use the first available key from the outer map as mainRegion fallback
                for (java.util.Map.Entry<String, java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>>> rEntry : data.entrySet()) {
                    String rKey = rEntry.getKey();
                    if (mainRegion == null || mainRegion.isEmpty()) {
                        mainRegion = (rKey != null && !rKey.isEmpty()) ? rKey : "";
                    }
                    for (java.util.List<com.pvtd.students.models.Student> sysList : rEntry.getValue().values()) {
                        fullCenterStudents.addAll(sysList);
                        if (!sysList.isEmpty() && (mainRegion == null || mainRegion.isEmpty())) {
                            String r = sysList.get(0).getRegion();
                            if (r != null && !r.isEmpty()) mainRegion = r;
                        }
                    }
                }

                if (!fullCenterStudents.isEmpty()) {
                    updateStatus(startPct + (endPct - startPct) / 2, 100, label + ": جاري إنشاء الملف الموحد للمركز");

                    // 2. Sort all students by System then Seat Number
                    fullCenterStudents.sort((s1, s2) -> {
                        String sys1 = s1.getExamSystem() != null ? s1.getExamSystem() : "";
                        String sys2 = s2.getExamSystem() != null ? s2.getExamSystem() : "";
                        int sysComp = sys1.compareTo(sys2);
                        if (sysComp != 0) return sysComp;

                        String sn1 = s1.getSeatNo() != null ? s1.getSeatNo().trim() : "";
                        String sn2 = s2.getSeatNo() != null ? s2.getSeatNo().trim() : "";
                        String sn1Clean = sn1.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                        String sn2Clean = sn2.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                        if (!sn1Clean.isEmpty() && !sn2Clean.isEmpty()) {
                            try { return Long.compare(Long.parseLong(sn1Clean), Long.parseLong(sn2Clean)); } catch (Exception ex) {}
                        }
                        return sn1.compareTo(sn2);
                    });

                    // 3. One single report containing everyone
                    // Swapped from gradReportFailMixed to the corrected gradReportFail class
                    gradReportFail report = new gradReportFail(centerName, mainRegion, fullCenterStudents, selM, admM);
                    report.appendToDocument(combinedDoc);
                }

                combinedDoc.close();
                updateStatus(endPct, 100, "تم الانتهاء بنجاح");
            }
        };
        worker.start();
    }

    public FailedFramePage() {
        initComponents();
        modernizeComponents();
        initDashboardLayout();
        loadRegions();
        setupTableUi();

        setTitle("تقرير الراسبين");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                String center = (String) cmdcenter.getSelectedItem();
                String region = (String) combobox1.getSelectedItem();
                if (center != null && region != null && !center.startsWith("اختر") && !region.startsWith("اختر")) {
                    loadStudents(center, region);
                }
            }
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {}
        });
        
        filterPanel.addFilterChangeListener(e -> cmdcenterActionPerformed(null));
        
        jTable1.getSelectionModel().addListSelectionListener(e -> updateStats());
    }

    private void updateStats() {
        if (lblStatTotal == null) return;
        
        int totalInDb = jTable1.getRowCount(); 
        int selected = jTable1.getSelectedRowCount();
        
        lblStatTotal.setText(String.valueOf(totalInDb));
        lblStatFiltered.setText(String.valueOf(totalInDb)); 
        lblStatSelected.setText(String.valueOf(selected));
    }

    private javax.swing.JLabel lblStatFiltered;
    private javax.swing.JLabel lblStatSelected;

    private void initDashboardLayout() {
        jPanel1.removeAll();
        jPanel1.setBackground(UITheme.BG_LIGHT);
        
        javax.swing.JPanel mainContent = new javax.swing.JPanel(new java.awt.GridBagLayout());
        mainContent.setBackground(UITheme.BG_LIGHT);
        mainContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(0, 0, 20, 0);
        mainContent.add(createStatsPanel(), gbc);
        
        gbc.gridy = 1;
        mainContent.add(createFilterCard(), gbc);
        
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        mainContent.add(createTableCard(), gbc);
        
        jPanel1.add(mainContent, java.awt.BorderLayout.CENTER);
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private javax.swing.JPanel createStatsPanel() {
        statsContainer = new javax.swing.JPanel(new java.awt.GridLayout(1, 3, 20, 0));
        statsContainer.setBackground(UITheme.BG_LIGHT);
        
        lblStatTotal = new javax.swing.JLabel("0");
        lblStatFiltered = new javax.swing.JLabel("0");
        lblStatSelected = new javax.swing.JLabel("0");
        
        statsContainer.add(createStatCard("إجمالي الطلاب", lblStatTotal, new java.awt.Color(220, 38, 38))); 
        statsContainer.add(createStatCard("الطلاب المعروضين", lblStatFiltered, new java.awt.Color(185, 28, 28))); 
        statsContainer.add(createStatCard("الطلاب المحددين", lblStatSelected, new java.awt.Color(153, 27, 27))); 
        
        return statsContainer;
    }

    private javax.swing.JPanel createStatCard(String title, javax.swing.JLabel valLabel, java.awt.Color accentColor) {
        javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.BorderLayout(10, 5));
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        javax.swing.JLabel titleLbl = new javax.swing.JLabel(title);
        titleLbl.setFont(UITheme.FONT_BODY);
        titleLbl.setForeground(UITheme.TEXT_SECONDARY);
        
        valLabel.setFont(UITheme.FONT_CARD_TITLE);
        valLabel.setForeground(accentColor);
        
        card.add(titleLbl, java.awt.BorderLayout.NORTH);
        card.add(valLabel, java.awt.BorderLayout.CENTER);
        
        javax.swing.JPanel bar = new javax.swing.JPanel();
        bar.setPreferredSize(new java.awt.Dimension(0, 3));
        bar.setBackground(accentColor);
        card.add(bar, java.awt.BorderLayout.SOUTH);
        
        return card;
    }

    private javax.swing.JPanel createFilterCard() {
        javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.BorderLayout());
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        jPanel2.setBackground(java.awt.Color.WHITE);
        jPanel2.setBorder(null);
        
        filterPanel = new com.pvtd.students.ui.utils.ReportFilterPanel();
        filterPanel.setBackground(java.awt.Color.WHITE);
        
        card.add(jPanel2, java.awt.BorderLayout.NORTH);
        card.add(filterPanel, java.awt.BorderLayout.SOUTH);
        
        return card;
    }

    private javax.swing.JPanel createTableCard() {
        javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.BorderLayout());
        card.setBackground(java.awt.Color.WHITE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        jScrollPane1.setBorder(null);
        card.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        
        return card;
    }

    private void modernizeComponents() {
        jPanel2.setBackground(java.awt.Color.WHITE);
        jLabel1.setFont(UITheme.FONT_CARD_TITLE);
        jLabel1.setForeground(UITheme.TEXT_PRIMARY);
        
        UITheme.styleButton(jButton1, new java.awt.Color(37, 99, 235), new java.awt.Color(29, 78, 216), new java.awt.Color(30, 64, 175));
        jButton1.setFont(UITheme.FONT_HEADER);
        jButton1.setForeground(java.awt.Color.WHITE);
        
        buttonGradient3.setText("📄 عرض كشف الراسبين");
        buttonGradient3.setColor1(new java.awt.Color(220, 38, 38));
        buttonGradient3.setColor2(new java.awt.Color(153, 27, 27));
        buttonGradient3.setFont(UITheme.FONT_HEADER);
        buttonGradient3.setRadius(20);
        
        buttonGradient1.setText("📊 كشف بالدرجات");
        buttonGradient1.setColor1(new java.awt.Color(127, 29, 29));
        buttonGradient1.setColor2(new java.awt.Color(69, 10, 10));
        buttonGradient1.setFont(UITheme.FONT_HEADER);
        buttonGradient1.setRadius(20);

        jButton1.setText("✅ اختيار الكل");
    }

    private void setupTableUi() {
        if (jTable1 == null) return;
        jTable1.setRowHeight(40);
        jTable1.setFont(UITheme.FONT_BODY);
        
        jTable1.setShowGrid(true);
        jTable1.setGridColor(UITheme.TABLE_GRID);
        jTable1.setIntercellSpacing(new java.awt.Dimension(1, 1));
        
        jTable1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = jTable1.rowAtPoint(e.getPoint());
                if (row != (int)jTable1.getClientProperty("hoveredRow")) {
                    jTable1.putClientProperty("hoveredRow", row);
                    jTable1.repaint();
                }
            }
        });
        jTable1.putClientProperty("hoveredRow", -1);

        jTable1.getTableHeader().setFont(UITheme.FONT_HEADER);
        jTable1.getTableHeader().setBackground(java.awt.Color.WHITE);
        jTable1.getTableHeader().setForeground(UITheme.TEXT_PRIMARY);
        jTable1.setSelectionBackground(new java.awt.Color(37, 99, 235, 40));
        jTable1.setSelectionForeground(UITheme.TEXT_PRIMARY);
        
        jTable1.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new java.awt.Color(241, 245, 249));
                setForeground(UITheme.TEXT_PRIMARY);
                setFont(UITheme.FONT_HEADER);
                setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 1, UITheme.TABLE_GRID),
                    javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5)
                ));
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return this;
            }
        });

        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int hr = (int) table.getClientProperty("hoveredRow");
                if (isSelected) {
                    c.setBackground(new java.awt.Color(37, 99, 235, 40));
                } else if (row == hr) {
                    c.setBackground(new java.awt.Color(226, 232, 240));
                } else {
                    c.setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(248, 250, 252));
                }
                c.setForeground(UITheme.TEXT_PRIMARY);
                setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 1, UITheme.TABLE_GRID),
                    javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8)
                ));
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return c;
            }
        });
    }

    public void loadRegions() {
        combobox1.removeAllItems();
        combobox1.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            combobox1.addItem(r);
        }
    }

    public void loadStudents(String center, String region) {
        new javax.swing.SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                java.util.List<Object[]> data = new java.util.ArrayList<>();
                try (Connection con = DatabaseConnection.getConnection()) {
                    String sql = "SELECT name, profession, registration_no, seat_no, status, national_id, coordination_no, exam_system "
                            + "FROM students "
                            + "WHERE center_name = ? "
                            + "AND region = ? "
                            + "AND status = 'راسب' "
                            + "ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, center);
                        ps.setString(2, region);
                        try (ResultSet rs = ps.executeQuery()) {
                            int i = 1;
                            while (rs.next()) {
                                loadedSystemName = rs.getString("exam_system");
                                data.add(new Object[]{
                                    rs.getString("status"),
                                    rs.getString("seat_no"),
                                    rs.getString("coordination_no"),
                                    rs.getString("national_id"),
                                    rs.getString("registration_no"),
                                    rs.getString("profession"),
                                    rs.getString("name"),
                                    i++
                                });
                            }
                        }
                    }
                }
                return data;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                    model.setRowCount(0);
                    java.util.List<Object[]> data = get();
                    for (Object[] row : data) {
                        model.addRow(row);
                    }
                    updateStats();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("اختر المركز...");
        java.util.Map<String, String> centers = com.pvtd.students.services.StudentService.getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) {
            cmdcenter.addItem(c);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        combobox1 = new com.pvtd.students.ui.components.Combobox();
        buttonGradient3 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel2.setBackground(new java.awt.Color(139, 0, 0));
        jPanel2.setPreferredSize(new java.awt.Dimension(1091, 80));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        // 1. Title Label
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("تقارير الطلاب الراسبين");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4; gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        jPanel2.add(jLabel1, gridBagConstraints);

        // 2. ComboBoxes
        combobox1.setLabeText("المنطقة");
        combobox1.addActionListener(this::combobox1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3; gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 130;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(combobox1, gridBagConstraints);

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 130;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cmdcenter, gridBagConstraints);

        // 3. Actions Panel
        javax.swing.JPanel actionsPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 0));
        actionsPanel.setOpaque(false);
        
        jButton1.setText("✅ اختيار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton1.setPreferredSize(new java.awt.Dimension(130, 40));
        actionsPanel.add(jButton1);
        
        buttonGradient3.setText("📄 عرض كشف");
        buttonGradient3.addActionListener(this::buttonGradient3ActionPerformed);
        buttonGradient3.setPreferredSize(new java.awt.Dimension(160, 40));
        actionsPanel.add(buttonGradient3);
        
        buttonGradient1.setText("📊 كشف بالدرجات");
        buttonGradient1.addActionListener(this::buttonSecretReportActionPerformed);
        buttonGradient1.setPreferredSize(new java.awt.Dimension(160, 40));
        actionsPanel.add(buttonGradient1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actionsPanel, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {{null, null, null, null, null, null, null, null}},
            new String [] {"حالة التلميذ", "رقم الجلوس ", "كود التنسيق", "الرقم القومي", "رقم التسجيل", "المهنة", "الاسم", "م"}
        ));
        jScrollPane1.setViewportView(jTable1);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE));
        pack();
    }// </editor-fold>

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter.getSelectedItem() != null && combobox1.getSelectedItem() != null) {
            String center = cmdcenter.getSelectedItem().toString();
            String region = combobox1.getSelectedItem().toString();
            if (center.equals("اختر المركز...") || region.equals("اختر المنطقة...")) {
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                updateStats();
                return;
            }
            loadStudents(center, region);
        }
    }

    /** "كشف الراسبين" logic without grades (A4 portrait) */
    private void buttonGradient3ActionPerformed(java.awt.event.ActionEvent evt) {                                                
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = combobox1.getSelectedItem() != null ? combobox1.getSelectedItem().toString() : "";
        String[] months = filterPanel != null ? filterPanel.getSelectedMonths() : new String[]{"", "", "", "", "", ""};
        if (months == null || months.length < 6) return;
        Failed report = new Failed(months[4], months[5]);
        if (report.isCancelled) return;

        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        ReportWorker worker = new ReportWorker(this, "كشف الطلاب الراسبين", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(10, 100, "جاري تحضير البيانات...");
                // Sort systems by name, and rows within each system by seat number
                java.util.TreeMap<String, java.util.List<java.util.Vector>> sortedBySystem = new java.util.TreeMap<>((s1, s2) -> {
                    if (s1 == null) return -1;
                    if (s2 == null) return 1;
                    return s1.compareTo(s2);
                });

                try (java.sql.Connection con = com.pvtd.students.db.DatabaseConnection.getConnection()) {
                    for (int i = 0; i < selectedRows.length; i++) {
                        int r = jTable1.convertRowIndexToModel(selectedRows[i]);
                        String seatNo = String.valueOf(model1.getValueAt(r, 1) != null ? model1.getValueAt(r, 1) : "");
                        
                        String systemNameFromDb = "";
                        try (java.sql.PreparedStatement ps = con.prepareStatement("SELECT s.exam_system FROM students s WHERE s.seat_no = ?")) {
                            ps.setString(1, seatNo);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    systemNameFromDb = rs.getString("exam_system");
                                    if (systemNameFromDb == null) systemNameFromDb = "";
                                }
                            }
                        }
                        
                        java.util.Vector rowData = new java.util.Vector();
                        rowData.add(model1.getValueAt(r, 0)); rowData.add(model1.getValueAt(r, 1)); rowData.add(model1.getValueAt(r, 4));
                        rowData.add(String.valueOf(model1.getValueAt(r, 5)).trim()); 
                        rowData.add(String.valueOf(model1.getValueAt(r, 6)).trim()); 
                        rowData.add(0); // placeholder for index
                        sortedBySystem.computeIfAbsent(systemNameFromDb, k -> new java.util.ArrayList<>()).add(rowData);
                    }
                }

                // Sort students within each system numerically by seat number (يدعم الأرقام العربية مثل الكشف الكبير)
                for (java.util.List<java.util.Vector> rows : sortedBySystem.values()) {
                    rows.sort((v1, v2) -> {
                        String sn1 = String.valueOf(v1.get(1)).trim();
                        String sn2 = String.valueOf(v2.get(1)).trim();
                        String sn1C = sn1.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                        String sn2C = sn2.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                        if (!sn1C.isEmpty() && !sn2C.isEmpty()) {
                            try { return Long.compare(Long.parseLong(sn1C), Long.parseLong(sn2C)); } catch (Exception e) {}
                        }
                        return sn1.compareTo(sn2);
                    });
                }

                // Re-calculate global index after sorting
                java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystemSorted = new java.util.LinkedHashMap<>();
                int gIdx = 1;
                for (java.util.Map.Entry<String, java.util.List<java.util.Vector>> entry : sortedBySystem.entrySet()) {
                    for (java.util.Vector v : entry.getValue()) {
                        v.set(5, gIdx++);
                    }
                    bySystemSorted.put(entry.getKey(), entry.getValue());
                }

                updateStatus(60, 100, "جاري إنشاء ملف PDF...");
                report.createPDFGroupedBySystem(bySystemSorted, centerName, regionName, true);
                return null;
            }
        };
        worker.start();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) { jTable1.selectAll(); }

    private void combobox1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (combobox1.getSelectedItem() != null) {
            String region = combobox1.getSelectedItem().toString();
            if (region.equals("اختر المنطقة...")) {
                cmdcenter.removeAllItems();
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                updateStats();
                return;
            }
            loadCenters(region);
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) { logger.log(java.util.logging.Level.SEVERE, null, ex); }
        java.awt.EventQueue.invokeLater(() -> new FailedFramePage().setVisible(true));
    }

    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient3;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox combobox1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
}
