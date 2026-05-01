package com.pvtd.students.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final String EXTERNAL_CONFIG = "application.properties";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        String[] paths = { EXTERNAL_CONFIG, "app/" + EXTERNAL_CONFIG };
        for (String path : paths) {
            File extFile = new File(path);
            if (extFile.exists() && extFile.isFile()) {
                try (FileInputStream fis = new FileInputStream(extFile)) {
                    properties.load(fis);
                    System.out.println("Loaded external config from: " + path);
                    return;
                } catch (Exception e) {
                    System.err.println("Failed to load " + path + ": " + e.getMessage());
                }
            }
        }

        try {
            // Fallback to internal classpath resource
            try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (in != null) {
                    properties.load(in);
                    System.out.println("Loaded internal application.properties");
                } else {
                    System.out.println("No application.properties found. Using defaults.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load application.properties: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
