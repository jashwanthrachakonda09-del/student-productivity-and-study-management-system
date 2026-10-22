package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import util.Calculator;

// Study history: one row for every day the student used the focus timer.
// UNIT V - JTable + JDBC     UNIT IV - ArrayList
public class HistoryPanel extends JPanel implements ActionListener {

    private MainFrame frame;
    private String[] columns = {"Date", "Sessions", "Minutes studied"};
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel totalLabel = new JLabel("", SwingConstants.CENTER);
    private JButton refreshBtn = new JButton("Refresh");

    public HistoryPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // table whose cells cannot be edited (anonymous inner class)
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(Theme.ROW_HEIGHT);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(totalLabel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn.addActionListener(this);
        buttonPanel.add(refreshBtn);
        bottom.add(buttonPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        try {
            int userId = frame.getStudent().getId();
            ArrayList<String[]> rows = frame.getTaskDAO().getStudyHistory(userId);
            ArrayList<Integer> minutes = frame.getTaskDAO().getStudyMinutes(userId);

            tableModel.setRowCount(0);
            for (String[] row : rows) {
                tableModel.addRow(row);
            }

            int totalMinutes = Calculator.sumMinutes(minutes);      // recursion
            totalLabel.setText("Total sessions: " + minutes.size()
                    + "   |   Total study time: " + Calculator.formatMinutes(totalMinutes));
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
