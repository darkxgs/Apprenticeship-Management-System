package com.pvtd.students.ui.components;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import javax.swing.JPanel;

public class PDFGenerator {

    public PDFGenerator(){}
    public static void createPDF(JPanel panel) {

        try {

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream("result.pdf"));

            document.open();

            BufferedImage image = new BufferedImage(
                    panel.getWidth(),
                    panel.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2 = image.createGraphics();
            panel.paint(g2);
            g2.dispose();

            Image img = Image.getInstance(image, null);

            img.scaleToFit(PageSize.A4.getWidth() - 40, PageSize.A4.getHeight() - 40);

            document.add(img);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}