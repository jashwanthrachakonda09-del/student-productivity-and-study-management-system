package ui;

import db.TaskDAO;
import db.UserDAO;
import exceptions.LoginFailedException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import model.Student;
import model.Task;
import threads.ReminderThread;
import util.Calculator;
import util.FileHelper;
import util.Helper;
import util.Holder;

// UNIT V  - JFrame, JMenuBar, JMenu, JMenuItem, JCheckBoxMenuItem, JRadioButtonMenuItem, JSeparator
// UNIT IV - CardLayout (switch screens), BorderLayout, GridLayout, WindowAdapter, ActionListener
public class MainFrame extends JFrame implements ActionListener {

    // Generic class Holder<Student> keeps the logged-in student
    private Holder<Student> userBox = new Holder<Student>();
    private TaskDAO taskDAO = new TaskDAO();

    // Names of the 5 screens (used for the buttons, the menu and the CardLayout)
    private String[] screens = {"Dashboard", "Tasks", "Focus Timer", "Study History", "Subjects"};

    private CardLayout cardLayout = new CardLayout();
    private JPanel centerPanel = new JPanel(cardLayout);

    private DashboardPanel dashboardPanel;
    private TaskPanel taskPanel;
    private TimerPanel timerPanel;
    private HistoryPanel historyPanel;
    private SubjectPanel subjectPanel;

    private JCheckBoxMenuItem remindersItem;
    private ReminderThread reminderThread;

    public MainFrame(Student student) {
        super("Student Productivity & Study Management System");
        userBox.set(student);

        setSize(1200, 780);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);                // open the window full screen
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   // we ask "Are you sure?" ourselves

