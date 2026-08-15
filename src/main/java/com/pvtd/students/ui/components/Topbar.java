package com.pvtd.students.ui.components;

import com.pvtd.students.models.User;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Topbar extends JPanel {

    public Topbar(User user) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(0, 70)); // Height 70px

        // Gentle bottom shadow border
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)), // Tailwind slate-200
                new EmptyBorder(0, 24, 0, 24)));

        // ── Right Side: User Profile & Details ─────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);

        String role = user != null ? user.getRole() : "Admin";
        String displayRole = role.equals("admin") ? "مدير النظام" : "مدخل بيانات";

        JLabel userLabel = new JLabel("<html><div style='text-align:right'><b>"
                + (user != null ? user.getUsername() : "المستخدم")
                + "</b><br><span style='color:#64748b; font-size:11px'>" + displayRole + "</span></div></html>");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel avatarLabel = new JLabel("👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        avatarLabel.setPreferredSize(new Dimension(45, 45));
        avatarLabel.setBackground(new Color(241, 245, 249)); // slate-100
        avatarLabel.setOpaque(true);
        // Soft rounded corners for avatar
        avatarLabel.setBorder(BorderFactory.createEmptyBorder());

        rightPanel.add(userLabel);
        rightPanel.add(Box.createHorizontalStrut(12));
        rightPanel.add(avatarLabel);

        // ── Left Side: Date & Notifications ──────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        // Formatted Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM  yyyy", new Locale("ar", "EG"));
        String today = sdf.format(new Date());

        JLabel dateLabel = new JLabel("📅  " + today);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dateLabel.setForeground(new Color(100, 116, 139)); // slate-500

        // Mock Notification Icon
        JLabel notiLabel = new JLabel("🔔");
        notiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        notiLabel.setToolTipText("لا توجد إشعارات جديدة");

        // Small notification badge
        JPanel notiBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 68, 68)); // red-500
                g2.fillOval(0, 0, 8, 8);
                g2.dispose();
            }
        };
        notiBadge.setOpaque(false);
        notiBadge.setPreferredSize(new Dimension(8, 8));
        notiBadge.setMaximumSize(new Dimension(8, 8));

        // Stack to overlay the red dot over the bell visually
        JPanel notiWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        notiWrapper.setOpaque(false);
        notiWrapper.add(notiBadge);
        notiWrapper.add(notiLabel);

        leftPanel.add(notiWrapper);
        leftPanel.add(Box.createHorizontalStrut(24));
        leftPanel.add(dateLabel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
}
