package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.BackupService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private final JComboBox<String> day1Combo = new JComboBox<>(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
    private final JComboBox<String> day2Combo = new JComboBox<>(new String[]{"None", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
    private final JSpinner startTimeSpinner;
    private final JSpinner endTimeSpinner;
    private final JTextField sectionRoomField = new JTextField(10);
    private final JSpinner sectionCapacitySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 300, 1));
    private final JCheckBox hasSectionNameCheck = new JCheckBox("Assign Section Name?");
    private final JTextField sectionNameField = new JTextField(5);
    private final JComboBox<String> semesterDropdown = new JComboBox<>(new String[]{"Monsoon", "Winter", "Summer"});
    private final JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));

    // --- Settings Components ---
    private final JSpinner addDeadlineSpinner;
    private final JSpinner dropDeadlineSpinner;
    private final JButton saveSettingsButton = new JButton("Save Settings");

    private AdminSectionsPanel sectionsPanel;

    public AdminCoursePanel(AdminService adminService) {
        this.adminService = adminService;
        this.courseDAO = new CourseDAO();
        this.backupService = new BackupService();

        startTimeSpinner = createTimeSpinner();
        endTimeSpinner = createTimeSpinner();
        addDeadlineSpinner = createDateSpinner();
        dropDeadlineSpinner = createDateSpinner();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel(new GridLayout(1, 2, 10, 0));

        JPanel createCoursePanel = new JPanel(new GridBagLayout());
        createCoursePanel.setBorder(BorderFactory.createTitledBorder("Create New Course"));
        setupCourseForm(createCoursePanel);

        JPanel createSectionPanel = new JPanel(new GridBagLayout());
        createSectionPanel.setBorder(BorderFactory.createTitledBorder("Create New Section"));
        setupSectionForm(createSectionPanel);

        topContainer.add(createCoursePanel);
        topContainer.add(createSectionPanel);

        sectionsPanel = new AdminSectionsPanel(adminService);
        JPanel settingsPanel = createSettingsPanel();

        add(topContainer, BorderLayout.NORTH);
        add(sectionsPanel, BorderLayout.CENTER);
        add(settingsPanel, BorderLayout.SOUTH);

        loadCourseDropdown();
        loadDeadlines();
    }

    private JSpinner createTimeSpinner() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor de = new JSpinner.DateEditor(s, "HH:mm");
        s.setEditor(de);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        s.setValue(cal.getTime());
        return s;
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        model.setStart(new Date());
        JSpinner s = new JSpinner(model);
        s.setEditor(new JSpinner.DateEditor(s, "yyyy-MM-dd"));
        return s;
    }

    private void setupCourseForm(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Code:"), gbc);
        gbc.gridx=1; panel.add(courseCodeField, gbc);
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Title:"), gbc);
        gbc.gridx=1; panel.add(courseTitleField, gbc);
        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Credits:"), gbc);
        gbc.gridx=1; panel.add(courseCreditsSpinner, gbc);

        JButton createCourseButton = new JButton("Create Course");
        createCourseButton.addActionListener(e -> onCreateCourse());
        gbc.gridx=1; gbc.gridy=3; panel.add(createCourseButton, gbc);
        gbc.gridy=4; gbc.weighty=1.0; panel.add(new JPanel(), gbc);
    }

    private void setupSectionForm(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Course:"), gbc);
        gbc.gridx=1;
        courseDropdown.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) setText(((Course) value).getCode());
                return this;
            }
        });
        panel.add(courseDropdown, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Days:"), gbc);
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dayPanel.add(day1Combo);
        dayPanel.add(new JLabel(" & "));
        dayPanel.add(day2Combo);
        gbc.gridx=1; panel.add(dayPanel, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Time:"), gbc);
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timePanel.add(startTimeSpinner);
        timePanel.add(new JLabel(" to "));
        timePanel.add(endTimeSpinner);
        gbc.gridx=1; panel.add(timePanel, gbc);

        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("Room:"), gbc);
        gbc.gridx=1; panel.add(sectionRoomField, gbc);

        gbc.gridx=0; gbc.gridy=4; panel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx=1; panel.add(sectionCapacitySpinner, gbc);

        gbc.gridx=0; gbc.gridy=5; panel.add(new JLabel("Term:"), gbc);
        JPanel termPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        termPanel.add(semesterDropdown);
        termPanel.add(Box.createHorizontalStrut(5));
        termPanel.add(yearSpinner);
        gbc.gridx=1; panel.add(termPanel, gbc);

        gbc.gridx=0; gbc.gridy=6; panel.add(hasSectionNameCheck, gbc);
        JPanel secNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        secNamePanel.add(new JLabel("Name:"));
        secNamePanel.add(sectionNameField);
        gbc.gridx=1; panel.add(secNamePanel, gbc);
        sectionNameField.setEnabled(false);
        hasSectionNameCheck.addActionListener(e -> sectionNameField.setEnabled(hasSectionNameCheck.isSelected()));

        JButton createSectionButton = new JButton("Create Section");
        createSectionButton.addActionListener(e -> onCreateSection());
        gbc.gridx=1; gbc.gridy=7; panel.add(createSectionButton, gbc);
        gbc.gridy=8; gbc.weighty=1.0; panel.add(new JPanel(), gbc);
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings & Backups"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx=0; gbc.gridy=0; settingsPanel.add(new JLabel("Add Deadline:"), gbc);
        gbc.gridx=1; settingsPanel.add(addDeadlineSpinner, gbc);

        gbc.gridx=2; settingsPanel.add(new JLabel("Drop Deadline:"), gbc);
        gbc.gridx=3; settingsPanel.add(dropDeadlineSpinner, gbc);

        saveSettingsButton.addActionListener(e -> onSaveSettings());
        gbc.gridx=4; settingsPanel.add(saveSettingsButton, gbc);

        JButton createBackupBtn = new JButton("Create Backup");
        createBackupBtn.setBackground(new Color(0, 100, 0));
        createBackupBtn.setForeground(Color.WHITE);
        createBackupBtn.addActionListener(e -> performBackup());
        gbc.gridx=5; settingsPanel.add(createBackupBtn, gbc);

        JButton restoreBtn = new JButton("Restore Backup");
        restoreBtn.setBackground(new Color(200, 50, 50));
        restoreBtn.setForeground(Color.WHITE);
        restoreBtn.addActionListener(e -> performRestore());
        gbc.gridx=6; settingsPanel.add(restoreBtn, gbc);

        return settingsPanel;
    }

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
            String dropStr = adminService.getSetting("drop_deadline");
            String addStr = adminService.getSetting("add_deadline");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (addStr != null) addDeadlineSpinner.setValue(sdf.parse(addStr));
            if (dropStr != null) dropDeadlineSpinner.setValue(sdf.parse(dropStr));
        } catch (Exception e) { }
    }

    private void onCreateCourse() {
        if (courseCodeField.getText().isBlank() || courseTitleField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            adminService.createCourse(courseCodeField.getText(), courseTitleField.getText(), (Integer) courseCreditsSpinner.getValue());
            JOptionPane.showMessageDialog(this, "Course created!");
            loadCourseDropdown();
            courseCodeField.setText(""); courseTitleField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void onCreateSection() {
        Course selectedCourse = (Course) courseDropdown.getSelectedItem();
        if (selectedCourse == null) { JOptionPane.showMessageDialog(this, "Select a course."); return; }

        if (sectionRoomField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Room required.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String d1 = (String) day1Combo.getSelectedItem();
        String d2 = (String) day2Combo.getSelectedItem();
        String days = d1;
        if (!"None".equals(d2) && !d2.equals(d1)) days += "/" + d2;

        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
        String start = timeFmt.format((Date) startTimeSpinner.getValue());
        String end = timeFmt.format((Date) endTimeSpinner.getValue());
        String combined = days + " " + start + "-" + end;

        String secName = "N/A";
        if (hasSectionNameCheck.isSelected()) {
            secName = sectionNameField.getText().trim();
            if (secName.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Section Name."); return; }
        }

        try {
            adminService.createSection(selectedCourse.getCourseId(), combined, sectionRoomField.getText(),
                    (Integer) sectionCapacitySpinner.getValue(), (String) semesterDropdown.getSelectedItem(),
                    (Integer) yearSpinner.getValue(), secName);
            JOptionPane.showMessageDialog(this, "Section created!");
            sectionsPanel.loadSections();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void onSaveSettings() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String add = sdf.format((Date) addDeadlineSpinner.getValue());
        String drop = sdf.format((Date) dropDeadlineSpinner.getValue());

        try {
            adminService.setSetting("add_deadline", add);
            adminService.setSetting("drop_deadline", drop);
            JOptionPane.showMessageDialog(this, "Settings saved.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to save.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performBackup() {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() { backupService.createBackup(); return null; }
            protected void done() { JOptionPane.showMessageDialog(AdminCoursePanel.this, "Backup Created!"); }
        }.execute();
    }

    // RESTORED: Wait Dialog with visual feedback
    private void performRestore() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore from the last MANUAL backup?\nAny changes made since then will be lost.",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            JDialog loading = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Restoring...", true);
            loading.add(new JLabel("Restoring data, please wait...", SwingConstants.CENTER));
            loading.setSize(300, 100);
            loading.setLocationRelativeTo(this);
            loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

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