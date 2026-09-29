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
    /** الرابط الذي نجح في آخر اختبار — هو الذي يُحفظ */
    private String workingUrl = null;

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

    /**
     * يجرّب كل صيغ الاتصال المعروفة — إصدارات أوراكل المختلفة تحتاج صيغاً
     * مختلفة، فبدل ما يفشل المستخدم بسبب الصيغة نجرّبها له كلها.
     * يرجع الرابط الذي نجح، أو null مع تعبئة lastError.
     */
    private String lastError = "";

    private String findWorkingUrl() {
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        String firstError = null;
        for (String url : ConfigManager.candidateUrls(
                hostField.getText(), portField.getText(), sidField.getText())) {
            String err = DatabaseConnection.testConnection(url, user, pass);
            if (err == null) return url;
            if (firstError == null) firstError = err;
            // كلمة مرور أو مستخدم خطأ: لا فائدة من تجربة باقي الصيغ
            if (err.contains("ORA-01017") || err.contains("invalid username")) {
                firstError = err;
                break;
            }
        }
        lastError = firstError == null ? "سبب غير معروف" : firstError;
        return null;
    }

    private void testConnection() {
        statusLbl.setForeground(Color.DARK_GRAY);
        statusLbl.setText("جاري الاختبار...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingUtilities.invokeLater(() -> {
            String ok = findWorkingUrl();
            setCursor(Cursor.getDefaultCursor());
            if (ok != null) {
                workingUrl = ok;
                statusLbl.setForeground(new Color(0x15803D));
                statusLbl.setText("تم الاتصال بنجاح ✔   (" + ok + ")");
            } else {
                workingUrl = null;
                statusLbl.setForeground(new Color(0xC0392B));
                statusLbl.setText("<html><div style='text-align:center'>فشل الاتصال:<br>"
                        + lastError + "<br><b>" + hintFor(lastError) + "</b></div></html>");
            }
        });
    }

    /** ترجمة رسالة أوراكل إلى سبب مفهوم وخطوة عملية */
    private static String hintFor(String err) {
        if (err == null) return "";
        if (err.contains("ORA-01017") || err.contains("invalid username"))
            return "اسم المستخدم أو كلمة المرور غير صحيحة — جرّب كلمة المرور التي وضعتها أثناء تنصيب أوراكل.";
        if (err.contains("ORA-12505") || err.contains("ORA-12514"))
            return "اسم القاعدة غير صحيح — في أوراكل الحديث اكتب XEPDB1 بدل XE.";
        if (err.contains("ORA-12541") || err.contains("no listener"))
            return "خدمة أوراكل غير مشغّلة على الجهاز — شغّل خدمة OracleServiceXE و Listener.";
        if (err.contains("Network Adapter") || err.contains("ORA-12170") || err.contains("timed out"))
            return "لا يمكن الوصول للجهاز — راجع العنوان وجدار الحماية على منفذ 1521.";
        if (err.contains("ORA-28000")) return "الحساب مقفول — افتحه من أوراكل.";
        if (err.contains("ORA-28001")) return "كلمة المرور منتهية — غيّرها من أوراكل.";
        return "";
    }

    private void saveAndClose() {
        try {
            // إن لم يُختبر الاتصال بعد، نبحث عن صيغة ناجحة قبل الحفظ
            String url = workingUrl;
            if (url == null) url = findWorkingUrl();
            if (url == null) {
                int go = JOptionPane.showConfirmDialog(this,
                        "تعذّر الاتصال بالإعدادات المكتوبة:\n" + lastError
                        + "\n\n" + hintFor(lastError)
                        + "\n\nهل تريد حفظها رغم ذلك؟",
                        "الاتصال غير ناجح", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (go != JOptionPane.YES_OPTION) return;
                url = currentUrl();
            }
            ConfigManager.saveDbSettings(url, userField.getText(),
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
