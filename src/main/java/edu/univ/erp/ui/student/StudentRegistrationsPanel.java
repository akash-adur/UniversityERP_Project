package edu.univ.erp.ui.student;

import edu.univ.erp.domain.EnrollmentDetails;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentRegistrationsPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;

    private JTable enrollmentsTable;
    private DefaultTableModel tableModel;
    private List<EnrollmentDetails> enrollmentList = new ArrayList<>();

    private JLabel dropDeadlineLabel;

    public StudentRegistrationsPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Term", "Course Code", "Title", "Instructor", "Day/Time", "Room", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        enrollmentsTable = new JTable(tableModel);
        enrollmentsTable.getTableHeader().setReorderingAllowed(false);
        enrollmentsTable.getTableHeader().setResizingAllowed(false);

        // --- CENTER ALIGNMENT FIX ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        enrollmentsTable.setDefaultRenderer(Object.class, centerRenderer);
        // ----------------------------

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        dropDeadlineLabel = new JLabel("Loading deadline...");
        dropDeadlineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        dropDeadlineLabel.setForeground(Color.GRAY);
        bottomPanel.add(dropDeadlineLabel, BorderLayout.WEST);

        JButton dropButton = new JButton("Drop Selected Section");
        bottomPanel.add(dropButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        dropButton.addActionListener(e -> onDropSection());

        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                loadEnrollments();
                loadDeadline();
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        loadEnrollments();
        loadDeadline();
    }

    private void loadDeadline() {
        try {
            LocalDate deadline = studentService.getDropDeadline();
            dropDeadlineLabel.setText("Last day to drop: " + deadline);
        } catch (Exception e) {
            dropDeadlineLabel.setText("Could not load drop deadline.");
        }
    }

    public void loadEnrollments() {
        try {
            enrollmentList = studentService.getEnrollmentsForStudent(session.getUserId());

            Collections.sort(enrollmentList, (e1, e2) -> {
                if (e1.getYear() != e2.getYear()) {
                    return Integer.compare(e2.getYear(), e1.getYear());
                }
                return Integer.compare(getSemWeight(e2.getSemester()), getSemWeight(e1.getSemester()));
            });

            tableModel.setRowCount(0);
            for (EnrollmentDetails details : enrollmentList) {
                tableModel.addRow(new Object[]{
                        details.getTerm(),
                        details.getCourseCode(),
                        details.getCourseTitle(),
                        details.getInstructorName(),
                        details.getDayTime(),
                        details.getRoom(),
                        details.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading enrollments: " + e.getMessage());
        }
    }

    private int getSemWeight(String sem) {
        if ("Monsoon".equalsIgnoreCase(sem)) return 3;
        if ("Summer".equalsIgnoreCase(sem)) return 2;
        if ("Winter".equalsIgnoreCase(sem)) return 1;
        return 0;
    }

    private void onDropSection() {
        int selectedRow = enrollmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to drop.", "No Section Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EnrollmentDetails selectedEnrollment = enrollmentList.get(selectedRow);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop " + selectedEnrollment.getCourseCode() + "?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                studentService.dropSection(selectedEnrollment.getEnrollmentId());
                JOptionPane.showMessageDialog(this, "Section dropped successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEnrollments();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}