package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.UserSession;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService;

    // --- Components ---
    private final JTextField userField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Login");
    private final JLabel statusLabel = new JLabel(" "); // For error messages

    public LoginFrame() {
        this.authService = new AuthService();

        setTitle("University ERP Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Use GridBagLayout ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 5px padding

        // Row 0: Username Label
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Username:"), gbc);

        // Row 0: Username Field
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(userField, gbc);

        // Row 1: Password Label
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);

        // Row 1: Password Field
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(passField, gbc);

        // Row 2: Status Label
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2; // Span 2 columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);

        // Row 3: Login Button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2; // Span 2 columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(loginButton, gbc);

        add(panel);

        // --- Finalize Frame ---
        pack(); // Size the frame
        setMinimumSize(new Dimension(350, 200));
        setLocationRelativeTo(null); // Center on screen

        // --- Action Listeners ---
        loginButton.addActionListener(e -> onLogin());
        passField.addActionListener(e -> onLogin());
    }

    /**
     * Helper method to handle the login action.
     */
    private void onLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        try {
            UserSession session = authService.login(username, password);

            // Login was successful!
            MainFrame mainFrame = new MainFrame(session);
            mainFrame.setVisible(true);

            dispose(); // Close this login window

        } catch (AuthService.AuthException ex) {
            // Login failed
            statusLabel.setText(ex.getMessage());
        }
    }
}