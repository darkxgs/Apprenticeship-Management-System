import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CheckFooter {
    public static void main(String[] args) throws Exception {
        String filePath = "src/main/java/com/pvtd/students/ui/pages/Report/gradReportSequential.java";
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        
        Pattern p = Pattern.compile("(String\\[\\]\\s+titles\\s*=\\s*\\{)(.*?)(\\};)", Pattern.DOTALL);
        Matcher m = p.matcher(content);
        if (m.find()) {
            String itemsStr = m.group(2);
            String[] items = itemsStr.split(",");
            for (int i=0; i<items.length; i++) {
                System.out.println(i + ": " + items[i].trim());
            }
        }
    }
}