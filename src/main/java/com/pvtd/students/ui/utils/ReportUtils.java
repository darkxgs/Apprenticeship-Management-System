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
        String examMonth = (String) JOptionPane.showInputDialog(parent, "اختر المنعقد فيه:", "تحديد الموعد",
                JOptionPane.QUESTION_MESSAGE, null, monthsList, "يوليو");
        if (examMonth == null) return null;
        
        String admissionMonth = (String) JOptionPane.showInputDialog(parent, "اختر شهر دفعة القبول:", "تحديد الموعد",
                JOptionPane.QUESTION_MESSAGE, null, monthsList, "أكتوبر");
        if (admissionMonth == null) return null;
        
        return new String[]{examMonth, admissionMonth};
    }
}
