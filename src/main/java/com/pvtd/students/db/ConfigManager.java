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
        try {
            // First try to load from an external file in the current working directory
            File extFile = new File(EXTERNAL_CONFIG);
            if (extFile.exists() && extFile.isFile()) {
                try (FileInputStream fis = new FileInputStream(extFile)) {
                    properties.load(fis);
                    System.out.println("Loaded external " + EXTERNAL_CONFIG);
                    return;
                }
            }

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
