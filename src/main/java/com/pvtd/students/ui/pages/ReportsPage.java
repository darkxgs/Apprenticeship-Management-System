package com.pvtd.students.ui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.pvtd.students.ui.utils.UITheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

public class ReportsPage extends JPanel {

    public ReportsPage() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setBackground(UITheme.BG_LIGHT);

        JLabel titleLabel = new JLabel("التقارير والإحصائيات", SwingConstants.RIGHT);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        gridPanel.setOpaque(false);

        gridPanel.add(createReportCard("شهادة طالب", "استخراج شهادة نجاح مفصلة لطالب محدد برقم الجلوس.",
                "icons/dashboard.svg", () -> {
                    String seatNo = JOptionPane.showInputDialog(this, "أدخل رقم جلوس الطالب:", "شهادة طالب",
                            JOptionPane.QUESTION_MESSAGE);
                    if (seatNo != null && !seatNo.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "جاري العمل على توليد الشهادة للطالب رقم: " + seatNo,
                                "تحت الإنشاء", JOptionPane.INFORMATION_MESSAGE);
                    }
                }));

        gridPanel.add(createReportCard("كشف الناجحين", "اصدار قائمة كاملة بجميع الطلاب الناجحين بملف إكسل.",
                "icons/reports.svg", () -> {
                    JOptionPane.showMessageDialog(this, "جاري العمل على تصدير ملف الإكسل...", "تحت الإنشاء",
                            JOptionPane.INFORMATION_MESSAGE);
                }));

        gridPanel.add(createReportCard("كشف الراسبين", "اصدار قائمة بالطلاب الراسبين موزعة حسب المراكز.",
                "icons/reports.svg", () -> {
                    JOptionPane.showMessageDialog(this, "جاري العمل على تصدير ملف الإكسل...", "تحت الإنشاء",
                            JOptionPane.INFORMATION_MESSAGE);
                }));

        gridPanel.add(createReportCard("تقرير إحصائي", "توليد تقرير رسومي عن نسب النجاح والرسوب بصيغة PDF.",
                "icons/students.svg", () -> {
                    JOptionPane.showMessageDialog(this, "جاري العمل على تصدير التقرير...", "تحت الإنشاء",
                            JOptionPane.INFORMATION_MESSAGE);
                }));

        add(gridPanel, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, String description, String iconPath, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(25, 25, 25, 25)));

        // Icon
        JLabel iconLbl = new JLabel();
        try {
            FlatSVGIcon icon = new FlatSVGIcon(iconPath, 48, 48);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UITheme.PRIMARY));
            iconLbl.setIcon(icon);
        } catch (Exception e) {
        }
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(iconLbl, BorderLayout.NORTH);

        // Content
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);
        textPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_CARD_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>",
                SwingConstants.CENTER);
        descLbl.setFont(UITheme.FONT_BODY);
        descLbl.setForeground(UITheme.TEXT_SECONDARY);

        textPanel.add(titleLbl);
        textPanel.add(descLbl);

        card.add(textPanel, BorderLayout.CENTER);

        JButton actionBtn = new JButton("تصدير الآن");
        actionBtn.setFont(UITheme.FONT_HEADER);
        actionBtn.setForeground(UITheme.PRIMARY);
        actionBtn.setBackground(UITheme.CARD_BG);
        actionBtn.putClientProperty("JButton.buttonType", "roundRect");
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionBtn.addActionListener(e -> action.run());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(actionBtn);

        card.add(btnPanel, BorderLayout.SOUTH);

        // Hover Lift Animation
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 10, 0.12f, 20, UITheme.CARD_BG),
                        new EmptyBorder(20, 25, 30, 25)));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20, UITheme.CARD_BG),
                        new EmptyBorder(25, 25, 25, 25)));
            }
        });

        return card;
    }
}
