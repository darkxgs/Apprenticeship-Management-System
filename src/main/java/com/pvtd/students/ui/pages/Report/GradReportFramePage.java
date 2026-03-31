package com.pvtd.students.ui.pages.Report;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Selector frame: pick a profession → view students → open gradReport / gradReportSucc
 */
public class GradReportFramePage extends JFrame {

    private JComboBox<String> comboProfession;
    private JComboBox<String> comboCenter;
    private JTable table;
    private DefaultTableModel model;

    public GradReportFramePage() {
        setTitle("كشوف الدرجات - اختيار المهنة");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setBackground(Color.WHITE);

        initUI();
        loadProfessions();
        loadCenters();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setBackground(new Color(0x1E3A5F));
        topBar.setPreferredSize(new Dimension(0, 70));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 15, 10, 15);

        // Title
        JLabel titleLbl = new JLabel("كشوف الدرجات", SwingConstants.RIGHT);
        titleLbl.setFont(new Font("Tahoma", Font.BOLD, 20)); // Tahoma for Arabic
        titleLbl.setForeground(Color.WHITE);
        g.gridx = 4; g.gridy = 0; g.weightx = 1.0; g.anchor = GridBagConstraints.EAST;
        topBar.add(titleLbl, g);

        // Centre combobox
        comboCenter = new JComboBox<>();
        comboCenter.setFont(new Font("Tahoma", Font.PLAIN, 14));
        comboCenter.setPreferredSize(new Dimension(250, 36));
        comboCenter.addActionListener(e -> loadStudents());
        g.gridx = 3; g.gridy = 0; g.weightx = 0; g.anchor = GridBagConstraints.CENTER;
        topBar.add(comboCenter, g);

        JLabel centerLbl = new JLabel("المركز:", SwingConstants.RIGHT);
        centerLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        centerLbl.setForeground(Color.WHITE);
        g.gridx = 4; g.gridy = 0; g.weightx = 0; // overwrite
        // Place beside combo - adjust manually
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(new JLabel("المركز:") {{ setFont(new Font("Tahoma",Font.BOLD,13)); setForeground(Color.WHITE); }});
        centerPanel.add(comboCenter);
        g.gridx = 3; g.gridy = 0; g.weightx = 0;
        topBar.add(centerPanel, g);

        // Profession combobox
        comboProfession = new JComboBox<>();
        comboProfession.setFont(new Font("Tahoma", Font.PLAIN, 14));
        comboProfession.setPreferredSize(new Dimension(280, 36));
        comboProfession.addActionListener(e -> loadStudents());

        JPanel profPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        profPanel.setOpaque(false);
        profPanel.add(new JLabel("المهنة:") {{ setFont(new Font("Tahoma",Font.BOLD,13)); setForeground(Color.WHITE); }});
        profPanel.add(comboProfession);
        g.gridx = 2; g.gridy = 0;
        topBar.add(profPanel, g);

        // Buttons
        JButton btnSucc = new JButton("📋  كشف الناجحين");
        styleBtn(btnSucc, new Color(0x16A34A));
        btnSucc.addActionListener(e -> openReport(false));
        g.gridx = 1; g.gridy = 0;
        topBar.add(btnSucc, g);

        JButton btnFail = new JButton("📋  كشف الراسبين");
        styleBtn(btnFail, new Color(0xDC2626));
        btnFail.addActionListener(e -> openReport(true));
        g.gridx = 0; g.gridy = 0;
        topBar.add(btnFail, g);

        root.add(topBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        model = new DefaultTableModel(
            new String[]{"م", "الاسم", "المهنة", "المركز", "رقم الجلوس", "حالة التلميذ"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0x1E3A5F));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) { c.setBackground(new Color(0xBFDBFE)); }
                else { c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF1F5F9)); }
                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(scroll, BorderLayout.CENTER);

        // ── Status bar ────────────────────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        statusBar.setBackground(new Color(0xF8FAFC));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE2E8F0)));
        JLabel hint = new JLabel("اختر المهنة والمركز ثم اضغط على الكشف المطلوب");
        hint.setFont(new Font("Tahoma", Font.PLAIN, 13));
        hint.setForeground(new Color(0x64748B));
        statusBar.add(hint);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Load combos ───────────────────────────────────────────────────────────
    private void loadProfessions() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT value FROM system_dictionaries WHERE category='PROFESSION' " +
                 "UNION " +
                 "SELECT DISTINCT profession FROM students WHERE profession IS NOT NULL " +
                 "ORDER BY 1");
             ResultSet rs = ps.executeQuery()) {
            comboProfession.removeAllItems();
            comboProfession.addItem("-- كل المهن --");
            while (rs.next()) {
                String p = rs.getString(1);
                if (p != null && !p.trim().isEmpty()) {
                    comboProfession.addItem(p.trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCenters() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT value FROM system_dictionaries WHERE category='CENTER' " +
                 "UNION " +
                 "SELECT DISTINCT center_name FROM students WHERE center_name IS NOT NULL " +
                 "ORDER BY 1");
             ResultSet rs = ps.executeQuery()) {
            comboCenter.removeAllItems();
            comboCenter.addItem("-- كل المراكز --");
            while (rs.next()) {
                String c = rs.getString(1);
                if (c != null && !c.trim().isEmpty()) {
                    comboCenter.addItem(c.trim());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Load students into preview table ──────────────────────────────────────
    private void loadStudents() {
        model.setRowCount(0);
        String prof = (String) comboProfession.getSelectedItem();
        String center = (String) comboCenter.getSelectedItem();
        if (prof == null || center == null) return;

        StringBuilder sql = new StringBuilder(
            "SELECT name, profession, center_name, seat_no, status FROM students WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (!prof.startsWith("--")) { sql.append(" AND profession = ?"); params.add(prof); }
        if (!center.startsWith("--")) { sql.append(" AND center_name = ?"); params.add(center); }
        sql.append(" ORDER BY seat_no");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            int seq = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    seq++,
                    rs.getString("name"),
                    rs.getString("profession"),
                    rs.getString("center_name"),
                    rs.getString("seat_no"),
                    rs.getString("status")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Open the report window ────────────────────────────────────────────────
    private void openReport(boolean failedReport) {
        String prof = (String) comboProfession.getSelectedItem();
        if (prof == null || prof.startsWith("--")) {
            JOptionPane.showMessageDialog(this,
                "من فضلك اختر مهنة محددة أولاً", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String center = (String) comboCenter.getSelectedItem();

        // Build filter
        String statusFilter = failedReport ? "راسب" : "ناجح";
        List<Student> students = StudentService.getStudentsByProfessionAndStatus(
            prof,
            center != null && !center.startsWith("--") ? center : null,
            statusFilter
        );

        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "لا يوجد طلاب بهذه المعايير", "لا توجد بيانات", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (failedReport) {
            gradReport report = new gradReport(prof, students);
            report.setVisible(true);
        } else {
            gradReportSucc report = new gradReportSucc(prof, students);
            report.setVisible(true);
        }
    }

    private void styleBtn(JButton btn, Color bg) {
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 40));
    }
}
