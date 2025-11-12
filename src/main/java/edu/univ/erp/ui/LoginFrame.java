package edu.univ.erp.ui;

import com.miglayout.swing.MigLayout; // You'll need to add this to your pom.xml
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.UserSession;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService;

    public LoginFrame() {
        this.authService = new AuthService();

        setTitle("University ERP Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(350, 200));
        setLocationRelativeTo(null); // Center on screen

        // Use MigLayout for easy form building
        setLayout(new MigLayout("wrap 2, fillx, insets 15", "[right]10[fill,grow]", "[]10[]10[]"));

        // --- Components ---
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JLabel statusLabel = new JLabel(" "); // For error messages
        statusLabel.setForeground(Color.RED);

        // --- Layout ---
        add(userLabel);
        add(userField, "growx");

        add(passLabel);
        add(passField, "growx");

        add(statusLabel, "span, growx"); // Spans both columns
        add(loginButton, "span, tag ok, align center"); // Spans both columns

        // --- Action Listeners ---
        // Add action listener for the login button
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            loginUser(username, password, statusLabel);
        });

        // Also allow pressing 'Enter' in the password field to log in
        passField.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            loginUser(username, password, statusLabel);
        });
    }

    private void loginUser(String username, String password, JLabel statusLabel) {
        try {
            // 1. Call the "brain" (AuthService)
            UserSession session = authService.login(username, password);

            // 2. Login was successful!
            JOptionPane.showMessageDialog(this, "Login Successful! Role: " + session.getRole(), "Success", JOptionPane.INFORMATION_MESSAGE);

            // TODO: Open the Main Application Dashboard here
            // e.g., new MainDashboard(session).setVisible(true);

            // Close this login window
            dispose();

        } catch (AuthService.AuthException ex) {
            // 3. Login failed, show the error message
            statusLabel.setText(ex.getMessage());
        }
    }
}