package com.pvtd.students.services;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
//import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.ui.utils.UITheme;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;




public class PdfService {

    public static String generatePassCertificate(Student student) throws Exception {
        String destPath = getStudentPdfPath(student, "شهادة_نجاح");

        // 1. Create a high-resolution JPanel representing the certificate
        JPanel certPanel = new JPanel(new BorderLayout());
        certPanel.setBackground(Color.WHITE);
        certPanel.setSize(800, 1131); // A4 roughly at 96 DPI
        certPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 10),
                new EmptyBorder(40, 40, 40, 40)));

        // Header
        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setBackground(Color.WHITE);
        JLabel title1 = new JLabel("جمهورية مصر العربية", SwingConstants.CENTER);
        title1.setFont(new Font("Tahoma", Font.BOLD, 20));
        JLabel title2 = new JLabel("مصلحة الكفاية الإنتاجية والتدريب المهني", SwingConstants.CENTER);
        title2.setFont(new Font("Tahoma", Font.BOLD, 22));
        JLabel title3 = new JLabel("شهادة نجاح وتخرج", SwingConstants.CENTER);
        title3.setFont(new Font("Tahoma", Font.BOLD, 28));
        title3.setForeground(UITheme.PRIMARY);

        header.add(title1);
        header.add(title2);
        header.add(title3);
        certPanel.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(40, 0, 40, 0));

        String bodyText = String.format(
                "<html><div style='text-align:center; font-family:tahoma; font-size:18px; line-height:2.0;' dir='rtl'>"
                        +
                        "تشهد إدارة المركز بأن الطالب / <b>%s</b><br>" +
                        "المولود في <b>%s-%s-%s</b> ، والرقم القومي: <b>%s</b><br>" +
                        "رقم الجلوس: <b>%s</b> ، التخصص: <b>%s</b><br><br>" +
                        "قد أتم بنجاح متطلبات التخرج واجتاز جميع الاختبارات المقررة بحالة: " +
                        "<b style='color:green;'>ناجح</b>.<br>" +
                        "ونتمنى له دوام التوفيق والنجاح في مسيرته المهنية.</div></html>",
                student.getName(), student.getDobYear(), student.getDobMonth(), student.getDobDay(),
                student.getNationalId(), student.getSeatNo(), getSpecializationName(student.getSpecializationId()));

        JLabel bodyLabel = new JLabel(bodyText, SwingConstants.CENTER);
        bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(bodyLabel);

        certPanel.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 2));
        footer.setBackground(Color.WHITE);
        JLabel sign1 = new JLabel("شئون الطلبة", SwingConstants.CENTER);
        sign1.setFont(new Font("Tahoma", Font.BOLD, 18));
        JLabel sign2 = new JLabel("مدير المركز", SwingConstants.CENTER);
        sign2.setFont(new Font("Tahoma", Font.BOLD, 18));
        footer.add(sign1);
        footer.add(sign2);
        certPanel.add(footer, BorderLayout.SOUTH);

        // 2. Render to Image
        certPanel.doLayout();
        BufferedImage image = new BufferedImage(certPanel.getWidth(), certPanel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        certPanel.printAll(g2d);
        g2d.dispose();

        // 3. Write Image to PDF
//        writeImageToPdf(image, destPath);
        return destPath;
    }

    public static String generateStudentForm(Student student) throws Exception {
        String destPath = getStudentPdfPath(student, "استمارة_بيانات");

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setSize(800, 1131);
        formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("استمارة بيانات ودرجات الطالب", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 26));
        formPanel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 0, 30, 0));

        Font f = new Font("Tahoma", Font.PLAIN, 18);
        content.add(createRightAlignedLabel("الاسم: " + student.getName(), f));
        content.add(createRightAlignedLabel("الرقم القومي: " + student.getNationalId(), f));
        content.add(createRightAlignedLabel("رقم الجلوس: " + student.getSeatNo(), f));
        content.add(createRightAlignedLabel("مركز التدريب: " + student.getCenterName(), f));
        content.add(createRightAlignedLabel("التخصص: " + getSpecializationName(student.getSpecializationId()), f));
        content.add(createRightAlignedLabel("الحالة: " + student.getStatus(), f));

        // Add Grades
        content.add(createRightAlignedLabel(" ", f));
        content.add(createRightAlignedLabel("--- بيان الدرجات ---", new Font("Tahoma", Font.BOLD, 18)));

        List<Subject> subjects = SubjectService.getSubjectsBySpecialization(student.getSpecializationId());
        Map<Integer, Integer> grades = student.getGrades();
        int totalMax = 0;
        int totalAttained = 0;

        for (Subject sub : subjects) {
            int score = (grades != null && grades.containsKey(sub.getId())) ? grades.get(sub.getId()) : 0;
            totalMax += sub.getMaxMark();
            totalAttained += score;
            content.add(createRightAlignedLabel(sub.getName() + " : " + score + " / " + sub.getMaxMark(), f));
        }

        content.add(createRightAlignedLabel("-------------------", f));
        content.add(createRightAlignedLabel("المجموع الكلي: " + totalAttained + " / " + totalMax,
                new Font("Tahoma", Font.BOLD, 18)));

        formPanel.add(content, BorderLayout.CENTER);

        formPanel.doLayout();
        BufferedImage image = new BufferedImage(formPanel.getWidth(), formPanel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        formPanel.printAll(g2d);
        g2d.dispose();

       // writeImageToPdf(image, destPath);
        return destPath;
    }
