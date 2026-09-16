package com.pvtd.students.db;

import java.awt.*;
import javax.swing.*;

/**
 * شاشة إعدادات الاتصال بقاعدة البيانات.
 * تظهر تلقائياً عند فشل الاتصال عند بدء التشغيل، وتتيح تغيير عنوان الخادم
 * (IP) دون فتح أي ملفات — لأن نقل القاعدة إلى جهاز آخر يغيّر العنوان.
 */
public class DbSettingsDialog extends JDialog {

    private final JTextField hostField = new JTextField(16);
    private final JTextField portField = new JTextField(6);
    private final JTextField sidField  = new JTextField(8);
    private final JTextField userField = new JTextField(12);
    private final JPasswordField passField = new JPasswordField(12);
    private final JLabel statusLbl = new JLabel(" ");
    private boolean saved = false;

    public DbSettingsDialog(Frame owner) {
        super(owner, "إعدادات الاتصال بقاعدة البيانات", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        root.setBackground(Color.WHITE);
        root.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel title = new JLabel("إعدادات الاتصال بقاعدة البيانات", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0x1A5F7A));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gc, 0, "عنوان الخادم (IP):", hostField);
        addRow(form, gc, 1, "المنفذ:", portField);
        addRow(form, gc, 2, "اسم القاعدة (SID):", sidField);
        addRow(form, gc, 3, "المستخدم:", userField);
        addRow(form, gc, 4, "كلمة المرور:", passField);

        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        form.add(statusLbl, gc);

        JLabel pathLbl = new JLabel("ملف الإعدادات: " + ConfigManager.activePath(),
                SwingConstants.CENTER);
        pathLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pathLbl.setForeground(Color.GRAY);
        gc.gridy = 6;
        form.add(pathLbl, gc);

        root.add(form, BorderLayout.CENTER);

        JButton testBtn = new JButton("اختبار الاتصال");
        testBtn.addActionListener(e -> testConnection());

        JButton saveBtn = new JButton("حفظ وإعادة المحاولة");
        saveBtn.setBackground(new Color(0x1A5F7A));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> saveAndClose());

        JButton cancelBtn = new JButton("إلغاء");
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(testBtn);
        buttons.add(saveBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        loadCurrent();
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent field) {
        gc.gridwidth = 1;
        gc.gridx = 1; gc.gridy = y; gc.weightx = 0;
        JLabel l = new JLabel(label, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(l, gc);
        gc.gridx = 0; gc.weightx = 1;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(field, gc);
    }

    /** تفكيك الرابط الحالي إلى خانات مقروءة */
    private void loadCurrent() {
        String url = ConfigManager.get("db.url", "jdbc:oracle:thin:@localhost:1521:XE");
        String host = "localhost", port = "1521", sid = "XE";
        try {
            String tail = url.substring(url.indexOf('@') + 1);
            String[] parts = tail.split(":");
            if (parts.length >= 1) host = parts[0];
            if (parts.length >= 2) port = parts[1];
            if (parts.length >= 3) sid = parts[2];
        } catch (Exception ignore) { }
        hostField.setText(host);
        portField.setText(port);
        sidField.setText(sid);
        userField.setText(ConfigManager.get("db.user", "system"));
        passField.setText(ConfigManager.get("db.password", ""));
    }

    private String currentUrl() {
        return ConfigManager.buildUrl(hostField.getText(), portField.getText(), sidField.getText());
    }

    private void testConnection() {
        statusLbl.setForeground(Color.DARK_GRAY);
        statusLbl.setText("جاري الاختبار...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingUtilities.invokeLater(() -> {
            String err = DatabaseConnection.testConnection(currentUrl(),
                    userField.getText(), new String(passField.getPassword()));
            setCursor(Cursor.getDefaultCursor());
            if (err == null) {
                statusLbl.setForeground(new Color(0x15803D));
                statusLbl.setText("تم الاتصال بنجاح ✔");
            } else {
                statusLbl.setForeground(new Color(0xC0392B));
                statusLbl.setText("فشل الاتصال: " + err);
            }
        });
    }

    private void saveAndClose() {
        try {
            ConfigManager.saveDbSettings(currentUrl(), userField.getText(),
                    new String(passField.getPassword()));
            saved = true;
            JOptionPane.showMessageDialog(this,
                    "تم حفظ الإعدادات.\nسيتم إغلاق البرنامج — افتحيه من جديد للاتصال بالخادم الجديد.",
                    "تم الحفظ", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "تعذّر حفظ الإعدادات:\n" + ex.getMessage()
                    + "\n\nالملف: " + ConfigManager.activePath(),
                    "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }

    /** يعرض الشاشة ويرجع true إذا حُفظت إعدادات جديدة */
    public static boolean showDialog(Frame owner) {
        DbSettingsDialog d = new DbSettingsDialog(owner);
        d.setVisible(true);
        return d.isSaved();
    }
}
