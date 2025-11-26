package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.BackupService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminCoursePanel extends JPanel {

    private final AdminService adminService;
    private final CourseDAO courseDAO;
    private final BackupService backupService;

    // Course form
    private final JTextField courseCodeField = new JTextField(10);
    private final JTextField courseTitleField = new JTextField(20);
    private final JSpinner courseCreditsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 6, 1));

    // Section form
    private final JComboBox<Course> courseDropdown = new JComboBox<>();
    private final JTextField sectionDayTimeField = new JTextField(15);
    private final JTextField sectionRoomField = new JTextField(10);
    private final JSpinner sectionCapacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 300, 1));

    // Semester
    private final JComboBox<String> semesterDropdown = new JComboBox<>(new String[]{"Monsoon", "Winter", "Summer"});
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));

    // Deadlines
    private final JTextField dropDeadlineField = new JTextField(10);
    private final JTextField addDeadlineField = new JTextField(10);
    private final JButton saveSettingsButton = new JButton("Save Settings");

    private AdminSectionsPanel sectionsPanel;

    public AdminCoursePanel(AdminService adminService) {
        this.adminService = adminService;
        this.courseDAO = new CourseDAO();
        this.backupService = new BackupService();

        setLayout(new BorderLayout(10, 10)); // Use BorderLayout for the main panel
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: Creation Forms (Side-by-Side) ---
        JPanel topContainer = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 row, 2 cols

        // 1. Create Course Panel
        JPanel createCoursePanel = new JPanel(new GridBagLayout());
        createCoursePanel.setBorder(BorderFactory.createTitledBorder("Create New Course"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        createCoursePanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        createCoursePanel.add(courseCodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        createCoursePanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        createCoursePanel.add(courseTitleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        createCoursePanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        createCoursePanel.add(courseCreditsSpinner, gbc);

        JButton createCourseButton = new JButton("Create Course");
        createCourseButton.addActionListener(e -> onCreateCourse());
        gbc.gridx = 1; gbc.gridy = 3;
        createCoursePanel.add(createCourseButton, gbc);

        // Push everything to top
        gbc.gridx = 0; gbc.gridy = 4; gbc.weighty = 1.0;
        createCoursePanel.add(new JPanel(), gbc);


        // 2. Create Section Panel
        JPanel createSectionPanel = new JPanel(new GridBagLayout());
        createSectionPanel.setBorder(BorderFactory.createTitledBorder("Create New Section"));

        // Reset GBC
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        createSectionPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        courseDropdown.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) setText(((Course) value).getCode() + " - " + ((Course) value).getTitle());
                return this;
            }
        });
        createSectionPanel.add(courseDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1; createSectionPanel.add(new JLabel("Day/Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; createSectionPanel.add(sectionDayTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; createSectionPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; createSectionPanel.add(sectionRoomField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; createSectionPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; createSectionPanel.add(sectionCapacitySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; createSectionPanel.add(new JLabel("Term:"), gbc);
        JPanel termPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        termPanel.add(semesterDropdown);
        termPanel.add(Box.createHorizontalStrut(5));
        termPanel.add(yearSpinner);
        gbc.gridx = 1; gbc.gridy = 4; createSectionPanel.add(termPanel, gbc);

        JButton createSectionButton = new JButton("Create Section");
        createSectionButton.addActionListener(e -> onCreateSection());
        gbc.gridx = 1; gbc.gridy = 5; gbc.anchor = GridBagConstraints.LINE_START;
        createSectionPanel.add(createSectionButton, gbc);

        // Spacer to align with left panel
        gbc.gridx = 0; gbc.gridy = 6; gbc.weighty = 1.0;
        createSectionPanel.add(new JPanel(), gbc);

        // Add both to top container
        topContainer.add(createCoursePanel);
        topContainer.add(createSectionPanel);

        // --- CENTER: Assign Instructor (Big Table) ---
        sectionsPanel = new AdminSectionsPanel(adminService);

        // --- BOTTOM: Settings & Backups ---
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Application Settings & Backups"));

        gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; settingsPanel.add(new JLabel("Add Deadline (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; settingsPanel.add(addDeadlineField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; settingsPanel.add(new JLabel("Drop Deadline:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; settingsPanel.add(dropDeadlineField, gbc);

        saveSettingsButton.addActionListener(e -> onSaveSettings());
        gbc.gridx = 4; gbc.gridy = 0; settingsPanel.add(saveSettingsButton, gbc);

        JButton createBackupBtn = new JButton("Create Backup");
        createBackupBtn.setBackground(new Color(0, 100, 0)); // Green
        createBackupBtn.setForeground(Color.WHITE);
        createBackupBtn.addActionListener(e -> performBackup());
        gbc.gridx = 5; gbc.gridy = 0; settingsPanel.add(createBackupBtn, gbc);

        JButton restoreBtn = new JButton("Restore Backup");
        restoreBtn.setBackground(new Color(200, 50, 50)); // Red
        restoreBtn.setForeground(Color.WHITE);
        restoreBtn.addActionListener(e -> performRestore());
        gbc.gridx = 6; gbc.gridy = 0; settingsPanel.add(restoreBtn, gbc);

        // --- ASSEMBLE ---
        add(topContainer, BorderLayout.NORTH);
        add(sectionsPanel, BorderLayout.CENTER); // This will expand to fill space
        add(settingsPanel, BorderLayout.SOUTH);

        loadCourseDropdown();
        loadDeadlines();
    }

    // ... (Keep private helper methods: loadCourseDropdown, loadDeadlines, onCreateCourse, etc.) ...
    // Copy them from your previous file as they haven't changed logic, just layout.
    private void loadCourseDropdown() {
        try {
            List<Course> courseList = courseDAO.getAllCourses();
            courseDropdown.removeAllItems();
            for (Course course : courseList) courseDropdown.addItem(course);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    private void loadDeadlines() {
        try {
            String drop = adminService.getSetting("drop_deadline");
            dropDeadlineField.setText(drop);
            String add = adminService.getSetting("add_deadline");
            addDeadlineField.setText(add);
        } catch (SQLException e) {
            dropDeadlineField.setText("Error");
        }
    }

    private void onCreateCourse() {
        if (courseCodeField.getText().isBlank() || courseTitleField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            adminService.createCourse(courseCodeField.getText(), courseTitleField.getText(), (Integer) courseCreditsSpinner.getValue());
            JOptionPane.showMessageDialog(this, "Course created!");
            loadCourseDropdown();
            courseCodeField.setText(""); courseTitleField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating course: " + e.getMessage());
        }
    }

    private void onCreateSection() {
        Course selectedCourse = (Course) courseDropdown.getSelectedItem();
        if (selectedCourse == null) { JOptionPane.showMessageDialog(this, "Select a course."); return; }
        if (sectionDayTimeField.getText().isBlank() || sectionRoomField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String semester = (String) semesterDropdown.getSelectedItem();
            int year = (Integer) yearSpinner.getValue();
            adminService.createSection(
                    selectedCourse.getCourseId(),
                    sectionDayTimeField.getText(),
                    sectionRoomField.getText(),
                    (Integer) sectionCapacitySpinner.getValue(),
                    semester, year
            );
            JOptionPane.showMessageDialog(this, "Section created!");
            sectionDayTimeField.setText(""); sectionRoomField.setText("");
            sectionsPanel.loadSections();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating section: " + e.getMessage());
        }
    }

    private void onSaveSettings() {
        String drop = dropDeadlineField.getText();
        String add = addDeadlineField.getText();
        if (!drop.matches("\\d{4}-\\d{2}-\\d{2}") || !add.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid format. Please use YYYY-MM-DD.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            adminService.setSetting("drop_deadline", drop);
            adminService.setSetting("add_deadline", add);
            JOptionPane.showMessageDialog(this, "Settings saved.");
            loadDeadlines();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performBackup() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                backupService.createBackup();
                return null;
            }
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(AdminCoursePanel.this, "Backup Created Successfully!");
            }
        }.execute();
    }

    private void performRestore() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore from the last MANUAL backup?\nAny changes made since then will be lost.",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            JDialog loading = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Restoring...", true);
            loading.add(new JLabel("Restoring data, please wait...", SwingConstants.CENTER));
            loading.setSize(300, 100);
            loading.setLocationRelativeTo(this);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    backupService.restoreBackup();
                    return null;
                }
                @Override
                protected void done() {
                    loading.dispose();
                    try {
                        get();
                        JOptionPane.showMessageDialog(AdminCoursePanel.this, "Restore Complete! Please restart the app.");
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AdminCoursePanel.this, "Restore Failed: " + e.getMessage());
                    }
                }
            }.execute();

            loading.setVisible(true);
        }
    }
}