package com.pvtd.students.ui.pages;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SystemSettingsPage — Admin-only page to manage:
 * 1. Region codes (name + code)
 * 2. Center codes (name + code, linked to a region)
 * 3. Secret number increment setting
 */
public class SystemSettingsPage extends JPanel {

    private final AppFrame parentFrame;

    // Regions tab
    private DefaultTableModel regionsModel;
    private JTable regionsTable;
    private JTextField regionNameField, regionCodeField;

    // Centers tab
    private DefaultTableModel centersModel;
    private JTable centersTable;
    private JTextField centerNameField, centerCodeField;
    private JComboBox<String> centerRegionCombo;

    // ProfGroups tab
    private DefaultTableModel profGroupsModel;
    private JTable profGroupsTable;
    private JTextField profGroupNameField;

    // Professions tab
    private DefaultTableModel profsModel;
    private JTable profsTable;
    private JTextField profNameField;
    private JComboBox<String> profGroupCombo;
    private JComboBox<String> profSystemCombo;

    // Secret number increment
    private JTextField incrementField;
    
    private final int itemsPerPage = 15;
    
    // Search fields
    private JTextField regionsSearchField;
    private JTextField centersSearchField;
    private JTextField profGroupsSearchField;
    private JTextField profsSearchField;

    private List<Object[]> regionsData = new ArrayList<>();
    private List<Object[]> regionsFilteredData = new ArrayList<>();
    private int regionsPage = 1;
    private JPanel regionsPaginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    private List<Object[]> centersData = new ArrayList<>();
    private List<Object[]> centersFilteredData = new ArrayList<>();
    private int centersPage = 1;
    private JPanel centersPaginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    private JComboBox<String> centersFilterCombo;

    private List<Object[]> profGroupsData = new ArrayList<>();
    private List<Object[]> profGroupsFilteredData = new ArrayList<>();
    private int profGroupsPage = 1;
    private JPanel profGroupsPaginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    private List<Object[]> profsData = new ArrayList<>();
    private List<Object[]> profsFilteredData = new ArrayList<>();
    private int profsPage = 1;
    private JPanel profsPaginationBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
    private JComboBox<String> profsFilterCombo;
    private JComboBox<String> profsSystemFilterCombo;

