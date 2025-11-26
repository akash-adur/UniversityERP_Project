package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.data.CourseDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentCatalogPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private final CourseDAO courseDAO;

    private JTable courseTable;
    private DefaultTableModel courseModel;
    private JTable sectionTable;
    private DefaultTableModel sectionModel;
    private JButton registerButton;

    public StudentCatalogPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top: Course Table ---
        String[] courseCols = {"ID", "Code", "Title", "Credits"};
        courseModel = new DefaultTableModel(courseCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        courseTable = new JTable(courseModel);
        styleTable(courseTable);

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSectionsForSelectedCourse();
        });

        // --- Bottom: Section Table (UPDATED COLUMNS) ---
        String[] sectionCols = {"Section ID", "Sec", "Days", "Time", "Room", "Capacity", "Instructor"};
        sectionModel = new DefaultTableModel(sectionCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        sectionTable = new JTable(sectionModel);
        styleTable(sectionTable);

        // Resize "Sec" col
        sectionTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        sectionTable.getColumnModel().getColumn(1).setMaxWidth(60);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(courseTable), new JScrollPane(sectionTable));
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

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

        loadCourses();
    }

    private void styleTable(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.setRowHeight(25);
    }

    private void loadCourses() {
        try {
            List<Course> courses = courseDAO.getAllCourses();
            courseModel.setRowCount(0);
            for (Course c : courses) {
                courseModel.addRow(new Object[]{c.getCourseId(), c.getCode(), c.getTitle(), c.getCredits()});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadSectionsForSelectedCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) return;
        int courseId = (Integer) courseModel.getValueAt(row, 0);

        try {
            List<Section> sections = studentService.getSectionsForCourse(courseId);
            sectionModel.setRowCount(0);
            for (Section s : sections) {
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
                        days,
                        time,
                        s.getRoom(),
                        s.getCapacity(),
                        s.getInstructorName()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void registerAction() {
        int row = sectionTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        int sectionId = (Integer) sectionModel.getValueAt(row, 0);

        try {
            studentService.registerForSection(session.getUserId(), sectionId);
            JOptionPane.showMessageDialog(this, "Successfully registered!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}