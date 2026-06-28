package com.pvtd.students.ui.utils;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends JDialog {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel detailLabel;

    public ProgressDialog(Window parent, String title) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setResizable(false);
        setUndecorated(true);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 102), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        content.setBackground(Color.WHITE);

        statusLabel = new JLabel("جاري معالجة التقرير...");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        detailLabel = new JLabel("برجاء الانتظار...");
        detailLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        detailLabel.setForeground(new Color(100, 100, 100));
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 150, 136)); // Teal color
        progressBar.setBackground(new Color(240, 240, 240));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(statusLabel);
        content.add(Box.createVerticalStrut(15));
        content.add(detailLabel);
        content.add(Box.createVerticalStrut(20));
        content.add(progressBar);

        add(content, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
    }

    public void updateProgress(int value, String detail) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            if (detail != null) detailLabel.setText(detail);
        });
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
}
