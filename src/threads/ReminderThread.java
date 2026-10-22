package threads;

import db.TaskDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.Task;

// UNIT III - Daemon thread.
// A daemon thread runs in the background and stops automatically when the program ends.
// It checks the database for tasks due today/tomorrow and shows a pop-up once per task.
public class ReminderThread extends Thread {

    private TaskDAO taskDAO;
    private int userId;
    private JFrame parent;
    private boolean remindersOn = true;
    private HashSet<Integer> alreadyReminded = new HashSet<Integer>();   // Set: no duplicates

    public ReminderThread(TaskDAO taskDAO, int userId, JFrame parent) {
        super(ThreadHelper.GROUP, "ReminderThread");
        this.taskDAO = taskDAO;
        this.userId = userId;
        this.parent = parent;
        setDaemon(true);                      // make it a daemon thread
        setPriority(Thread.MIN_PRIORITY);     // lowest priority, it is not urgent
    }

    public synchronized void setRemindersOn(boolean on) {
        this.remindersOn = on;
    }

    private synchronized boolean isRemindersOn() {
        return remindersOn;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000);                    // small wait after login
            while (true) {
                if (isRemindersOn()) {
                    checkTasks();
                }
                Thread.sleep(30000);               // check again after 30 seconds
            }
        } catch (InterruptedException e) {
            // interrupt() is called on logout -> the thread ends here
        }
    }

    private void checkTasks() {
        try {
            ArrayList<Task> tasks = taskDAO.getAllTasks(userId);
            final StringBuilder message = new StringBuilder();
            for (Task t : tasks) {
                if (t.isDueSoon() && !alreadyReminded.contains(t.getId())) {
                    alreadyReminded.add(t.getId());
                    message.append(t.getReminderMessage()).append("\n");
                }
            }
            if (message.length() > 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(parent, message.toString(),
                                "Deadline Reminder", JOptionPane.WARNING_MESSAGE);
                    }
                });
            }
        } catch (SQLException e) {
            System.out.println("Reminder check failed: " + e.getMessage());
        }
    }
}
