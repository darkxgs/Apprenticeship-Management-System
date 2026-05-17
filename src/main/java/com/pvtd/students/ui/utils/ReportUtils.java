package com.pvtd.students.ui.utils;

import javax.swing.JOptionPane;
import java.awt.Component;

public class ReportUtils {
    public static String[] chooseMonths(Component parent) {
        String[] monthsList = {
            "يناير", "فبراير", "مارس", "أبريل",
            "مايو", "يونيو", "يوليو", "أغسطس",
            "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };
        
        int currentYearNum = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String[] yearsList = new String[25];
        for (int i = 0; i < 25; i++) {
            yearsList[i] = String.valueOf(currentYearNum - 10 + i);
        }

        javax.swing.JComboBox<String> admissionMonthCombo = new javax.swing.JComboBox<>(monthsList);
        admissionMonthCombo.setSelectedItem("أكتوبر");
        javax.swing.JComboBox<String> admissionYearCombo = new javax.swing.JComboBox<>(yearsList);
        admissionYearCombo.setSelectedItem(String.valueOf(currentYearNum - 3));

        javax.swing.JComboBox<String> examMonthCombo = new javax.swing.JComboBox<>(monthsList);
        examMonthCombo.setSelectedItem("يوليو");
        javax.swing.JComboBox<String> examYearCombo = new javax.swing.JComboBox<>(yearsList);
        examYearCombo.setSelectedItem(String.valueOf(currentYearNum));

        java.awt.Font font = new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16);
        admissionMonthCombo.setFont(font); admissionYearCombo.setFont(font);
        examMonthCombo.setFont(font); examYearCombo.setFont(font);

        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(4, 2, 10, 10));
        panel.setComponentOrientation(java.awt.ComponentOrientation.RIGHT_TO_LEFT);

        javax.swing.JLabel lbl1 = new javax.swing.JLabel("شهر دفعة القبول:", javax.swing.SwingConstants.RIGHT); lbl1.setFont(font);
        javax.swing.JLabel lbl2 = new javax.swing.JLabel("سنة دفعة القبول:", javax.swing.SwingConstants.RIGHT); lbl2.setFont(font);
        javax.swing.JLabel lbl3 = new javax.swing.JLabel("شهر الانعقاد (المنعقد في):", javax.swing.SwingConstants.RIGHT); lbl3.setFont(font);
        javax.swing.JLabel lbl4 = new javax.swing.JLabel("سنة الانعقاد:", javax.swing.SwingConstants.RIGHT); lbl4.setFont(font);

        panel.add(lbl1); panel.add(admissionMonthCombo);
        panel.add(lbl2); panel.add(admissionYearCombo);
        panel.add(lbl3); panel.add(examMonthCombo);
        panel.add(lbl4); panel.add(examYearCombo);

        int result = javax.swing.JOptionPane.showConfirmDialog(parent, panel, "تحديد مواعيد التسويدة",
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (result == javax.swing.JOptionPane.OK_OPTION) {
            String examStr = examMonthCombo.getSelectedItem() + " لسنة " + examYearCombo.getSelectedItem();
            String admissionStr = admissionMonthCombo.getSelectedItem() + " لسنة " + admissionYearCombo.getSelectedItem();
            return new String[]{examStr, admissionStr};
        }

        return null;
    }
}