        // WindowAdapter: we write only the methods we need
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }

            // The student switched to ANOTHER application.
            // getOppositeWindow() is null when the other window is not ours.
            // (If it is not null, it is one of our own pop-up dialogs, so we ignore it.)
            public void windowDeactivated(WindowEvent e) {
                if (e.getOppositeWindow() == null && timerPanel != null) {
                    timerPanel.windowLeft();
                }
            }

            // The student came back from another application
            public void windowActivated(WindowEvent e) {
                if (e.getOppositeWindow() == null && timerPanel != null) {
                    timerPanel.windowBack();
                }
            }
        });

        setJMenuBar(buildMenuBar());

        // NORTH: header
        add(Theme.makeHeader("Student Productivity & Study Management System   |   Welcome, "
                + student.getName()), BorderLayout.NORTH);

        // WEST: navigation buttons in a GridLayout (one below the other)
        JPanel navPanel = new JPanel(new GridLayout(screens.length, 1, 5, 8));
        for (int i = 0; i < screens.length; i++) {
            navPanel.add(makeNavButton(screens[i]));
        }
        JPanel navWrapper = new JPanel(new BorderLayout());
        navWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        navWrapper.add(navPanel, BorderLayout.NORTH);
        add(navWrapper, BorderLayout.WEST);

        // CENTER: the five screens, placed on top of each other using CardLayout
        dashboardPanel = new DashboardPanel(this);
        taskPanel = new TaskPanel(this);
        timerPanel = new TimerPanel(this);
        historyPanel = new HistoryPanel(this);
        subjectPanel = new SubjectPanel(this);
        centerPanel.add(dashboardPanel, "Dashboard");
        centerPanel.add(taskPanel, "Tasks");
        centerPanel.add(timerPanel, "Focus Timer");
        centerPanel.add(historyPanel, "Study History");
        centerPanel.add(subjectPanel, "Subjects");
        add(centerPanel, BorderLayout.CENTER);

        // Start the background reminder thread
        reminderThread = new ReminderThread(taskDAO, student.getId(), this);
        reminderThread.start();

        refreshAll();
        setVisible(true);
    }

    // ----- Used by the panels -----

    public Student getStudent() {
        return userBox.get();
    }

    public TaskDAO getTaskDAO() {
        return taskDAO;
    }

    // Reload the data in every screen
    public void refreshAll() {
        dashboardPanel.refresh();
        taskPanel.loadTasks();
        historyPanel.refresh();
        subjectPanel.refresh();
    }

    // ----- Building the screen -----

    private JButton makeNavButton(String text) {
        JButton button = new JButton(text);
        button.setActionCommand(text);
        button.addActionListener(this);
        return button;
    }

    // Small helper: give a menu item its command and listener
    private void setup(AbstractButton item, String command) {
        item.setActionCommand(command);
        item.addActionListener(this);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // ---- File menu ----
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Report");
        JMenuItem passwordItem = new JMenuItem("Change Password");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        setup(exportItem, "Export");
        setup(passwordItem, "Password");
        setup(logoutItem, "Logout");
        setup(exitItem, "Exit");
        fileMenu.add(exportItem);
        fileMenu.add(passwordItem);
        fileMenu.add(new JSeparator());          // a line between menu items
        fileMenu.add(logoutItem);
        fileMenu.add(exitItem);

        // ---- View menu (radio items: only one can be selected) ----
        JMenu viewMenu = new JMenu("View");
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < screens.length; i++) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(screens[i]);
            setup(item, screens[i]);
            group.add(item);
            viewMenu.add(item);
            if (i == 0) {
                item.setSelected(true);
            }
        }

        // ---- Options menu (check box item: on / off) ----
        JMenu optionsMenu = new JMenu("Options");
        remindersItem = new JCheckBoxMenuItem("Deadline reminders", true);
        setup(remindersItem, "Reminders");
        optionsMenu.add(remindersItem);

        // ---- Help menu ----
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        setup(aboutItem, "About");
        helpMenu.add(aboutItem);

        bar.add(fileMenu);
        bar.add(viewMenu);
        bar.add(optionsMenu);
        bar.add(helpMenu);
        return bar;
    }

    // ----- Handling clicks (buttons and menu items) -----

    // true if the command is the name of one of our screens
    private boolean isScreen(String command) {
        for (int i = 0; i < screens.length; i++) {
            if (screens[i].equals(command)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (isScreen(command)) {
            refreshAll();
            cardLayout.show(centerPanel, command);       // switch to that screen
        } else if (command.equals("Export")) {
            exportReport();
        } else if (command.equals("Password")) {
            changePassword();
        } else if (command.equals("Logout")) {
            logout();
        } else if (command.equals("Exit")) {
            confirmExit();
        } else if (command.equals("Reminders")) {
            reminderThread.setRemindersOn(remindersItem.isSelected());
        } else if (command.equals("About")) {
            JOptionPane.showMessageDialog(this,
                    "Student Productivity & Study Management System\n"
                    + "Plan tasks, track deadlines and use the focus timer.\n"
                    + "Built with Java Swing and JDBC (MySQL).",
                    "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to exit?",
                "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void logout() {
        reminderThread.interrupt();     // stop the reminder thread
        dispose();
        new LoginFrame();
    }

    // Asks for old and new password and updates the database
    private void changePassword() {
        JPasswordField oldField = new JPasswordField(15);
        JPasswordField newField = new JPasswordField(15);
        JPasswordField confirmField = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Old password:"));
        panel.add(oldField);
        panel.add(new JLabel("New password:"));
        panel.add(newField);
        panel.add(new JLabel("Confirm new password:"));
        panel.add(confirmField);

        int choice = JOptionPane.showConfirmDialog(this, panel, "Change Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String oldPass = new String(oldField.getPassword());
        String newPass = new String(newField.getPassword());
        String confirmPass = new String(confirmField.getPassword());

        if (newPass.length() < 4) {
            JOptionPane.showMessageDialog(this, "New password must have at least 4 characters",
                    "Invalid password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirm password do not match",
                    "Invalid password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            new UserDAO().changePassword(getStudent().getId(), oldPass, newPass);
            JOptionPane.showMessageDialog(this, "Password changed successfully");
        } catch (LoginFailedException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Writes a text report file (java.io)
    private void exportReport() {
        try {
            Student student = getStudent();
            ArrayList<Task> tasks = taskDAO.getAllTasks(student.getId());
            ArrayList<Integer> minutes = taskDAO.getStudyMinutes(student.getId());

            int done = 0;
            int overdue = 0;
            for (Task t : tasks) {
                if (t.isCompleted()) {
                    done++;
                } else if (t.isOverdue()) {
                    overdue++;
                }
            }
            int score = Calculator.percentage(done, tasks.size());

            StringBuilder report = new StringBuilder();
            report.append("STUDENT PRODUCTIVITY REPORT\n");
            report.append("---------------------------\n");
            report.append("Student : ").append(student.getName()).append("\n");
            report.append("Date    : ").append(Helper.today()).append("\n\n");
            report.append("Tasks completed    : ").append(done).append("/").append(tasks.size()).append("\n");
            report.append("Tasks overdue      : ").append(overdue).append("\n");
            report.append("Study time         : ").append(Calculator.formatMinutes(Calculator.sumMinutes(minutes))).append("\n");
            report.append("Productivity score : ").append(score).append("%\n\n");
            report.append("ALL TASKS\n");
            report.append(Helper.listToText(tasks));

            String fileName = "report_" + student.getUsername() + ".txt";
            FileHelper.writeReport(fileName, report.toString());
            JOptionPane.showMessageDialog(this,
                    "Report saved:\n" + new File(fileName).getAbsolutePath());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not write the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
