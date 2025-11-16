package edu.univ.erp.ui;

import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.admin.AdminCoursePanel;
import edu.univ.erp.ui.admin.AdminUserPanel;
import edu.univ.erp.ui.student.StudentCatalogPanel;
import edu.univ.erp.ui.student.StudentRegistrationsPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final UserSession session;

    private final StudentService studentService;
    private final AdminService adminService;

    public MainFrame(UserSession session) {
        this.session = session;

        this.studentService = new StudentService();
        this.adminService = new AdminService();

        setTitle("University ERP Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainTabs(), BorderLayout.CENTER);

        JLabel statusBar = new JLabel("Logged in as: " + session.getUsername() + " (" + session.getRole() + ")");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + session.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        panel.add(welcomeLabel, BorderLayout.WEST);
        panel.add(logoutButton, BorderLayout.EAST);

        // This version does NOT have the maintenance banner

        return panel;
    }

    private JTabbedPane createMainTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Home", new JPanel()); // Placeholder

        // --- Student Tabs ---
        if (session.isStudent()) {
            tabbedPane.addTab("Course Catalog", new StudentCatalogPanel(session, studentService));
            tabbedPane.addTab("My Registrations", new StudentRegistrationsPanel(session, studentService));
            tabbedPane.addTab("My Grades", new JPanel()); // Placeholder
        }

        // --- Instructor Tabs ---
        if (session.isInstructor()) {
            // This tab is a placeholder
            tabbedPane.addTab("My Sections", new JPanel());
            tabbedPane.addTab("Gradebook", new JPanel());
        }

        // --- Admin Tabs ---
        if (session.isAdmin()) {
            // All panels are now passed the AdminService
            tabbedPane.addTab("User Management", new AdminUserPanel(adminService));
            tabbedPane.addTab("Course Management", new AdminCoursePanel(adminService));
            // This tab is a placeholder
            tabbedPane.addTab("Maintenance", new JPanel());
        }

        return tabbedPane;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
}