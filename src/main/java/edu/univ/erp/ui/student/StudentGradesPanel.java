package edu.univ.erp.ui.student;

import edu.univ.erp.domain.EnrollmentDetails;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentGradesPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;

    // UI Components
    private JTable gradesTable;
    private DefaultTableModel tableModel;

    public StudentGradesPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Title Section ---
        JLabel titleLabel = new JLabel("My Academic Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        // We will show the Course info and the Final Grade
        String[] columnNames = {"Course Code", "Course Title", "Instructor", "Credits", "Final Grade"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        gradesTable = new JTable(tableModel);
        gradesTable.setRowHeight(25);
        gradesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Refresh Button (Bottom) ---
        JButton refreshButton = new JButton("Refresh Grades");
        refreshButton.addActionListener(e -> loadGrades());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data when panel is created
        loadGrades();
    }

    private void loadGrades() {
        try {
            List<EnrollmentDetails> enrollments = studentService.getEnrollmentsForStudent(session.getUserId());

            tableModel.setRowCount(0);

            for (EnrollmentDetails e : enrollments) {
                // USE THE REAL GRADE NOW
                String grade = e.getFinalGrade();

                tableModel.addRow(new Object[]{
                        e.getCourseCode(),
                        e.getCourseTitle(),
                        e.getInstructorName(),
                        "4",
                        grade // <--- Displays real grade from DB (e.g. "A", "85.5", or "N/A")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage());
        }
    }
}