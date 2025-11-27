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

public class StudentCatalogPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private final CourseDAO courseDAO;

    private JTable courseTable;
    private DefaultTableModel courseModel;
    private JTable sectionTable;
    private DefaultTableModel sectionModel;

    // UI Elements
    private JComboBox<String> termFilter;
    private JLabel deadlineLabel;
    private JButton registerButton;

    // Data Cache
    private List<Course> allCourses = new ArrayList<>();

    public StudentCatalogPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Top Panel: Filter & Deadline ---
        JPanel topPanel = new JPanel(new BorderLayout());

        // Left: Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Term:"));
        termFilter = new JComboBox<>(new String[]{"All", "Monsoon", "Winter", "Summer"});
        termFilter.addActionListener(e -> loadCourseData());
        filterPanel.add(termFilter);
        topPanel.add(filterPanel, BorderLayout.WEST);

        // Right: Deadline Label (Requested Fix)
        deadlineLabel = new JLabel("Loading deadline...");
        deadlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deadlineLabel.setForeground(new Color(200, 50, 50));
        topPanel.add(deadlineLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Middle: Course & Section Tables ---

        // Course Table
        String[] courseCols = {"Code", "Title", "Credits", "Terms"}; // Included 'Terms' column
        courseModel = new DefaultTableModel(courseCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        courseTable = new JTable(courseModel);
        styleTable(courseTable);

        // Listener: Load sections when course selected
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSectionsForSelectedCourse();
        });

        // Section Table
        String[] sectionCols = {"Section ID", "Sec", "Term", "Days", "Time", "Room", "Instructor"};
        sectionModel = new DefaultTableModel(sectionCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        sectionTable = new JTable(sectionModel);
        styleTable(sectionTable);

        // Resize specific columns
        sectionTable.getColumnModel().getColumn(1).setPreferredWidth(40); // Sec
        sectionTable.getColumnModel().getColumn(1).setMaxWidth(60);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(courseTable), new JScrollPane(sectionTable));
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // --- 3. Bottom: Register Button ---
        registerButton = new JButton("Register for Selected Section");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(new Color(0, 120, 215));
        registerButton.setForeground(Color.WHITE);
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(e -> registerAction());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(registerButton);
        add(btnPanel, BorderLayout.SOUTH);

        // --- Initial Load ---
        loadDeadline();
        loadCourseData();
    }

    private void styleTable(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void loadDeadline() {
        try {
            LocalDate deadline = studentService.getAddDeadline();
            deadlineLabel.setText("⚠️ Last day to add: " + deadline);
        } catch (Exception e) {
            deadlineLabel.setText("Check syllabus for deadline");
        }
    }

    private void loadCourseData() {
        try {
            allCourses = courseDAO.getAllCourses();
            courseModel.setRowCount(0);
            String selectedTerm = (String) termFilter.getSelectedItem();

            for (Course c : allCourses) {
                // We need to know which terms this course is offered in to filter it
                List<Section> sections = studentService.getSectionsForCourse(c.getCourseId());
                Set<String> distinctTerms = new HashSet<>();
                for(Section s : sections) distinctTerms.add(s.getSemester());

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
                    String termStr = String.join(", ", distinctTerms);
                    courseModel.addRow(new Object[]{
                            c.getCode(),
                            c.getTitle(),
                            c.getCredits(),
                            termStr
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSectionsForSelectedCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) return;

        // Find the course object based on Code (col 0)
        String code = (String) courseModel.getValueAt(row, 0);
        Course selectedCourse = allCourses.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst().orElse(null);

        if (selectedCourse == null) return;

        try {
            List<Section> sections = studentService.getSectionsForCourse(selectedCourse.getCourseId());
            sectionModel.setRowCount(0);

            String filter = (String) termFilter.getSelectedItem();

            for (Section s : sections) {
                // Apply term filter to sections as well
                if (!"All".equals(filter) && !s.getSemester().equalsIgnoreCase(filter)) {
                    continue;
                }

                // Split Day/Time for display
                String dayTime = s.getDayTime();
                String days = dayTime;
                String time = "";
                if (dayTime != null && dayTime.contains(" ")) {
                    String[] parts = dayTime.split(" ", 2);
                    days = parts[0];
                    time = parts[1];
                }

                sectionModel.addRow(new Object[]{
                        s.getSectionId(),
                        s.getSectionName(),
                        s.getSemester() + " " + s.getYear(),
                        days,
                        time,
                        s.getRoom(),
                        s.getInstructorName(),
                        s.getCapacity()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void registerAction() {
        int row = sectionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (Integer) sectionModel.getValueAt(row, 0);

        try {
            studentService.registerForSection(session.getUserId(), sectionId);
            JOptionPane.showMessageDialog(this, "Successfully registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}