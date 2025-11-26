package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final AuthService authService;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField currentPassField = new JPasswordField(15);
    private final JPasswordField newPassField = new JPasswordField(15);
    private final JButton updateButton = new JButton("Update Password");
    private final JButton cancelButton = new JButton("Cancel");

    public ChangePasswordDialog(Frame owner) {
        super(owner, "Change Password", true);
        this.authService = new AuthService();

        setLayout(new GridBagLayout());
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        add(currentPassField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        add(newPassField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        updateButton.setBackground(new Color(0, 100, 0));
        updateButton.setForeground(Color.WHITE);

        btnPanel.add(cancelButton);
        btnPanel.add(updateButton);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(btnPanel, gbc);

        // Actions
        cancelButton.addActionListener(e -> dispose());
        updateButton.addActionListener(e -> performChange());

        pack();
        setLocationRelativeTo(owner);
    }

    private void performChange() {
        String user = usernameField.getText().trim();
        String oldPass = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());

        if (user.isEmpty() || oldPass.isEmpty() || newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 4) { // Basic check
            JOptionPane.showMessageDialog(this, "New password is too short (min 4 chars).", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authService.changePassword(user, oldPass, newPass);
            JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (AuthService.AuthException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}