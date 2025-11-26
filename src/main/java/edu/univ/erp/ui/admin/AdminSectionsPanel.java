package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminSectionsPanel extends JPanel {

    private final AdminService adminService;
    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private JComboBox<Instructor> instructorDropdown = new JComboBox<>();

    // Search Components
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    private List<Section> sectionList = new ArrayList<>();
    private List<Instructor> instructorList = new ArrayList<>();

    public AdminSectionsPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Assign Instructors to Sections"));

        // --- Top Panel: Search ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("Search Course:"));
        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        // --- Table Setup ---
        String[] columnNames = {"Section ID", "Course Code", "Day/Time", "Current Instructor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(tableModel);

        // Sorter & Lockdown
        sorter = new TableRowSorter<>(tableModel);
        sectionsTable.setRowSorter(sorter);
        sectionsTable.getTableHeader().setReorderingAllowed(false);
        sectionsTable.getTableHeader().setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Assignment Panel ---
        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assignPanel.add(new JLabel("Assign to:"));
        assignPanel.add(instructorDropdown);
        JButton assignButton = new JButton("Assign Instructor");
        assignPanel.add(assignButton);
        add(assignPanel, BorderLayout.SOUTH);

        loadInstructors();
        loadSections();

        assignButton.addActionListener(e -> onAssignInstructor());
    }

    private void filter() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public void loadSections() {
        try {
            sectionList = adminService.getAllSections();
            tableModel.setRowCount(0);
            for (Section s : sectionList) {
                tableModel.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getDayTime(),
                        s.getInstructorName()
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
                instructorDropdown.addItem(i);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading instructors: " + e.getMessage());
        }
    }

    private void onAssignInstructor() {
        int viewRow = sectionsTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row to model row (crucial for sorting)
        int modelRow = sectionsTable.convertRowIndexToModel(viewRow);
        Section selectedSection = sectionList.get(modelRow);

        Instructor selectedInstructor = (Instructor) instructorDropdown.getSelectedItem();
        if (selectedInstructor == null) return;

        // --- RESTORED WARNING LOGIC ---
        String currentInstructor = selectedSection.getInstructorName();
        if (currentInstructor != null && !currentInstructor.isEmpty() && !"TBD".equals(currentInstructor)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This section is already assigned to " + currentInstructor + ".\nDo you want to overwrite this assignment?",
                    "Confirm Reassignment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return; // User cancelled
            }
        }
        // ------------------------------

        try {
            adminService.assignInstructor(selectedSection.getSectionId(), selectedInstructor.getUserId());
            JOptionPane.showMessageDialog(this, "Instructor assigned!");
            loadSections();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error assigning instructor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}