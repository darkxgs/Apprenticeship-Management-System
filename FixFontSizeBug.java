import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class FixFontSizeBug {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportSequential.java",
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportSucc.java",
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportFail.java",
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportFailMixed.java",
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportGeneric.java"
        };
        for (String filePath : files) {
            File f = new File(filePath);
            if (!f.exists()) continue;
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            
            // Fix the corrupted lines:
            // lbl("???? "true) or whatever it became
            // Let's use regex to find anything that looks like lbl("... ") or label("...") that was corrupted
            // Actually, we know exactly what is missing: + pageNum + " من " + totalPages, 140, true
            
            // In gradReportSequential: left.add(lbl("صفحة " ...
            content = content.replaceAll("lbl\\(\"صفحة \"\\s*\\)", "lbl(\"صفحة \" + pageNum + \" من \" + totalPages, 140, true)");
            content = content.replaceAll("label\\(\"صفحة \"\\s*\\)", "label(\"صفحة \" + pageNum + \" من \" + totalPages, 140, true)");
            
            // Since PowerShell replacing might have messed up Arabic, let's also check for "?" string
            content = content.replaceAll("lbl\\(\"\\?\\?\\?\\? \"\\s*\\)", "lbl(\"صفحة \" + pageNum + \" من \" + totalPages, 140, true)");
            content = content.replaceAll("label\\(\"\\?\\?\\?\\? \"\\s*\\)", "label(\"صفحة \" + pageNum + \" من \" + totalPages, 140, true)");
            
            // Wait, Java regex replaceAll doesn't fail on missing group if we do  No, java.util.regex.Matcher throws IndexOutOfBoundsException if group > groupCount. But wait, did it compile?
            // Actually, my regex was "".  is interpreted as group 1.
            // Let's print out what it actually looks like around "pageNum" or "totalPages". Oh wait, pageNum was DELETED.
            
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Fixed " + f.getName());
        }
    }
}