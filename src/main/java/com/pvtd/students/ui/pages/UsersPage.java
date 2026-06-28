package com.pvtd.students.ui.pages;

import com.pvtd.students.models.User;
import com.pvtd.students.services.AuthService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.DropShadowBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class UsersPage extends JPanel {

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private List<User> currentUsersList;

    public UsersPage(AppFrame parentFrame) {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        setBackground(UITheme.BG_LIGHT);

        // ─── Header ───────────────────────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel titleLabel = new JLabel("إدارة مستخدمي النظام", SwingConstants.RIGHT);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        headerRow.add(titleLabel, BorderLayout.EAST);
        add(headerRow, BorderLayout.NORTH);

        // ─── Table Card ───────────────────────────────────────────────────────
        String[] columns = { "#", "اسم المستخدم", "الاسم الكامل", "الصلاحية" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        usersTable = buildStyledTable();

        // Role badge column
        usersTable.getColumnModel().getColumn(0).setMaxWidth(55);
        usersTable.getColumnModel().getColumn(3).setCellRenderer(new RoleBadgeRenderer());

        loadUserData();

        JScrollPane scroll = new JScrollPane(usersTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 6, 0.07f, 20, UITheme.CARD_BG),
                new EmptyBorder(0, 0, 0, 0)));
        tableCard.add(scroll, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        // ─── Action Bar ───────────────────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        actionBar.setOpaque(false);
        actionBar.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnAdd = makeBtn("＋ إضافة مستخدم", UITheme.SUCCESS, Color.WHITE);
        JButton btnDelete = makeBtn("✕ حذف", new Color(0xFEF2F2), UITheme.DANGER);
        JButton btnResetPwd = makeBtn("🔑 تغيير كلمة السر", new Color(0xEFF6FF), new Color(0x2563EB));

        btnAdd.addActionListener(e -> {
            JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
            p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            JTextField userF = new JTextField();
            userF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            JPasswordField passF = new JPasswordField();
            JTextField nameF = new JTextField();
            nameF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            JComboBox<String> roleC = new JComboBox<>(new String[] { "admin", "data_entry", "viewer" });
            p.add(new JLabel("اسم المستخدم:", SwingConstants.RIGHT));
            p.add(userF);
            p.add(new JLabel("كلمة المرور:", SwingConstants.RIGHT));
            p.add(passF);
            p.add(new JLabel("الاسم الكامل:", SwingConstants.RIGHT));
            p.add(nameF);
            p.add(new JLabel("الصلاحية:", SwingConstants.RIGHT));
            p.add(roleC);
            if (JOptionPane.showConfirmDialog(this, p, "إضافة مستخدم جديد",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String u = userF.getText().trim();
                String pw = new String(passF.getPassword()).trim();
                String nm = nameF.getText().trim();
                String rl = (String) roleC.getSelectedItem();
                if (u.isEmpty() || pw.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "اسم المستخدم وكلمة المرور مطلوبان.", "تنبيه",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (AuthService.addUser(u, pw, rl, nm))
                    loadUserData();
                else
                    JOptionPane.showMessageDialog(this, "فشل إضافة المستخدم. ربما اسم المستخدم مكرر.", "خطأ",
                            JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row < 0) {
                warn("يرجى تحديد مستخدم للحذف.");
                return;
            }
            User u = currentUsersList.get(row);
            if ("admin".equals(u.getUsername())) {
                warn("لا يمكن حذف المدير العام.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "هل أنت متأكد من حذف المستخدم «" + u.getUsername() + "»؟",
                    "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                AuthService.deleteUser(u.getId());
                loadUserData();
            }
        });

        btnResetPwd.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row < 0) {
                warn("يرجى تحديد مستخدم أولاً.");
                return;
            }
            User u = currentUsersList.get(row);
            JPasswordField pf = new JPasswordField();
            if (JOptionPane.showConfirmDialog(this, new Object[] { "كلمة المرور الجديدة:", pf },
                    "تغيير كلمة السر لـ " + u.getUsername(),
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newPw = new String(pf.getPassword()).trim();
                if (newPw.isEmpty()) {
                    warn("كلمة المرور لا تكون فارغة.");
                    return;
                }
                // Simple inline update
                try (java.sql.Connection conn = com.pvtd.students.db.DatabaseConnection.getConnection();
                        java.sql.PreparedStatement ps = conn
                                .prepareStatement("UPDATE users SET password=? WHERE id=?")) {
                    ps.setString(1, newPw);
                    ps.setInt(2, u.getId());
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "تم تغيير كلمة المرور بنجاح.", "نجاح",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        actionBar.add(btnDelete);
        actionBar.add(btnResetPwd);
        actionBar.add(btnAdd);
        add(actionBar, BorderLayout.SOUTH);
    }

    // ─── Role Badge Renderer ──────────────────────────────────────────────────
    private static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean foc, int row, int col) {
            JLabel lbl = new JLabel(translateRole(value != null ? value.toString() : ""), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    String role = getText();
                    Color bg = "مدير".equals(role) ? new Color(0xFEF3C7)
                            : "إدخال بيانات".equals(role) ? new Color(0xE0F2FE) : new Color(0xF1F5F9);
                    Color fg = "مدير".equals(role) ? new Color(0xD97706)
                            : "إدخال بيانات".equals(role) ? new Color(0x0284C7) : new Color(0x64748B);
                    g2.setColor(bg);
                    g2.fillRoundRect(6, 10, getWidth() - 12, getHeight() - 20, 10, 10);
                    g2.setColor(fg);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            lbl.setOpaque(false);
            return lbl;
        }

        private String translateRole(String r) {
            switch (r) {
                case "admin":
                    return "مدير";
                case "data_entry":
                    return "إدخال بيانات";
                default:
                    return "عارض";
            }
        }
    }

    private JTable buildStyledTable() {
        JTable table = new JTable(tableModel);
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        table.setRowHeight(52);
        table.setFont(UITheme.FONT_BODY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(UITheme.CARD_BG);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_HEADER);
        header.setBackground(new Color(0xF8FAFF));
        header.setForeground(new Color(0x64748B));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)));
        header.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        header.setPreferredSize(new Dimension(header.getWidth(), 46));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row,
                    int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel)
                    c.setBackground(row % 2 == 0 ? UITheme.CARD_BG : new Color(0xF8FAFF));
                setHorizontalAlignment(col == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                setFont(UITheme.FONT_BODY);
                return c;
            }
        });
        return table;
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_HEADER);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 40));
        return btn;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        currentUsersList = AuthService.getAllUsers();
        int idx = 1;
        for (User u : currentUsersList) {
            tableModel.addRow(new Object[] { idx++, u.getUsername(), u.getFullName(), u.getRole() });
        }
    }
}
