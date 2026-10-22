package db;

import exceptions.LoginFailedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Student;

// UNIT V - JDBC for the users table (register + login).
// DAO = Data Access Object: a class that only talks to the database.
public class UserDAO {

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public void register(String name, String username, String password) throws SQLException {
        String sql = "INSERT INTO users (name, username, password) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.executeUpdate();
        }
    }

    // Returns the Student if the login is correct, otherwise throws LoginFailedException
    public Student login(String username, String password) throws SQLException, LoginFailedException {
        String sql = "SELECT id, name, username FROM users WHERE username = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Student(rs.getInt("id"), rs.getString("name"), rs.getString("username"));
            }
            throw new LoginFailedException("Wrong username or password");
        }
    }

    // Changes the password. If the old password is wrong, no row is updated -> exception.
    public void changePassword(int userId, String oldPassword, String newPassword)
            throws SQLException, LoginFailedException {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.setString(3, oldPassword);
            int rows = ps.executeUpdate();          // number of rows changed
            if (rows == 0) {
                throw new LoginFailedException("Old password is wrong");
            }
        }
    }
}
