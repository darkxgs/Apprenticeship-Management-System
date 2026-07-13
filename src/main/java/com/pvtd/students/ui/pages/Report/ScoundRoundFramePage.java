package com.pvtd.students.ui.pages.Report;

import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import com.itextpdf.text.pdf.PdfWriter;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.ReportWorker;
import com.pvtd.students.ui.utils.ReportUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ScoundRoundFramePage extends javax.swing.JFrame {

    private com.pvtd.students.ui.utils.ReportFilterPanel filterPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ScoundRoundFramePage.class.getName());

    private javax.swing.JLabel lblStatTotal;
    private javax.swing.JLabel lblStatFiltered;
    private javax.swing.JLabel lblStatSelected;
    private javax.swing.JPanel statsContainer;

    public ScoundRoundFramePage() {
        initComponents();
        modernizeComponents();
        initDashboardLayout();
        loadRegions();
        setupTableUi();

        setTitle("كشف طلاب الدور الثاني");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                refreshData();
            }
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {}
        });
        
        if (filterPanel != null) {
            filterPanel.addFilterChangeListener(e -> refreshData());
        }
        
        jTable1.getSelectionModel().addListSelectionListener(e -> updateStats());
    }

    private void refreshData() {
        String center = (String) cmdcenter.getSelectedItem();
        String region = (String) cmdcenter1.getSelectedItem();
        if (center != null && region != null && !center.startsWith("اختر") && !region.startsWith("اختر")) {
            loadStudents(center, region);
        } else {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            updateStats();
        }
    }

    private void updateStats() {
        if (lblStatTotal == null) return;
        int totalInDb = jTable1.getRowCount(); 
        int selected = jTable1.getSelectedRowCount();
        lblStatTotal.setText(String.valueOf(totalInDb));
        lblStatFiltered.setText(String.valueOf(totalInDb)); 
        lblStatSelected.setText(String.valueOf(selected));
    }

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
        
        statsContainer.add(createStatCard("إجمالي الطلاب", lblStatTotal, new java.awt.Color(37, 99, 235))); 
        statsContainer.add(createStatCard("الطلاب المعروضين", lblStatFiltered, new java.awt.Color(59, 130, 246))); 
        statsContainer.add(createStatCard("الطلاب المحددين", lblStatSelected, new java.awt.Color(96, 165, 250))); 
        
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
        
        buttonGradient1.setText("📄 كشف بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(37, 99, 235));
        buttonGradient1.setColor2(new java.awt.Color(29, 78, 216));
        buttonGradient1.setFont(UITheme.FONT_HEADER);
        buttonGradient1.setRadius(20);
        
        buttonGradient2.setText("📊 كشف بالدرجات");
        buttonGradient2.setColor1(new java.awt.Color(13, 148, 136));
        buttonGradient2.setColor2(new java.awt.Color(15, 118, 110));
        buttonGradient2.setFont(UITheme.FONT_HEADER);
        buttonGradient2.addActionListener(this::buttonGradient2ActionPerformed);

        UITheme.styleButton(btnSelectAll, new java.awt.Color(37, 99, 235), new java.awt.Color(29, 78, 216), new java.awt.Color(30, 64, 175));
        btnSelectAll.setFont(UITheme.FONT_HEADER);
        btnSelectAll.setForeground(java.awt.Color.WHITE);
        btnSelectAll.setText("✅ اختيار الكل");
    }

    private void setupTableUi() {
        if (jTable1 == null) return;
        jTable1.setRowHeight(40);
        jTable1.setFont(UITheme.FONT_BODY);
        jTable1.setShowGrid(true);
        jTable1.setGridColor(UITheme.TABLE_GRID);
        
        jTable1.getTableHeader().setFont(UITheme.FONT_HEADER);
        jTable1.getTableHeader().setBackground(new java.awt.Color(241, 245, 249));
        jTable1.setSelectionBackground(new java.awt.Color(37, 99, 235, 40));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTable1.setDefaultRenderer(Object.class, centerRenderer);
    }

    public void loadRegions() {
        cmdcenter1.removeAllItems();
        cmdcenter1.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            cmdcenter1.addItem(r);
        }
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("اختر المركز...");
        if (region == null || region.trim().isEmpty() || region.startsWith("اختر")) {
            return;
        }

        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();

        // 1. Get from proper metadata (centers joined with regions)
        String sql = "SELECT c.name, c.code FROM centers c " +
                     "JOIN regions r ON c.region_id = r.id " +
                     "WHERE TRIM(r.name) = TRIM(?) OR " +
                     "      REPLACE(REPLACE(REPLACE(REPLACE(TRIM(r.name), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') = " +
                     "      REPLACE(REPLACE(REPLACE(REPLACE(TRIM(?), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') " +
                     "ORDER BY c.code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, region);
            stmt.setString(2, region);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String code = rs.getString("code");
                    map.put(name, (code != null && !code.trim().isEmpty()) ? code.trim() : name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Also retrieve centers implicitly mapped to this region in the students table (useful for imported Excel data)
        String sqlFallback = "SELECT DISTINCT center_name FROM students WHERE center_name IS NOT NULL AND " +
                             "(TRIM(region) = TRIM(?) OR " +
                             " REPLACE(REPLACE(REPLACE(REPLACE(TRIM(region), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') = " +
                             " REPLACE(REPLACE(REPLACE(REPLACE(TRIM(?), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا'))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlFallback)) {
            stmt.setString(1, region);
            stmt.setString(2, region);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String cName = rs.getString("center_name");
                    if (cName != null) {
                        cName = cName.replaceAll("(^[\\s\\xA0\\u200B\\p{Z}]+)|([\\s\\xA0\\u200B\\p{Z}]+$)", "");
                        if (!cName.isEmpty() && !map.containsKey(cName)) {
                            // Try to find if this implicit center has a code somehow
                            String codeSql = "SELECT code FROM centers WHERE TRIM(name) = TRIM(?) OR " +
                                             "REPLACE(REPLACE(REPLACE(REPLACE(TRIM(name), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') = " +
                                             "REPLACE(REPLACE(REPLACE(REPLACE(TRIM(?), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا')";
                            try (PreparedStatement cStmt = conn.prepareStatement(codeSql)) {
                                cStmt.setString(1, cName);
                                cStmt.setString(2, cName);
                                try (ResultSet crs = cStmt.executeQuery()) {
                                    if (crs.next()) {
                                        String code = crs.getString("code");
                                        map.put(cName, (code != null && !code.trim().isEmpty()) ? code.trim() : cName);
                                    } else {
                                        map.put(cName, cName); // Fallback code is name
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String c : map.keySet()) {
            cmdcenter.addItem(c);
        }
    }

    public void loadStudents(String center, String region) {
        new javax.swing.SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                java.util.List<Object[]> data = new java.util.ArrayList<>();
                try (Connection con = DatabaseConnection.getConnection()) {
                    // This is the corrected query with the status filter and robust Arabic spelling tolerance
                    String sql = "SELECT s.name, s.profession, s.registration_no, s.seat_no, s.status, s.coordination_no, s.exam_system "
                            + "FROM students s "
                            + "WHERE (TRIM(s.center_name) = TRIM(?) OR "
                            + "       REPLACE(REPLACE(REPLACE(REPLACE(TRIM(s.center_name), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') = "
                            + "       REPLACE(REPLACE(REPLACE(REPLACE(TRIM(?), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا')) "
                            + "AND (TRIM(s.region) = TRIM(?) OR "
                            + "     REPLACE(REPLACE(REPLACE(REPLACE(TRIM(s.region), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') = "
                            + "     REPLACE(REPLACE(REPLACE(REPLACE(TRIM(?), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا')) "
                            + "AND s.status = 'دور ثاني' "
                            + "ORDER BY CASE WHEN REGEXP_LIKE(s.seat_no, '^[0-9]+$') THEN TO_NUMBER(s.seat_no) ELSE 999999 END, s.id ASC";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, center);
                        ps.setString(2, center);
                        ps.setString(3, region);
                        ps.setString(4, region);
                        try (ResultSet rs = ps.executeQuery()) {
                            int i = 1;
                            while (rs.next()) {
                                String seatNo = rs.getString("seat_no");
                                String[] failedSubjects = getFailedSubjectsForSeat(con, seatNo);
                                String subjectsStr = String.join(" - ", Arrays.stream(failedSubjects).filter(s -> !s.isEmpty()).toArray(String[]::new));
                                
                                data.add(new Object[]{
                                    subjectsStr,
                                    seatNo,
                                    rs.getString("registration_no"),
                                    rs.getString("profession"),
                                    rs.getString("name"),
                                    i++,
                                    rs.getString("exam_system") // Store system in index 6
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
                    // Keep the storage index for system but don't show it in the header if not needed
                    model.setColumnIdentifiers(new String[]{"مواد الدور الثاني", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م", "النظام"});
                    model.setRowCount(0);
                    for (Object[] row : get()) {
                        model.addRow(row);
                    }
                    // Optional: Hide the System column from view if desired
                    jTable1.getColumnModel().getColumn(6).setMinWidth(0);
                    jTable1.getColumnModel().getColumn(6).setMaxWidth(0);
                    jTable1.getColumnModel().getColumn(6).setWidth(0);
                    
                    updateStats();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private String[] getFailedSubjectsForSeat(Connection con, String seatNo) {
        String[] res = new String[6];
        java.util.Arrays.fill(res, "");
        if (seatNo == null || seatNo.trim().isEmpty()) return res;

        try {
            int studentId = -1;
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM students WHERE seat_no = ?")) {
                ps.setString(1, seatNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) studentId = rs.getInt("id");
                }
            }
            if (studentId == -1) return res;

            List<String> theoryFailed = new ArrayList<>();
            boolean failedPractical = false;
            boolean failedApplied = false;

            String sql = "SELECT sub.name, sub.type, sub.pass_mark, NVL(sg.obtained_mark, 0) as mark "
                    + "FROM subjects sub "
                    + "JOIN students st ON TRIM(st.profession) = TRIM(sub.profession) "
                    + "LEFT JOIN student_grades sg ON sg.subject_id = sub.id AND sg.student_id = st.id "
                    + "WHERE st.id = ? AND sub.parent_subject_id IS NULL";
            
            try (PreparedStatement ps2 = con.prepareStatement(sql)) {
                ps2.setInt(1, studentId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) {
                        String sName = rs2.getString("name");
                        String sType = rs2.getString("type");
                        int mark = rs2.getInt("mark");
                        int passMark = rs2.getInt("pass_mark");

                        if (mark < passMark && mark >= 0) {
                            if ("نظري".equals(sType)) theoryFailed.add(sName);
                            else if ("تطبيقي".equals(sType)) failedApplied = true;
                            else failedPractical = true;
                        }
                    }
                }
            }
            for (int i = 0; i < 4 && i < theoryFailed.size(); i++) res[i] = theoryFailed.get(i);
            if (failedPractical) res[4] = "عملي";
            if (failedApplied) res[5] = "تطبيقي";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private void cmdcenter1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter1.getSelectedItem() != null) {
            String region = cmdcenter1.getSelectedItem().toString();
            loadCenters(region);
            refreshData();
        }
    }

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
    }

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String centerName = cmdcenter.getSelectedItem().toString();
        String regionName = cmdcenter1.getSelectedItem().toString();
        String[] months = filterPanel.getSelectedMonths();

        ReportWorker worker = new ReportWorker(this, "كشف طلاب الدور الثاني", null) {
            @Override
            protected Void doInBackground() throws Exception {
                java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem = new java.util.LinkedHashMap<>();
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                
                for (int rowIdx : selectedRows) {
                    int mIdx = jTable1.convertRowIndexToModel(rowIdx);
                    String systemName = String.valueOf(model.getValueAt(mIdx, 6)); // Fetch system from invisible col 6
                    if (systemName == null || systemName.equals("null")) systemName = "";
                    
                    java.util.Vector rowData = new java.util.Vector();
                    for(int i=0; i<6; i++) rowData.add(model.getValueAt(mIdx, i)); // Copy only first 6 cols to report
                    
                    bySystem.computeIfAbsent(systemName, k -> new java.util.ArrayList<>()).add(rowData);
                }

                // ترتيب الأنظمة بالاسم ثم الطلاب داخل كل نظام برقم الجلوس تصاعدياً (نفس الكشف الكبير)
                java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystemSorted = new java.util.LinkedHashMap<>(new java.util.TreeMap<>(bySystem));
                for (java.util.List<java.util.Vector> rows : bySystemSorted.values()) {
                    rows.sort((v1, v2) -> {
                        String sn1 = String.valueOf(v1.get(1)).trim();
                        String sn2 = String.valueOf(v2.get(1)).trim();
                        String sn1C = sn1.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                        String sn2C = sn2.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                        if (!sn1C.isEmpty() && !sn2C.isEmpty()) {
                            try { return Long.compare(Long.parseLong(sn1C), Long.parseLong(sn2C)); } catch (Exception ex) {}
                        }
                        return sn1.compareTo(sn2);
                    });
                }

                SecondRound report = new SecondRound(months[4], months[5]);
                report.createPDFGroupedBySystem(bySystemSorted, centerName, regionName, true);
                return null;
            }
        };
        worker.start();
    }

    private void buttonGradient2ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String centerName = cmdcenter.getSelectedItem().toString();
        final String regionName = cmdcenter1.getSelectedItem().toString();
        final String[] months = filterPanel.getSelectedMonths();

        ReportWorker worker = new ReportWorker(this, "كشف الدور الثاني بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                List<com.pvtd.students.models.Student> studentList = new ArrayList<>();
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                
                try (Connection con = DatabaseConnection.getConnection()) {
                    List<String> seatNumbers = new ArrayList<>();
                    for (int row : selectedRows) {
                        int modelRow = jTable1.convertRowIndexToModel(row);
                        seatNumbers.add(String.valueOf(model.getValueAt(modelRow, 1)));
                    }

                    if (seatNumbers.isEmpty()) return null;

                    // Fetch students
                    Map<String, com.pvtd.students.models.Student> studentMap = new HashMap<>();
                    List<Integer> studentIds = new ArrayList<>();
                    
                    int batchSize = 500;
                    for (int i = 0; i < seatNumbers.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, seatNumbers.size());
                        List<String> batch = seatNumbers.subList(i, end);
                        
                        StringBuilder sb = new StringBuilder("SELECT s.id, s.name, s.registration_no, s.seat_no, s.status, s.national_id, s.profession, s.professional_group, s.secret_no, s.coordination_no, s.exam_system " +
                                     "FROM students s " +
                                     "WHERE s.seat_no IN (");
                        for (int j = 0; j < batch.size(); j++) {
                            sb.append("?");
                            if (j < batch.size() - 1) sb.append(",");
                        }
                        sb.append(")");

                        try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
                            for (int j = 0; j < batch.size(); j++) ps.setString(j + 1, batch.get(j));
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
                                    st.setExamSystem(rs.getString("exam_system"));
                                    st.setCoordinationNo(rs.getString("coordination_no"));
                                    st.setSecretNo(rs.getString("secret_no"));
                                    st.setProfession(rs.getString("profession"));
                                    studentMap.put(st.getSeatNo(), st);
                                    studentIds.add(st.getId());
                                }
                            }
                        }
                    }

                    // Fetch grades
                    Map<Integer, Map<Integer, Integer>> gradesMap = new HashMap<>();
                    if (!studentIds.isEmpty()) {
                        for (int i = 0; i < studentIds.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, studentIds.size());
                            List<Integer> batchIds = studentIds.subList(i, end);
                            StringBuilder sbG = new StringBuilder("SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN (");
                            for (int j = 0; j < batchIds.size(); j++) {
                                sbG.append("?");
                                if (j < batchIds.size() - 1) sbG.append(",");
                            }
                            sbG.append(")");
                            
                            try (PreparedStatement psG = con.prepareStatement(sbG.toString())) {
                                for (int j = 0; j < batchIds.size(); j++) psG.setInt(j + 1, batchIds.get(j));
                                try (ResultSet rsG = psG.executeQuery()) {
                                    while (rsG.next()) {
                                        gradesMap.computeIfAbsent(rsG.getInt("student_id"), k -> new HashMap<>())
                                                 .put(rsG.getInt("subject_id"), rsG.getInt("obtained_mark"));
                                    }
                                }
                            }
                        }
                    }

                    for (String seat : seatNumbers) {
                        com.pvtd.students.models.Student st = studentMap.get(seat);
                        if (st != null) {
                            st.setGrades(gradesMap.getOrDefault(st.getId(), new HashMap<>()));
                            studentList.add(st);
                        }
                    }
                }

                if (studentList.isEmpty()) return null;

                updateStatus(80, 100, "جاري إنشاء ملف PDF...");
                
                String folderStr = "التقارير/تبييضة/دور ثاني";
                File folder = new File(folderStr);
                if (!folder.exists()) folder.mkdirs();

                String sanctionedCenter = centerName.replace("/", "_").replace("\\", "_").replace(":", "_");
                if (sanctionedCenter.isEmpty()) sanctionedCenter = "مركز_غير_محدد";
                String filePath = folderStr + "/كشف الدور الثاني بالدرجات - " + sanctionedCenter + ".pdf";

                com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A3.rotate(), 0, 0, 0, 0);
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                // Sort by seat number
                studentList.sort((s1, s2) -> {
                    String sn1 = s1.getSeatNo() != null ? s1.getSeatNo().trim() : "";
                    String sn2 = s2.getSeatNo() != null ? s2.getSeatNo().trim() : "";
                    String sn1C = sn1.replaceAll("[^0-9]", "");
                    String sn2C = sn2.replaceAll("[^0-9]", "");
                    if (!sn1C.isEmpty() && !sn2C.isEmpty()) {
                        try { return Long.compare(Long.parseLong(sn1C), Long.parseLong(sn2C)); } catch (Exception ex) {}
                    }
                    return sn1.compareTo(sn2);
                });

                gradReportFailMixed report = new gradReportFailMixed(centerName, regionName, studentList, months[4], months[5], true);
                report.appendToDocument(document);
                document.close();

                Desktop.getDesktop().open(new File(folderStr));
                return null;
            }
        };
        worker.start();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        cmdcenter1 = new com.pvtd.students.ui.components.Combobox();
        jLabel1 = new javax.swing.JLabel();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradient2 = new com.pvtd.students.ui.components.ButtonGradient();
        btnSelectAll = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setPreferredSize(new java.awt.Dimension(1091, 80));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 130;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cmdcenter, gridBagConstraints);

        cmdcenter1.setLabeText("المنطقة");
        cmdcenter1.addActionListener(this::cmdcenter1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3; gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 130;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cmdcenter1, gridBagConstraints);

        jLabel1.setText("كشف التلاميذ الدور ثاني");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4; gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        jPanel2.add(jLabel1, gridBagConstraints);

        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);
        buttonGradient2.addActionListener(this::buttonGradient2ActionPerformed);

        javax.swing.JPanel actionsPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 0));
        actionsPanel.setOpaque(false);

        btnSelectAll.addActionListener(e -> jTable1.selectAll());
        btnSelectAll.setPreferredSize(new java.awt.Dimension(130, 40));
        actionsPanel.add(btnSelectAll);

        buttonGradient1.setPreferredSize(new java.awt.Dimension(160, 40));
        actionsPanel.add(buttonGradient1);

        buttonGradient2.setPreferredSize(new java.awt.Dimension(160, 40));
        actionsPanel.add(buttonGradient2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actionsPanel, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"مواد الدور الثاني", "رقم الجلوس", "رقم التسجيل", "المهنة", "الاسم", "م"}
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );

        pack();
    }

    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JLabel jLabel1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient2;
    private javax.swing.JButton btnSelectAll;
}
