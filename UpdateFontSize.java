import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class UpdateFontSize {
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
            
            String newContent = content.replaceAll("(\\+\\s*pageNum\\s*\\+\\s*\".*?\"\\s*\\+\\s*totalPages\\s*,\\s*)\\d+(\\s*,\\s*)false", "");
            
            if (!content.equals(newContent)) {
                Files.write(Paths.get(filePath), newContent.getBytes(StandardCharsets.UTF_8));
                System.out.println("Updated font size in " + f.getName());
            }
        }
    }
}