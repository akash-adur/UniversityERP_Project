package edu.univ.erp.ui;

import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.ui.admin.AdminCoursePanel;
import edu.univ.erp.ui.admin.AdminMaintenancePanel;
import edu.univ.erp.ui.admin.AdminUserPanel;
import edu.univ.erp.ui.instructor.InstructorGradebookPanel;
import edu.univ.erp.ui.instructor.InstructorGradesManagementPanel;
import edu.univ.erp.ui.instructor.InstructorSectionsPanel;
import edu.univ.erp.ui.student.StudentCatalogPanel;
import edu.univ.erp.ui.student.StudentGradesPanel;
import edu.univ.erp.ui.student.StudentRegistrationsPanel;
import edu.univ.erp.ui.student.StudentTimetablePanel;

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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Standard Close
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);

        // --- Layout Setup ---
        setLayout(new BorderLayout());
        JPanel mainContainer = new JPanel(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);

        // 1. Maintenance Banner
        addMaintenanceBanner(mainContainer);

        // 2. Top Panel & Tabs
        JPanel topPanel = createTopPanel();
        JTabbedPane tabbedPane = createMainTabs();

        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.add(topPanel, BorderLayout.NORTH);
        centerContent.add(tabbedPane, BorderLayout.CENTER);
        mainContainer.add(centerContent, BorderLayout.CENTER);

        // 3. Status Bar
        JLabel statusBar = new JLabel("Logged in as: " + session.getUsername() + " (" + session.getRole() + ")");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);
    }

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
                container.add(banner, BorderLayout.NORTH);
            }
        } catch (Exception e) {
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

        // REMOVED: tabbedPane.addTab("Home", new JPanel());

        if (session.isStudent()) {
            tabbedPane.addTab("Course Catalog", new StudentCatalogPanel(session, studentService));
            tabbedPane.addTab("My Registrations", new StudentRegistrationsPanel(session, studentService));
            tabbedPane.addTab("My Timetable", new StudentTimetablePanel(session, studentService));
            tabbedPane.addTab("My Grades", new StudentGradesPanel(session, studentService));
        }

        if (session.isInstructor()) {
            tabbedPane.addTab("My Sections", new InstructorSectionsPanel(session));
            tabbedPane.addTab("Grades Management", new InstructorGradesManagementPanel(session));
            tabbedPane.addTab("Gradebook View", new InstructorGradebookPanel(session));
        }

        if (session.isAdmin()) {
            tabbedPane.addTab("User Management", new AdminUserPanel(adminService));
            tabbedPane.addTab("Course Management", new AdminCoursePanel(adminService));
            tabbedPane.addTab("Maintenance", new AdminMaintenancePanel(adminService, () -> {
                // Quick refresh for banner toggling
                this.dispose();
                new MainFrame(session).setVisible(true);
            }));
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