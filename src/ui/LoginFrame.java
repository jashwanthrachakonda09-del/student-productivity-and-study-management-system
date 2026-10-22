package ui;

import db.UserDAO;
import exceptions.LoginFailedException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import model.Student;
import util.FileHelper;

// UNIT V  - Swing: JFrame, JLabel, JTextField, JPasswordField, JButton, JCheckBox
// UNIT IV - Event handling (ActionListener, KeyAdapter) and layouts (Border, GridBag, Flow)
public class LoginFrame extends JFrame implements ActionListener {

    private UserDAO userDAO = new UserDAO();

    private JTextField usernameField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JCheckBox rememberBox = new JCheckBox("Remember my username");
    private JButton loginBtn = new JButton("Login");
    private JButton registerBtn = new JButton("Register");

    public LoginFrame() {
        super("Login");
        setSize(620, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);          // open in the centre of the screen
        setLayout(new BorderLayout());        // BorderLayout: NORTH, CENTER, SOUTH...

        // NORTH: title
        add(Theme.makeHeader("Student Productivity & Study Management System"), BorderLayout.NORTH);

        // CENTER: form using GridBagLayout (rows and columns with spacing)
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Username:"), c);
        c.gridx = 1;
        form.add(usernameField, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Password:"), c);
        c.gridx = 1;
        form.add(passwordField, c);

        c.gridx = 1; c.gridy = 2;
        form.add(rememberBox, c);
        add(form, BorderLayout.CENTER);

        // SOUTH: buttons using FlowLayout (side by side)
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(loginBtn);
        buttons.add(registerBtn);
        add(buttons, BorderLayout.SOUTH);

        // Register this class as the listener of both buttons
        loginBtn.addActionListener(this);
        registerBtn.addActionListener(this);

        // KeyAdapter: we only write keyPressed, no need to write the other key methods
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });

        // Fill the username if it was remembered (reading a file - java.io)
        String saved = FileHelper.loadLastUsername();
        if (!saved.isEmpty()) {
            usernameField.setText(saved);
            rememberBox.setSelected(true);
        }

        setVisible(true);
    }

    // Called when a button is clicked
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            doLogin();
        } else if (e.getSource() == registerBtn) {
            doRegister();
        }
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password",
                    "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student student = userDAO.login(username, password);

            if (rememberBox.isSelected()) {
                FileHelper.saveLastUsername(username);
            } else {
                FileHelper.deleteLastUsername();
            }
            dispose();                  // close the login window
            new MainFrame(student);     // open the main window
        } catch (LoginFailedException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        JTextField nameField = new JTextField(15);
        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));    // GridLayout: 3 rows, 2 columns
        panel.add(new JLabel("Full name:"));
        panel.add(nameField);
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int choice = JOptionPane.showConfirmDialog(this, panel, "Register",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (name.isEmpty() || username.isEmpty() || password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                    "Fill all fields. Password must have at least 4 characters.",
                    "Invalid details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.usernameExists(username)) {
                JOptionPane.showMessageDialog(this, "This username is already taken",
                        "Register", JOptionPane.WARNING_MESSAGE);
                return;
            }
            userDAO.register(name, username, password);
            usernameField.setText(username);
            passwordField.setText("");
            JOptionPane.showMessageDialog(this, "Registered successfully. You can login now.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
