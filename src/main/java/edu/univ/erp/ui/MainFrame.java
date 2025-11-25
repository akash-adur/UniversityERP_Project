package edu.univ.erp.ui;

import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.admin.AdminCoursePanel;
import edu.univ.erp.ui.admin.AdminMaintenancePanel;
import edu.univ.erp.ui.admin.AdminUserPanel;
import edu.univ.erp.ui.instructor.InstructorGradebookPanel;
import edu.univ.erp.ui.instructor.InstructorSectionsPanel;
import edu.univ.erp.ui.student.StudentCatalogPanel;
import edu.univ.erp.ui.student.StudentGradesPanel;
import edu.univ.erp.ui.student.StudentRegistrationsPanel;
import edu.univ.erp.ui.instructor.InstructorGradesManagementPanel;

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

        // --- UPDATED LAYOUT FOR BANNER ---
        setLayout(new BorderLayout());

        // 1. Create Main Container to hold Banner + Content
        JPanel mainContainer = new JPanel(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);

        // 2. Add Maintenance Banner Check
        addMaintenanceBanner(mainContainer);

        // 3. Create Top Panel (Welcome + Logout)
        JPanel topPanel = createTopPanel();

        // 4. Create Main Content Tabs
        JTabbedPane tabbedPane = createMainTabs();

        // 5. Assemble Center Content (Top Panel + Tabs)
        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.add(topPanel, BorderLayout.NORTH);
        centerContent.add(tabbedPane, BorderLayout.CENTER);

        mainContainer.add(centerContent, BorderLayout.CENTER);

        // 6. Status Bar (Bottom)
        JLabel statusBar = new JLabel("Logged in as: " + session.getUsername() + " (" + session.getRole() + ")");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Checks the database settings and adds a red banner if maintenance is ON.
     */
    private void addMaintenanceBanner(JPanel container) {
        try {
            String mode = adminService.getSetting("maintenance_mode");
            if ("true".equalsIgnoreCase(mode)) {
                JLabel banner = new JLabel("⚠️ SYSTEM UNDER MAINTENANCE - CHANGES RESTRICTED ⚠️", SwingConstants.CENTER);
                banner.setOpaque(true);
                banner.setBackground(Color.RED);
                banner.setForeground(Color.WHITE);
                banner.setFont(new Font("Segoe UI", Font.BOLD, 14));
                banner.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

                // Add banner to the very top of the main container
                container.add(banner, BorderLayout.NORTH);
            }
        } catch (Exception e) {
            // Ignore if DB error on startup (banner just won't show)
            System.err.println("Could not check maintenance status: " + e.getMessage());
        }
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

        return panel;
    }

    private JTabbedPane createMainTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Home", new JPanel()); // Placeholder or Welcome text

        // --- Student Tabs ---
        if (session.isStudent()) {
            tabbedPane.addTab("Course Catalog", new StudentCatalogPanel(session, studentService));
            tabbedPane.addTab("My Registrations", new StudentRegistrationsPanel(session, studentService));
            // UPDATED: Now uses the real grades panel
            tabbedPane.addTab("My Grades", new StudentGradesPanel(session, studentService));
        }

        // --- Instructor Tabs ---
        if (session.isInstructor()) {
            tabbedPane.addTab("My Sections", new InstructorSectionsPanel(session));
            // 1. New Management Panel
            tabbedPane.addTab("Grades Management", new InstructorGradesManagementPanel(session));
            // 2. Old (now View-Only) Gradebook
            tabbedPane.addTab("Gradebook View", new InstructorGradebookPanel(session));
        }

        // --- Admin Tabs ---
        if (session.isAdmin()) {
            tabbedPane.addTab("User Management", new AdminUserPanel(adminService));
            tabbedPane.addTab("Course Management", new AdminCoursePanel(adminService));
            // UPDATED: Now uses the real maintenance panel
            tabbedPane.addTab("Maintenance", new AdminMaintenancePanel(adminService));
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