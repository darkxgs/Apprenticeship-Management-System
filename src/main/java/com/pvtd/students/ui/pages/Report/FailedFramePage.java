/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
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

import com.pvtd.students.models.Student;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Seif
 */
public class FailedFramePage extends javax.swing.JFrame {

    private com.pvtd.students.ui.utils.ReportFilterPanel filterPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(FailedFramePage.class.getName());

    private void buttonSecretReportActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] months = filterPanel != null ? filterPanel.getSelectedMonths()
                : new String[] { "", "", "", "", "", "" };
        if (months == null || months.length < 6)
            return;

        final String selMonth = months[4];
        final String admMonth = months[5];

        // Collect all selected seat numbers (no grouping by profession)
        List<String> selectedSeatNos = new ArrayList<>();
        for (int row : selectedRows) {
            selectedSeatNos.add(String.valueOf(model1.getValueAt(row, 2)));
        }

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";

        // Build an ordered list of seat numbers as they appear in the table (for the
        // new copy)
        final List<String> orderedSeatNos = new ArrayList<>(selectedSeatNos);

        ReportWorker worker = new ReportWorker(this, "كشف الراسبين بالدرجات", null) {
            @Override
            protected Void doInBackground() throws Exception {
                // Map seatNo -> Student and seatNo -> region (used for both old & new
                // generation)
                java.util.Map<String, com.pvtd.students.models.Student> allSeatToStudent = new HashMap<>();
                java.util.Map<String, String> seatToRegion = new HashMap<>();

                try (Connection con = DatabaseConnection.getConnection()) {

                    // Map: region -> list of students (all professions mixed)
                    java.util.LinkedHashMap<String, List<com.pvtd.students.models.Student>> byRegion = new java.util.LinkedHashMap<>();

                    int chunkSize = 500;
                    for (int i = 0; i < selectedSeatNos.size(); i += chunkSize) {
                        List<String> chunk = selectedSeatNos.subList(i,
                                Math.min(selectedSeatNos.size(), i + chunkSize));
                        String inClause = String.join(",", java.util.Collections.nCopies(chunk.size(), "?"));

                        String getStudentSql = "SELECT id, name, registration_no, seat_no, status, national_id, " +
                                "professional_group, secret_no, coordination_no, profession, region " +
                                "FROM students WHERE seat_no IN (" + inClause + ")";

                        Map<Integer, com.pvtd.students.models.Student> idToStudent = new HashMap<>();

                        try (PreparedStatement ps = con.prepareStatement(getStudentSql)) {
                            for (int j = 0; j < chunk.size(); j++)
                                ps.setString(j + 1, chunk.get(j));
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
                                    st.setSecretNo(rs.getString("secret_no"));
                                    st.setCoordinationNo(rs.getString("coordination_no"));
                                    st.setProfession(rs.getString("profession"));
                                    st.setGrades(new HashMap<>());

                                    String reg = rs.getString("region");
                                    if (reg == null || reg.isEmpty())
                                        reg = regionName;

                                    idToStudent.put(st.getId(), st);
                                    byRegion.computeIfAbsent(reg, k -> new ArrayList<>()).add(st);
                                    // Track for new ordered copy
                                    allSeatToStudent.put(st.getSeatNo(), st);
                                    seatToRegion.put(st.getSeatNo(), reg);
                                }
                            }
                        }

                        // Load grades for this chunk
                        if (!idToStudent.isEmpty()) {
                            String idIn = String.join(",", java.util.Collections.nCopies(idToStudent.size(), "?"));
                            try (PreparedStatement gps = con.prepareStatement(
                                    "SELECT student_id, subject_id, obtained_mark FROM student_grades WHERE student_id IN ("
                                            + idIn + ")")) {
                                int idx = 1;
                                for (Integer sid : idToStudent.keySet())
                                    gps.setInt(idx++, sid);
                                try (ResultSet rg = gps.executeQuery()) {
                                    while (rg.next()) {
                                        int sid = rg.getInt("student_id");
                                        int subId = rg.getInt("subject_id");
                                        int mark = rg.getInt("obtained_mark");
                                        if (idToStudent.containsKey(sid))
                                            idToStudent.get(sid).getGrades().put(subId, mark);
                                    }
                                }
                            }
                        }
                    }

                    // === النظام القديم: ترتيب حسب رقم الجلوس ===
                    // Sort each region's students by seat_no
                    for (List<com.pvtd.students.models.Student> list : byRegion.values()) {
                        list.sort((a, b) -> {
                            try {
                                return Integer.compare(Integer.parseInt(a.getSeatNo()),
                                        Integer.parseInt(b.getSeatNo()));
                            } catch (Exception e) {
                                return a.getSeatNo().compareTo(b.getSeatNo());
                            }
                        });
                    }

                    // Output folder
                    java.io.File folder = new java.io.File("التقارير/تبييضة/راسبين");
                    if (!folder.exists())
                        folder.mkdirs();

                    String combinedFn = "التقارير/تبييضة/راسبين/مجمع تبييض راسبين.pdf";
                    com.itextpdf.text.Document combinedDoc = new com.itextpdf.text.Document();
                    com.itextpdf.text.pdf.PdfWriter.getInstance(combinedDoc, new java.io.FileOutputStream(combinedFn));
                    combinedDoc.open();

                    for (Map.Entry<String, List<com.pvtd.students.models.Student>> entry : byRegion.entrySet()) {
                        String currentRegion = entry.getKey();
                        List<com.pvtd.students.models.Student> list = entry.getValue();

                        String sanitized = currentRegion.replace("/", "_").replace("\\", "_").replace(":", "_");
                        String fn = "التقارير/تبييضة/راسبين/" + sanitized + ".pdf";

                        updateStatus(100, 100,
                                "جاري توليد تقرير منطقة: " + currentRegion + " (" + list.size() + " طالب)");

                        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fn));
                        document.open();

                        gradReportFailMixed report = new gradReportFailMixed(centerName, currentRegion, list, selMonth,
                                admMonth);
                        report.appendToDocument(document);
                        report.appendToDocument(combinedDoc);

                        document.close();
                    }
                    combinedDoc.close();

                    // === نسخة جديدة: ترتيب الجدول (بدون تصنيف تلقائي) ===
                    // Build region map preserving the selection order from the table
                    java.util.LinkedHashMap<String, List<com.pvtd.students.models.Student>> orderedByRegion = new java.util.LinkedHashMap<>();
                    for (String seatNo : orderedSeatNos) {
                        com.pvtd.students.models.Student st = allSeatToStudent.get(seatNo);
                        if (st != null) {
                            String reg = seatToRegion.getOrDefault(seatNo, regionName);
                            orderedByRegion.computeIfAbsent(reg, k -> new ArrayList<>()).add(st);
                        }
                    }

                    java.io.File newFolder = new java.io.File("التقارير/تبييضة/راسبين/بدون تصنيف");
                    if (!newFolder.exists())
                        newFolder.mkdirs();

                    String newCombinedFn = "التقارير/تبييضة/راسبين/بدون تصنيف/مجمع راسبين بدون تصنيف.pdf";
                    com.itextpdf.text.Document newCombinedDoc = new com.itextpdf.text.Document();
                    com.itextpdf.text.pdf.PdfWriter.getInstance(newCombinedDoc,
                            new java.io.FileOutputStream(newCombinedFn));
                    newCombinedDoc.open();

                    for (Map.Entry<String, List<com.pvtd.students.models.Student>> entry : orderedByRegion.entrySet()) {
                        String currentRegion = entry.getKey();
                        List<com.pvtd.students.models.Student> list = entry.getValue();

                        String sanitized = currentRegion.replace("/", "_").replace("\\", "_").replace(":", "_");
                        String fn = "التقارير/تبييضة/راسبين/بدون تصنيف/" + sanitized + ".pdf";

                        updateStatus(100, 100, "[بدون تصنيف] جاري توليد تقرير منطقة: " + currentRegion);

                        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fn));
                        document.open();

                        gradReportFailMixed newReport = new gradReportFailMixed(centerName, currentRegion, list,
                                selMonth, admMonth);
                        newReport.appendToDocument(document);
                        newReport.appendToDocument(newCombinedDoc);

                        document.close();
                    }
                    newCombinedDoc.close();
                }

                java.awt.Desktop.getDesktop().open(new java.io.File("التقارير/تبييضة/راسبين"));
                return null;
            }
        };
        worker.start();
    }

    /**
     * Creates new form FailedFramePage
     */
    public FailedFramePage() {
        initComponents();
        loadRegions();
        setTitle("تقرير الراسبين");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        this.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                String center = (String) cmdcenter.getSelectedItem();
                String region = (String) cmdcenter1.getSelectedItem();
                if (center != null && region != null && !center.startsWith("اختر") && !region.startsWith("اختر")) {
                    loadStudents(center, region);
                }
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
            }
        });

        // jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // jTable1.setShowGrid(true);
        // jTable1.setGridColor(new Color(220, 220, 220));
        // jTable1.setRowHeight(32);
        // jTable1.setFont(new Font("Tahoma", Font.PLAIN, 14));
        // jTable1.setFillsViewportHeight(true);
        // jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        // @Override public Component getTableCellRendererComponent(JTable t, Object v,
        // boolean sel, boolean foc, int row, int col) {
        // Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
        // if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(255, 245,
        // 245));
        // c.setFont(new Font("Tahoma", Font.PLAIN, 14));
        // ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
        // return c;
        // }
        // });
        // jTable1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
        // @Override public Component getTableCellRendererComponent(JTable t, Object v,
        // boolean sel, boolean foc, int row, int col) {
        // Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
        // c.setBackground(new Color(180, 30, 30)); c.setForeground(Color.WHITE);
        // c.setFont(new Font("Tahoma", Font.BOLD, 15));
        // ((DefaultTableCellRenderer)c).setHorizontalAlignment(CENTER);
        // return c;
        // }
        // });
        // jTable1.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 38));
        //
        // jPanel2.setBackground(new Color(180, 30, 30));
        // javax.swing.JLabel titleLbl = new javax.swing.JLabel(" تقرير الراسبين",
        // javax.swing.SwingConstants.RIGHT);
        // titleLbl.setFont(new Font("Tahoma", Font.BOLD, 20));
        // titleLbl.setForeground(Color.WHITE);
        // java.awt.GridBagConstraints gbcT = new java.awt.GridBagConstraints();
        // gbcT.gridx = 2; gbcT.gridy = 0; gbcT.weightx = 1.0;
        // gbcT.anchor = java.awt.GridBagConstraints.EAST;
        // gbcT.insets = new java.awt.Insets(10, 30, 10, 20);
        // jPanel2.add(titleLbl, gbcT);
        //
        // javax.swing.JButton btnClose = new javax.swing.JButton("✖ إغلاق");
        // btnClose.setForeground(Color.WHITE);
        // btnClose.setFont(new Font("Tahoma", Font.BOLD, 14));
        // btnClose.setFocusPainted(false);
        // btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        // btnClose.addActionListener(e -> this.dispose());
        // UITheme.styleButton(btnClose, new Color(220,38,38), new Color(180,20,20), new
        // Color(140,10,10));
        // java.awt.GridBagConstraints gbcC = new java.awt.GridBagConstraints();
        // gbcC.gridx = 0; gbcC.gridy = 0; gbcC.anchor =
        // java.awt.GridBagConstraints.WEST;
        // gbcC.insets = new java.awt.Insets(8, 12, 8, 12);
        // jPanel2.add(btnClose, gbcC);
        //
        // jPanel3.setBackground(new Color(245, 247, 250));
        // buttonGradient3.setText("📄 إنشاء تقرير PDF للراسبين");
        // jButton1.setText("✔ تحديد الكل"); jButton1.setFont(new Font("Tahoma",
        // Font.BOLD, 13));
        // UITheme.styleButton(jButton1, new Color(37,99,235), new Color(29,78,216), new
        // Color(23,64,180));
        // jButton1.setForeground(Color.WHITE);
        // jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new
        // Color(200,210,230), 1));

        loadRegions();
        setupTableUi();

        filterPanel = new com.pvtd.students.ui.utils.ReportFilterPanel();
        filterPanel.addFilterChangeListener(e -> cmdcenterActionPerformed(null));
        javax.swing.JPanel topContainer = new javax.swing.JPanel(new java.awt.BorderLayout());
        topContainer.add(jPanel2, java.awt.BorderLayout.CENTER);
        topContainer.add(filterPanel, java.awt.BorderLayout.SOUTH);
        jPanel1.add(topContainer, java.awt.BorderLayout.PAGE_START);
    }

    private void setupTableUi() {
        if (jTable1 == null)
            return;
        jTable1.setRowHeight(120);
        jTable1.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
        jTable1.setForeground(java.awt.Color.BLACK);
        jTable1.setSelectionBackground(new java.awt.Color(135, 206, 250)); // Light sky blue for better contrast
        jTable1.setSelectionForeground(java.awt.Color.BLACK);

        // Set column widths to prevent clipping of subjects
        if (jTable1.getColumnCount() >= 7) {
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(650); // مواد الدور الثاني
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(80); // حالة الطالب
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(100); // رقم الجلوس
            jTable1.getColumnModel().getColumn(3).setPreferredWidth(100); // رقم التسجيل
            jTable1.getColumnModel().getColumn(4).setPreferredWidth(180); // المهنة
            jTable1.getColumnModel().getColumn(5).setPreferredWidth(250); // الاسم
            jTable1.getColumnModel().getColumn(6).setPreferredWidth(40); // م
        }

        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                if (!isSelected) {
                    c.setBackground(java.awt.Color.WHITE);
                }
                c.setForeground(java.awt.Color.BLACK);
                c.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return c;
            }
        });
    }

    public void loadRegions() {
        cmdcenter1.removeAllItems();
        cmdcenter1.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService
                .getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {
            cmdcenter1.addItem(r);
        }
    }

    public void loadCenters(String region) {
        cmdcenter.removeAllItems();
        cmdcenter.addItem("اختر المركز...");
        java.util.Map<String, String> centers = com.pvtd.students.services.StudentService
                .getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) {
            cmdcenter.addItem(c);
        }
    }

    public void loadStudents(String center, String region) {

        try {

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            try (Connection con = DatabaseConnection.getConnection()) {

                String sql = """
                        SELECT st.name, st.profession, st.registration_no, st.seat_no, st.status,
                        (
                            SELECT LISTAGG(sub.name, '<br/>') WITHIN GROUP (ORDER BY sub.display_order)
                            FROM subjects sub
                            WHERE TRIM(sub.profession) = TRIM(st.profession)
                            AND sub.parent_subject_id IS NULL
                            AND (
                                sub.id NOT IN (SELECT sg.subject_id FROM student_grades sg WHERE sg.student_id = st.id)
                                OR
                                sub.id IN (SELECT sg.subject_id FROM student_grades sg WHERE sg.student_id = st.id AND sg.obtained_mark < sub.pass_mark)
                            )
                        ) as failed_subjects
                        FROM students st
                        WHERE st.center_name = ?
                        AND st.region = ?
                        AND st.status = 'راسب'
                        """;

                sql += "ORDER BY CASE WHEN REGEXP_LIKE(st.seat_no, '^[0-9]+$') THEN TO_NUMBER(st.seat_no) ELSE 999999 END, st.id ASC";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, center);
                ps.setString(2, region);

                ResultSet rs = ps.executeQuery();

                int i = 1;

                while (rs.next()) {
                    String failedSubs = rs.getString("failed_subjects");
                    if (failedSubs == null)
                        failedSubs = "";

                    model.addRow(new Object[] {
                            failedSubs, // مواد الدور الثاني
                            "دور ثاني",
                            rs.getString("seat_no"),
                            rs.getString("registration_no"),
                            rs.getString("profession"),
                            rs.getString("name"),
                            i++
                    });

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdcenter = new com.pvtd.students.ui.components.Combobox();
        jLabel1 = new javax.swing.JLabel();
        cmdcenter1 = new com.pvtd.students.ui.components.Combobox();
        buttonGradient3 = new com.pvtd.students.ui.components.ButtonGradient();
        buttonGradient1 = new com.pvtd.students.ui.components.ButtonGradient();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(0, 102, 102));
        jPanel2.setPreferredSize(new java.awt.Dimension(476, 100));

        cmdcenter.setLabeText("المركز");
        cmdcenter.addActionListener(this::cmdcenterActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("كشف التلاميذ الراسبين");

        cmdcenter1.setLabeText("المنطقة");
        cmdcenter1.addActionListener(this::cmdcenter1ActionPerformed);

        buttonGradient1.setText("كشف الطلاب الراسبين بدون درجات");
        buttonGradient1.setColor1(new java.awt.Color(9, 54, 55));
        buttonGradient1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        buttonGradient1.setRadius(30);
        buttonGradient1.addActionListener(this::buttonGradient1ActionPerformed);

        buttonGradient3.setText("كشف الطلاب الراسبين بي الدرجات ");
        buttonGradient3.setColor1(new java.awt.Color(9, 54, 55));
        buttonGradient3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        buttonGradient3.setRadius(30);
        buttonGradient3.addActionListener(this::buttonSecretReportActionPerformed);

        jButton1.setBackground(new java.awt.Color(51, 0, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("اختار الكل");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(buttonGradient1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(buttonGradient3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1)))
                                .addGap(18, 18, 18)
                                .addComponent(cmdcenter, javax.swing.GroupLayout.PREFERRED_SIZE, 189,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(cmdcenter1, javax.swing.GroupLayout.PREFERRED_SIZE, 189,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1)));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(29, 29, 29)
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(cmdcenter1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(cmdcenter, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                                .addComponent(buttonGradient1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        7, Short.MAX_VALUE)
                                                                .addGroup(jPanel2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(buttonGradient3,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jButton1)))
                                                        .addComponent(jLabel1))))
                                .addGap(0, 10, Short.MAX_VALUE)));

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null },
                        { null, null, null, null, null, null }
                },
                new String[] {
                        "مواد الدور الثاني", "حالة الطالب", "رقم الجلوس ", "رقم التسجيل", "المهنه", "الاسم", "م"
                }));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmdcenterActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter.getSelectedItem() != null && cmdcenter1.getSelectedItem() != null) {
            String center = cmdcenter.getSelectedItem().toString();
            String region = cmdcenter1.getSelectedItem().toString();
            if (center.equals("اختر المركز...") || region.equals("اختر المنطقة...")) {
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadStudents(center, region);
        }
    }// GEN-LAST:event_cmdcenterActionPerformed

    private void cmdcenter1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmdcenter1.getSelectedItem() != null) {
            String region = cmdcenter1.getSelectedItem().toString();
            if (region.equals("اختر المنطقة...")) {
                cmdcenter.removeAllItems();
                ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
                return;
            }
            loadCenters(region);
        }
    }// GEN-LAST:event_cmdcenter1ActionPerformed

    private void buttonGradient1ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] selectedRows = jTable1.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "برجاء اختيار طلاب أولاً", "تحذير", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String centerName = cmdcenter.getSelectedItem() != null ? cmdcenter.getSelectedItem().toString() : "";
        String regionName = cmdcenter1.getSelectedItem() != null ? cmdcenter1.getSelectedItem().toString() : "";

        String[] filters = filterPanel != null ? filterPanel.getSelectedMonths()
                : new String[] { "", "", "", "", "", "" };
        if (filters == null || filters.length < 6)
            return;
        String selMonth = filters[4];
        String admMonth = filters[5];

        Failed report = new Failed(selMonth, admMonth);
        if (report.isCancelled)
            return;

        DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();

        ReportWorker worker = new ReportWorker(this, "كشف الطلاب الراسبين", null) {
            @Override
            protected Void doInBackground() throws Exception {
                updateStatus(10, 100, "جاري جلب أنظمة المهن...");

                java.util.LinkedHashMap<String, java.util.List<java.util.Vector>> bySystem = new java.util.LinkedHashMap<>();
                java.util.Map<String, String> profToSystem = new java.util.HashMap<>();

                try (Connection con = DatabaseConnection.getConnection()) {
                    String sql = "SELECT name, exam_system FROM professions";
                    try (PreparedStatement ps = con.prepareStatement(sql);
                            ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String pName = rs.getString("name");
                            String pSys = rs.getString("exam_system");
                            if (pName != null) {
                                profToSystem.put(pName.trim(), pSys != null ? pSys : "نظامي");
                            }
                        }
                    }

                    for (int i = 0; i < selectedRows.length; i++) {
                        // jTable1 has 7 cols matching Failed.jTable2: [مواد الدور الثاني(0), حالة
                        // الطالب(1), رقم الجلوس(2), رقم التسجيل(3), المهنه(4), الاسم(5), م(6)]
                        java.util.Vector rowData = (java.util.Vector) model1.getDataVector().get(selectedRows[i]);
                        String prof = String.valueOf(model1.getValueAt(selectedRows[i], 4)).trim();
                        if (prof.startsWith("<html>")) {
                            prof = prof.replaceAll("<[^>]*>", "");
                        }

                        String systemName = profToSystem.getOrDefault(prof, "نظامي");
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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {

        jTable1.selectAll();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FailedFramePage().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient1;
    private com.pvtd.students.ui.components.ButtonGradient buttonGradient3;
    private javax.swing.JButton jButton1;
    private com.pvtd.students.ui.components.Combobox cmdcenter;
    private com.pvtd.students.ui.components.Combobox cmdcenter1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
