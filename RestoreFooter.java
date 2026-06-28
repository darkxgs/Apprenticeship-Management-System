import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RestoreFooter {
    public static void main(String[] args) throws Exception {
        String filePath = "src/main/java/com/pvtd/students/ui/pages/Report/gradReportSequential.java";
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        
        Pattern p = Pattern.compile("(String\\[\\]\\s+titles\\s*=\\s*\\{)(.*?)(\\};)", Pattern.DOTALL);
        Matcher m = p.matcher(content);
        if (m.find()) {
            String prefix = m.group(1);
            String suffix = m.group(3);
            
            // Arabic text in UTF-8
            String replacement = "\n" +
                "            \"كتبه\",\n" +
                "            \"راجعه\",\n" +
                "            \"راجع الاملاء\",\n" +
                "            \"رصد ووضع الدوائر الحمراء\",\n" +
                "            \"راجع الدوائر الحمراء والرصد\",\n" +
                "            \"راجع المراجعة\",\n" +
                "            \"رئيس لجنة النظام والمراقبة\"\n" +
                "        ";
                
            String newContent = content.substring(0, m.start()) + prefix + replacement + suffix + content.substring(m.end());
            
            // Wait, gradReportSequential uses 	 from 	itles and lbl(t, 130, true)
            // But we need to make sure we don't have font checking logic that is missing.
            
            Files.write(Paths.get(filePath), newContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("Restored Arabic titles in gradReportSequential.java");
        }
    }
}