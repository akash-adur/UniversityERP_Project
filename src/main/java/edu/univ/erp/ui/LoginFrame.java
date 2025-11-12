package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.UserSession;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService;

    // --- Components ---
    // Declared here so they can be accessed in the constructor and methods
    private final JTextField userField = new JTextField(20); // Give it a preferred size
    private final JPasswordField passField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Login");
    private final JLabel statusLabel = new JLabel(" "); // For error messages

    public LoginFrame() {
        this.authService = new AuthService();

        setTitle("University ERP Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Use GridBagLayout ---
        // We will add components to a JPanel, then add the panel to the JFrame.
        // This makes it easier to manage insets (padding).
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Padding around the panel

        // This is the object that defines "where" components go
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Layout ---

        // A helper for default spacing
        gbc.insets = new Insets(5, 5, 5, 5); // 5px padding around components

        // Row 0: Username Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END; // Align to the right
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; // Don't grow horizontally
        panel.add(new JLabel("Username:"), gbc);

        // Row 0: Username Field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START; // Align to the left
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontal space
        gbc.weightx = 1.0; // This column can grow
        panel.add(userField, gbc);

        // Row 1: Password Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);

        // Row 1: Password Field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(passField, gbc);

        // Row 2: Status Label (for errors)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across 2 columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);

        // Row 3: Login Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span across 2 columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // Don't fill
        gbc.weightx = 0; // Don't grow
        panel.add(loginButton, gbc);

        // Add the panel to the frame
        add(panel);

        // --- Finalize Frame ---
        pack(); // Size the frame to fit the components
        setMinimumSize(new Dimension(350, 200)); // Set a reasonable minimum
        setLocationRelativeTo(null); // Center on screen

        // --- Action Listeners ---
        // Add action listener for the login button
        loginButton.addActionListener(e -> onLogin());

        // Also allow pressing 'Enter' in the password field to log in
        passField.addActionListener(e -> onLogin());
    }

    /**
     * Helper method to handle the login action.
     */
    private void onLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        try {
            // 1. Call the "brain" (AuthService)
            UserSession session = authService.login(username, password);

            // 2. Login was successful!
            //    --- THIS IS THE UPDATED PART ---

            // Create and show the main application frame
            MainFrame mainFrame = new MainFrame(session);
            mainFrame.setVisible(true);

            // Close this login window
            dispose();

        } catch (AuthService.AuthException ex) {
            // 3. Login failed, show the error message
            statusLabel.setText(ex.getMessage());
        }
    }
}