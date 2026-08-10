import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FixFooter {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportSequential.java",
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportSucc.java",
            "src/main/java/com/pvtd/students/ui/pages/Report/gradReportFail.java"
        };
        for (String filePath : files) {
            File f = new File(filePath);
            if (!f.exists()) continue;
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            
            Pattern p = Pattern.compile("(String\\[\\]\\s+titles\\s*=\\s*\\{)(.*?)(\\};)", Pattern.DOTALL);
            Matcher m = p.matcher(content);
            if (m.find()) {
                String prefix = m.group(1);
                String itemsStr = m.group(2);
                String suffix = m.group(3);
                
                String[] items = itemsStr.split(",");
                boolean hasKetboFirst = items[0].contains("OOO\"Oc") || items[0].contains("???"); // "كتبه" checking
                
                // Always reverse because right now "رئيس لجنة النظام والمراقبة" is first in the array.
                // It's safer to check if "رئيس لجنة" is first.
                boolean isReisFirst = items[0].contains("OOUSO3") || items[0].contains("OOUSO3 U,OU+Oc") || items[0].contains("?????");
                
                if (isReisFirst) {
                    StringBuilder newItemsStr = new StringBuilder();
                    for (int i = items.length - 1; i >= 0; i--) {
                        newItemsStr.append(items[i].trim());
                        if (i > 0) newItemsStr.append(",\n            ");
                    }
                    String newContent = content.substring(0, m.start()) + prefix + "\n            " + newItemsStr.toString() + "\n        " + suffix + content.substring(m.end());
                    Files.write(Paths.get(filePath), newContent.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Reversed footer titles in " + f.getName());
                } else {
                    System.out.println("Already reversed in " + f.getName());
                }
            }
        }
    }
}