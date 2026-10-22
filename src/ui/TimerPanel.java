package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import threads.FocusTimer;
import util.Calculator;

// The focus timer screen. The countdown runs in FocusTimer (a separate thread).
// UNIT III - Multithreading     UNIT V - Swing components
// UNIT IV  - Window events: MainFrame tells this panel when the student leaves or returns to the app.
public class TimerPanel extends JPanel implements ActionListener {

    private MainFrame frame;

    private JLabel timeLabel = new JLabel("25:00", SwingConstants.CENTER);
    private JLabel distractionLabel = new JLabel("Distractions this session: 0", SwingConstants.CENTER);
    private JTextField minutesField = new JTextField("25", 5);
    private JButton startBtn = new JButton("Start");
    private JButton pauseBtn = new JButton("Pause");
    private JButton stopBtn = new JButton("Stop");

    private FocusTimer timer;           // null when no timer is running
    private int sessionMinutes = 25;
    private boolean paused = false;

    private int distractions = 0;               // how many times the student left the app
    private boolean leftDuringSession = false;  // true while the student is away

    public TimerPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Focus Timer - study without distractions", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, Theme.FONT_SIZE + 6));
        add(title, BorderLayout.NORTH);

        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 110));
        add(timeLabel, BorderLayout.CENTER);

        JPanel south = new JPanel(new GridLayout(2, 1, 5, 5));
        south.add(distractionLabel);
        JPanel controls = new JPanel(new FlowLayout());
        controls.add(new JLabel("Minutes:"));
        controls.add(minutesField);
        controls.add(startBtn);
        controls.add(pauseBtn);
        controls.add(stopBtn);
        south.add(controls);
        add(south, BorderLayout.SOUTH);

        startBtn.addActionListener(this);
        pauseBtn.addActionListener(this);
        stopBtn.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startBtn) {
            startTimer();
        } else if (e.getSource() == pauseBtn) {
            pauseOrResume();
        } else if (e.getSource() == stopBtn) {
            stopTimer();
        }
    }

    private boolean isRunning() {
        return timer != null && timer.isAlive();
    }

    private void startTimer() {
        if (isRunning()) {
            JOptionPane.showMessageDialog(this, "The timer is already running");
            return;
        }
        try {
            int minutes = Integer.parseInt(minutesField.getText().trim());   // String -> int (wrapper class)
            if (minutes < 1 || minutes > 180) {
                JOptionPane.showMessageDialog(this, "Enter minutes between 1 and 180");
                return;
            }
            sessionMinutes = minutes;
            paused = false;
            pauseBtn.setText("Pause");
            distractions = 0;
            leftDuringSession = false;
            distractionLabel.setText("Distractions this session: 0");

            // Create the thread and start it. start() calls run() of FocusTimer.
            timer = new FocusTimer(minutes, timeLabel, new FinishAction());
            timer.start();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number for minutes",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void pauseOrResume() {
        if (!isRunning()) {
            return;
        }
        if (!paused) {
            timer.pauseTimer();
            paused = true;
            pauseBtn.setText("Resume");
        } else {
            timer.resumeTimer();
            paused = false;
            pauseBtn.setText("Pause");
        }
    }

    private void stopTimer() {
        if (!isRunning()) {
            return;
        }
        int elapsed = timer.getElapsedMinutes();     // full minutes studied so far
        timer.stopTimer();
        timer = null;
        paused = false;
        pauseBtn.setText("Pause");
        timeLabel.setText(String.format("%02d:00", sessionMinutes));

        if (elapsed >= 1) {
            saveStudyTime(elapsed);
            JOptionPane.showMessageDialog(this, "Stopped. " + elapsed + " minute(s) added to your study time.");
        }
    }

    // ----- Leave-the-app alert -----
    // MainFrame calls these two methods from its WindowAdapter
    // (windowDeactivated = student switched to another app, windowActivated = came back).

    public void windowLeft() {
        if (isRunning() && !paused) {
            distractions++;
            leftDuringSession = true;
            distractionLabel.setText("Distractions this session: " + distractions);
            Toolkit.getDefaultToolkit().beep();        // makes a beep sound
        }
    }

    public void windowBack() {
        if (leftDuringSession) {
            leftDuringSession = false;
            JOptionPane.showMessageDialog(this,
                    "You left the app during your focus session!\nStay focused.\n"
                    + "Distractions so far: " + distractions,
                    "Stay Focused", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Saves the minutes in the database and refreshes the dashboard
    private void saveStudyTime(int minutes) {
        try {
            frame.getTaskDAO().addStudyLog(frame.getStudent().getId(), minutes);
            frame.refreshAll();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // UNIT I - INNER CLASS. FocusTimer calls run() of this class when the time reaches 00:00.
    private class FinishAction implements Runnable {
        public void run() {
            timer = null;
            saveStudyTime(sessionMinutes);
            timeLabel.setText(String.format("%02d:00", sessionMinutes));
            JOptionPane.showMessageDialog(TimerPanel.this,
                    "Session complete! Total study time added: "
                    + Calculator.formatMinutes(sessionMinutes)
                    + "\nDistractions: " + distractions);
        }
    }
}
