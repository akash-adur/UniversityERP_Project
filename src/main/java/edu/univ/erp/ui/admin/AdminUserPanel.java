package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.SQLException;

public class AdminUserPanel extends JPanel {

    private final AdminService adminService;

    // Create Panel Components
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);
    private final JComboBox<String> roleDropdown = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
    private final JLabel deptLabel = new JLabel("Department:");
    private final JTextField deptField = new JTextField(15);
    private final JButton createUserButton = new JButton("Create User");

    // Unlock Panel Components
    private final JTextField unlockUserField = new JTextField(15);
    private final JButton unlockButton = new JButton("Unlock Account");

    public AdminUserPanel(AdminService adminService) {
        this.adminService = adminService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        // --- Panel 1: Create User ---
        JPanel createPanel = new JPanel(new GridBagLayout());
        createPanel.setBorder(BorderFactory.createTitledBorder("Create New User"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        createPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        createPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        createPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        createPanel.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        createPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        createPanel.add(roleDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_END;
        createPanel.add(deptLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        createPanel.add(deptField, gbc);

        deptLabel.setVisible(false);
        deptField.setVisible(false);

        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        createPanel.add(createUserButton, gbc);

        // --- Panel 2: Account Management (Unlock) ---
        JPanel unlockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        unlockPanel.setBorder(BorderFactory.createTitledBorder("Account Management"));

        unlockPanel.add(new JLabel("Username to Unlock:"));
        unlockPanel.add(unlockUserField);
        unlockPanel.add(unlockButton);

        // Add panels to main container
        mainContainer.add(createPanel);
        mainContainer.add(Box.createVerticalStrut(15));
        mainContainer.add(unlockPanel);

        // Add scroll pane just in case
        add(new JScrollPane(mainContainer), BorderLayout.CENTER);

        // --- Listeners ---
        roleDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean isInstructor = "Instructor".equals(e.getItem());
                deptLabel.setVisible(isInstructor);
                deptField.setVisible(isInstructor);
                revalidate(); repaint();
            }
        });

        createUserButton.addActionListener(e -> onCreateUser());
        unlockButton.addActionListener(e -> onUnlockUser());
    }

    private void onCreateUser() {
        String username = userField.getText();
        String password = new String(passField.getPassword());
        String role = (String) roleDropdown.getSelectedItem();
        String department = deptField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password are required.");
            return;
        }

        if ("Instructor".equals(role) && department.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Department is required for Instructors.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Admin".equals(role)) {
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(this, new Object[]{"Creating a new Admin requires authorization.\nPlease enter YOUR current admin password:", pf}, "Security Check", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (okCxl == JOptionPane.OK_OPTION) {
                String currentAdminPass = new String(pf.getPassword());
                if (currentAdminPass.isEmpty()) return;
            } else {
                return;
            }
        }

        try {
            adminService.createUser(username, password, role, department);
            JOptionPane.showMessageDialog(this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            userField.setText(""); passField.setText(""); deptField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUnlockUser() {
        String username = unlockUserField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.");
            return;
        }
        try {
            adminService.unlockUser(username);
            JOptionPane.showMessageDialog(this, "User account unlocked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            unlockUserField.setText("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error unlocking user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}