package com.pvtd.students.ui;

import com.pvtd.students.models.User;
import com.pvtd.students.ui.components.Sidebar;
import com.pvtd.students.ui.pages.DashboardPage;
import com.pvtd.students.ui.pages.DataEntryPage;
import com.pvtd.students.ui.pages.ReportsPage;

import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {
    private JPanel contentPanel;
    private Sidebar sidebar;
    private User loggedInUser;

    public AppFrame(User user) {
        this.loggedInUser = user;

        setTitle("نظام إدارة التلمذة الصناعية");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximized by default
        setLayout(new BorderLayout());

        // Set window icon from logo.jpg
        java.net.URL logoUrl = getClass().getClassLoader().getResource("logo.jpg");
        if (logoUrl != null) {
            setIconImage(new ImageIcon(logoUrl).getImage());
        }

        // Setup Modern Architecture
        sidebar = new Sidebar(this);

        add(sidebar, BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Pre-initialize specific overrides
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        // Default landing page per role
        String role = user.getRole() != null ? user.getRole() : "admin";
        if (role.equals("data_entry")) {
            showPage(new DataEntryPage(this));
        } else if (role.equals("reporter")) {
            showPage(new ReportsPage());
        } else {
            showPage(new DashboardPage());
        }
    }

    public void showPage(JPanel page) {
        if (page == null) {
            JOptionPane.showMessageDialog(this, "هذه الصفحة قيد التطوير حالياً.", "معلومة",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        contentPanel.removeAll();
        contentPanel.add(page, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Called by Sidebar to register nav button references (for startup active
     * state)
     */
    public void registerSidebarButtons(com.pvtd.students.ui.components.Sidebar sb,
            JButton... buttons) {
        // Sidebar manages active state internally via click listeners.
        // This hook is here in case we need cross-page activation in the future.
    }
}
