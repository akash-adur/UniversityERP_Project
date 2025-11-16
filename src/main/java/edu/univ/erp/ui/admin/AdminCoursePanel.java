package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminCoursePanel extends JPanel {

    private final AdminService adminService;
    private final CourseDAO courseDAO;

    // Course form components
    private final JTextField courseCodeField = new JTextField(10);
    private final JTextField courseTitleField = new JTextField(20);
    private final JSpinner courseCreditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));

    // Section form components
    private final JComboBox<Course> courseDropdown = new JComboBox<>();
    private final JTextField sectionDayTimeField = new JTextField(15);
    private final JTextField sectionRoomField = new JTextField(10);
    private final JSpinner sectionCapacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 300, 1));

    // --- NEW DEADLINE COMPONENTS ---
    private final JTextField deadlineField = new JTextField(15);
    private final JButton saveDeadlineButton = new JButton("Save Deadline");

    // Reference to the sections panel
    private AdminSectionsPanel sectionsPanel;

    public AdminCoursePanel(AdminService adminService) {
        this.adminService = adminService;
        this.courseDAO = new CourseDAO();

        // Main layout: 4 rows, 1 column
        setLayout(new GridLayout(4, 1, 10, 10)); // <-- CHANGED TO 4
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel 1: Create Course ---
        JPanel createCoursePanel = new JPanel(new GridBagLayout());
        createCoursePanel.setBorder(BorderFactory.createTitledBorder("Create New Course"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        createCoursePanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        createCoursePanel.add(courseCodeField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        createCoursePanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        createCoursePanel.add(courseTitleField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        createCoursePanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        createCoursePanel.add(courseCreditsSpinner, gbc);
        JButton createCourseButton = new JButton("Create Course");
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        createCoursePanel.add(createCourseButton, gbc);

        // --- Panel 2: Create Section ---
        JPanel createSectionPanel = new JPanel(new GridBagLayout());
        createSectionPanel.setBorder(BorderFactory.createTitledBorder("Create New Section"));
        gbc = new GridBagConstraints(); // Reset gbc
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        createSectionPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        courseDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    setText(((Course) value).getCode() + " - " + ((Course) value).getTitle());
                }
                return this;
            }
        });
        createSectionPanel.add(courseDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        createSectionPanel.add(new JLabel("Day/Time (e.g., MWF 10-11):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        createSectionPanel.add(sectionDayTimeField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        createSectionPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        createSectionPanel.add(sectionRoomField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_END;
        createSectionPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        createSectionPanel.add(sectionCapacitySpinner, gbc);
        JButton createSectionButton = new JButton("Create Section");
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.LINE_START;
        createSectionPanel.add(createSectionButton, gbc);

        // --- Panel 3: Assign Instructor ---
        sectionsPanel = new AdminSectionsPanel(adminService);

        // --- Panel 4: Drop Deadline (NEW) ---
        JPanel deadlinePanel = new JPanel(new GridBagLayout());
        deadlinePanel.setBorder(BorderFactory.createTitledBorder("Application Settings"));
        gbc = new GridBagConstraints(); // Reset gbc
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        deadlinePanel.add(new JLabel("Drop Deadline (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        deadlinePanel.add(deadlineField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START;
        deadlinePanel.add(saveDeadlineButton, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 1.0; // Spacer
        deadlinePanel.add(new JPanel(), gbc);

        // --- Add all panels to main layout ---
        add(createCoursePanel);
        add(createSectionPanel);
        add(sectionsPanel);
        add(deadlinePanel); // Add the new panel

        // --- Load initial data ---
        loadCourseDropdown();
        loadDeadline(); // Load the deadline

        // --- Action Listeners ---
        createCourseButton.addActionListener(e -> onCreateCourse());
        createSectionButton.addActionListener(e -> onCreateSection());
        saveDeadlineButton.addActionListener(e -> onSaveDeadline()); // Add new listener
    }

    private void loadCourseDropdown() {
        try {
            List<Course> courseList = courseDAO.getAllCourses();
            courseDropdown.removeAllItems();
            for (Course course : courseList) {
                courseDropdown.addItem(course);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    private void loadDeadline() {
        try {
            String deadline = adminService.getSetting("drop_deadline");
            deadlineField.setText(deadline);
        } catch (SQLException e) {
            deadlineField.setText("Error loading deadline.");
            deadlineField.setEnabled(false);
            saveDeadlineButton.setEnabled(false);
        }
    }

    private void onCreateCourse() {
        try {
            adminService.createCourse(
                    courseCodeField.getText(),
                    courseTitleField.getText(),
                    (Integer) courseCreditsSpinner.getValue()
            );
            JOptionPane.showMessageDialog(this, "Course created!");
            loadCourseDropdown();
            courseCodeField.setText("");
            courseTitleField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating course: " + e.getMessage());
        }
    }

    private void onCreateSection() {
        try {
            Course selectedCourse = (Course) courseDropdown.getSelectedItem();
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(this, "Please select a course.");
                return;
            }
            adminService.createSection(
                    selectedCourse.getCourseId(),
                    sectionDayTimeField.getText(),
                    sectionRoomField.getText(),
                    (Integer) sectionCapacitySpinner.getValue(),
                    "Fall", 2025
            );
            JOptionPane.showMessageDialog(this, "Section created!");
            sectionDayTimeField.setText("");
            sectionRoomField.setText("");
            sectionsPanel.loadSections();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating section: " + e.getMessage());
        }
    }

    private void onSaveDeadline() {
        String newDeadline = deadlineField.getText();

        // Simple validation
        if (newDeadline.isBlank() || !newDeadline.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid format. Please use YYYY-MM-DD.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            adminService.setSetting("drop_deadline", newDeadline);
            JOptionPane.showMessageDialog(this, "Drop deadline saved.");
            loadDeadline(); // Refresh
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to save deadline.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}