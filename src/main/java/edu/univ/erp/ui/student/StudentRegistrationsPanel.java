package edu.univ.erp.ui.student;

import edu.univ.erp.domain.EnrollmentDetails;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StudentRegistrationsPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private JTable enrollmentsTable;
    private DefaultTableModel tableModel;
    private JLabel dropDeadlineLabel; // RESTORED: Class level variable

    public StudentRegistrationsPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"Course Code", "Title", "Sec", "Instructor", "Days", "Time", "Room", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        enrollmentsTable = new JTable(tableModel);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        enrollmentsTable.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer)enrollmentsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // --- ADDED: Disable column reordering ---
        enrollmentsTable.getTableHeader().setReorderingAllowed(false);

        enrollmentsTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        enrollmentsTable.getColumnModel().getColumn(2).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(enrollmentsTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton dropButton = new JButton("Drop Selected Section");
        dropButton.addActionListener(e -> dropAction());

        JPanel bottomPanel = new JPanel(new BorderLayout());

        // RESTORED: Dynamic Label initialization
        dropDeadlineLabel = new JLabel("Loading deadline...");
        dropDeadlineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        dropDeadlineLabel.setForeground(Color.GRAY);

        bottomPanel.add(dropDeadlineLabel, BorderLayout.WEST);
        bottomPanel.add(dropButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Auto-refresh when tab is opened
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                loadRegistrations();
                loadDeadline(); // RESTORED: Fetch deadline on tab open
            }
            public void ancestorRemoved(AncestorEvent event) {}
            public void ancestorMoved(AncestorEvent event) {}
        });

        loadRegistrations();
        loadDeadline(); // Initial load
    }

    // RESTORED: Logic to fetch deadline from DB
    private void loadDeadline() {
        try {
            LocalDate deadline = studentService.getDropDeadline();
            dropDeadlineLabel.setText("Last day to drop: " + deadline);
        } catch (Exception e) {
            dropDeadlineLabel.setText("Could not load drop deadline.");
        }
    }

    private void loadRegistrations() {
        try {
            List<EnrollmentDetails> list = studentService.getEnrollmentsForStudent(session.getUserId());
            tableModel.setRowCount(0);
            for (EnrollmentDetails e : list) {
                String dayTime = e.getDayTime();
                String days = dayTime;
                String time = "";
                if (dayTime != null && dayTime.contains(" ")) {
                    String[] parts = dayTime.split(" ", 2);
                    days = parts[0];
                    time = parts[1];
                }

                tableModel.addRow(new Object[]{
                        e.getCourseCode(), e.getCourseTitle(), e.getSectionName(),
                        e.getInstructorName(), days, time, e.getRoom(), e.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void dropAction() {
        int row = enrollmentsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a section."); return; }

        try {
            List<EnrollmentDetails> list = studentService.getEnrollmentsForStudent(session.getUserId());
            int enrollmentId = list.get(row).getEnrollmentId();
            studentService.dropSection(enrollmentId);
            JOptionPane.showMessageDialog(this, "Dropped!");
            loadRegistrations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}