public static void exportPanelToPDF(JPanel panel, String path) {

    try {

        int width = panel.getWidth();
        int height = panel.getHeight();

        if (width == 0 || height == 0) {
            width = 1400;
            height = 900;
            panel.setSize(width, height);
        }

        panel.doLayout();
        panel.repaint();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        panel.printAll(g2);

        g2.dispose();

        Document document = new Document(PageSize.A4.rotate());

        PdfWriter.getInstance(document, new FileOutputStream(path));

        document.open();

      com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(image,null);

        img.scaleToFit(PageSize.A4.getHeight(), PageSize.A4.getWidth());

        document.add((Element) img);

        document.close();

        Desktop.getDesktop().browse(new File(path).toURI());

    } catch (Exception e) {
        e.printStackTrace();
    }

}
    private static JLabel createRightAlignedLabel(String text, Font font) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(font);
        l.setBackground(Color.WHITE);
        l.setOpaque(true);
        return l;
    }

    private static String getSpecializationName(int specId) {
        return SpecializationService.getAllSpecializations().stream()
                .filter(s -> s.getId() == specId)
                .map(com.pvtd.students.models.Specialization::getName)
                .findFirst()
                .orElse("غير محدد");
    }

    private static String getStudentPdfPath(Student student, String prefix) {
        String safeId = (student.getNationalId() != null && !student.getNationalId().trim().isEmpty())
                ? student.getNationalId().trim().replaceAll("[^a-zA-Z0-9.-]", "_")
                : "unknown_id";

        String userHome = System.getProperty("user.home");
        File studentFolder = new File(userHome, ".student_mgmt/students/" + safeId);
        if (!studentFolder.exists()) {
            studentFolder.mkdirs();
        }

        String safeName = student.getName().replaceAll("[^a-zA-Z0-9.\u0600-\u06FF\\s-]", "_").trim();
        File dest = new File(studentFolder, prefix + "_" + safeName + ".pdf");
        return dest.getAbsolutePath();
    }

//    private static void writeImageToPdf(BufferedImage image, String destPath) throws Exception {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageIO.write(image, "png", baos);
//        ImageData imageData = ImageDataFactory.create(baos.toByteArray());
//
//        PdfWriter writer = new PdfWriter(destPath);
//        PdfDocument pdf = new PdfDocument(writer);
//        Document document = new Document(pdf, PageSize.A4);
//        document.setMargins(0, 0, 0, 0);
//
//        Image pdfImage = new Image(imageData);
//        pdfImage.setWidth(PageSize.A4.getWidth());
//        pdfImage.setHeight(PageSize.A4.getHeight());
//
//        document.add(pdfImage);
//        document.close();
//    }
}
