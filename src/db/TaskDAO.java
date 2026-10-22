package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.AssignmentTask;
import model.StudyTask;
import model.Task;

// UNIT V - JDBC CRUD operations for tasks (Create, Read, Update, Delete)
// and for the study_log table (focus timer minutes).
public class TaskDAO {

    // CREATE
    public void addTask(Task task, int userId) throws SQLException {
        String sql = "INSERT INTO tasks (user_id, subject, title, deadline, priority, task_type, completed, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, task.getSubject());
            ps.setString(3, task.getTitle());
            ps.setDate(4, java.sql.Date.valueOf(task.getDeadline()));
            ps.setString(5, task.getPriority());
            ps.setString(6, task.getTaskType());
            ps.setBoolean(7, task.isCompleted());
            ps.setString(8, task.getNotes());
            ps.executeUpdate();
        }
    }

    // READ
    public ArrayList<Task> getAllTasks(int userId) throws SQLException {
        ArrayList<Task> list = new ArrayList<Task>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY deadline";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String subject = rs.getString("subject");
                String title = rs.getString("title");
                String deadline = rs.getDate("deadline").toString();
                String priority = rs.getString("priority");
                String type = rs.getString("task_type");
                boolean completed = rs.getBoolean("completed");

                // create the right kind of object depending on the type column
                Task task;
                if (type.equals("Assignment")) {
                    task = new AssignmentTask(id, subject, title, deadline, priority, completed);
                } else {
                    task = new StudyTask(id, subject, title, deadline, priority, completed);
                }
                String notes = rs.getString("notes");
                if (notes != null) {
                    task.setNotes(notes);
                }
                list.add(task);
            }
        }
        return list;
    }

    // UPDATE
    public void updateTask(Task task) throws SQLException {
        String sql = "UPDATE tasks SET subject = ?, title = ?, deadline = ?, priority = ?, "
                + "task_type = ?, completed = ?, notes = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, task.getSubject());
            ps.setString(2, task.getTitle());
            ps.setDate(3, java.sql.Date.valueOf(task.getDeadline()));
            ps.setString(4, task.getPriority());
            ps.setString(5, task.getTaskType());
            ps.setBoolean(6, task.isCompleted());
            ps.setString(7, task.getNotes());
            ps.setInt(8, task.getId());
            ps.executeUpdate();
        }
    }

    // UPDATE only the completed column
    public void setCompleted(int taskId, boolean done) throws SQLException {
        String sql = "UPDATE tasks SET completed = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, done);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteTask(int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    // ----- Study time (focus timer) -----

    public void addStudyLog(int userId, int minutes) throws SQLException {
        String sql = "INSERT INTO study_log (user_id, minutes, log_date) VALUES (?, ?, CURDATE())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, minutes);
            ps.executeUpdate();
        }
    }

    // Returns every saved session length. We add them up using recursion (Calculator).
    public ArrayList<Integer> getStudyMinutes(int userId) throws SQLException {
        ArrayList<Integer> list = new ArrayList<Integer>();
        String sql = "SELECT minutes FROM study_log WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("minutes"));     // int is converted to Integer (autoboxing)
            }
        }
        return list;
    }

    // Minutes studied TODAY (used for the daily goal)
    public int getTodayMinutes(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(minutes), 0) FROM study_log WHERE user_id = ? AND log_date = CURDATE()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    // Study history: one row per date -> {date, number of sessions, total minutes}
    public ArrayList<String[]> getStudyHistory(int userId) throws SQLException {
        ArrayList<String[]> rows = new ArrayList<String[]>();
        String sql = "SELECT log_date, COUNT(*) AS sessions, SUM(minutes) AS total "
                + "FROM study_log WHERE user_id = ? GROUP BY log_date ORDER BY log_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String date = rs.getDate("log_date").toString();
                String sessions = String.valueOf(rs.getInt("sessions"));
                String total = String.valueOf(rs.getInt("total"));
                rows.add(new String[]{date, sessions, total});
            }
        }
        return rows;
    }
}
