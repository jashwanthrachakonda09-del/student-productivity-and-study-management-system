package ui;

import exceptions.InvalidTaskException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import model.AssignmentTask;
import model.StudyTask;
import model.Task;
import util.Helper;

// The main screen: add, update, delete, complete, filter and sort tasks.
// UNIT V  - JTable, JComboBox, JTextField, JTextArea, JRadioButton, JCheckBox, JPopupMenu
// UNIT IV - ArrayList, TreeSet, Collections.sort + Comparator, MouseAdapter, ActionListener, inner classes
public class TaskPanel extends JPanel implements ActionListener {

    private MainFrame frame;

    // List of all tasks of this student (read from the database)
    private ArrayList<Task> allTasks = new ArrayList<Task>();

    // UNIT I - arrays
    private String[] columns = {"ID", "Subject", "Task", "Type", "Deadline", "Priority", "Status"};
    private String[] defaultSubjects = {"Java", "DBMS", "IoT", "Discrete Maths",
            "Computer Organization", "Innovation & Entrepreneurship", "Other"};

    // Table
    private DefaultTableModel tableModel;
    private JTable table;

    // Filter and sort row
    private JComboBox<String> subjectFilter = new JComboBox<String>();
    private JComboBox<String> statusFilter =
            new JComboBox<String>(new String[]{"All", "Pending", "Overdue", "Completed"});
    private JComboBox<String> sortBox =
            new JComboBox<String>(new String[]{"Deadline", "Priority", "Subject", "Task name"});
    private JButton applyBtn = new JButton("Apply");
    private JButton showAllBtn = new JButton("Show All");

    // Form
    private JComboBox<String> subjectBox;
    private JTextField titleField = new JTextField();
    private JTextField deadlineField = new JTextField(Helper.today());
    private JComboBox<String> typeBox = new JComboBox<String>(new String[]{"Study", "Assignment"});
    private JRadioButton lowRadio = new JRadioButton("Low");
    private JRadioButton mediumRadio = new JRadioButton("Medium", true);
    private JRadioButton highRadio = new JRadioButton("High");
    private JCheckBox completedBox = new JCheckBox("Completed");
    private JTextArea notesArea = new JTextArea(5, 20);

    // Buttons
    private JButton addBtn = new JButton("Add Task");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton completeBtn = new JButton("Mark Completed");
    private JButton clearBtn = new JButton("Clear Form");

    // Right-click menu
    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem popComplete = new JMenuItem("Mark as completed");
    private JMenuItem popDelete = new JMenuItem("Delete task");

    private int selectedId = 0;      // id of the task selected in the table (0 = nothing)

    public TaskPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---------- NORTH: filter and sort row (FlowLayout) ----------
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Subject:"));
        filterPanel.add(subjectFilter);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        filterPanel.add(new JLabel("Sort by:"));
        filterPanel.add(sortBox);
        filterPanel.add(applyBtn);
        filterPanel.add(showAllBtn);
        add(filterPanel, BorderLayout.NORTH);

        // ---------- CENTER: table ----------
        // This is an ANONYMOUS INNER CLASS: we change one method of DefaultTableModel
        // so that the cells cannot be edited by typing in the table.
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(Theme.ROW_HEIGHT);
        table.addMouseListener(new TableClickHandler());
        table.getColumnModel().getColumn(0).setPreferredWidth(40);    // make the ID column narrow
        table.getColumnModel().getColumn(2).setPreferredWidth(220);   // make the Task column wide
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---------- SOUTH: form + notes + buttons ----------
        subjectBox = new JComboBox<String>(defaultSubjects);
        subjectBox.setEditable(true);            // student can also type a new subject

