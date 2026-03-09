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
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 25, 25)); // 2 cards per row
        gridPanel.setOpaque(false);

        gridPanel.add(createReportCard("تقرير الطلاب  الناجحين",
                "استخراج تقرير  بي جميع الطلاب الناجحين",
                "icons/dashboard.svg", () -> {

                   SucssfullPageEdit s = new SucssfullPageEdit();
                   s.setVisible(true);
                   s.setExtendedState(JFrame.MAXIMIZED_BOTH);
                  
                }));

        gridPanel.add(createReportCard("كشف الطلاب المفصولين",
                "اصدار قائمة كاملة بجميع الطلاب  المفصولين بملف pdf .",
                "icons/reports.svg",
                () -> {
                
                DetailersFRamepage de =new DetailersFRamepage();
                    de.setVisible(true);
                    de.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
                }));

        gridPanel.add(createReportCard("كشف الراسبين",
                " اصدار تقرير بي جميع الطلاب الراسبين",
                "icons/reports.svg",
                () -> {
                    FailedFramePage pa = new FailedFramePage();
                pa.setVisible(true);
                pa.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
                        } ));
        
        gridPanel.add(createReportCard("كشف المحرومين",
                "اصدار تقرير بي الطلاب المحرومين",
                "icons/reports.svg",
                () -> {
                DeprivedFramePage de=new DeprivedFramePage();
                de.setVisible(true);
                de.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }));
        
        gridPanel.add(createReportCard("كشف دور ثاني",
                "اصدار تقرير بي الطلاب الدور ثاني",
                "icons/reports.svg",
                () -> {
                
                    ScoundRoundFramePage sc =new ScoundRoundFramePage();
                   sc.setVisible(true);
                sc.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
                
                }));
        
        gridPanel.add(createReportCard("كشف المعتذرين",
                "اصدار تقرير بي الطلاب المعتذرين",
                "icons/reports.svg",
                () -> {
                
                    ApologeticFramePage sc =new ApologeticFramePage();
                   sc.setVisible(true);
                sc.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
                
                }));
        
        gridPanel.add(createReportCard("كشف غائبون ",
                "اصدار تقرير بي الطلاب  غائبون ",
                "icons/reports.svg",
                () -> {
                
                    absentFramePage frame =new absentFramePage();
                     frame.setVisible(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
                
                }));
        
        gridPanel.add(createReportCard("كشف المؤجلين",
                "اصدار تقرير بي الطلاب المؤجلين",
                "icons/reports.svg",
                () -> {
                
                    delayedFramePage frame =new delayedFramePage();
                     frame.setVisible(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                
                
                }));
        
        
        
        
        
        

        gridPanel.add(createReportCard("تقرير إحصائي",
                "توليد تقرير رسومي عن نسب النجاح والرسوب بصيغة PDF.",
                "icons/students.svg",
                () -> JOptionPane.showMessageDialog(this, "جاري العمل على تصدير التقرير...")));

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20); // سرعة السكرول
        scrollPane.getVerticalScrollBar().setBlockIncrement(60);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, String description, String iconPath, Runnable action) {

        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setPreferredSize(new Dimension(350, 220)); // نفس حجم كل كارد
        card.setOpaque(false);

        card.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(25, 25, 25, 25)));

        JLabel iconLbl = new JLabel();
        try {

            FlatSVGIcon icon = new FlatSVGIcon(iconPath, 48, 48);
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
        titleLbl.setFont(UITheme.FONT_CARD_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel descLbl = new JLabel(
                "<html><div style='text-align:center;width:220px;'>" + description + "</div></html>",
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

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(actionBtn);

        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }
}