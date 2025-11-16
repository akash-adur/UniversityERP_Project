package edu.univ.erp.ui.student;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentCatalogPanel extends JPanel {

    private final UserSession session;
    private final CourseDAO courseDAO;
    private final StudentService studentService;

    // Tables
    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private JTable sectionsTable;
    private DefaultTableModel sectionsTableModel;

    // Data lists
    private List<Course> courseList = new ArrayList<>();
    private List<Section> sectionsList = new ArrayList<>();

    public StudentCatalogPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.courseDAO = new CourseDAO();
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Course Table (Top) ---
        String[] courseColumnNames = {"Course Code", "Course Title", "Credits"};
        courseTableModel = new DefaultTableModel(courseColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        courseTable = new JTable(courseTableModel);
        JScrollPane courseScrollPane = new JScrollPane(courseTable);
        courseScrollPane.setBorder(BorderFactory.createTitledBorder("Step 1: Select a Course"));

        // --- Sections Table (Bottom) ---
        String[] sectionColumnNames = {"Section ID", "Instructor", "Day/Time", "Room", "Capacity"};
        sectionsTableModel = new DefaultTableModel(sectionColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(sectionsTableModel);
        JScrollPane sectionScrollPane = new JScrollPane(sectionsTable);
        sectionScrollPane.setBorder(BorderFactory.createTitledBorder("Step 2: Select a Section"));

        // --- Create Split Pane ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                courseScrollPane,
                sectionScrollPane);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // --- Register Button (Bottom) ---
        JButton registerButton = new JButton("Register for Selected Section");
        add(registerButton, BorderLayout.SOUTH);

        // --- Load Initial Data ---
        loadCourseData();

        // --- Action Listeners ---
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = courseTable.getSelectedRow();
                if (selectedRow != -1) {
                    Course selectedCourse = courseList.get(selectedRow);
                    loadSections(selectedCourse.getCourseId());
                }
            }
        });

        registerButton.addActionListener(e -> onRegister());
    }

    private void loadCourseData() {
        try {
            courseList = courseDAO.getAllCourses();

            courseTableModel.setRowCount(0);
            for (Course course : courseList) {
                courseTableModel.addRow(new Object[]{
                        course.getCode(),
                        course.getTitle(),
                        course.getCredits()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    private void loadSections(int courseId) {
        try {
            sectionsList = studentService.getSectionsForCourse(courseId);

            sectionsTableModel.setRowCount(0);
            for (Section section : sectionsList) {
                sectionsTableModel.addRow(new Object[]{
                        section.getSectionId(),
                        section.getInstructorName(),
                        section.getDayTime(),
                        section.getRoom(),
                        section.getCapacity()
                });
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

        Section selectedSection = sectionsList.get(selectedRow);

        try {
            studentService.registerForSection(session.getUserId(), selectedSection.getSectionId());
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}