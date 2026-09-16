package com.pvtd.students.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * قراءة وحفظ إعدادات الاتصال بقاعدة البيانات.
 *
 * الملف يُبحث عنه بجوار ملفات البرنامج نفسها (مجلد app داخل التطبيق) وليس
 * بحسب مجلد التشغيل — كان الاعتماد على مجلد التشغيل يجعل تعديل المستخدم
 * للملف يُتجاهل بصمت فيعود البرنامج إلى localhost.
 */
public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final String FILE_NAME = "application.properties";

    /** الملف الذي حُمّلت منه الإعدادات فعلاً (أو الذي سيُحفظ فيه) */
    private static File activeFile = null;

    static {
        loadConfig();
    }

    /** مجلد ملفات البرنامج (حيث يوجد app.jar) */
    private static File codeDir() {
        try {
            File f = new File(ConfigManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return f.isDirectory() ? f : f.getParentFile();
        } catch (Exception e) {
            return null;
        }
    }

    /** كل الأماكن المحتملة للملف، بالأولوية */
    private static java.util.List<File> candidates() {
        java.util.List<File> list = new java.util.ArrayList<>();
        File dir = codeDir();
        if (dir != null) {
            list.add(new File(dir, FILE_NAME));                 // بجوار app.jar
            if (dir.getParentFile() != null) {
                list.add(new File(dir.getParentFile(), FILE_NAME)); // مجلد التطبيق
            }
        }
        list.add(new File(FILE_NAME));                           // مجلد التشغيل
        list.add(new File("app" + File.separator + FILE_NAME));
        return list;
    }

    private static synchronized void loadConfig() {
        properties.clear();
        for (File f : candidates()) {
            if (f.exists() && f.isFile()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    properties.load(fis);
                    activeFile = f;
                    System.out.println("Loaded config from: " + f.getAbsolutePath());
                    return;
                } catch (Exception e) {
                    System.err.println("Failed to load " + f + ": " + e.getMessage());
                }
            }
        }
        // لا يوجد ملف خارجي — نقرأ النسخة الداخلية ونحدد مكان الحفظ المستقبلي
        try (InputStream in = ConfigManager.class.getClassLoader()
                .getResourceAsStream(FILE_NAME)) {
            if (in != null) {
                properties.load(in);
                System.out.println("Loaded internal " + FILE_NAME);
            }
        } catch (Exception e) {
            System.err.println("Failed to load internal config: " + e.getMessage());
        }
        File dir = codeDir();
        activeFile = (dir != null) ? new File(dir, FILE_NAME) : new File(FILE_NAME);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /** مسار ملف الإعدادات الفعلي — يُعرض للمستخدم عند الأعطال */
    public static String activePath() {
        return activeFile != null ? activeFile.getAbsolutePath() : "(غير معروف)";
    }

    /** حفظ بيانات الاتصال في ملف الإعدادات الخارجي */
    public static synchronized void saveDbSettings(String url, String user, String password)
            throws Exception {
        properties.setProperty("db.url", url);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", password);
        File target = activeFile;
        if (target.getParentFile() != null) target.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(target)) {
            properties.store(out, "Apprenticeship Management System settings");
        }
        System.out.println("Saved config to: " + target.getAbsolutePath());
    }

    /** بناء رابط اتصال أوراكل من عنوان الخادم والمنفذ واسم القاعدة */
    public static String buildUrl(String host, String port, String sid) {
        return "jdbc:oracle:thin:@" + host.trim() + ":" + port.trim() + ":" + sid.trim();
    }
}
