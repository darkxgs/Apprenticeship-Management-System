package com.pvtd.students.ui.pages;

import com.pvtd.students.services.StatusesService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.DropShadowBorder;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StatusesPage extends JPanel {

    private final AppFrame frame;
    private JPanel gridPanel;

    // Color palette for status cards — auto-cycles
    private static final Color[][] PALETTE = {
            { new Color(0xDBEAFE), new Color(0x1D4ED8) }, // blue
            { new Color(0xDCFCE7), new Color(0x15803D) }, // green
            { new Color(0xFEE2E2), new Color(0xDC2626) }, // red
            { new Color(0xFEF3C7), new Color(0xD97706) }, // amber
            { new Color(0xEDE9FE), new Color(0x7C3AED) }, // violet
            { new Color(0xFCE7F3), new Color(0xBE185D) }, // pink
            { new Color(0xF0FDF4), new Color(0x166534) }, // emerald
            { new Color(0xE0F2FE), new Color(0x0369A1) }, // sky
    };

    public StatusesPage(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_LIGHT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(), BorderLayout.CENTER);

        loadCards();
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JLabel titleLbl = new JLabel("إدارة حالات الطلاب", SwingConstants.RIGHT);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("أضف أو احذف الحالات التي تُستخدم في تقييم الطلاب", SwingConstants.RIGHT);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subLbl);

        JButton btnAdd = new JButton("إضافة حالة جديدة");
        btnAdd.setFont(UITheme.FONT_HEADER);
        btnAdd.setBackground(UITheme.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(170, 40));
        btnAdd.addActionListener(e -> handleAdd());

        header.add(titleBlock, BorderLayout.EAST);
        header.add(btnAdd, BorderLayout.WEST);
        return header;
    }

    // ── Card Grid ──────────────────────────────────────────────────────────────
    private JScrollPane buildGrid() {
        gridPanel = new JPanel();
        gridPanel.setOpaque(false);
        gridPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        gridPanel.setBorder(new EmptyBorder(12, 24, 24, 24));
        // 3-column grid; rows grow automatically
        gridPanel.setLayout(new GridLayout(0, 3, 20, 20));

        JScrollPane scroll = new JScrollPane(gridPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void loadCards() {
        gridPanel.removeAll();
        List<String> statuses = StatusesService.getAllStatuses();
        int i = 0;
        for (String status : statuses) {
            Color[] colors = PALETTE[i % PALETTE.length];
            gridPanel.add(buildCard(status, colors[0], colors[1]));
            i++;
        }
        if (statuses.isEmpty()) {
            JLabel empty = new JLabel("لا توجد حالات مُضافة بعد. اضغط «إضافة» لإنشاء أول حالة.",
                    SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(UITheme.TEXT_SECONDARY);
            gridPanel.add(empty);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildCard(String statusName, Color bgColor, Color accentColor) {
        JPanel card = new JPanel(null) { // null layout for manual placement
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Accent top bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 8, 18, 18);
                g2.fillRect(0, 4, getWidth(), 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(200, 130));
        card.setBorder(new DropShadowBorder(Color.BLACK, 6, 0.07f, 18, Color.WHITE));

        // Color badge (top right pill)
        JLabel badge = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setForeground(accentColor);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setText("  حالة  ");
        badge.setBorder(new EmptyBorder(2, 4, 2, 4));
        badge.setBounds(12, 16, 56, 22);
        card.add(badge);

        // Status name label
        JLabel nameLbl = new JLabel(statusName, SwingConstants.RIGHT);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        nameLbl.setBounds(12, 48, 176, 28);
        card.add(nameLbl);

        // Delete button
        JButton btnDel = new JButton("حذف");
        btnDel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDel.setForeground(UITheme.DANGER);
        btnDel.setBackground(new Color(0xFEF2F2));
        btnDel.setFocusPainted(false);
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.putClientProperty("JButton.buttonType", "roundRect");
        btnDel.setBounds(12, 90, 76, 28);
        btnDel.addActionListener(e -> handleDelete(statusName));
        card.add(btnDel);

        // Edit / Rename button
        JButton btnEdit = new JButton("تعديل");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEdit.setForeground(accentColor);
        btnEdit.setBackground(bgColor);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.setBounds(100, 90, 76, 28);
        btnEdit.addActionListener(e -> handleRename(statusName));
        card.add(btnEdit);

        return card;
    }

    // ── Actions ────────────────────────────────────────────────────────────────
    private void handleAdd() {
        String name = JOptionPane.showInputDialog(this,
                "اسم الحالة الجديدة (مثال: محوّل، غائب، قيد الفحص):",
                "إضافة حالة جديدة", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            try {
                StatusesService.addStatus(name.trim());
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ: " + ex.getMessage(),
                        "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(String statusName) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف الحالة: «" + statusName + "»؟\nسيؤثر ذلك على الطلاب الحاملين لهذه الحالة.",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                StatusesService.deleteStatus(statusName);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "لا يمكن حذف هذه الحالة: " + ex.getMessage(),
                        "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRename(String oldName) {
        String newName = (String) JOptionPane.showInputDialog(this,
                "الاسم الجديد للحالة:",
                "تعديل الحالة", JOptionPane.PLAIN_MESSAGE, null, null, oldName);
        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(oldName)) {
            try {
                // Delete old + add new — simple rename workaround
                StatusesService.deleteStatus(oldName);
                StatusesService.addStatus(newName.trim());
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التعديل: " + ex.getMessage(),
                        "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
