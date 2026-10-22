package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import model.Task;
import util.Calculator;
import util.FileHelper;

// UNIT V - JPanel, JLabel, JList, JProgressBar, JButton
// Shows: total / completed / pending / overdue tasks, study time, progress %, productivity score,
// the daily study goal, and the list of tasks that need attention.
public class DashboardPanel extends JPanel implements ActionListener {

    private MainFrame frame;

    private JLabel totalValue = new JLabel("0", SwingConstants.CENTER);
    private JLabel doneValue = new JLabel("0", SwingConstants.CENTER);
    private JLabel pendingValue = new JLabel("0", SwingConstants.CENTER);
    private JLabel overdueValue = new JLabel("0", SwingConstants.CENTER);
    private JLabel studyValue = new JLabel("0h 0m", SwingConstants.CENTER);

    private JProgressBar progressBar = new JProgressBar(0, 100);
    private JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);
    private JProgressBar goalBar = new JProgressBar(0, 100);
    private JLabel goalLabel = new JLabel("", SwingConstants.CENTER);

    private DefaultListModel<String> dueModel = new DefaultListModel<String>();
    private JList<String> dueList = new JList<String>(dueModel);
    private JButton goalBtn = new JButton("Set Daily Goal");
    private JButton refreshBtn = new JButton("Refresh");

    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // NORTH: five small boxes in one row (GridLayout 1 row, 5 columns)
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        statsPanel.add(makeCard("Total Tasks", totalValue));
        statsPanel.add(makeCard("Completed", doneValue));
        statsPanel.add(makeCard("Pending", pendingValue));
        statsPanel.add(makeCard("Overdue", overdueValue));
        statsPanel.add(makeCard("Study Time", studyValue));
        add(statsPanel, BorderLayout.NORTH);

        // CENTER: progress bars + list
        JPanel middle = new JPanel(new BorderLayout(10, 10));

        JPanel progressPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        progressPanel.setBorder(new TitledBorder("Task Progress and Daily Study Goal"));
        progressBar.setStringPainted(true);          // show the % text on the bar
        goalBar.setStringPainted(true);
        progressPanel.add(progressBar);
        progressPanel.add(scoreLabel);
        progressPanel.add(goalBar);
        progressPanel.add(goalLabel);
        middle.add(progressPanel, BorderLayout.NORTH);

        JScrollPane listScroll = new JScrollPane(dueList);
        listScroll.setBorder(new TitledBorder("Needs attention: overdue, due today or due tomorrow"));
        middle.add(listScroll, BorderLayout.CENTER);
        add(middle, BorderLayout.CENTER);

        // SOUTH: buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        goalBtn.addActionListener(this);
        refreshBtn.addActionListener(this);
        bottom.add(goalBtn);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    // A white box with a small title and a big number
    private JPanel makeCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(Color.GRAY));
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, Theme.FONT_SIZE + 16));
        card.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(150, 100));
        return card;
    }

    // Reads the data from the database and updates all the labels
    public void refresh() {
        try {
            int userId = frame.getStudent().getId();
            ArrayList<Task> tasks = frame.getTaskDAO().getAllTasks(userId);
            ArrayList<Integer> minutes = frame.getTaskDAO().getStudyMinutes(userId);

            int total = tasks.size();
            int done = 0;
            int overdue = 0;
            dueModel.clear();
            for (Task t : tasks) {
                if (t.isCompleted()) {
                    done++;
                } else if (t.isOverdue()) {
                    overdue++;
                    dueModel.addElement("OVERDUE - " + t.getSubject() + " - " + t.getTitle()
                            + "  (was due " + t.getDeadline() + ")");
                }
                if (t.isDueSoon()) {
                    dueModel.addElement(t.getSubject() + " - " + t.getTitle()
                            + "  (due " + t.getDeadline() + ", " + t.getPriority() + ")");
                }
            }
            if (dueModel.isEmpty()) {
                dueModel.addElement("Nothing urgent right now. Good job!");
            }

            int pending = total - done - overdue;
            int percent = Calculator.percentage(done, total);
            int studyMinutes = Calculator.sumMinutes(minutes);      // uses recursion
            String studyText = Calculator.formatMinutes(studyMinutes);

            totalValue.setText(String.valueOf(total));
            doneValue.setText(String.valueOf(done));
            pendingValue.setText(String.valueOf(pending));
            overdueValue.setText(String.valueOf(overdue));
            studyValue.setText(studyText);
            progressBar.setValue(percent);
            scoreLabel.setText("Tasks completed: " + done + "/" + total
                    + "   |   Study time: " + studyText
                    + "   |   Productivity score: " + percent + "%");

            // Daily goal: minutes studied today compared with the goal
            int todayMinutes = frame.getTaskDAO().getTodayMinutes(userId);
            int goal = FileHelper.loadGoal(frame.getStudent().getUsername());
            int goalPercent = Calculator.percentage(todayMinutes, goal);
            if (goalPercent > 100) {
                goalPercent = 100;
            }
            goalBar.setValue(goalPercent);
            String goalText = "Today's study: " + todayMinutes + " / " + goal + " minutes";
            if (todayMinutes >= goal) {
                goalText = goalText + "   -   Goal reached!";
            }
            goalLabel.setText(goalText);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ask the student for a new daily goal and save it in a file
    private void changeGoal() {
        String username = frame.getStudent().getUsername();
        int current = FileHelper.loadGoal(username);
        String input = JOptionPane.showInputDialog(this, "Daily study goal in minutes:", String.valueOf(current));
        if (input == null) {
            return;                       // Cancel was clicked
        }
        try {
            int minutes = Integer.parseInt(input.trim());
            if (minutes < 10 || minutes > 720) {
                JOptionPane.showMessageDialog(this, "Enter a goal between 10 and 720 minutes");
                return;
            }
            FileHelper.saveGoal(username, minutes);
            refresh();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            refresh();
        } else if (e.getSource() == goalBtn) {
            changeGoal();
        }
    }
}
