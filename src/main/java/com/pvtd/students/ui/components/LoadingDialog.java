package com.pvtd.students.ui.components;

import com.pvtd.students.ui.utils.UITheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoadingDialog extends JDialog {

    private JProgressBar progressBar;
    private JLabel statusLabel;

    public LoadingDialog(Frame parent, String title) {
        super(parent, title, true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setPreferredSize(new Dimension(400, 150));

        JPanel content = new JPanel(new BorderLayout(10, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        statusLabel = new JLabel("جاري المعالجة... برجاء الانتظار", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_HEADER);
        statusLabel.setForeground(UITheme.TEXT_PRIMARY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(300, 12));
        progressBar.setForeground(UITheme.PRIMARY);
        progressBar.setBackground(new Color(241, 245, 249));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true);
        progressBar.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD, 10f));

        content.add(statusLabel, BorderLayout.NORTH);
        content.add(progressBar, BorderLayout.CENTER);

        add(content);
        pack();
        setLocationRelativeTo(getParent());
    }

    public void setProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(value + "%");
        });
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public void setMax(int max) {
        SwingUtilities.invokeLater(() -> progressBar.setMaximum(max));
    }
}
