package com.pvtd.students.ui.pages;

import com.pvtd.students.services.StatusesService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.DropShadowBorder;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class StatusesPage extends JPanel {

    private final AppFrame frame;
    private JPanel gridPanel;

    // Semantic Color map - [bgColor, accentColor]
    private static final Color[][] PALETTE = {
            { new Color(0xDBEAFE), new Color(0x2563EB) }, // blue
            { new Color(0xDCFCE7), new Color(0x059669) }, // green
            { new Color(0xFEE2E2), new Color(0xDC2626) }, // red
            { new Color(0xFEF3C7), new Color(0xD97706) }, // amber
            { new Color(0xEDE9FE), new Color(0x7C3AED) }, // violet
            { new Color(0xFCE7F3), new Color(0xBE185D) }, // pink
            { new Color(0xF1F5F9), new Color(0x475569) }, // slate
            { new Color(0xFFEDD5), new Color(0xEA580C) }, // orange
    };

    // Color labels that are shown in the color picker combo
    private static final String[] COLOR_NAMES = {
            "ازرق", "اخضر", "احمر", "ذهبي", "بنفسجي", "وردي", "رمادي", "برتقالي"
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

        JButton btnAdd = new JButton("+ إضافة حالة جديدة");
        btnAdd.setFont(UITheme.FONT_HEADER);
        btnAdd.setBackground(UITheme.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(190, 40));
        btnAdd.addActionListener(e -> handleAdd());

        header.add(titleBlock, BorderLayout.EAST);
        header.add(btnAdd, BorderLayout.WEST);
        return header;
    }

    // ── Card Grid ────────────────────────────────────────────────────────────────────
    private JScrollPane buildGrid() {
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 5, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setBackground(UITheme.BG_LIGHT);
        gridPanel.setBorder(new EmptyBorder(16, 24, 24, 24));

        // TO PREVENT VERTICAL STRETCHING: Wrap gridPanel in a panel that keeps it at the top
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(topWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int availableWidth = scroll.getViewport().getWidth();
                int cardWidth = 270 + 16;
                int cols = Math.max(1, availableWidth / cardWidth);
                if (gridPanel.getLayout() instanceof GridLayout) {
                    ((GridLayout) gridPanel.getLayout()).setColumns(cols);
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });

        return scroll;
    }

    private void loadCards() {
        gridPanel.removeAll();
        java.util.Map<String, Integer> counts = com.pvtd.students.services.StudentService.getDashboardStats();

        List<String> statuses = StatusesService.getAllStatuses();
        for (String status : statuses) {
            Color[] colors = getSemanticColor(status);
            gridPanel.add(buildCard(status, colors[0], colors[1], counts.getOrDefault(status, 0)));
        }
        if (statuses.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel(
                "<html><div style='text-align:center'>لا توجد حالات مُضافة بعد.<br>اضغط &laquo;+ إضافة حالة جديدة&raquo; لإنشاء أول حالة.</div></html>",
                SwingConstants.CENTER);
            empty.setFont(new Font("Tahoma", Font.PLAIN, 15));
            empty.setForeground(UITheme.TEXT_SECONDARY);
            empty.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gridPanel.add(empty, BorderLayout.CENTER);
        } else {
            if (!(gridPanel.getLayout() instanceof GridLayout)) {
                gridPanel.setLayout(new GridLayout(0, 5, 16, 16));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private Color[] getSemanticColor(String status) {
        if (status.contains("ناجح"))
            return new Color[] { new Color(0xDCFCE7), new Color(0x059669) };
        if (status.contains("راسب"))
            return new Color[] { new Color(0xFEE2E2), new Color(0xDC2626) };
        if (status.contains("دور ثاني"))
            return new Color[] { new Color(0xFFEDD5), new Color(0xEA580C) };
        if (status.contains("مؤجل"))
            return new Color[] { new Color(0xDBEAFE), new Color(0x2563EB) };
        if (status.contains("معتذر"))
            return new Color[] { new Color(0xF1F5F9), new Color(0x475569) };
        if (status.contains("غائب"))
            return new Color[] { new Color(0xFEF3C7), new Color(0xD97706) };
        if (status.contains("محروم") || status.contains("مفصول"))
            return new Color[] { new Color(0xFCE7F3), new Color(0xBE185D) };
        int hash = Math.abs(status.hashCode()) % PALETTE.length;
        return PALETTE[hash];
    }

    private JPanel buildCard(String statusName, Color bgColor, Color accentColor, int studentCount) {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        // Fixed height to prevent stretching
        cardWrapper.setPreferredSize(new Dimension(270, 130));
        cardWrapper.setMinimumSize(new Dimension(270, 130));
        cardWrapper.setMaximumSize(new Dimension(270, 130));
        cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 6, 0.07f, 16, UITheme.BG_LIGHT),
                new EmptyBorder(0, 0, 0, 0)));

        JPanel cardInner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Base white card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Colored top bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 10, 16, 16);
                g2.fillRect(0, 6, getWidth(), 4);
                g2.dispose();
            }
        };
        cardInner.setOpaque(false);
        cardInner.setBorder(new EmptyBorder(18, 16, 14, 16));

        // Top: Status Name + Count
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JTextArea nameLbl = new JTextArea(statusName);
        nameLbl.setFont(new Font("Tahoma", Font.BOLD, 14));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        nameLbl.setBackground(Color.WHITE);
        nameLbl.setOpaque(false);
        nameLbl.setEditable(false);
        nameLbl.setFocusable(false);
        nameLbl.setLineWrap(true);
        nameLbl.setWrapStyleWord(true);
        nameLbl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        nameLbl.setBorder(null);

        // Colored dot badge
        JPanel dotBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(accentColor);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.fillOval(cx - 4, cy - 4, 8, 8);
                g2.dispose();
            }
        };
        dotBadge.setOpaque(false);
        dotBadge.setPreferredSize(new Dimension(28, 22));

        topRow.add(nameLbl, BorderLayout.EAST);
        topRow.add(dotBadge, BorderLayout.WEST);

        // Bottom: Count label + Buttons
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel countLbl = new JLabel("الطلاب: " + studentCount, SwingConstants.LEFT);
        countLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLbl.setForeground(UITheme.TEXT_SECONDARY);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);

        // Edit: filled primary
        JButton btnEdit = new JButton("تعديل");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setBackground(accentColor);
        btnEdit.setFocusPainted(false);
        btnEdit.setPreferredSize(new Dimension(72, 28));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> handleRename(statusName));

        // Delete: danger outline
        JButton btnDel = new JButton("حذف") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.DANGER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDel.setContentAreaFilled(false);
        btnDel.setBorderPainted(false);
        btnDel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDel.setForeground(UITheme.DANGER);
        btnDel.setFocusPainted(false);
        btnDel.setPreferredSize(new Dimension(65, 28));
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.addActionListener(e -> handleDelete(statusName));

        btnRow.add(btnEdit);
        btnRow.add(btnDel);

        bottomRow.add(countLbl, BorderLayout.EAST);
        bottomRow.add(btnRow, BorderLayout.WEST);

        cardInner.add(topRow, BorderLayout.NORTH);
        cardInner.add(bottomRow, BorderLayout.SOUTH);
        cardWrapper.add(cardInner, BorderLayout.CENTER);

        // Hover effect
        cardWrapper.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                        new DropShadowBorder(Color.BLACK, 12, 0.12f, 18, UITheme.BG_LIGHT),
                        new EmptyBorder(0, 0, 0, 0)));
                cardWrapper.revalidate();
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                        new DropShadowBorder(Color.BLACK, 6, 0.07f, 16, UITheme.BG_LIGHT),
                        new EmptyBorder(0, 0, 0, 0)));
                cardWrapper.revalidate();
            }
        });

        return cardWrapper;
    }

    // ── Actions ────────────────────────────────────────────────────────────────
    private void handleAdd() {
        // Dialog with name + color picker
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField nameField = new JTextField();
        nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JComboBox<String> colorCombo = new JComboBox<>(COLOR_NAMES);
        colorCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        // Render each item with its color swatch
        colorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                if (idx >= 0 && idx < PALETTE.length)
                    setBackground(PALETTE[idx][0]);
                return this;
            }
        });

        p.add(new JLabel("اسم الحالة:", SwingConstants.RIGHT));
        p.add(nameField);
        p.add(new JLabel("اللون:", SwingConstants.RIGHT));
        p.add(colorCombo);

        int result = JOptionPane.showConfirmDialog(this, p, "إضافة حالة جديدة", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                warn("اسم الحالة لا يمكن أن يكون فارغاً.");
                return;
            }
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                StatusesService.addStatus(name, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(String statusName) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف الحالة: «" + statusName + "»؟\nسيؤثر ذلك على الطلاب الحاملين لهذه الحالة.",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                StatusesService.deleteStatus(statusName, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "لا يمكن حذف هذه الحالة: " + ex.getMessage(), "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRename(String oldName) {
        String newName = (String) JOptionPane.showInputDialog(this,
                "الاسم الجديد للحالة:",
                "تعديل الحالة", JOptionPane.PLAIN_MESSAGE, null, null, oldName);
        if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(oldName)) {
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                StatusesService.deleteStatus(oldName, user);
                StatusesService.addStatus(newName.trim(), user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التعديل: " + ex.getMessage(), "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
}
