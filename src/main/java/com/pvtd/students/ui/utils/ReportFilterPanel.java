package com.pvtd.students.ui.utils;

import com.pvtd.students.ui.components.Combobox;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JPanel;

public class ReportFilterPanel extends JPanel {
    private Combobox examMonthCombo;
    private Combobox examYearCombo;
    private Combobox admissionMonthCombo;
    private Combobox admissionYearCombo;

    public ReportFilterPanel() {
        setBackground(new Color(0, 102, 102));
        setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        examMonthCombo = new Combobox();
        examMonthCombo.setLabeText("شهر المنعقد فيه");
        examMonthCombo.setPreferredSize(new java.awt.Dimension(140, 45));
        
        examYearCombo = new Combobox();
        examYearCombo.setLabeText("سنة المنعقد فيه");
        examYearCombo.setPreferredSize(new java.awt.Dimension(140, 45));

        admissionMonthCombo = new Combobox();
        admissionMonthCombo.setLabeText("شهر دفعة القبول");
        admissionMonthCombo.setPreferredSize(new java.awt.Dimension(140, 45));

        admissionYearCombo = new Combobox();
        admissionYearCombo.setLabeText("سنة دفعة القبول");
        admissionYearCombo.setPreferredSize(new java.awt.Dimension(140, 45));

        String[] months = {
            "يناير", "فبراير", "مارس", "أبريل",
            "مايو", "يونيو", "يوليو", "أغسطس",
            "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };

        for (String m : months) {
            examMonthCombo.addItem(m);
            admissionMonthCombo.addItem(m);
        }
        examMonthCombo.setSelectedItem("يوليو");
        admissionMonthCombo.setSelectedItem("أكتوبر");

        // Populate Years (2000 to 9999)
        admissionYearCombo.addItem("الكل");
        for (int y = 2000; y <= 9999; y++) {
            String yearStr = String.valueOf(y);
            admissionYearCombo.addItem(yearStr);
            examYearCombo.addItem(yearStr);
        }

        String currentYear = String.valueOf(java.time.Year.now().getValue());
        examYearCombo.setSelectedItem(currentYear);
        admissionYearCombo.setSelectedItem("2023"); // Default for admission year if desired, or "الكل"

        add(admissionYearCombo);
        add(admissionMonthCombo);
        add(examYearCombo);
        add(examMonthCombo);
    }

    public void addFilterChangeListener(java.awt.event.ActionListener listener) {
        examMonthCombo.addActionListener(listener);
        examYearCombo.addActionListener(listener);
        admissionMonthCombo.addActionListener(listener);
        admissionYearCombo.addActionListener(listener);
    }

    public String[] getSelectedMonths() {
        String examMonth = examMonthCombo.getSelectedItem() != null ? examMonthCombo.getSelectedItem().toString() : "";
        String examYear = examYearCombo.getSelectedItem() != null ? examYearCombo.getSelectedItem().toString() : "";
        String admissionMonth = admissionMonthCombo.getSelectedItem() != null ? admissionMonthCombo.getSelectedItem().toString() : "";
        String admissionYear = admissionYearCombo.getSelectedItem() != null ? admissionYearCombo.getSelectedItem().toString() : "";

        String formattedExam = examMonth + (examYear.isEmpty() ? "" : " لسنة " + toArabicNumbers(examYear));
        String formattedAdmission = admissionMonth + (admissionYear.equals("الكل") || admissionYear.isEmpty() ? "" : " لسنة " + toArabicNumbers(admissionYear));

        return new String[]{
            examMonth, 
            examYear, 
            admissionMonth, 
            admissionYear, 
            formattedExam, 
            formattedAdmission
        };
    }

    private String toArabicNumbers(String number) {
        if (number == null) return "";
        return number
                .replace("0", "٠")
                .replace("1", "١")
                .replace("2", "٢")
                .replace("3", "٣")
                .replace("4", "٤")
                .replace("5", "٥")
                .replace("6", "٦")
                .replace("7", "٧")
                .replace("8", "٨")
                .replace("9", "٩");
    }
}
