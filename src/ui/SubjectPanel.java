package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import model.Task;
import util.Calculator;

// Shows tasks grouped by subject.
// UNIT V  - JTree     UNIT IV - HashMap (Map from the Collections framework)
public class SubjectPanel extends JPanel implements ActionListener {

    private MainFrame frame;
    private DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("My Subjects"));
    private JTree tree = new JTree(treeModel);
    private JTextArea summaryArea = new JTextArea(10, 28);
    private JButton refreshBtn = new JButton("Refresh");

    public SubjectPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        tree.setRowHeight(Theme.ROW_HEIGHT);
        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(new TitledBorder("Subjects and their tasks"));
        add(treeScroll, BorderLayout.CENTER);

        summaryArea.setEditable(false);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(new TitledBorder("Subject-wise progress"));
        add(summaryScroll, BorderLayout.EAST);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn.addActionListener(this);
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            ArrayList<Task> tasks = frame.getTaskDAO().getAllTasks(frame.getStudent().getId());

            // Map: subject name -> tree node / number of tasks / number of completed tasks
            HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<String, DefaultMutableTreeNode>();
            HashMap<String, Integer> totalMap = new HashMap<String, Integer>();
            HashMap<String, Integer> doneMap = new HashMap<String, Integer>();

            DefaultMutableTreeNode root = new DefaultMutableTreeNode("My Subjects");

            for (Task t : tasks) {
                String subject = t.getSubject();

                // first time we see this subject: create its node and counters
                if (!nodes.containsKey(subject)) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(subject);
                    nodes.put(subject, node);
                    root.add(node);
                    totalMap.put(subject, 0);        // 0 is autoboxed to Integer
                    doneMap.put(subject, 0);
                }

                nodes.get(subject).add(new DefaultMutableTreeNode(t.getTitle() + "  [" + t.getStatusText() + "]"));
                totalMap.put(subject, totalMap.get(subject) + 1);
                if (t.isCompleted()) {
                    doneMap.put(subject, doneMap.get(subject) + 1);
                }
            }

            treeModel.setRoot(root);
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);                   // open all the branches
            }

            // Build the text for the right side
            StringBuilder text = new StringBuilder();
            for (String subject : totalMap.keySet()) {
                int total = totalMap.get(subject);
                int done = doneMap.get(subject);
                text.append(subject).append("\n   ")
                    .append(done).append("/").append(total).append(" completed (")
                    .append(Calculator.percentage(done, total)).append("%)\n\n");
            }
            if (text.length() == 0) {
                text.append("No tasks yet.");
            }
            summaryArea.setText(text.toString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshBtn) {
            refresh();
        }
    }
}
