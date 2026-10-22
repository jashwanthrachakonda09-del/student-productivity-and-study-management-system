package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// UNIT V - JDBC: step 1 and 2 -> load the driver and get a connection.
// CHANGE USER and PASSWORD below to your own MySQL username and password.
public class DBConnection {

    public static final String USER = "root";
    public static final String PASSWORD = "root";

    public static final String DB_NAME = "study_db";
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String OPTIONS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Step 1: load the MySQL driver class (from the connector .jar file)
    private static void loadDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver (connector .jar) is not added to the project");
        }
    }

    // Connection to the MySQL server only (used to create the database)
    public static Connection getServerConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(SERVER_URL + OPTIONS, USER, PASSWORD);
    }

    // Connection to our database study_db
    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(SERVER_URL + DB_NAME + OPTIONS, USER, PASSWORD);
    }
}
