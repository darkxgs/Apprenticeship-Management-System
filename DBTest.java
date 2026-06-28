import java.sql.*;

public class DBTest {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "system";
        String pass = "123";
        
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                System.out.println("Checking region column in students...");
                ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT region FROM students WHERE ROWNUM <= 5");
                while(rs.next()) System.out.println("Region: [" + rs.getString(1) + "]");
                
                System.out.println("Checking governorate column in students...");
                rs = conn.createStatement().executeQuery("SELECT DISTINCT governorate FROM students WHERE ROWNUM <= 5");
                while(rs.next()) System.out.println("Gov: [" + rs.getString(1) + "]");
                
                System.out.println("Checking center_name column in students...");
                rs = conn.createStatement().executeQuery("SELECT DISTINCT center_name FROM students WHERE ROWNUM <= 5");
                while(rs.next()) System.out.println("Center: [" + rs.getString(1) + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
