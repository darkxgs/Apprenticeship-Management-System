package com.pvtd.students.ui.components;

import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.pages.DashboardPage;
import com.pvtd.students.ui.pages.StudentsPage;
import com.pvtd.students.ui.pages.SubjectsPage;
import com.pvtd.students.ui.pages.StatusesPage;
import com.pvtd.students.ui.pages.ReportsPage;
import com.pvtd.students.ui.pages.ImportPage;
import com.pvtd.students.ui.pages.UsersPage;
import com.pvtd.students.ui.pages.DataEntryPage;
import com.pvtd.students.ui.pages.ArchivesPage;
import com.pvtd.students.ui.pages.AdminLogsPage;
import com.pvtd.students.ui.pages.BackupRestorePage;
import com.pvtd.students.ui.pages.SystemSettingsPage;
import com.pvtd.students.ui.pages.SecretNumberPage;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Sidebar extends JPanel {

    private final List<HoverButton> navButtons = new ArrayList<>();
    private HoverButton activeButton = null;

    public Sidebar(AppFrame frame) {
        setPreferredSize(new Dimension(250, 0));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 12, 20, 12));
        setBackground(UITheme.BG_SIDEBAR);

        // ── Top Navigation ─────────────────────────────────────────────────
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        // Logo / Title Container for absolute centering
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setMaximumSize(new Dimension(250, 120));

        JLabel logoIcon = new JLabel("", SwingConstants.CENTER);
        java.net.URL logoUrlOuter = getClass().getClassLoader().getResource("logo.jpg");
        if (logoUrlOuter != null) {
            ImageIcon icon = new ImageIcon(logoUrlOuter);
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            logoIcon.setIcon(new ImageIcon(img));
        }
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("نظام الكفاية", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("الإنتاجية والتدريب المهني", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x94A3B8));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(logoIcon);
        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(subtitle);

        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(brandPanel);
        navPanel.add(Box.createVerticalStrut(24));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0x1E293B));
        navPanel.add(sep);
        navPanel.add(Box.createVerticalStrut(16));

        // ── Navigation Buttons ──────────────────────────────────────────────────
        String role = frame.getLoggedInUser() != null ? frame.getLoggedInUser().getRole() : "admin";

        HoverButton btnSettings  = menu("إعدادات النظام",              () -> frame.showPage(new SystemSettingsPage(frame)));
        HoverButton btnSubj      = menu("المواد الدراسية",             () -> frame.showPage(new SubjectsPage(frame)));
        HoverButton btnStat      = menu("حالات الطلاب",                () -> frame.showPage(new StatusesPage(frame)));
        HoverButton btnStudents  = menu("الطلاب",                      () -> frame.showPage(new StudentsPage(frame)));
        HoverButton btnImport    = menu("استيراد بيانات",              () -> frame.showPage(new ImportPage(frame)));
        HoverButton btnSecret    = menu("إنشاء الرقم السري",           () -> frame.showPage(new SecretNumberPage(frame)));
        HoverButton btnDataEntry = menu("إدخال الدرجات السريع",        () -> frame.showPage(new DataEntryPage(frame)));
        HoverButton btnRep       = menu("التقارير",                    () -> frame.showPage(new ReportsPage()));
        HoverButton btnArchives  = menu("الأرشيف",                     () -> frame.showPage(new ArchivesPage()));
        HoverButton btnUsers     = menu("المستخدمين",                  () -> frame.showPage(new UsersPage(frame)));
        HoverButton btnLogs      = menu("سجل النشاط",                  () -> frame.showPage(new AdminLogsPage(frame)));
        HoverButton btnBackup    = menu("النسخ الاحتياطي",             () -> frame.showPage(new BackupRestorePage(frame)));
        HoverButton btnDash      = menu("لوحة التحكم",                 () -> frame.showPage(new DashboardPage(frame)));

        if (role.equals("data_entry")) {
            // ── Data Entry Role: minimal sidebar ──
            navPanel.add(createSectionLabel("إدخال البيانات"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnDataEntry);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnImport);
            navPanel.add(Box.createVerticalStrut(6));

        } else if (role.equals("admin")) {
            // ══════ Section 1: البيانات الأكاديمية ══════
            navPanel.add(createSectionLabel("البيانات الأكاديمية"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnSettings);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnSubj);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnStat);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));

            // ══════ Section 2: الطلاب ══════
            navPanel.add(createSectionLabel("الطلاب"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnStudents);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnImport);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));

            // ══════ Section 3: الرقم السري ══════
            navPanel.add(createSectionLabel("الرقم السري"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnSecret);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));

            // ══════ Section 4: التقارير ══════
            navPanel.add(createSectionLabel("التقارير"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnRep);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));

            // ══════ Section 5: أخرى ══════
            navPanel.add(createSectionLabel("أخرى"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnArchives);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnUsers);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnLogs);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnBackup);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));

            // ══════ Section 6: الإحصائيات ══════
            navPanel.add(createSectionLabel("الإحصائيات"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnDash);
            navPanel.add(Box.createVerticalStrut(6));

        } else {
            // ── Reporter or other roles ──
            navPanel.add(createSectionLabel("عرض البيانات"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnStudents);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnSubj);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(btnStat);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSectionLabel("التقارير"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnRep);
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSeparator());
            navPanel.add(Box.createVerticalStrut(6));
            navPanel.add(createSectionLabel("الإحصائيات"));
            navPanel.add(Box.createVerticalStrut(4));
            navPanel.add(btnDash);
            navPanel.add(Box.createVerticalStrut(6));
        }

        add(navPanel, BorderLayout.CENTER);

        // ── Bottom Logout ──────────────────────────────────────────────────
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        JSeparator sepBottom = new JSeparator();
        sepBottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepBottom.setForeground(new Color(0x1E293B));
        bottomPanel.add(sepBottom);
        bottomPanel.add(Box.createVerticalStrut(16));

        HoverButton logoutBtn = menu("تسجيل الخروج", () -> {
            int confirm = JOptionPane.showConfirmDialog(this, "هل أنت متأكد من تسجيل الخروج؟", "تأكيد",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (frame.getLoggedInUser() != null) {
                    com.pvtd.students.services.LogService.logAction(
                            frame.getLoggedInUser().getUsername(), "LOGOUT", "تأكيد تسجيل الخروج");
                }
                frame.dispose();
                com.pvtd.students.MainApp.main(null);
            }
        });

        // Don't track logout button in navButtons
        navButtons.remove(logoutBtn);
        logoutBtn.setForeground(new Color(0xF87171));
        logoutBtn.setHoverColors(new Color(0x3B0000), new Color(0xFCA5A5));
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // Default active page = Dashboard
        setActive(role.equals("admin") ? btnSettings : btnStudents);

        // Setup click listeners for active states
        for (HoverButton b : navButtons) {
            b.addActionListener(e -> setActive(b));
        }
    }

    public void setActive(HoverButton btn) {
        if (activeButton != null) {
            activeButton.setActive(false);
        }
        activeButton = btn;
        if (btn != null) {
            btn.setActive(true);
        }
    }

    private HoverButton menu(String name, Runnable action) {
        HoverButton btn = new HoverButton(name);

        btn.setFont(UITheme.FONT_BODY);
        btn.setPreferredSize(new Dimension(226, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "borderless");

        btn.addActionListener(e -> action.run());

        navButtons.add(btn);
        return btn;
    }
    
    private JLabel createSectionLabel(String title) {
        JLabel lbl = new JLabel(title, SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(0x64748B)); // Muted slate color
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 16));
        lbl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        return lbl;
    }
    
    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0x334155));
        return sep;
    }

    // ── Custom Button Class for Gradient & Indicator ─────────────────────

    private class HoverButton extends JButton {
        private boolean active = false;
        private Color normalBg = UITheme.BG_SIDEBAR;
        private Color normalFg = UITheme.TEXT_SIDEBAR;
        private Color hoverBg = UITheme.HOVER_SIDEBAR;
        private Color hoverFg = UITheme.TEXT_SIDEBAR_HOVER;
        private boolean isHovered = false;

        public HoverButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            setForeground(normalFg);

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    if (!active)
                        repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                    if (!active)
                        repaint();
                }
            });
        }

        public void setHoverColors(Color bg, Color fg) {
            this.hoverBg = bg;
            this.hoverFg = fg;
        }

        public void setActive(boolean a) {
            this.active = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                // Background Gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x1F4FD6),
                        getWidth(), getHeight(), new Color(0x2563EB));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Right Border Indicator
                g2.setColor(new Color(0x60A5FA));
                g2.fillRoundRect(getWidth() - 4, 10, 4, getHeight() - 20, 2, 2);

                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else if (isHovered) {
                g2.setColor(hoverBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                setForeground(hoverFg);
                setFont(UITheme.FONT_BODY);
            } else {
                g2.setColor(normalBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                setForeground(normalFg);
                setFont(UITheme.FONT_BODY);
            }

            super.paintComponent(g);
            g2.dispose();
        }
    }
}
