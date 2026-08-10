package com.pvtd.students.services;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

/**
 * Service to check for updates from the remote git repository.
 */
public class UpdateService {

    private static final String BRANCH_NAME = "updates";

    public static void startUpdateCheck() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (checkForUpdates()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                                "هناك تحديثات جديدة متوفرة في الفرع " + BRANCH_NAME + ".\nيرجى سحب التحديثات (Git Pull) للحصول على آخر التعديلات.",
                                "تحديث النظام",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            } catch (Exception e) {
                System.err.println("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private static boolean checkForUpdates() throws Exception {
        // 1. Fetch remote changes
        Process fetch = new ProcessBuilder("git", "fetch", "origin", BRANCH_NAME)
                .redirectErrorStream(true)
                .start();
        fetch.waitFor();

        // 2. Check how many commits are behind
        Process revList = new ProcessBuilder("git", "rev-list", "HEAD..origin/" + BRANCH_NAME, "--count")
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(revList.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                int count = Integer.parseInt(line.trim());
                return count > 0;
            }
        }
        revList.waitFor();
        return false;
    }
}
