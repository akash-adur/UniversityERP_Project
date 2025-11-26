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

    // --- Course Form Components ---
    private final JTextField courseCodeField = new JTextField(10);
    private final JTextField courseTitleField = new JTextField(20);
    private final JSpinner courseCreditsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 6, 1));

    // --- Section Form Components ---
    private final JComboBox<Course> courseDropdown = new JComboBox<>();

    // UPDATED: Split Day/Time into two fields
    private final JTextField sectionDaysField = new JTextField(8); // e.g. Mon/Wed
    private final JTextField sectionTimeField = new JTextField(8); // e.g. 10:00

    private final JTextField sectionRoomField = new JTextField(10);
    private final JSpinner sectionCapacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 300, 1));

    // NEW: Section Name Components
    private final JCheckBox hasSectionNameCheck = new JCheckBox("Assign Section Name?");
    private final JTextField sectionNameField = new JTextField(5);

    // Semester
    private final JComboBox<String> semesterDropdown = new JComboBox<>(new String[]{"Monsoon", "Winter", "Summer"});
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));

    // --- Settings Components ---
    private final JTextField dropDeadlineField = new JTextField(10);
    private final JTextField addDeadlineField = new JTextField(10);
    private final JButton saveSettingsButton = new JButton("Save Settings");

    private AdminSectionsPanel sectionsPanel;

    public AdminCoursePanel(AdminService adminService) {
        this.adminService = adminService;
        this.courseDAO = new CourseDAO();
        this.backupService = new BackupService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: Creation Forms (Side-by-Side) ---
        JPanel topContainer = new JPanel(new GridLayout(1, 2, 10, 0));

        // 1. Create Course Panel
        JPanel createCoursePanel = new JPanel(new GridBagLayout());
        createCoursePanel.setBorder(BorderFactory.createTitledBorder("Create New Course"));
        setupCourseForm(createCoursePanel);

        // 2. Create Section Panel
        JPanel createSectionPanel = new JPanel(new GridBagLayout());
        createSectionPanel.setBorder(BorderFactory.createTitledBorder("Create New Section"));
        setupSectionForm(createSectionPanel);

        // Add both to top container
        topContainer.add(createCoursePanel);
        topContainer.add(createSectionPanel);

        // --- CENTER: Assign Instructor (Big Table) ---
        sectionsPanel = new AdminSectionsPanel(adminService);

        // --- BOTTOM: Settings & Backups ---
        JPanel settingsPanel = createSettingsPanel();

        // --- ASSEMBLE ---
        add(topContainer, BorderLayout.NORTH);
        add(sectionsPanel, BorderLayout.CENTER);
        add(settingsPanel, BorderLayout.SOUTH);

        loadCourseDropdown();
        loadDeadlines();
    }

    private void setupCourseForm(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(courseCodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(courseTitleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(courseCreditsSpinner, gbc);

        JButton createCourseButton = new JButton("Create Course");
        createCourseButton.addActionListener(e -> onCreateCourse());
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(createCourseButton, gbc);

        // Spacer to push content up
        gbc.gridx = 0; gbc.gridy = 4; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
    }

    private void setupSectionForm(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 0: Course Dropdown
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        courseDropdown.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) setText(((Course) value).getCode() + " - " + ((Course) value).getTitle());
                return this;
            }
        });
        panel.add(courseDropdown, gbc);

        // Row 1: Days and Time (Split Fields)
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Days:"), gbc);

        // Create a sub-panel for Days and Time to fit them on one logical line or separate rows
        // Here I'll put them on separate rows for clarity as requested
        gbc.gridx = 1; gbc.gridy = 1; panel.add(sectionDaysField, gbc); // e.g. Mon/Wed

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(sectionTimeField, gbc); // e.g. 10:00

        // Row 3: Room
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(sectionRoomField, gbc);

        // Row 4: Capacity
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(sectionCapacitySpinner, gbc);

        // Row 5: Term
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Term:"), gbc);
        JPanel termPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        termPanel.add(semesterDropdown);
        termPanel.add(Box.createHorizontalStrut(5));
        termPanel.add(yearSpinner);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(termPanel, gbc);

        // Row 6: Section Name Checkbox
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(hasSectionNameCheck, gbc);

        JPanel secNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        secNamePanel.add(new JLabel("Name (e.g. A):"));
        secNamePanel.add(sectionNameField);
        gbc.gridx = 1; gbc.gridy = 6;
        panel.add(secNamePanel, gbc);

        // Logic: Disable text field by default
        sectionNameField.setEnabled(false);
        hasSectionNameCheck.addActionListener(e -> sectionNameField.setEnabled(hasSectionNameCheck.isSelected()));

        // Row 7: Button
        JButton createSectionButton = new JButton("Create Section");
        createSectionButton.addActionListener(e -> onCreateSection());
        gbc.gridx = 1; gbc.gridy = 7; gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(createSectionButton, gbc);

        // Spacer
        gbc.gridx = 0; gbc.gridy = 8; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Application Settings & Backups"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

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

        return settingsPanel;
    }

    // --- LOGIC METHODS ---

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

        // Check both day and time fields
        if (sectionDaysField.getText().isBlank() || sectionTimeField.getText().isBlank() || sectionRoomField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Combine Days and Time ---
        String days = sectionDaysField.getText().trim();
        String time = sectionTimeField.getText().trim();
        String combinedDayTime = days + " " + time;

        // --- Section Name Logic ---
        String secName = "N/A";
        if (hasSectionNameCheck.isSelected()) {
            secName = sectionNameField.getText().trim();
            if (secName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Section Name (e.g. A, B).");
                return;
            }
        }

        try {
            String semester = (String) semesterDropdown.getSelectedItem();
            int year = (Integer) yearSpinner.getValue();

            adminService.createSection(
                    selectedCourse.getCourseId(),
                    combinedDayTime, // Pass combined string
                    sectionRoomField.getText(),
                    (Integer) sectionCapacitySpinner.getValue(),
                    semester, year,
                    secName
            );
            JOptionPane.showMessageDialog(this, "Section created!");

            // Reset Fields
            sectionDaysField.setText(""); sectionTimeField.setText("");
            sectionRoomField.setText("");
            sectionNameField.setText(""); hasSectionNameCheck.setSelected(false);
            sectionNameField.setEnabled(false);

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