package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// UNIT IV - java.io package: reading and writing text files.
public class FileHelper {

    private static final String LAST_USER_FILE = "lastuser.txt";

    // Remember the username so the login box can fill it next time.
    public static void saveLastUsername(String username) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(LAST_USER_FILE);
            writer.write(username);
        } catch (IOException e) {
            System.out.println("Could not save username: " + e.getMessage());
        } finally {
            // finally always runs - we use it to close the file
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("Could not close file");
            }
        }
    }

    // Read the remembered username (empty text if nothing is saved).
    public static String loadLastUsername() {
        File file = new File(LAST_USER_FILE);
        if (!file.exists()) {
            return "";
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            if (line == null) {
                return "";
            }
            return line.trim();
        } catch (IOException e) {
            return "";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Could not close file");
            }
        }
    }

    public static void deleteLastUsername() {
        File file = new File(LAST_USER_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    // Write the report text into a file. 'throws IOException' means the caller must handle the error.
    public static void writeReport(String fileName, String text) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(text);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    // ----- Daily study goal (minutes), saved in a small file for each user -----

    public static void saveGoal(String username, int minutes) {
        FileWriter writer = null;
        try {
            writer = new FileWriter("goal_" + username + ".txt");
            writer.write(String.valueOf(minutes));
        } catch (IOException e) {
            System.out.println("Could not save goal: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("Could not close file");
            }
        }
    }

    // Returns the saved goal, or 120 minutes if nothing is saved yet
    public static int loadGoal(String username) {
        File file = new File("goal_" + username + ".txt");
        if (!file.exists()) {
            return 120;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            return Integer.parseInt(line.trim());
        } catch (IOException e) {
            return 120;
        } catch (NumberFormatException e) {
            return 120;
        } catch (NullPointerException e) {
            return 120;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Could not close file");
            }
        }
    }
}
