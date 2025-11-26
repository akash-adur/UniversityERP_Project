package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.UserSession;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

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

        // Use a main panel with a border layout for the "Card" look
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 240, 245)); // Light gray background
        setContentPane(mainPanel);

        // --- Login Box (The White Card) ---
        JPanel loginBox = new JPanel(new GridBagLayout());
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLbl = new JLabel("University ERP");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        titleLbl.setForeground(new Color(50, 50, 50));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginBox.add(titleLbl, gbc);

        // Username
        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.weightx = 0;
        loginBox.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        loginBox.add(userField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        loginBox.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        loginBox.add(passField, gbc);

        // Login Button
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginBox.add(loginButton, gbc);

        // Status Label
        statusLabel.setForeground(new Color(220, 50, 50)); // Red
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginBox.add(statusLabel, gbc);

        // Add Login Box to Main Panel
        mainPanel.add(loginBox);

        // --- Finalize Frame ---
        pack();
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(null); // Center on screen

        // --- Action Listeners ---
        loginButton.addActionListener(e -> performLogin());
        passField.addActionListener(e -> performLogin());
    }

    /**
     * Handles login with a "Please Wait" dialog and background worker.
     */
    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        // 1. Create the "Please Wait" Dialog
        JDialog waitDialog = new JDialog(this, "Authenticating", true);
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        p.add(new JLabel("Verifying credentials...", SwingConstants.CENTER), BorderLayout.NORTH);

        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        p.add(pb, BorderLayout.CENTER);

        waitDialog.setContentPane(p);
        waitDialog.pack();
        waitDialog.setLocationRelativeTo(this);
        waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // 2. Create Background Worker
        SwingWorker<UserSession, Void> loginWorker = new SwingWorker<>() {
            @Override
            protected UserSession doInBackground() throws Exception {
                // Simulate a slight network delay for better UX (optional)
                Thread.sleep(500);
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                waitDialog.dispose(); // Close the loading popup

                try {
                    UserSession session = get(); // Get result (or throw exception)

                    // Login Successful
                    MainFrame mainFrame = new MainFrame(session);
                    mainFrame.setVisible(true);
                    dispose(); // Close Login Frame

                } catch (InterruptedException e) {
                    statusLabel.setText("Login interrupted.");
                } catch (ExecutionException e) {
                    // Extract the actual cause (AuthException or Database error)
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        statusLabel.setText(cause.getMessage());
                    } else {
                        statusLabel.setText("Unknown login error occurred.");
                    }
                    // Shake animation for visual feedback (Optional polish)
                    shakeFrame();
                }
            }
        };

        // 3. Start Worker and Show Dialog
        loginWorker.execute();
        waitDialog.setVisible(true); // This blocks user input until worker calls dispose()
    }

    private void shakeFrame() {
        try {
            Point original = getLocation();
            for (int i = 0; i < 5; i++) {
                setLocation(original.x + 5, original.y);
                Thread.sleep(20);
                setLocation(original.x - 5, original.y);
                Thread.sleep(20);
            }
            setLocation(original);
        } catch (Exception ignored) {}
    }
}