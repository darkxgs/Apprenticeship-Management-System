import java.sql.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DBTest {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "system";
        String pass = "123";
        
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("db_output.txt"), StandardCharsets.UTF_8))) {
                
                pw.println("=== VERIFYING SQL QUERY FOR REGION SEARCH ===");
                
                String[] testRegions = {"شرق الإسكندرية", "شرق الاسكندرية"};
                for (String region : testRegions) {
                    pw.println("\nTesting region selection: [" + region + "]");
                    
                    String sql = "SELECT c.name, c.code FROM centers c " +
                                 "JOIN regions r ON c.region_id = r.id " +
                                 "WHERE TRIM(r.name) = TRIM(?) OR " +
                                 "      REPLACE(REPLACE(REPLACE(REPLACE(TRIM(r.name), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') = " +
                                 "      REPLACE(REPLACE(REPLACE(REPLACE(TRIM(?), 'ة', 'ه'), 'أ', 'ا'), 'إ', 'ا'), 'آ', 'ا') " +
                                 "ORDER BY c.code";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, region);
                        stmt.setString(2, region);
                        try (ResultSet rs = stmt.executeQuery()) {
                            int count = 0;
                            while (rs.next()) {
                                count++;
                                pw.println("  Center " + count + ": [" + rs.getString("name") + "] -> Code: [" + rs.getString("code") + "]");
                            }
                            pw.println("  Total centers found: " + count);
                        }
                    }
                }
                
                System.out.println("Verification finished. Results printed to db_output.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
