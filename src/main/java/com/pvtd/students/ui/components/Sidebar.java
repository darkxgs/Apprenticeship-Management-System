package com.pvtd.students.ui.components;

import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.pages.DashboardPage;
import com.pvtd.students.ui.pages.StudentsPage;
import com.pvtd.students.ui.pages.SpecializationsPage;
import com.pvtd.students.ui.pages.SubjectsPage;
import com.pvtd.students.ui.pages.StatusesPage;
import com.pvtd.students.ui.pages.ReportsPage;
import com.pvtd.students.ui.pages.ImportPage;
import com.pvtd.students.ui.pages.UsersPage;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Sidebar extends JPanel {

    // Track all nav buttons so we can toggle active state
    private final List<JButton> navButtons = new ArrayList<>();
    private JButton activeButton = null;

    // Active button appearance
    private static final Color ACTIVE_BG = new Color(0x1E3A5F); // darker tint
    private static final Color ACTIVE_FG = Color.WHITE;

    public Sidebar(AppFrame frame) {
        setPreferredSize(new Dimension(240, 0));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 12, 20, 12));
        setBackground(UITheme.BG_SIDEBAR);

        // ── Top Navigation ─────────────────────────────────────────────────
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        // Logo / Title
        JLabel title = new JLabel("نظام الكفاية", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel subtitle = new JLabel("الإنتاجية والتدريب المهني", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(0x94A3B8));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(title);
        navPanel.add(Box.createVerticalStrut(3));
        navPanel.add(subtitle);
        navPanel.add(Box.createVerticalStrut(16));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0x1E293B));
        navPanel.add(sep);
        navPanel.add(Box.createVerticalStrut(12));

        // ── Nav Items ──────────────────────────────────────────────────────
        String role = frame.getLoggedInUser() != null ? frame.getLoggedInUser().getRole() : "admin";

        JButton btnDash = menu("لوحة التحكم", () -> frame.showPage(new DashboardPage()));
        JButton btnStudents = menu("الطلاب", () -> frame.showPage(new StudentsPage(frame)));
        JButton btnSpec = menu("التخصصات", () -> frame.showPage(new SpecializationsPage(frame)));
        JButton btnSubj = menu("المواد الدراسية", () -> frame.showPage(new SubjectsPage(frame)));
        JButton btnStat = menu("حالات الطلاب", () -> frame.showPage(new StatusesPage(frame)));
        JButton btnRep = menu("التقارير", () -> frame.showPage(new ReportsPage()));

        navPanel.add(btnDash);
        navPanel.add(btnStudents);
        navPanel.add(btnSpec);
        navPanel.add(btnSubj);
        navPanel.add(btnStat);
        navPanel.add(btnRep);

        if (role.equals("admin") || role.equals("data_entry")) {
            navPanel.add(menu("استيراد Excel", () -> frame.showPage(new ImportPage())));
        }
        if (role.equals("admin")) {
            navPanel.add(menu("المستخدمين", () -> frame.showPage(new UsersPage(frame))));
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
        bottomPanel.add(Box.createVerticalStrut(10));

        JButton logoutBtn = menu("تسجيل الخروج", () -> {
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
        // Don't track logout button in navButtons — no active state needed
        navButtons.remove(logoutBtn);
        logoutBtn.setForeground(new Color(0xF87171));
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        // Default active page = Dashboard
        setActive(btnDash);

        // Expose button references to AppFrame via page-ID approach
        frame.registerSidebarButtons(this, btnDash, btnStudents, btnSpec, btnSubj, btnStat, btnRep);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Call this to mark a button as the active page */
    public void setActive(JButton btn) {
        // Deactivate previous
        if (activeButton != null) {
            styleInactive(activeButton);
        }
        // Activate new
        activeButton = btn;
        if (btn != null) {
            styleActive(btn);
        }
    }

    // ── Styling helpers ───────────────────────────────────────────────────────

    private void styleActive(JButton btn) {
        btn.setBackground(ACTIVE_BG);
        btn.setForeground(ACTIVE_FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 4, UITheme.PRIMARY),
                BorderFactory.createEmptyBorder(0, 14, 0, 10)));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.putClientProperty("JButton.buttonType", "borderless");
    }

    private void styleInactive(JButton btn) {
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setForeground(UITheme.TEXT_SIDEBAR);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        btn.setFont(UITheme.FONT_HEADER);
        btn.putClientProperty("JButton.buttonType", "borderless");
    }

    // ── Button factory ────────────────────────────────────────────────────────

    private JButton menu(String name, Runnable action) {
        JButton btn = new JButton(name);

        btn.setFont(UITheme.FONT_HEADER);
        btn.setPreferredSize(new Dimension(216, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(UITheme.TEXT_SIDEBAR);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        btn.putClientProperty("JButton.buttonType", "borderless");

        // Track in navButtons list
        navButtons.add(btn);

        // Hover effect — only applied when button is NOT active
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(UITheme.HOVER_SIDEBAR);
                    btn.setForeground(UITheme.TEXT_SIDEBAR_HOVER);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 0, 4, UITheme.PRIMARY),
                            BorderFactory.createEmptyBorder(0, 14, 0, 10)));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeButton) {
                    styleInactive(btn);
                }
            }
        });

        btn.addActionListener(e -> {
            setActive(btn);
            action.run();
        });

        return btn;
    }
}
