package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class AdminUserPanel extends JPanel {

    private final AdminService adminService;

    // Form components
    private final JTextField userField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JComboBox<String> roleDropdown = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
    private final JButton createUserButton = new JButton("Create User");

    // --- THIS CONSTRUCTOR IS NOW FIXED ---
    public AdminUserPanel(AdminService adminService) {
        this.adminService = adminService; // It now uses the service passed to it

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Create New User"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        add(userField, gbc);

        // Row 1: Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        add(passField, gbc);

        // Row 2: Role
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        add(roleDropdown, gbc);

        // Row 3: Button
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        add(createUserButton, gbc);

        // Add a "spacer"
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);

        // --- Action Listener ---
        createUserButton.addActionListener(e -> onCreateUser());
    }

    private void onCreateUser() {
        String username = userField.getText();
        String password = new String(passField.getPassword());
        String role = (String) roleDropdown.getSelectedItem();

        try {
            adminService.createUser(username, password, role);
            JOptionPane.showMessageDialog(this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            userField.setText("");
            passField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}