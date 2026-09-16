package com.pvtd.students;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.LoginFrame;
import com.pvtd.students.ui.SplashScreenFrame;
import com.pvtd.students.ui.utils.UITheme;

public class MainApp {
    public static void main(String[] args) {
        System.out.println("Starting Industrial Apprenticeship Management System...");

        try {

            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Component.accentColor", UITheme.PRIMARY);
            UIManager.put("Component.focusColor", UITheme.HOVER_PRIMARY);
            UIManager.put("Button.arc", UITheme.ARC_MODERN);
            UIManager.put("Component.arc", UITheme.ARC_MODERN);
            UIManager.put("ProgressBar.arc", UITheme.ARC_MODERN);
            UIManager.put("TextComponent.arc", UITheme.ARC_MODERN);
            UIManager.put("defaultFont", UITheme.FONT_BODY);
            UIManager.put("Table.rowHeight", 40);
            UIManager.put("TableHeader.height", 45);
            UIManager.put("TableHeader.font", UITheme.FONT_HEADER);
            UIManager.put("Table.selectionBackground", UITheme.BG_LIGHT);
            UIManager.put("Table.selectionForeground", UITheme.PRIMARY);
            UIManager.put("Table.alternateRowColor", UITheme.BG_LIGHT);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // التحقق من الوصول لقاعدة البيانات قبل أي شيء — لو الخادم اتغيّر
        // (نقل القاعدة لجهاز آخر) نفتح شاشة الإعدادات بدل ما يفشل البرنامج بصمت
        if (!ensureDatabaseReachable()) {
            return;
        }

        System.out.println("Initializing Database Connections...");
        DatabaseConnection.initializeDatabase();

        System.out.println("Checking for updates...");
        com.pvtd.students.services.UpdateService.startUpdateCheck();

        SwingUtilities.invokeLater(() -> {
            new SplashScreenFrame(() -> {
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }).setVisible(true);
        });
    }

    /**
     * يتأكد من إمكانية الاتصال بقاعدة البيانات، ويفتح شاشة الإعدادات عند الفشل
     * حتى يستطيع المستخدم تغيير عنوان الخادم دون تحرير أي ملفات.
     * يرجع false إذا تعذّر الاتصال وقرر المستخدم الخروج.
     */
    private static boolean ensureDatabaseReachable() {
        while (true) {
            String err = DatabaseConnection.currentConnectionError();
            if (err == null) return true;

            final String message =
                    "تعذّر الاتصال بقاعدة البيانات." + NLC + NLC
                    + "الخادم الحالي: " + DatabaseConnection.currentUrl() + NLC
                    + "سبب الفشل: " + err + NLC + NLC
                    + "لو القاعدة اتنقلت لجهاز آخر، اضغط «إعدادات الاتصال» واكتب عنوان الجهاز الجديد.";

            final int[] choice = new int[1];
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> choice[0] =
                        javax.swing.JOptionPane.showOptionDialog(null, message,
                                "فشل الاتصال بقاعدة البيانات",
                                javax.swing.JOptionPane.DEFAULT_OPTION,
                                javax.swing.JOptionPane.ERROR_MESSAGE, null,
                                new Object[] { "خروج", "إعادة المحاولة", "إعدادات الاتصال" },
                                "إعدادات الاتصال"));
            } catch (Exception e) {
                return false;
            }

            if (choice[0] == 2) {          // إعدادات الاتصال
                final boolean[] saved = new boolean[1];
                try {
                    javax.swing.SwingUtilities.invokeAndWait(() ->
                            saved[0] = com.pvtd.students.db.DbSettingsDialog.showDialog(null));
                } catch (Exception e) {
                    return false;
                }
                // الإعدادات تُقرأ مرة واحدة عند التحميل، فالحفظ يتطلب إعادة تشغيل
                if (saved[0]) return false;
            } else if (choice[0] != 1) {   // خروج أو إغلاق النافذة
                return false;
            }
        }
    }

    private static final String NLC = System.lineSeparator();
}