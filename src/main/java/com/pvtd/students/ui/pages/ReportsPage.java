package com.pvtd.students.ui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.pvtd.students.ui.utils.UITheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.pvtd.students.ui.pages.Report.ApologeticFramePage;
import com.pvtd.students.ui.pages.Report.DeprivedFramePage;
import com.pvtd.students.ui.pages.Report.DetailersFRamepage;
import com.pvtd.students.ui.pages.Report.FailedFramePage;
import com.pvtd.students.ui.pages.Report.ScoundRoundFramePage;
import com.pvtd.students.ui.pages.Report.SucssfullPageEdit;
import com.pvtd.students.ui.pages.Report.absentFramePage;
import com.pvtd.students.ui.pages.Report.delayedFramePage;
import com.pvtd.students.ui.pages.Report.sucsseccFromPage;
import com.pvtd.students.ui.pages.Report.EltaSoeda;

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

        // Grid Panel
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 25, 25));
        gridPanel.setOpaque(false);

        gridPanel.add(createReportCard("كشف الطلاب الناجحين",
                "استخراج تقرير بجميع الطلاب الناجحين",
                "icons/dashboard.svg", () -> {

                    SucssfullPageEdit s = new SucssfullPageEdit();
                    s.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    s.setVisible(true);
                    s.setExtendedState(JFrame.MAXIMIZED_BOTH);

                }));

        gridPanel.add(createReportCard("كشف الطلاب الراسبين",
                "استخراج تقرير بجميع الطلاب الناجحين",
                "icons/dashboard.svg", () -> {

                    FailedFramePage pa = new FailedFramePage();
                    pa.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    pa.setVisible(true);
                    pa.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }));

        gridPanel.add(createReportCard("كشف الطلاب المفصولين",
                "إصدار قائمة كاملة بجميع الطلاب المفصولين بملف PDF",
                "icons/reports.svg",
                () -> {

                    DetailersFRamepage de = new DetailersFRamepage();
                    de.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    de.setVisible(true);
                    de.setExtendedState(JFrame.MAXIMIZED_BOTH);

                }));

        gridPanel.add(createReportCard("كشف المحرومين",
                "إصدار تقرير بالطلاب المحرومين",
                "icons/reports.svg",
                () -> {
                    DeprivedFramePage de = new DeprivedFramePage();
                    de.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    de.setVisible(true);
                    de.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }));

        gridPanel.add(createReportCard("كشف دور ثاني",
                "إصدار تقرير بالطلاب (دور ثاني)",
                "icons/reports.svg",
                () -> {

                    ScoundRoundFramePage sc = new ScoundRoundFramePage();
                    sc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    sc.setVisible(true);
                    sc.setExtendedState(JFrame.MAXIMIZED_BOTH);

                }));

        gridPanel.add(createReportCard("كشف المعتذرين",
                "إصدار تقرير بالطلاب المعتذرين",
                "icons/reports.svg",
                () -> {

                    ApologeticFramePage sc = new ApologeticFramePage();
                    sc.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    sc.setVisible(true);
                    sc.setExtendedState(JFrame.MAXIMIZED_BOTH);

                }));

        gridPanel.add(createReportCard("كشف الغائبين",
                "إصدار تقرير بالطلاب الغائبين",
                "icons/reports.svg",
                () -> {

                    absentFramePage frame = new absentFramePage();
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    frame.setVisible(true);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                }));

        gridPanel.add(createReportCard("كشف المؤجلين",
                "إصدار تقرير بالطلاب المؤجلين",
                "icons/reports.svg",
                () -> {

                    delayedFramePage frame = new delayedFramePage();
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 🔥 ده المهم

                    frame.setVisible(true);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                }));

        gridPanel.add(createReportCard("التسويده ",
                "إخراج كشف رصد الدرجات (تلقائياً حسب نظام المادة)",
                "icons/reports.svg",
                () -> {
                   com.pvtd.students.ui.pages.Report.EltaSoeda e = new com.pvtd.students.ui.pages.Report.EltaSoeda();
                   e.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                   e.setVisible(true);
                   e.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }));
        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(60);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, String description, String iconPath, Runnable action) {

        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setPreferredSize(new Dimension(350, 320)); // كبرنا الكارد
        card.setOpaque(false);

        card.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(25, 25, 25, 25)));

        JLabel iconLbl = new JLabel();
        try {
            FlatSVGIcon icon = new FlatSVGIcon(iconPath, 60, 60); // كبرنا الأيقونة
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> UITheme.PRIMARY));
            iconLbl.setIcon(icon);
        } catch (Exception e) {
        }

        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLbl, BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setOpaque(false);
        textPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_CARD_TITLE.deriveFont(20f)); // كبرنا العنوان
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel(
                "<html><div style='text-align:center;'>" + description + "</div></html>",
                SwingConstants.CENTER);

        descLbl.setFont(UITheme.FONT_BODY.deriveFont(15f)); // كبرنا الوصف
        descLbl.setForeground(UITheme.TEXT_SECONDARY);

        textPanel.add(titleLbl);
        textPanel.add(descLbl);

        card.add(textPanel, BorderLayout.CENTER);

        JButton actionBtn = new JButton("تصدير الآن");
        actionBtn.setFont(UITheme.FONT_HEADER.deriveFont(15f));
        actionBtn.setForeground(UITheme.PRIMARY);
        actionBtn.setBackground(UITheme.CARD_BG);
        actionBtn.putClientProperty("JButton.buttonType", "roundRect");
        actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionBtn.addActionListener(e -> action.run());

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(actionBtn);

        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }
}