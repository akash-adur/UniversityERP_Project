package edu.univ.erp.ui;

import edu.univ.erp.domain.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * The main application window that opens after a successful login.
 * It holds the role-specific dashboards (Student, Instructor, Admin).
 */
public class MainFrame extends JFrame {

    private final UserSession session;

    public MainFrame(UserSession session) {
        this.session = session;

        setTitle("University ERP Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600)); // A good starting size
        setLocationRelativeTo(null); // Center on screen

        // Use BorderLayout for the main structure
        setLayout(new BorderLayout());

        // --- 1. Top Banner (Optional but good) ---
        // This panel shows who is logged in and a logout button.
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Role-Specific Dashboard (The Core) ---
        // We use a JTabbedPane to hold the different features.
        JTabbedPane mainTabs = createMainTabs();
        add(mainTabs, BorderLayout.CENTER);

        // --- 3. Status Bar (Bottom) ---
        JLabel statusBar = new JLabel("Logged in as: " + session.getUsername() + " (" + session.getRole() + ")");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Creates the top panel with a welcome message and logout button.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + session.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(logoutButton, BorderLayout.EAST);

        // Add a visible banner if in maintenance mode (from the spec) [cite: 19, 139]
        // We will implement this check later.
        // if (isMaintenanceMode) {
        //     JLabel maintBanner = new JLabel("MAINTENANCE MODE IS ON. All actions are read-only.", SwingConstants.CENTER);
        //     maintBanner.setOpaque(true);
        //     maintBanner.setBackground(Color.ORANGE);
        //     panel.add(maintBanner, BorderLayout.SOUTH);
        // }

        return panel;
    }

    /**
     * Creates the JTabbedPane and adds the correct tabs based on the user's role.
     * This is where you fulfill the "role-matched dashboard" requirement[cite: 23].
     */
    private JTabbedPane createMainTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Every user gets a "Home" tab
        tabbedPane.addTab("Home", new JPanel()); // Placeholder

        // Now, add role-specific tabs
        if (session.isStudent()) {
            // TODO: Replace 'new JPanel()' with 'new StudentDashboardPanel()'
            tabbedPane.addTab("Course Catalog", new JPanel());
            tabbedPane.addTab("My Registrations", new JPanel());
            tabbedPane.addTab("My Grades", new JPanel());
        }

        if (session.isInstructor()) {
            // TODO: Replace 'new JPanel()' with 'new InstructorDashboardPanel()'
            tabbedPane.addTab("My Sections", new JPanel());
            tabbedPane.addTab("Gradebook", new JPanel());
        }

        if (session.isAdmin()) {
            // TODO: Replace 'new JPanel()' with 'new AdminUserPanel()' etc.
            tabbedPane.addTab("User Management", new JPanel());
            tabbedPane.addTab("Course Management", new JPanel());
            tabbedPane.addTab("Maintenance", new JPanel());
        }

        return tabbedPane;
    }

    /**
     * Handles the logout action.
     */
    private void logout() {
        // Confirm before logging out
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // Close this window
            dispose();
            // Open a new LoginFrame
            // We must run this on the Swing thread
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
}