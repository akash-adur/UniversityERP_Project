package edu.univ.erp.ui.student;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StudentCatalogPanel extends JPanel {

    private final UserSession session;
    private final CourseDAO courseDAO;
    private final StudentService studentService;

    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private JTable sectionsTable;
    private DefaultTableModel sectionsTableModel;
    private JComboBox<String> termFilter;
    private JLabel deadlineLabel;

    private List<Course> courseList = new ArrayList<>();
    private List<Section> sectionsList = new ArrayList<>();

    public StudentCatalogPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.courseDAO = new CourseDAO();
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Filter & Deadline ---
        JPanel topControlPanel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Term:"));
        termFilter = new JComboBox<>(new String[]{"All", "Monsoon", "Winter", "Summer"});
        termFilter.addActionListener(e -> loadCourseData());
        filterPanel.add(termFilter);
        topControlPanel.add(filterPanel, BorderLayout.WEST);

        deadlineLabel = new JLabel("Loading deadline...");
        deadlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deadlineLabel.setForeground(new Color(200, 50, 50));
        topControlPanel.add(deadlineLabel, BorderLayout.EAST);

        add(topControlPanel, BorderLayout.NORTH);

        // --- Course Table (Top) ---
        // Added "Terms" column
        String[] courseColumnNames = {"Course Code", "Course Title", "Credits", "Terms"};
        courseTableModel = new DefaultTableModel(courseColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        courseTable = new JTable(courseTableModel);
        courseTable.getTableHeader().setReorderingAllowed(false);
        courseTable.getTableHeader().setResizingAllowed(false);

        // Center Align Course Table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        courseTable.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setBorder(BorderFactory.createTitledBorder("Step 1: Select a Course"));

        // --- Sections Table (Bottom) ---
        String[] sectionColumnNames = {"Term", "Section ID", "Instructor", "Day/Time", "Room", "Capacity"};
        sectionsTableModel = new DefaultTableModel(sectionColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(sectionsTableModel);
        sectionsTable.getTableHeader().setReorderingAllowed(false);
        sectionsTable.getTableHeader().setResizingAllowed(false);

        // Center Align Sections Table
        sectionsTable.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane sectionScrollPane = new JScrollPane(sectionsTable);
        sectionScrollPane.setBorder(BorderFactory.createTitledBorder("Step 2: Select a Section"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, courseScrollPane, sectionScrollPane);
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);

        add(splitPane, BorderLayout.CENTER);

        JButton registerButton = new JButton("Register for Selected Section");
        add(registerButton, BorderLayout.SOUTH);

        loadDeadline();
        loadCourseData();

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = courseTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Get code from table to find the actual course object
                    String code = (String) courseTableModel.getValueAt(selectedRow, 0);
                    Course selectedCourse = courseList.stream()
                            .filter(c -> c.getCode().equals(code))
                            .findFirst()
                            .orElse(null);

                    if (selectedCourse != null) {
                        loadSections(selectedCourse.getCourseId());
                    }
                }
            }
        });

        registerButton.addActionListener(e -> onRegister());
    }

    private void loadDeadline() {
        try {
            LocalDate deadline = studentService.getAddDeadline();
            deadlineLabel.setText("⚠️ Last day to add: " + deadline);
        } catch (Exception e) {
            deadlineLabel.setText("Could not load deadline.");
        }
    }

    private void loadCourseData() {
        try {
            List<Course> allCourses = courseDAO.getAllCourses();
            courseList = allCourses; // Keep reference
            courseTableModel.setRowCount(0);

            String selectedTerm = (String) termFilter.getSelectedItem();

            for (Course course : allCourses) {
                // Determine available terms for this course
                List<Section> sections = studentService.getSectionsForCourse(course.getCourseId());
                Set<String> distinctTerms = new HashSet<>();
                for(Section s : sections) {
                    distinctTerms.add(s.getSemester());
                }

                // Filter Logic
                boolean match = "All".equals(selectedTerm);
                if (!match) {
                    for (String t : distinctTerms) {
                        if (t.equalsIgnoreCase(selectedTerm)) {
                            match = true;
                            break;
                        }
                    }
                }

                if (match) {
                    String termString = String.join(", ", distinctTerms);
                    courseTableModel.addRow(new Object[]{
                            course.getCode(),
                            course.getTitle(),
                            course.getCredits(),
                            termString // Show Terms
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    private void loadSections(int courseId) {
        try {
            sectionsList = studentService.getSectionsForCourse(courseId);
            sectionsTableModel.setRowCount(0);

            // Also respect the filter in the bottom view
            String selectedTerm = (String) termFilter.getSelectedItem();

            for (Section section : sectionsList) {
                if ("All".equals(selectedTerm) || section.getSemester().equalsIgnoreCase(selectedTerm)) {
                    sectionsTableModel.addRow(new Object[]{
                            section.getTerm(),
                            section.getSectionId(),
                            section.getInstructorName(),
                            section.getDayTime(),
                            section.getRoom(),
                            section.getCapacity()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + e.getMessage());
        }
    }

    private void onRegister() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to register for.", "No Section Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Map row index to list index isn't direct due to filtering, so we need to find the section ID
        int sectionId = (Integer) sectionsTableModel.getValueAt(selectedRow, 1);

        try {
            studentService.registerForSection(session.getUserId(), sectionId);
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}