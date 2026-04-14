import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {


    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USERNAME = "system";   //Oracle username
    private static final String PASSWORD = "tiger";   //  Oracle password


    // This holds our single shared connection
    private static Connection connection = null;


    private DBConnect() {}

  
    public static Connection getConnection() {
        try {
            // Check if we need to create a new connection
            if (connection == null || connection.isClosed()) {
                // Load the Oracle JDBC driver from ojdbc JAR
                Class.forName("oracle.jdbc.driver.OracleDriver");
                // Actually connect to the database
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[DB ERROR] ojdbc JAR not found! Add ojdbc17.jar to your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("[DB ERROR] Cannot connect to Oracle. Check your URL, username, and password.");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}