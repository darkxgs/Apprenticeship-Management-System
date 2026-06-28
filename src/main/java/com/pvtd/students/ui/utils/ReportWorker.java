package com.pvtd.students.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A specialized SwingWorker to handle report generation in the background.
 */
public abstract class ReportWorker extends SwingWorker<Void, String> {
    private final ProgressDialog progressDialog;
    private final String reportTitle;
    private final String successMessage;
    private int totalSteps = 100;

    public ReportWorker(Window parent, String reportTitle, String successMessage) {
        this.reportTitle = reportTitle;
        this.successMessage = successMessage;
        this.progressDialog = new ProgressDialog(parent, reportTitle);
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    @Override
    protected void process(List<String> chunks) {
        String lastMessage = chunks.get(chunks.size() - 1);
        progressDialog.updateProgress(getProgress(), lastMessage);
    }

    public void start() {
        execute();
        progressDialog.setVisible(true);
    }

    @Override
    protected void done() {
        progressDialog.dispose();
        try {
            get(); // Check for exceptions
            if (successMessage != null && !successMessage.isEmpty()) {
                JOptionPane.showMessageDialog(null, successMessage, "نجاح", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!(e instanceof java.util.concurrent.CancellationException)) {
                JOptionPane.showMessageDialog(null, "حدث خطأ أثناء استخراج التقرير: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void updateStatus(int current, int total, String message) {
        int progress = (int) (((double) current / total) * 100);
        setProgress(Math.min(100, progress));
        publish(message);
    }
}
