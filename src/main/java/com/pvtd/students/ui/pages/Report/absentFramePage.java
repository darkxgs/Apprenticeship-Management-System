package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.utils.UITheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.pvtd.students.ui.utils.ReportWorker;
import com.pvtd.students.ui.utils.ReportUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class absentFramePage extends javax.swing.JFrame {
    
    private com.pvtd.students.ui.utils.ReportFilterPanel filterPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(absentFramePage.class.getName());

    private javax.swing.JLabel lblStatTotal;
    private javax.swing.JLabel lblStatCenter;
    private javax.swing.JLabel lblStatRegion;
    private javax.swing.JPanel statsContainer;

    public absentFramePage() {
        initComponents();
        modernizeComponents();
        initDashboardLayout();
        loadRegions();
        setupTableUi();

        setTitle("تقرير الغائبين");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                String center = (String) cmdcenter.getSelectedItem();
                String region = (String) cmdcenter1.getSelectedItem();
                if (center != null && region != null && !center.startsWith("اختر") && !region.startsWith("اختر") && !center.equals("الكل") && !region.equals("الكل")) {
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
        
        int totalInDb = jTable1.getRowCount(); // In this context, total filtered
        int selected = jTable1.getSelectedRowCount();
        
        lblStatTotal.setText(String.valueOf(totalInDb));
        lblStatFiltered.setText(String.valueOf(totalInDb)); // Since local table IS the filtered set
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
        
        statsContainer.add(createStatCard("إجمالي الطلاب", lblStatTotal, new java.awt.Color(13, 148, 136))); 
        statsContainer.add(createStatCard("الطلاب المعروضين", lblStatFiltered, new java.awt.Color(20, 184, 166))); 
        statsContainer.add(createStatCard("الطلاب المحددين", lblStatSelected, new java.awt.Color(37, 99, 235))); 
        
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
        
        buttonGradient1.setText("📄 عرض بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(13, 148, 136));
        buttonGradient1.setColor2(new java.awt.Color(20, 94, 94));
        buttonGradient1.setFont(UITheme.FONT_HEADER);
        buttonGradient1.setRadius(20);
        
        buttonGradientGrades.setText("📊 كشف بالدرجات");
        buttonGradientGrades.setColor1(new java.awt.Color(15, 118, 110));
        buttonGradientGrades.setColor2(new java.awt.Color(2, 44, 34));
        buttonGradientGrades.setFont(UITheme.FONT_HEADER);
        buttonGradientGrades.setRadius(20);

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
        cmdcenter1.removeAllItems();
        cmdcenter1.addItem("الكل");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            cmdcenter1.addItem(r);
        }
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("الكل");
        java.util.Map<String, String> centers;
        if (region == null || region.equals("الكل")) centers = com.pvtd.students.services.StudentService.getCentersWithCodes();
        else centers = com.pvtd.students.services.StudentService.getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) cmdcenter.addItem(c);
    }

    public void loadStudents(String center, String region) {
        try {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            try (Connection con = DatabaseConnection.getConnection()) {
                StringBuilder sql = new StringBuilder("SELECT name, profession, registration_no, seat_no, status FROM students WHERE status = 'غائب' ");
                if (region != null && !region.equals("الكل")) sql.append(" AND TRIM(region) = TRIM(?) ");
                if (center != null && !center.equals("الكل")) sql.append(" AND TRIM(center_name) = TRIM(?) ");
                sql.append("ORDER BY CASE WHEN REGEXP_LIKE(seat_no, '^[0-9]+$') THEN TO_NUMBER(seat_no) ELSE 999999 END, id ASC");
                PreparedStatement ps = con.prepareStatement(sql.toString());
                int paramIdx = 1;
                if (region != null && !region.equals("الكل")) ps.setString(paramIdx++, region);
                if (center != null && !center.equals("الكل")) ps.setString(paramIdx++, center);
                ResultSet rs = ps.executeQuery();
                int i = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("status"), rs.getString("seat_no"), rs.getString("registration_no"), rs.getString("profession"), rs.getString("name"), i++});
                }
                updateStats();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        cmdcenter1 = new com.pvtd.students.ui.components.Combobox();
        jLabel1 = new javax.swing.JLabel();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradientGrades = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel2.setBackground(new java.awt.Color(0, 102, 102));
        jPanel2.setPreferredSize(new java.awt.Dimension(475, 100));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        // 1. Title Label
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 22));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف التلاميذ الغائبين");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4; gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 20);
        jPanel2.add(jLabel1, gridBagConstraints);

        // 2. ComboBoxes
        cmdcenter1.setLabeText("المنطقة");
        cmdcenter1.addActionListener(this::cmdcenter1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3; gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cmdcenter1, gridBagConstraints);

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2; gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(cmdcenter, gridBagConstraints);

        // 3. Actions Panel for Buttons
        javax.swing.JPanel actionsPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 0));
        actionsPanel.setOpaque(false);
        
        jButton1.setText("✅ اختيار الكل");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton1.setPreferredSize(new java.awt.Dimension(140, 40));
        actionsPanel.add(jButton1);
        
        buttonGradient1.setText("📄 بدون درجات");
        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);
        buttonGradient1.setPreferredSize(new java.awt.Dimension(180, 40));
        actionsPanel.add(buttonGradient1);
        
        buttonGradientGrades.setText("📊 كشف بالدرجات");
        buttonGradientGrades.addActionListener(this::buttonSecretReportActionPerformed);
        buttonGradientGrades.setPreferredSize(new java.awt.Dimension(180, 40));
        actionsPanel.add(buttonGradientGrades);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0; gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel2.add(actionsPanel, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {{null, null, null, null, null, null}},
            new String [] {"حالة الطالب", "رقم الجلوس ", "رقم التسجيل", "المهنه", "الاسم", "م"}
        ));
        jScrollPane1.setViewportView(jTable1);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE));
        pack();
    }// </editor-fold>

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter.getSelectedItem() != null && cmdcenter1.getSelectedItem() != null) {
            loadStudents(cmdcenter.getSelectedItem().toString(), cmdcenter1.getSelectedItem().toString());
        }
    }

    private void cmdcenter1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter1.getSelectedItem() != null) {
            String region = cmdcenter1.getSelectedItem().toString();
            loadCenters(region);
            if (cmdcenter.getSelectedItem() != null) loadStudents(cmdcenter.getSelectedItem().toString(), region);
        }
    }

    private void buttonSecretReportActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalSelected = selectedRows.length;
        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";

        String[] filters = filterPanel.getSelectedMonths();
        if (filters == null || filters.length < 6) return;
        String selMonth = filters[4];
        String admMonth = filters[5];

        ReportWorker worker = new ReportWorker(this, "كشف الغائبين بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> allStudentsByRegion = new java.util.LinkedHashMap<>();
                java.util.Map<String, String> allStudentSystems = new java.util.HashMap<>();
                java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>> studentsByRegion = new java.util.LinkedHashMap<>();
                java.util.List<String> selectedSeatNos = new java.util.ArrayList<>();
                for (int row : selectedRows) selectedSeatNos.add(String.valueOf(model1.getValueAt(row, 1)));

                try (Connection con = DatabaseConnection.getConnection()) {
                    java.util.Map<String, com.pvtd.students.models.Student> studentMap = new java.util.HashMap<>();
                    java.util.List<Integer> studentIds = new java.util.ArrayList<>();
                    
                    // Unified Logic: Fetch system directly from professions like the non-graded report
                    java.util.Map<String, String> profToSystem = new java.util.HashMap<>();
                    String sysSql = "SELECT name, exam_system FROM professions";
                    try (PreparedStatement psP = con.prepareStatement(sysSql); ResultSet rsP = psP.executeQuery()) {
                        while (rsP.next()) {
                            String pName = rsP.getString("name");
                            if (pName != null) profToSystem.put(pName.trim(), rsP.getString("exam_system"));
                        }
                    }

                    int batchSize = 500;
                    for (int i = 0; i < selectedSeatNos.size(); i += batchSize) {
                        java.util.List<String> batch = selectedSeatNos.subList(i, Math.min(i + batchSize, selectedSeatNos.size()));
                        String inClause = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
                        String sql = "SELECT id, name, registration_no, seat_no, status, national_id, profession, professional_group, secret_no, coordination_no, region FROM students WHERE seat_no IN (" + inClause + ")";
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            for (int j = 0; j < batch.size(); j++) ps.setString(j + 1, batch.get(j));
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    com.pvtd.students.models.Student st = new com.pvtd.students.models.Student();
                                    st.setId(rs.getInt("id")); st.setName(rs.getString("name"));
                                    st.setRegistrationNo(rs.getString("registration_no")); st.setSeatNo(rs.getString("seat_no"));
                                    st.setStatus(rs.getString("status")); st.setNationalId(rs.getString("national_id"));
                                    st.setProfessionalGroup(rs.getString("professional_group")); st.setCoordinationNo(rs.getString("coordination_no"));
                                    st.setSecretNo(rs.getString("secret_no"));
                                    String prof = rs.getString("profession");
                                    st.setProfession(prof);
                                    
                                    // Use unified system source
                                    String resolvedProf = (prof != null) ? prof.trim() : "";
                                    st.setExamSystem(profToSystem.getOrDefault(resolvedProf, ""));
                                    
                                    String r = rs.getString("region");
                                    if (r == null || r.trim().isEmpty()) r = regionName;
                                    st.setRegion(r);
                                    
                                    studentMap.put(st.getSeatNo(), st);
                                    studentIds.add(st.getId());
                                }
                            }
                        }
                    }

                    java.util.Map<Integer, java.util.Map<Integer, Integer>> gradesMap = new java.util.HashMap<>();
                    if (!studentIds.isEmpty()) {
                        for (int i = 0; i < studentIds.size(); i += batchSize) {
                            java.util.List<Integer> batch = studentIds.subList(i, Math.min(i + batchSize, studentIds.size()));
                            String inClause = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
                            try (PreparedStatement psG = con.prepareStatement("SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN (" + inClause + ")")) {
                                for (int j = 0; j < batch.size(); j++) psG.setInt(j + 1, batch.get(j));
                                try (ResultSet rsG = psG.executeQuery()) {
                                    while (rsG.next()) {
                                        gradesMap.computeIfAbsent(rsG.getInt("student_id"), k -> new java.util.HashMap<>()).put(rsG.getInt("subject_id"), rsG.getInt("obtained_mark"));
                                    }
                                }
                            }
                        }
                    }

                    // Grouping: region -> (system -> students)
                    java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>>> data = new java.util.LinkedHashMap<>();
                    for (String seatNo : selectedSeatNos) {
                        com.pvtd.students.models.Student st = studentMap.get(seatNo);
                        if (st != null) {
                            st.setGrades(gradesMap.getOrDefault(st.getId(), new java.util.HashMap<>()));
                            data.computeIfAbsent(st.getRegion(), k -> new java.util.LinkedHashMap<>())
                                .computeIfAbsent(st.getExamSystem(), k -> new java.util.ArrayList<>())
                                .add(st);
                        }
                    }

                    generatePdfFiles(data, centerName, selMonth, admMonth, "التقارير/تبييضة/غائبين");
                }
                java.awt.Desktop.getDesktop().open(new File("التقارير/تبييضة/غائبين"));
                return null;
            }

            private void generatePdfFiles(java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>>> data, 
                                        String center, String selM, String admM, String folderPath) throws Exception {
                File folder = new File(folderPath);
                if (!folder.exists()) folder.mkdirs();

                if (center == null) center = "مركز_غير_محدد";
                String sanitizedCenter = center.replace("/", "_").replace("\\", "_").replace(":", "_");
                if (sanitizedCenter.isEmpty()) sanitizedCenter = "مركز_غير_محدد";
                String combinedFn = folderPath + "/" + sanitizedCenter + ".pdf";

                Document combinedDoc = new Document();
                PdfWriter.getInstance(combinedDoc, new FileOutputStream(combinedFn));
                combinedDoc.open();

                if (!data.isEmpty()) {
                    // Group students into a map by system to handle page breaks
                    java.util.TreeMap<String, java.util.List<com.pvtd.students.models.Student>> groupedBySystem = new java.util.TreeMap<>();
                    String firstRegionFound = "";

                    for (java.util.Map.Entry<String, java.util.LinkedHashMap<String, java.util.List<com.pvtd.students.models.Student>>> regEntry : data.entrySet()) {
                        if (firstRegionFound.isEmpty()) firstRegionFound = regEntry.getKey();
                        for (java.util.Map.Entry<String, java.util.List<com.pvtd.students.models.Student>> entry : regEntry.getValue().entrySet()) {
                            String sys = entry.getKey() != null ? entry.getKey() : "";
                            groupedBySystem.computeIfAbsent(sys, k -> new java.util.ArrayList<>()).addAll(entry.getValue());
                        }
                    }

                    for (java.util.Map.Entry<String, java.util.List<com.pvtd.students.models.Student>> entry : groupedBySystem.entrySet()) {
                        java.util.List<com.pvtd.students.models.Student> sysList = entry.getValue();
                        
                        // Sort by seat number within this system
                        sysList.sort((s1, s2) -> {
                            String sn1 = s1.getSeatNo() != null ? s1.getSeatNo().trim() : "";
                            String sn2 = s2.getSeatNo() != null ? s2.getSeatNo().trim() : "";
                            String sn1Clean = sn1.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                            String sn2Clean = sn2.replaceAll("[^0-9\\u0660-\\u0669]", "").replace("٠","0").replace("١","1").replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5").replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9");
                            if (!sn1Clean.isEmpty() && !sn2Clean.isEmpty()) {
                                try { return Long.compare(Long.parseLong(sn1Clean), Long.parseLong(sn2Clean)); } catch (Exception ex) {}
                            }
                            return sn1.compareTo(sn2);
                        });

                        // Call appendToDocument for each system group to force a page break
                        gradReportSequential report = new gradReportSequential("تلاميذ غائبون", new java.awt.Color(100, 100, 100), center, firstRegionFound, sysList, selM, admM);
                        report.appendToDocument(combinedDoc);
                    }
                }

                combinedDoc.close();
            }
        };
        worker.start();
    }

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";
        String[] months = filterPanel.getSelectedMonths();
        if (months == null || months.length < 6) return;
        absent report = new absent(months[4], months[5]);
        if (report.isCancelled) return;
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
        ReportWorker worker = new ReportWorker(this, "كشف طلاب غائبين", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(10, 100, "جاري جلب أنظمة المهن...");
                java.util.TreeMap<String, java.util.List<java.util.Vector>> bySystem = new java.util.TreeMap<>();
                java.util.Map<String, String> profToSystem = new java.util.HashMap<>();
                try (java.sql.Connection con = com.pvtd.students.db.DatabaseConnection.getConnection()) {
                    String sql = "SELECT name, exam_system FROM professions";
                    try (java.sql.PreparedStatement ps = con.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String pName = rs.getString("name");
                            String pSys = rs.getString("exam_system");
                            if (pName != null) profToSystem.put(pName.trim(), pSys != null ? pSys : "");
                        }
                    }
                    for (int i = 0; i < selectedRows.length; i++) {
                        java.util.Vector rowData = (java.util.Vector) model1.getDataVector().get(selectedRows[i]);
                        String prof = String.valueOf(model1.getValueAt(selectedRows[i], 3)).trim();
                        if (prof.startsWith("<html>")) prof = prof.replaceAll("<[^>]*>", "");
                        String systemName = profToSystem.getOrDefault(prof, "");
                        bySystem.computeIfAbsent(systemName, k -> new java.util.ArrayList<>()).add(rowData);
                    }
                }
                updateStatus(50, 100, "جاري إنشاء ملف PDF...");
                report.createPDFGroupedBySystem(bySystem, centerName, regionName, true);
                return null;
            }
        };
        worker.start();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) { jTable1.selectAll(); }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) { logger.log(java.util.logging.Level.SEVERE, null, ex); }
        java.awt.EventQueue.invokeLater(() -> new absentFramePage().setVisible(true));
    }

    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradientGrades;
    private javax.swing.JButton jButton1;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
}
