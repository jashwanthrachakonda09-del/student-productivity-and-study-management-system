package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// UNIT V - JDBC: creating a new database and tables from Java.
// Runs every time the app starts. "IF NOT EXISTS" means it does nothing if they already exist.
public class DatabaseSetup {

    public static void createDatabaseAndTables() throws SQLException {

        // 1) create the database
        try (Connection con = DBConnection.getServerConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DBConnection.DB_NAME);
        }

        // 2) create the tables inside it
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(50) NOT NULL, "
                    + "username VARCHAR(30) NOT NULL UNIQUE, "
                    + "password VARCHAR(30) NOT NULL)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS tasks ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "subject VARCHAR(40) NOT NULL, "
                    + "title VARCHAR(100) NOT NULL, "
                    + "deadline DATE NOT NULL, "
                    + "priority VARCHAR(10) NOT NULL, "
                    + "task_type VARCHAR(15) NOT NULL, "
                    + "completed BOOLEAN DEFAULT FALSE, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS study_log ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "minutes INT NOT NULL, "
                    + "log_date DATE NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

            // Add the "notes" column to the tasks table.
            // If the column is already there, MySQL gives an error - we simply ignore it.
            try {
                st.executeUpdate("ALTER TABLE tasks ADD COLUMN notes VARCHAR(500) DEFAULT ''");
            } catch (SQLException e) {
                // column already exists, nothing to do
            }
        }
        // try-with-resources closes the Connection and Statement automatically
    }
}
