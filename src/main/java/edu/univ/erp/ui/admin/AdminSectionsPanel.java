package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminSectionsPanel extends JPanel {

    private final AdminService adminService;
    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private JComboBox<Instructor> instructorDropdown = new JComboBox<>();

    private List<Section> sectionList = new ArrayList<>();
    private List<Instructor> instructorList = new ArrayList<>();

    public AdminSectionsPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Assign Instructors to Sections"));

        // --- Table Setup ---
        String[] columnNames = {"Section ID", "Course Code", "Day/Time", "Current Instructor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Assignment Panel (Bottom) ---
        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assignPanel.add(new JLabel("Assign to:"));
        assignPanel.add(instructorDropdown);
        JButton assignButton = new JButton("Assign Instructor");
        assignPanel.add(assignButton);
        add(assignPanel, BorderLayout.SOUTH);

        // --- Load Data ---
        loadInstructors();
        loadSections();

        // --- Action Listener ---
        assignButton.addActionListener(e -> onAssignInstructor());
    }

    public void loadSections() {
        try {
            sectionList = adminService.getAllSections();
            tableModel.setRowCount(0);
            for (Section s : sectionList) {
                tableModel.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(), // Now this works
                        s.getDayTime(),
                        s.getInstructorName() // Now this works
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + e.getMessage());
        }
    }

    private void loadInstructors() {
        try {
            instructorList = adminService.getAllInstructors();
            instructorDropdown.removeAllItems();
            for (Instructor i : instructorList) {
                instructorDropdown.addItem(i); // Relies on Instructor.toString()
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading instructors: " + e.getMessage());
        }
    }

    private void onAssignInstructor() {
        int selectedRow = sectionsTable.getSelectedRow();
        Instructor selectedInstructor = (Instructor) instructorDropdown.getSelectedItem();

        if (selectedRow == -1 || selectedInstructor == null) {
            JOptionPane.showMessageDialog(this, "Please select a section AND an instructor.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Section selectedSection = sectionList.get(selectedRow);

        try {
            adminService.assignInstructor(selectedSection.getSectionId(), selectedInstructor.getUserId());
            JOptionPane.showMessageDialog(this, "Instructor assigned!");
            loadSections(); // Refresh table
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error assigning instructor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}