        ButtonGroup priorityGroup = new ButtonGroup();   // only one radio button can be selected
        priorityGroup.add(lowRadio);
        priorityGroup.add(mediumRadio);
        priorityGroup.add(highRadio);
        JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        priorityPanel.add(lowRadio);
        priorityPanel.add(mediumRadio);
        priorityPanel.add(highRadio);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 6));     // 6 rows, 2 columns (label, field)
        form.setBorder(new TitledBorder("Task Details"));
        form.add(new JLabel("Subject:"));
        form.add(subjectBox);
        form.add(new JLabel("Task:"));
        form.add(titleField);
        form.add(new JLabel("Deadline (yyyy-MM-dd):"));
        form.add(deadlineField);
        form.add(new JLabel("Type:"));
        form.add(typeBox);
        form.add(new JLabel("Priority:"));
        form.add(priorityPanel);
        form.add(new JLabel("Status:"));
        form.add(completedBox);

        // Notes box on the right side of the form
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(new TitledBorder("Notes (optional)"));
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        JPanel formRow = new JPanel(new GridLayout(1, 2, 10, 0));
        formRow.add(form);
        formRow.add(notesPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(completeBtn);
        buttonPanel.add(clearBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.add(formRow, BorderLayout.CENTER);
        south.add(buttonPanel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // ---------- Right-click menu ----------
        popupMenu.add(popComplete);
        popupMenu.add(popDelete);

        // ---------- Register listeners ----------
        applyBtn.addActionListener(this);
        showAllBtn.addActionListener(this);
        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        completeBtn.addActionListener(this);
        clearBtn.addActionListener(this);
        popComplete.addActionListener(this);
        popDelete.addActionListener(this);
    }

    // ================= Loading data =================

    // Read all tasks from the database and show them in the table
    public void loadTasks() {
        try {
            allTasks = frame.getTaskDAO().getAllTasks(frame.getStudent().getId());
            fillSubjectFilter();
            applyFilter();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    // A TreeSet keeps the subjects sorted and without duplicates
    private void fillSubjectFilter() {
        Object oldChoice = subjectFilter.getSelectedItem();
        TreeSet<String> subjects = new TreeSet<String>();
        for (Task t : allTasks) {
            subjects.add(t.getSubject());
        }
        subjectFilter.removeAllItems();
        subjectFilter.addItem("All");
        for (String s : subjects) {
            subjectFilter.addItem(s);
        }
        if (oldChoice != null) {
            subjectFilter.setSelectedItem(oldChoice);
        }
    }

    // Show only the tasks that match the filters, sorted in the chosen order
    private void applyFilter() {
        String subject = subjectFilter.getSelectedItem().toString();
        String status = statusFilter.getSelectedItem().toString();
        final String sortBy = sortBox.getSelectedItem().toString();

        // 1) collect the tasks that match the filters
        ArrayList<Task> shown = new ArrayList<Task>();
        for (Task t : allTasks) {
            boolean subjectOk = subject.equals("All") || t.getSubject().equals(subject);
            boolean statusOk = status.equals("All") || t.getStatusText().equals(status);
            if (subjectOk && statusOk) {
                shown.add(t);
            }
        }

        // 2) sort them. Comparator is an interface (Unit II) and we write it as an
        //    anonymous inner class (Unit I). compare() returns a negative number if a should come first.
        Collections.sort(shown, new Comparator<Task>() {
            public int compare(Task a, Task b) {
                if (sortBy.equals("Priority")) {
                    return priorityRank(a.getPriority()) - priorityRank(b.getPriority());
                } else if (sortBy.equals("Subject")) {
                    return a.getSubject().compareToIgnoreCase(b.getSubject());
                } else if (sortBy.equals("Task name")) {
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                }
                return a.getDeadline().compareTo(b.getDeadline());     // sort by deadline
            }
        });

        // 3) put them in the table
        tableModel.setRowCount(0);              // clear the table
        for (Task t : shown) {
            tableModel.addRow(new Object[]{t.getId(), t.getSubject(), t.getTitle(),
                    t.getTaskType(), t.getDeadline(), t.getPriority(), t.getStatusText()});
        }
    }

    // High priority should come first, so High = 0, Medium = 1, Low = 2
    private int priorityRank(String priority) {
        if (priority.equals("High")) {
            return 0;
        } else if (priority.equals("Medium")) {
            return 1;
        }
        return 2;
    }

    // ================= Button clicks =================

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == applyBtn) {
            applyFilter();
        } else if (source == showAllBtn) {
            subjectFilter.setSelectedItem("All");
            statusFilter.setSelectedItem("All");
            sortBox.setSelectedItem("Deadline");
            applyFilter();
        } else if (source == addBtn) {
            addTask();
        } else if (source == updateBtn) {
            updateTask();
        } else if (source == deleteBtn || source == popDelete) {
            deleteTask();
        } else if (source == completeBtn || source == popComplete) {
            markCompleted();
        } else if (source == clearBtn) {
            clearForm();
        }
    }

    private void addTask() {
        try {
            Task task = buildTaskFromForm();
            frame.getTaskDAO().addTask(task, frame.getStudent().getId());
            clearForm();
            frame.refreshAll();
            JOptionPane.showMessageDialog(this, "Task added");
        } catch (InvalidTaskException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid task", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void updateTask() {
        if (selectedId == 0) {
            JOptionPane.showMessageDialog(this, "Click a task in the table first");
            return;
        }
        try {
            Task task = buildTaskFromForm();
            task.setId(selectedId);
            frame.getTaskDAO().updateTask(task);
            clearForm();
            frame.refreshAll();
            JOptionPane.showMessageDialog(this, "Task updated");
        } catch (InvalidTaskException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid task", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteTask() {
        if (selectedId == 0) {
            JOptionPane.showMessageDialog(this, "Click a task in the table first");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Delete this task?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            frame.getTaskDAO().deleteTask(selectedId);
            clearForm();
            frame.refreshAll();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void markCompleted() {
        if (selectedId == 0) {
            JOptionPane.showMessageDialog(this, "Click a task in the table first");
            return;
        }
        try {
            frame.getTaskDAO().setCompleted(selectedId, true);
            clearForm();
            frame.refreshAll();
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    // ================= Form helpers =================

    // Reads the form, checks it, and creates a StudyTask or AssignmentTask.
    // 'throws InvalidTaskException' = our own exception for wrong input.
    private Task buildTaskFromForm() throws InvalidTaskException {
        Object selected = subjectBox.getSelectedItem();
        String subject = (selected == null) ? "" : selected.toString().trim();
        String title = titleField.getText().trim();
        String deadline = deadlineField.getText().trim();
        String type = typeBox.getSelectedItem().toString();
        String notes = notesArea.getText().trim();

        if (subject.isEmpty()) {
            throw new InvalidTaskException("Subject cannot be empty");
        }
        if (title.isEmpty()) {
            throw new InvalidTaskException("Task title cannot be empty");
        }
        if (title.length() > 100) {
            throw new InvalidTaskException("Task title must be less than 100 characters");
        }
        if (notes.length() > 500) {
            throw new InvalidTaskException("Notes must be less than 500 characters");
        }
        try {
            // Date.valueOf throws IllegalArgumentException if the format is wrong
            deadline = java.sql.Date.valueOf(deadline).toString();
        } catch (IllegalArgumentException e) {
            throw new InvalidTaskException("Deadline must be in the form yyyy-MM-dd, example 2026-10-25");
        }

        Task task;
        if (type.equals("Assignment")) {
            task = new AssignmentTask(subject, title, deadline, getSelectedPriority());
        } else {
            task = new StudyTask(subject, title, deadline, getSelectedPriority());
        }
        task.setCompleted(completedBox.isSelected());
        task.setNotes(notes);
        return task;
    }

    private String getSelectedPriority() {
        if (lowRadio.isSelected()) {
            return "Low";
        } else if (highRadio.isSelected()) {
            return "High";
        }
        return "Medium";
    }

    private void clearForm() {
        subjectBox.setSelectedIndex(0);
        titleField.setText("");
        deadlineField.setText(Helper.today());
        typeBox.setSelectedIndex(0);
        mediumRadio.setSelected(true);
        completedBox.setSelected(false);
        notesArea.setText("");
        selectedId = 0;
        table.clearSelection();
    }

    // Put the details of the clicked table row into the form
    private void fillFormFromRow(int row) {
        selectedId = (Integer) tableModel.getValueAt(row, 0);    // Integer -> int (unboxing)
        Task task = findTaskById(selectedId);
        if (task == null) {
            return;
        }
        subjectBox.setSelectedItem(task.getSubject());
        titleField.setText(task.getTitle());
        deadlineField.setText(task.getDeadline());
        typeBox.setSelectedItem(task.getTaskType());
        completedBox.setSelected(task.isCompleted());
        notesArea.setText(task.getNotes());
        if (task.getPriority().equals("Low")) {
            lowRadio.setSelected(true);
        } else if (task.getPriority().equals("High")) {
            highRadio.setSelected(true);
        } else {
            mediumRadio.setSelected(true);
        }
    }

    private Task findTaskById(int id) {
        for (Task t : allTasks) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================= Inner class for mouse events =================

    // UNIT I  - a (non-static) INNER CLASS: it can use the table, popupMenu etc. of TaskPanel.
    // UNIT IV - MouseAdapter: we write only mouseClicked, not all 5 mouse methods.
    private class TableClickHandler extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());     // which row was clicked?
            if (row < 0) {
                return;
            }
            table.setRowSelectionInterval(row, row);
            fillFormFromRow(row);

            // right mouse button -> show the popup menu
            if (e.getButton() == MouseEvent.BUTTON3) {
                popupMenu.show(table, e.getX(), e.getY());
            }
        }
    }
}
