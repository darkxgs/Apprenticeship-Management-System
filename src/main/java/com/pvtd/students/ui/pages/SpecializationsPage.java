package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Specialization;
import com.pvtd.students.services.SpecializationService;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.DropShadowBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * صفحة التخصصات — عرض كل تخصص في كرد مع زر تعديل وحذف
 */
public class SpecializationsPage extends JPanel {

    private JPanel gridPanel;
    private JLabel countLabel;

    public SpecializationsPage(AppFrame frame) {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        setBackground(UITheme.BG_LIGHT);

        // ─── Header ───────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel pageTitle = new JLabel("التخصصات", SwingConstants.RIGHT);
        pageTitle.setFont(UITheme.FONT_TITLE);
        pageTitle.setForeground(UITheme.TEXT_PRIMARY);
        pageTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);

        countLabel = new JLabel("", SwingConstants.RIGHT);
        countLabel.setFont(UITheme.FONT_BODY);
        countLabel.setForeground(UITheme.TEXT_SECONDARY);
        countLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        countLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        titleBlock.add(pageTitle);
        titleBlock.add(countLabel);

        JButton btnAdd = new JButton("＋ إضافة تخصص");
        btnAdd.setFont(UITheme.FONT_HEADER);
        btnAdd.setBackground(UITheme.SUCCESS);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(160, 44));
        btnAdd.addActionListener(e -> {
            String specName = JOptionPane.showInputDialog(this,
                    "أدخل اسم التخصص الجديد:", "إضافة تخصص", JOptionPane.PLAIN_MESSAGE);
            if (specName != null && !specName.trim().isEmpty()) {
                SpecializationService.addSpecialization(
                        SpecializationService.getDefaultDepartmentId(), specName.trim(), "");
                loadSpecializations();
            }
        });

        JPanel leftArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftArea.setOpaque(false);
        leftArea.add(btnAdd);

        header.add(titleBlock, BorderLayout.EAST);
        header.add(leftArea, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ─── Grid Panel (wraps with FlowLayout) ───────────────────────────────
        gridPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadSpecializations();
    }

    private void loadSpecializations() {
        gridPanel.removeAll();
        List<Specialization> list = SpecializationService.getSpecializationsByDepartment(
                SpecializationService.getDefaultDepartmentId());
        for (Specialization sp : list) {
            gridPanel.add(buildCard(sp));
        }
        countLabel.setText("إجمالي التخصصات: " + list.size());
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildCard(Specialization spec) {
        int count = SubjectService.countSubjectsBySpecialization(spec.getId());
        Color badgeBg = count == 0 ? new Color(0xF1F5F9) : count >= 5 ? new Color(0xDCFCE7) : new Color(0xE0F2FE);
        Color badgeFg = count == 0 ? new Color(0x94A3B8) : count >= 5 ? new Color(0x16A34A) : new Color(0x0284c7);

        // ── Card Shell ────────────────────────────────────────────────────────
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Top accent
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), 6, 6, 6);
                // White body
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 3, getWidth(), getHeight() - 3);
                // Border
                g2.setColor(UITheme.BORDER);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(260, 175));
        card.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 4, 0.06f, 14, UITheme.BG_LIGHT),
                new EmptyBorder(18, 18, 14, 18)));

        // ── Top Row: icon + badge ─────────────────────────────────────────────
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel icon = new JLabel("📚");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel badge = new JLabel("  " + count + " مادة  ", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setForeground(badgeFg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));

        topRow.add(icon, BorderLayout.EAST);
        topRow.add(badge, BorderLayout.WEST);
        card.add(topRow, BorderLayout.NORTH);

        // ── Middle: Spec Name ─────────────────────────────────────────────────
        JLabel nameLabel = new JLabel(
                "<html><div style='text-align:right;'>" + spec.getName() + "</div></html>",
                SwingConstants.RIGHT);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        card.add(nameLabel, BorderLayout.CENTER);

        // ── Bottom: Action Buttons ────────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnEdit = buildMiniBtn("تعديل", new Color(0xEFF6FF), new Color(0x2563EB));
        btnEdit.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "الاسم الجديد:", spec.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                SpecializationService.addSpecialization(spec.getDepartmentId(), newName.trim(), "");
                loadSpecializations();
            }
        });

        JButton btnDel = buildMiniBtn("حذف", new Color(0xFEF2F2), UITheme.DANGER);
        btnDel.addActionListener(e -> {
            String msg = "حذف تخصص «" + spec.getName() + "»?" +
                    (count > 0 ? "\nتحذير: سيتم حذف " + count + " مادة مرتبطة!" : "");
            if (JOptionPane.showConfirmDialog(this, msg, "تأكيد الحذف",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                SpecializationService.deleteSpecialization(spec.getId());
                loadSpecializations();
            }
        });

        actions.add(btnDel);
        actions.add(btnEdit);
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    private JButton buildMiniBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }
}
