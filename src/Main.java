import db.DatabaseSetup;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.LoginFrame;
import ui.Theme;

// Start of the program. Run THIS class.
public class Main {

    public static void main(String[] args) {

        // Use the normal look of the computer's operating system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // if it fails, Java's default look is used
        }

        // Make all the text bigger (size is set in ui/Theme.java)
        Theme.setGlobalFont();

        // Step 1: create the database and tables (if they are not there)
        try {
            DatabaseSetup.createDatabaseAndTables();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL.\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running and the user/password in db/DBConnection.java are correct.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 2: open the login window
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame();
            }
        });
    }
}