    public SystemSettingsPage(AppFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(18, 30, 18, 30)));

        JLabel title = new JLabel("إعدادات النظام", SwingConstants.RIGHT);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.EAST);

        JLabel sub = new JLabel("إدارة أكواد المناطق والمراكز وإعدادات الرقم السري", SwingConstants.LEFT);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        header.add(sub, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        tabs.setBorder(new EmptyBorder(10, 16, 16, 16));

        tabs.addTab("أكواد المناطق", buildRegionsTab());
        tabs.addTab("أكواد المراكز", buildCentersTab());
        tabs.addTab("المجموعات المهنية", buildProfGroupsTab());
        tabs.addTab("المهن", buildProfessionsTab());
        tabs.addTab("إعدادات الرقم السري", buildSecretSettingsTab());

        add(tabs, BorderLayout.CENTER);

        loadRegions();
        loadCenters();
        loadCenterRegionCombo(); // Populate region dropdown in centers tab
        loadProfGroups();
        loadProfessions();
        loadProfGroupCombo();
        loadSecretIncrement();
    }

    // ──────────────────────────── REGIONS TAB ────────────────────────────

    private JPanel buildRegionsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 10, 10, 10));

        // Table
        regionsModel = new DefaultTableModel(new String[]{"الكود", "اسم المنطقة"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regionsTable = buildTable(regionsModel);
        
        regionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && regionsTable.getSelectedRow() != -1) {
                int row = regionsTable.getSelectedRow();
                regionCodeField.setText(regionsModel.getValueAt(row, 0) != null ? regionsModel.getValueAt(row, 0).toString() : "");
                regionNameField.setText(regionsModel.getValueAt(row, 1) != null ? regionsModel.getValueAt(row, 1).toString() : "");
            }
        });
        
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        searchBar.setOpaque(false);
        searchBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        regionsSearchField = new JTextField(20);
        regionsSearchField.putClientProperty("JTextField.placeholderText", "🔍 بحث في المناطق...");
        regionsSearchField.setFont(UITheme.FONT_BODY);
        regionsSearchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        regionsSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { regionsPage = 1; applyRegionsFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { regionsPage = 1; applyRegionsFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { regionsPage = 1; applyRegionsFilter(); }
        });
        searchBar.add(regionsSearchField);
        searchBar.add(new JLabel("بحث:"));
        p.add(searchBar, BorderLayout.NORTH);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(new JScrollPane(regionsTable), BorderLayout.CENTER);
        tableWrap.add(regionsPaginationBar, BorderLayout.SOUTH);
        p.add(tableWrap, BorderLayout.CENTER);

        // Input row
        JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        form.setOpaque(false);
        form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        regionNameField = new JTextField(18);
        regionNameField.putClientProperty("JTextField.placeholderText", "اسم المنطقة");
        regionNameField.setFont(UITheme.FONT_BODY);
        regionNameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        regionCodeField = new JTextField(6);
        regionCodeField.putClientProperty("JTextField.placeholderText", "الكود (رقم 2 خانات)");
        regionCodeField.setFont(UITheme.FONT_BODY);
        regionCodeField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JButton btnAdd = primaryBtn("إضافة / تحديث");
        JButton btnDel = dangerBtn("حذف المحدد");
        JButton btnSelectAll = new JButton("تحديد الكل");
        btnSelectAll.setFont(UITheme.FONT_BODY);
        btnSelectAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelectAll.addActionListener(e -> regionsTable.selectAll());

        btnAdd.addActionListener(e -> addOrUpdateRegion());
        btnDel.addActionListener(e -> deleteSelectedRegion());

        form.add(btnDel);
        form.add(btnAdd);
        form.add(btnSelectAll);
        form.add(new JLabel("الكود:"));
        form.add(regionCodeField);
        form.add(new JLabel("الاسم:"));
        form.add(regionNameField);

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private void applyRegionsFilter() {
        regionsFilteredData.clear();
        String q = regionsSearchField != null ? regionsSearchField.getText().trim().toLowerCase() : "";
        for (Object[] r : regionsData) {
            if (q.isEmpty()) { regionsFilteredData.add(r); continue; }
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(q)) { regionsFilteredData.add(r); break; }
            }
        }
        int totalPages = Math.max(1, (int) Math.ceil((double) regionsFilteredData.size() / itemsPerPage));
        if (regionsPage > totalPages) regionsPage = totalPages;
        if (regionsPage < 1) regionsPage = 1;
        renderPage(regionsFilteredData, regionsModel, regionsPage, regionsPaginationBar, this::updateRegionsPage);
    }

    private void loadRegions() {
        regionsData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, code FROM regions ORDER BY code");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                regionsData.add(new Object[]{rs.getString("code"), rs.getString("name")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        applyRegionsFilter();
    }

    private void addOrUpdateRegion() {
        String name = regionNameField.getText().trim();
        String code = regionCodeField.getText().trim();
        if (name.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال الاسم والكود.", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Upsert: try insert, if exists update
            try (PreparedStatement ins = conn.prepareStatement(
                    "MERGE INTO regions r USING (SELECT ? n, ? c FROM DUAL) src " +
                    "ON (r.name = src.n) " +
                    "WHEN MATCHED THEN UPDATE SET code = src.c " +
                    "WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c)")) {
                ins.setString(1, name);
                ins.setString(2, code);
                ins.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "تم الحفظ بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            regionNameField.setText("");
            regionCodeField.setText("");
            loadRegions();
            loadCenterRegionCombo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedRegion() {
        int[] selectedRows = regionsTable.getSelectedRows();
        if (selectedRows.length == 0) { JOptionPane.showMessageDialog(this, "اختر صف أولاً."); return; }
        
        int confirm = JOptionPane.showConfirmDialog(this, "هل أنت متأكد من حذف " + selectedRows.length + " من المناطق؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM regions WHERE name = ?")) {
            for (int r : selectedRows) {
                String name = (String) regionsModel.getValueAt(r, 1);
                stmt.setString(1, name);
                stmt.addBatch();
            }
            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "تم الحذف بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            loadRegions();
            loadCenterRegionCombo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────── CENTERS TAB ────────────────────────────

    private JPanel buildCentersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 10, 10, 10));

        JPanel topForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        topForm.setOpaque(false);
        topForm.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        centersFilterCombo = new JComboBox<>();
        centersFilterCombo.setFont(UITheme.FONT_BODY);
        centersFilterCombo.setPreferredSize(new Dimension(200, 36));
        centersFilterCombo.addActionListener(e -> { centersPage = 1; applyCentersFilter(); });
        
        centersSearchField = new JTextField(20);
        centersSearchField.putClientProperty("JTextField.placeholderText", "🔍 بحث في المراكز...");
        centersSearchField.setFont(UITheme.FONT_BODY);
        centersSearchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        centersSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { centersPage = 1; applyCentersFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { centersPage = 1; applyCentersFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { centersPage = 1; applyCentersFilter(); }
        });
        
        topForm.add(centersSearchField);
        topForm.add(new JLabel("بحث:"));
        topForm.add(centersFilterCombo);
        topForm.add(new JLabel("تصفية بالمنطقة:"));
        p.add(topForm, BorderLayout.NORTH);

        centersModel = new DefaultTableModel(new String[]{"الكود", "اسم المركز", "المنطقة"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        centersTable = buildTable(centersModel);
        
        centersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && centersTable.getSelectedRow() != -1) {
                int row = centersTable.getSelectedRow();
                centerCodeField.setText(centersModel.getValueAt(row, 0) != null ? centersModel.getValueAt(row, 0).toString() : "");
                centerNameField.setText(centersModel.getValueAt(row, 1) != null ? centersModel.getValueAt(row, 1).toString() : "");
                String rName = centersModel.getValueAt(row, 2) != null ? centersModel.getValueAt(row, 2).toString() : null;
                if (rName != null) {
                    centerRegionCombo.setSelectedItem(rName);
                } else {
                    if (centerRegionCombo.getItemCount() > 0) centerRegionCombo.setSelectedIndex(0);
                }
            }
        });
        
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(new JScrollPane(centersTable), BorderLayout.CENTER);
        tableWrap.add(centersPaginationBar, BorderLayout.SOUTH);
        p.add(tableWrap, BorderLayout.CENTER);

        // Input
        JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        form.setOpaque(false);
        form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        centerNameField = new JTextField(18);
        centerNameField.putClientProperty("JTextField.placeholderText", "اسم المركز");
        centerNameField.setFont(UITheme.FONT_BODY);
        centerNameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        centerCodeField = new JTextField(6);
        centerCodeField.putClientProperty("JTextField.placeholderText", "الكود (رقم 3 خانات)");
        centerCodeField.setFont(UITheme.FONT_BODY);
        centerCodeField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        centerRegionCombo = new JComboBox<>();
        centerRegionCombo.setFont(UITheme.FONT_BODY);
        centerRegionCombo.setPreferredSize(new Dimension(160, 36));

        JButton btnAdd = primaryBtn("إضافة / تحديث");
        JButton btnDel = dangerBtn("حذف المحدد");
        JButton btnSelectAll = new JButton("تحديد الكل");
        btnSelectAll.setFont(UITheme.FONT_BODY);
        btnSelectAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelectAll.addActionListener(e -> centersTable.selectAll());

        btnAdd.addActionListener(e -> addOrUpdateCenter());
        btnDel.addActionListener(e -> deleteSelectedCenter());

        form.add(btnDel);
        form.add(btnAdd);
        form.add(btnSelectAll);
        form.add(new JLabel("المنطقة:"));
        form.add(centerRegionCombo);
        form.add(new JLabel("الكود:"));
        form.add(centerCodeField);
        form.add(new JLabel("الاسم:"));
        form.add(centerNameField);

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private void applyCentersFilter() {
        if (centersFilterCombo == null || centersData == null) return;
        centersFilteredData.clear();
        String sel = (String) centersFilterCombo.getSelectedItem();
        String q = centersSearchField != null ? centersSearchField.getText().trim().toLowerCase() : "";
        for (Object[] r : centersData) {
            String rName = r[2] != null ? r[2].toString() : null;
            boolean matchesFilter = sel == null || sel.equals("الكل") || (sel.equals("بدون منطقة") ? rName == null : sel.equals(rName));
            if (!matchesFilter) continue;
            if (q.isEmpty()) { centersFilteredData.add(r); continue; }
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(q)) { centersFilteredData.add(r); break; }
            }
        }
        int totalPages = Math.max(1, (int) Math.ceil((double) centersFilteredData.size() / itemsPerPage));
        if (centersPage > totalPages) centersPage = totalPages;
        if (centersPage < 1) centersPage = 1;
        renderPage(centersFilteredData, centersModel, centersPage, centersPaginationBar, this::updateCentersPage);
    }

    private void loadCenters() {
        centersData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.code, c.name, r.name as region_name " +
                 "FROM centers c LEFT JOIN regions r ON c.region_id = r.id ORDER BY c.code");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                centersData.add(new Object[]{rs.getString("code"), rs.getString("name"), rs.getString("region_name")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        applyCentersFilter();
    }

    private void loadCenterRegionCombo() {
        String sel = (String) centerRegionCombo.getSelectedItem();
        centerRegionCombo.removeAllItems();
        centerRegionCombo.addItem("-- بدون منطقة --");
        
        String fSel = centersFilterCombo != null ? (String) centersFilterCombo.getSelectedItem() : null;
        if (centersFilterCombo != null) {
            clearActionListeners(centersFilterCombo);
            centersFilterCombo.removeAllItems();
            centersFilterCombo.addItem("الكل");
            centersFilterCombo.addItem("بدون منطقة");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM regions ORDER BY code");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                centerRegionCombo.addItem(name);
                if (centersFilterCombo != null) centersFilterCombo.addItem(name);
            }
        } catch (Exception ignored) {}
        
        if (sel != null) centerRegionCombo.setSelectedItem(sel);
        if (centersFilterCombo != null) {
            if (fSel != null) centersFilterCombo.setSelectedItem(fSel);
            centersFilterCombo.addActionListener(e -> { centersPage = 1; applyCentersFilter(); });
            applyCentersFilter();
        }
    }

    private void clearActionListeners(JComboBox<?> combo) {
        for (java.awt.event.ActionListener al : combo.getActionListeners()) {
            combo.removeActionListener(al);
        }
    }

    private void addOrUpdateCenter() {
        String name = centerNameField.getText().trim();
        String code = centerCodeField.getText().trim();
        String region = (String) centerRegionCombo.getSelectedItem();
        if (name.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال الاسم والكود.", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Resolve region id
            Integer regionId = null;
            if (region != null && !region.startsWith("--")) {
                try (PreparedStatement st = conn.prepareStatement("SELECT id FROM regions WHERE name = ?")) {
                    st.setString(1, region);
                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next()) regionId = rs.getInt(1);
                    }
                }
            }
            String sql;
            if (regionId != null) {
                sql = "MERGE INTO centers c USING (SELECT ? n, ? cd, ? rid FROM DUAL) src " +
                      "ON (c.name = src.n) " +
                      "WHEN MATCHED THEN UPDATE SET code = src.cd, region_id = src.rid " +
                      "WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.cd, src.rid)";
                try (PreparedStatement ins = conn.prepareStatement(sql)) {
                    ins.setString(1, name);
                    ins.setString(2, code);
                    ins.setInt(3, regionId);
                    ins.executeUpdate();
                }
            } else {
                sql = "MERGE INTO centers c USING (SELECT ? n, ? cd FROM DUAL) src " +
                      "ON (c.name = src.n) " +
                      "WHEN MATCHED THEN UPDATE SET code = src.cd " +
                      "WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.cd)";
                try (PreparedStatement ins = conn.prepareStatement(sql)) {
                    ins.setString(1, name);
                    ins.setString(2, code);
                    ins.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "تم الحفظ بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            centerNameField.setText("");
            centerCodeField.setText("");
            loadCenters();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCenter() {
        int[] selectedRows = centersTable.getSelectedRows();
        if (selectedRows.length == 0) { JOptionPane.showMessageDialog(this, "اختر صف أولاً."); return; }
        
        int confirm = JOptionPane.showConfirmDialog(this, "هل أنت متأكد من حذف " + selectedRows.length + " من المراكز؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM centers WHERE name = ?")) {
            for (int r : selectedRows) {
                String name = (String) centersModel.getValueAt(r, 1);
                stmt.setString(1, name);
                stmt.addBatch();
            }
            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "تم الحذف بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            loadCenters();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────── PROFESSIONAL GROUPS TAB ────────────────────────────
    
    private JPanel buildProfGroupsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 10, 10, 10));

        profGroupsModel = new DefaultTableModel(new String[]{"اسم المجموعة المهنية"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        profGroupsTable = buildTable(profGroupsModel);
        
        profGroupsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && profGroupsTable.getSelectedRow() != -1) {
                int row = profGroupsTable.getSelectedRow();
                profGroupNameField.setText(profGroupsModel.getValueAt(row, 0) != null ? profGroupsModel.getValueAt(row, 0).toString() : "");
            }
        });

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        searchBar.setOpaque(false);
        searchBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        profGroupsSearchField = new JTextField(20);
        profGroupsSearchField.putClientProperty("JTextField.placeholderText", "🔍 بحث في المجموعات...");
        profGroupsSearchField.setFont(UITheme.FONT_BODY);
        profGroupsSearchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        profGroupsSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { profGroupsPage = 1; applyProfGroupsFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { profGroupsPage = 1; applyProfGroupsFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { profGroupsPage = 1; applyProfGroupsFilter(); }
        });
        searchBar.add(profGroupsSearchField);
        searchBar.add(new JLabel("بحث:"));
        p.add(searchBar, BorderLayout.NORTH);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(new JScrollPane(profGroupsTable), BorderLayout.CENTER);
        tableWrap.add(profGroupsPaginationBar, BorderLayout.SOUTH);
        p.add(tableWrap, BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        form.setOpaque(false);
        form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        profGroupNameField = new JTextField(25);
        profGroupNameField.putClientProperty("JTextField.placeholderText", "اسم المجموعة المهنية");
        profGroupNameField.setFont(UITheme.FONT_BODY);
        profGroupNameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JButton btnAdd = primaryBtn("إضافة / تحديث");
        JButton btnDel = dangerBtn("حذف المحدد");
        JButton btnSelectAll = new JButton("تحديد الكل");
        btnSelectAll.setFont(UITheme.FONT_BODY);
        btnSelectAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelectAll.addActionListener(e -> profGroupsTable.selectAll());

        btnAdd.addActionListener(e -> addOrUpdateProfGroup());
        btnDel.addActionListener(e -> deleteSelectedProfGroup());

        form.add(btnDel);
        form.add(btnAdd);
        form.add(btnSelectAll);
        form.add(new JLabel("الاسم:"));
        form.add(profGroupNameField);

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private void applyProfGroupsFilter() {
        profGroupsFilteredData.clear();
        String q = profGroupsSearchField != null ? profGroupsSearchField.getText().trim().toLowerCase() : "";
        for (Object[] r : profGroupsData) {
            if (q.isEmpty()) { profGroupsFilteredData.add(r); continue; }
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(q)) { profGroupsFilteredData.add(r); break; }
            }
        }
        int totalPages = Math.max(1, (int) Math.ceil((double) profGroupsFilteredData.size() / itemsPerPage));
        if (profGroupsPage > totalPages) profGroupsPage = totalPages;
        if (profGroupsPage < 1) profGroupsPage = 1;
        renderPage(profGroupsFilteredData, profGroupsModel, profGroupsPage, profGroupsPaginationBar, this::updateProfGroupsPage);
    }

    private void loadProfGroups() {
        profGroupsData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM professional_groups ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                profGroupsData.add(new Object[]{rs.getString("name")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        applyProfGroupsFilter();
    }

    private void addOrUpdateProfGroup() {
        String name = profGroupNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال اسم المجموعة المهنية.", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ins = conn.prepareStatement(
                    "MERGE INTO professional_groups p USING (SELECT ? n FROM DUAL) src " +
                    "ON (p.name = src.n) " +
                    "WHEN NOT MATCHED THEN INSERT (name) VALUES (src.n)")) {
                ins.setString(1, name);
                ins.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "تم الحفظ بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            profGroupNameField.setText("");
            loadProfGroups();
            loadProfGroupCombo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProfGroup() {
        int[] selectedRows = profGroupsTable.getSelectedRows();
        if (selectedRows.length == 0) { JOptionPane.showMessageDialog(this, "اختر صف أولاً."); return; }
        
        int confirm = JOptionPane.showConfirmDialog(this, "هل أنت متأكد من حذف " + selectedRows.length + " من المجموعات؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM professional_groups WHERE name = ?")) {
            for (int r : selectedRows) {
                String name = (String) profGroupsModel.getValueAt(r, 0);
                stmt.setString(1, name);
                stmt.addBatch();
            }
            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "تم الحذف بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            loadProfGroups();
            loadProfGroupCombo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────── PROFESSIONS TAB ────────────────────────────

    private JPanel buildProfessionsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 10, 10, 10));

        JPanel topForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        topForm.setOpaque(false);
        topForm.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        profsFilterCombo = new JComboBox<>();
        profsFilterCombo.setFont(UITheme.FONT_BODY);
        profsFilterCombo.setPreferredSize(new Dimension(200, 36));
        profsFilterCombo.addActionListener(e -> { profsPage = 1; applyProfsFilter(); });
        
        profsSearchField = new JTextField(20);
        profsSearchField.putClientProperty("JTextField.placeholderText", "🔍 بحث في المهن...");
        profsSearchField.setFont(UITheme.FONT_BODY);
        profsSearchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        profsSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { profsPage = 1; applyProfsFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { profsPage = 1; applyProfsFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { profsPage = 1; applyProfsFilter(); }
        });
        
        
        profsSystemFilterCombo = new JComboBox<>();
        profsSystemFilterCombo.setFont(UITheme.FONT_BODY);
        profsSystemFilterCombo.setPreferredSize(new Dimension(150, 36));
        profsSystemFilterCombo.addActionListener(e -> { profsPage = 1; applyProfsFilter(); });
        
        topForm.add(profsSearchField);
        topForm.add(new JLabel("بحث:"));
        topForm.add(profsSystemFilterCombo);
        topForm.add(new JLabel("تصفية بالنظام:"));
        topForm.add(profsFilterCombo);
        topForm.add(new JLabel("تصفية بالمجموعة:"));
        p.add(topForm, BorderLayout.NORTH);

        profsModel = new DefaultTableModel(new String[]{"المهنة", "النظام", "المجموعة المهنية"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        profsTable = buildTable(profsModel);
        
        profsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && profsTable.getSelectedRow() != -1) {
                int row = profsTable.getSelectedRow();
                profNameField.setText(profsModel.getValueAt(row, 0) != null ? profsModel.getValueAt(row, 0).toString() : "");
                String system = profsModel.getValueAt(row, 1) != null ? profsModel.getValueAt(row, 1).toString() : null;
                String pgName = profsModel.getValueAt(row, 2) != null ? profsModel.getValueAt(row, 2).toString() : null;
                
                if (system != null) profSystemCombo.setSelectedItem(system);
                if (pgName != null) {
                    profGroupCombo.setSelectedItem(pgName);
                } else if (profGroupCombo.getItemCount() > 0) {
                    profGroupCombo.setSelectedIndex(0);
                }
            }
        });
        
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(new JScrollPane(profsTable), BorderLayout.CENTER);
        tableWrap.add(profsPaginationBar, BorderLayout.SOUTH);
        p.add(tableWrap, BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        form.setOpaque(false);
        form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        profNameField = new JTextField(18);
        profNameField.putClientProperty("JTextField.placeholderText", "اسم المهنة");
        profNameField.setFont(UITheme.FONT_BODY);
        profNameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        profGroupCombo = new JComboBox<>();
        profGroupCombo.setFont(UITheme.FONT_BODY);
        profGroupCombo.setPreferredSize(new Dimension(160, 36));

        profSystemCombo = new JComboBox<>(new String[]{"تخصصى", "وحدات", "وحدات تبادلي", "وحدات جدارة"});
        profSystemCombo.setFont(UITheme.FONT_BODY);
        profSystemCombo.setPreferredSize(new Dimension(160, 36));

        JButton btnAdd = primaryBtn("إضافة / تحديث");
        JButton btnDel = dangerBtn("حذف المحدد");
        JButton btnSelectAll = new JButton("تحديد الكل");
        btnSelectAll.setFont(UITheme.FONT_BODY);
        btnSelectAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSelectAll.addActionListener(e -> profsTable.selectAll());

        btnAdd.addActionListener(e -> addOrUpdateProfession());
        btnDel.addActionListener(e -> deleteSelectedProfession());

        form.add(btnDel);
        form.add(btnAdd);
        form.add(btnSelectAll);
        form.add(new JLabel("المجموعة المهنية:"));
        form.add(profGroupCombo);
        form.add(new JLabel("النظام:"));
        form.add(profSystemCombo);
        form.add(new JLabel("المهنة:"));
        form.add(profNameField);

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private void applyProfsFilter() {
        if (profsFilterCombo == null || profsData == null) return;
        profsFilteredData.clear();
        String selGroup = (String) profsFilterCombo.getSelectedItem();
        String selSystem = (String) profsSystemFilterCombo.getSelectedItem();
        String q = profsSearchField != null ? profsSearchField.getText().trim().toLowerCase() : "";
        for (Object[] r : profsData) {
            String pgName = r[2] != null ? r[2].toString() : null;
            String sysName = r[1] != null ? r[1].toString() : null;
            
            boolean matchesGroup = selGroup == null || selGroup.equals("الكل") || (selGroup.equals("بدون مجموعة") ? pgName == null : selGroup.equals(pgName));
            boolean matchesSystem = selSystem == null || selSystem.equals("الكل") || selSystem.equals(sysName);
            
            if (!matchesGroup || !matchesSystem) continue;
            if (q.isEmpty()) { profsFilteredData.add(r); continue; }
            
            // Search in Profession name, System, or Group name
            boolean found = false;
            for (Object cell : r) {
                if (cell != null && cell.toString().toLowerCase().contains(q)) {
                    found = true;
                    break;
                }
            }
            if (found) profsFilteredData.add(r);
        }
        int totalPages = Math.max(1, (int) Math.ceil((double) profsFilteredData.size() / itemsPerPage));
        if (profsPage > totalPages) profsPage = totalPages;
        if (profsPage < 1) profsPage = 1;
        renderPage(profsFilteredData, profsModel, profsPage, profsPaginationBar, this::updateProfsPage);
    }

    private void loadProfessions() {
        profsData.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.name, p.exam_system, pg.name as pg_name " +
                 "FROM professions p LEFT JOIN professional_groups pg ON p.professional_group_id = pg.id ORDER BY p.name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                profsData.add(new Object[]{rs.getString("name"), rs.getString("exam_system"), rs.getString("pg_name")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        applyProfsFilter();
    }

    private void loadProfGroupCombo() {
        String sel = (String) profGroupCombo.getSelectedItem();
        profGroupCombo.removeAllItems();
        profGroupCombo.addItem("-- بدون مجموعة --");
        
        String fSel = profsFilterCombo != null ? (String) profsFilterCombo.getSelectedItem() : null;
        if (profsFilterCombo != null) {
            clearActionListeners(profsFilterCombo);
            profsFilterCombo.removeAllItems();
            profsFilterCombo.addItem("الكل");
            profsFilterCombo.addItem("بدون مجموعة");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM professional_groups ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                profGroupCombo.addItem(name);
                if (profsFilterCombo != null) profsFilterCombo.addItem(name);
            }
        } catch (Exception ignored) {}
        
        if (sel != null) profGroupCombo.setSelectedItem(sel);
        if (profsFilterCombo != null) {
            if (fSel != null) profsFilterCombo.setSelectedItem(fSel);
            profsFilterCombo.addActionListener(e -> { profsPage = 1; applyProfsFilter(); });
            
            // Initialize system filter if not done
            if (profsSystemFilterCombo.getItemCount() == 0) {
                profsSystemFilterCombo.addItem("الكل");
                profsSystemFilterCombo.addItem("تخصصى");
                profsSystemFilterCombo.addItem("وحدات");
                profsSystemFilterCombo.addItem("وحدات تبادلي");
                profsSystemFilterCombo.addItem("وحدات جدارة");
            }
            
            applyProfsFilter();
        }
    }

    private void addOrUpdateProfession() {
        String name = profNameField.getText().trim();
        String system = (String) profSystemCombo.getSelectedItem();
        String pgName = (String) profGroupCombo.getSelectedItem();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال اسم المهنة.", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            Integer pgId = null;
            if (pgName != null && !pgName.startsWith("--")) {
                try (PreparedStatement st = conn.prepareStatement("SELECT id FROM professional_groups WHERE name = ?")) {
                    st.setString(1, pgName);
                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next()) pgId = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ins = conn.prepareStatement(
                    "MERGE INTO professions p USING (SELECT ? n FROM DUAL) src " +
                    "ON (p.name = src.n) " +
                    "WHEN MATCHED THEN UPDATE SET exam_system = ?, professional_group_id = ? " +
                    "WHEN NOT MATCHED THEN INSERT (name, exam_system, professional_group_id) VALUES (src.n, ?, ?)")) {
                ins.setString(1, name);
                ins.setString(2, system);
                if (pgId != null) ins.setInt(3, pgId); else ins.setNull(3, java.sql.Types.INTEGER);
                ins.setString(4, system);
                if (pgId != null) ins.setInt(5, pgId); else ins.setNull(5, java.sql.Types.INTEGER);
                ins.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "تم الحفظ بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            profNameField.setText("");
            loadProfessions();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProfession() {
        int[] selectedRows = profsTable.getSelectedRows();
        if (selectedRows.length == 0) { JOptionPane.showMessageDialog(this, "اختر صف أولاً."); return; }
        
        int confirm = JOptionPane.showConfirmDialog(this, "هل أنت متأكد من حذف " + selectedRows.length + " من المهن؟", "تأكيد الحذف", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM professions WHERE name = ?")) {
            for (int r : selectedRows) {
                String name = (String) profsModel.getValueAt(r, 0);
                stmt.setString(1, name);
                stmt.addBatch();
            }
            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "تم الحذف بنجاح.", "نجاح", JOptionPane.INFORMATION_MESSAGE);
            loadProfessions();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────── SECRET NUMBER SETTINGS TAB ────────────────────────

    private JPanel buildSecretSettingsTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel info = new JLabel(
            "<html><div style='text-align:right; direction:rtl; font-family:Segoe UI; font-size:14px;'>" +
            "<b>قاعدة توليد الرقم السري (8+ خانات)</b><br><br>" +
            "الرقم السري يتكون من 3 أجزاء تلقائياً:<br>" +
            "• <b>اول جزء (2 خانة)</b>: كود المنطقة<br>" +
            "• <b>الجزء الثاني (3 خانات)</b>: كود المركز<br>" +
            "• <b>الجزء الثالث</b>: أول 3 أرقام من رقم الجلوس + القيمة المضافة أدناه<br><br>" +
            "مثال: منطقة كودها (02) + مركز كوده (010) + رقم جلوس (125) + 10 = 02010135" +
            "</div></html>",
            SwingConstants.RIGHT);
        info.setAlignmentX(Component.RIGHT_ALIGNMENT);
        info.setBorder(new EmptyBorder(0, 0, 30, 0));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        inputRow.setOpaque(false);

        incrementField = new JTextField(8);
        incrementField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        incrementField.setHorizontalAlignment(JTextField.CENTER);
        incrementField.setPreferredSize(new Dimension(120, 40));

        JButton btnSave = primaryBtn("حفظ الإعداد");
        btnSave.addActionListener(e -> saveSecretIncrement());

        JLabel lbl = new JLabel("القيمة المضافة على رقم الجلوس:");
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_SECONDARY);

        inputRow.add(btnSave);
        inputRow.add(incrementField);
        inputRow.add(lbl);

        p.add(info);
        p.add(inputRow);
        return p;
    }

    private void loadSecretIncrement() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT setting_value FROM system_settings WHERE setting_key = 'secret_number_increment'");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                incrementField.setText(rs.getString("setting_value"));
            } else {
                incrementField.setText("10");
            }
        } catch (Exception e) { incrementField.setText("10"); }
    }

    private void saveSecretIncrement() {
        String val = incrementField.getText().trim();
        try {
            Integer.parseInt(val);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال رقم صحيح فقط.", "خطأ", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "MERGE INTO system_settings s USING (SELECT 'secret_number_increment' k, ? v FROM DUAL) src " +
                 "ON (s.setting_key = src.k) " +
                 "WHEN MATCHED THEN UPDATE SET setting_value = src.v " +
                 "WHEN NOT MATCHED THEN INSERT (setting_key, setting_value) VALUES (src.k, src.v)")) {
            stmt.setString(1, val);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "تم حفظ الإعداد بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "خطأ: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────── HELPERS ────────────────────────────

    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(42);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Header Styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setBackground(new Color(0xF8FAFC));
        table.getTableHeader().setForeground(UITheme.TEXT_PRIMARY);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 45));
        ((javax.swing.table.DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Body Cell Styling
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, centerRenderer);
        
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setSelectionBackground(new Color(0xDBEAFE));
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(new Color(0xE2E8F0));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        return table;
    }

    private JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(UITheme.FONT_HEADER);
        b.setBackground(UITheme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setPreferredSize(new Dimension(150, 36));
        return b;
    }

    private JButton dangerBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(UITheme.FONT_BODY);
        b.setBackground(UITheme.DANGER);
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setPreferredSize(new Dimension(140, 36));
        return b;
    }

    // ──────────────────────────── PAGINATION HELPERS ────────────────────────────
    
    private JButton makePageBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(Color.WHITE);
        b.setForeground(UITheme.PRIMARY);
        return b;
    }

    private void renderPage(List<Object[]> data, DefaultTableModel model, int page, JPanel paginationBar, java.util.function.Consumer<Integer> onPageChange) {
        model.setRowCount(0);
        int total = data.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / itemsPerPage));

        int startIdx = (page - 1) * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, total);

        for (int i = startIdx; i < endIdx; i++) {
            model.addRow(data.get(i));
        }

        paginationBar.removeAll();
        paginationBar.setOpaque(false);

        // Prev Button
        if (page > 1) {
            JButton prev = makePageBtn("<");
            prev.addActionListener(e -> onPageChange.accept(page - 1));
            paginationBar.add(prev);
        }

        // Numeric buttons
        int startPage = Math.max(1, Math.min(page - 2, totalPages - 4));
        int endPage = Math.min(totalPages, startPage + 4);

        if (startPage > 1) {
            JButton first = makePageBtn("1");
            first.addActionListener(e -> onPageChange.accept(1));
            paginationBar.add(first);
            if (startPage > 2) {
                JLabel ellipsis = new JLabel("...");
                ellipsis.setFont(UITheme.FONT_BODY);
                ellipsis.setForeground(UITheme.TEXT_SECONDARY);
                paginationBar.add(ellipsis);
            }
        }

        for (int i = startPage; i <= endPage; i++) {
            JButton pb = makePageBtn(String.valueOf(i));
            if (i == page) {
                pb.setBackground(UITheme.PRIMARY);
                pb.setForeground(Color.WHITE);
            }
            int target = i;
            pb.addActionListener(e -> onPageChange.accept(target));
            paginationBar.add(pb);
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                JLabel ellipsis = new JLabel("...");
                ellipsis.setFont(UITheme.FONT_BODY);
                ellipsis.setForeground(UITheme.TEXT_SECONDARY);
                paginationBar.add(ellipsis);
            }
            JButton last = makePageBtn(String.valueOf(totalPages));
            last.addActionListener(e -> onPageChange.accept(totalPages));
            paginationBar.add(last);
        }

        // Next Button
        if (page < totalPages) {
            JButton next = makePageBtn(">");
            next.addActionListener(e -> onPageChange.accept(page + 1));
            paginationBar.add(next);
        }

        paginationBar.revalidate();
        paginationBar.repaint();
    }
    
    private void updateRegionsPage(int newPage) {
        regionsPage = newPage;
        renderPage(regionsFilteredData, regionsModel, regionsPage, regionsPaginationBar, this::updateRegionsPage);
    }
    
    private void updateCentersPage(int newPage) {
        centersPage = newPage;
        renderPage(centersFilteredData, centersModel, centersPage, centersPaginationBar, this::updateCentersPage);
    }

    private void updateProfGroupsPage(int newPage) {
        profGroupsPage = newPage;
        renderPage(profGroupsFilteredData, profGroupsModel, profGroupsPage, profGroupsPaginationBar, this::updateProfGroupsPage);
    }

    private void updateProfsPage(int newPage) {
        profsPage = newPage;
        renderPage(profsFilteredData, profsModel, profsPage, profsPaginationBar, this::updateProfsPage);
    }
}
