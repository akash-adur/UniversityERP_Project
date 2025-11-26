package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class AdminMaintenancePanel extends JPanel {

    private final AdminService adminService;
    private final JToggleButton toggleButton;
    private final JLabel statusLabel;

    // Callback to notify MainFrame
    private final Runnable onStatusChange;

    public AdminMaintenancePanel(AdminService adminService, Runnable onStatusChange) {
        this.adminService = adminService;
        this.onStatusChange = onStatusChange;

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Maintenance Control"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        add(statusLabel, gbc);

        gbc.gridy = 1;
        toggleButton = new JToggleButton("Toggle Maintenance Mode");
        toggleButton.setPreferredSize(new Dimension(200, 50));
        add(toggleButton, gbc);

        loadState();

        toggleButton.addActionListener(e -> toggleMaintenance());
    }

    private void loadState() {
        try {
            String mode = adminService.getSetting("maintenance_mode");
            boolean isOn = "true".equalsIgnoreCase(mode);
            updateUI(isOn);
        } catch (SQLException e) {
            statusLabel.setText("Error loading status");
        }
    }

    private void toggleMaintenance() {
        boolean turnOn = toggleButton.isSelected();
        try {
            adminService.setSetting("maintenance_mode", String.valueOf(turnOn));
            updateUI(turnOn);

            // Refresh the Banner immediately
            if (onStatusChange != null) {
                onStatusChange.run();
            }

            JOptionPane.showMessageDialog(this, "Maintenance Mode Updated!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            toggleButton.setSelected(!turnOn);
        }
    }

    private void updateUI(boolean isOn) {
        toggleButton.setSelected(isOn);
        if (isOn) {
            statusLabel.setText("🔴 SYSTEM MAINTENANCE IS ON");
            statusLabel.setForeground(Color.RED);
            toggleButton.setText("Turn Maintenance OFF");
        } else {
            statusLabel.setText("🟢 System is Normal");
            statusLabel.setForeground(new Color(0, 128, 0));
            toggleButton.setText("Turn Maintenance ON");
        }
    }
}