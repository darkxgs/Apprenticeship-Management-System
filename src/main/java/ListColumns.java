import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ListColumns {
    public static void main(String[] args) {
        String dbUrl = "jdbc:ucanaccess://الدور الاول2022_Backup.accdb;memory=false";
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("Columns in t_stu_data1:");
            try (ResultSet columns = metaData.getColumns(null, null, "t_stu_data1".toUpperCase(), null)) {
                while (columns.next()) {
                    System.out.println("- " + columns.getString("COLUMN_NAME"));
                }
            }
            try (ResultSet columns = metaData.getColumns(null, null, "t_stu_data1", null)) {
                while (columns.next()) {
                    System.out.println("- " + columns.getString("COLUMN_NAME"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
