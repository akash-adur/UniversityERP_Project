package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private List<Section> sectionList = new ArrayList<>();
    private List<Instructor> instructorList = new ArrayList<>();

    public AdminSectionsPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Assign Instructors to Sections"));

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

        // --- UPDATED COLUMNS: Split "Day/Time" to "Days", "Time" ---
        String[] columnNames = {"Section ID", "Course Code", "Sec", "Days", "Time", "Current Instructor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(tableModel);

        // Center Align
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        sectionsTable.setDefaultRenderer(Object.class, centerRenderer);

        // Set Width for "Sec" column
        sectionsTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        sectionsTable.getColumnModel().getColumn(2).setMaxWidth(60);

        sorter = new TableRowSorter<>(tableModel);
        sectionsTable.setRowSorter(sorter);
        sectionsTable.getTableHeader().setReorderingAllowed(false);
        sectionsTable.getTableHeader().setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assignPanel.add(new JLabel("Assign to:"));
        assignPanel.add(instructorDropdown);
        JButton assignButton = new JButton("Assign Instructor");
        assignButton.addActionListener(e -> onAssignInstructor());
        assignPanel.add(assignButton);
        add(assignPanel, BorderLayout.SOUTH);

        loadInstructors();
        loadSections();
    }

    private void filter() {
        String text = searchField.getText();
        if (text.trim().length() == 0) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }

    public void loadSections() {
        try {
            sectionList = adminService.getAllSections();
            tableModel.setRowCount(0);
            for (Section s : sectionList) {
                String dayTime = s.getDayTime();
                String days = dayTime;
                String time = "";
                if (dayTime != null && dayTime.contains(" ")) {
                    String[] parts = dayTime.split(" ", 2);
                    days = parts[0];
                    time = parts[1];
                }

                tableModel.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getSectionName(),
                        days, // Column 3
                        time, // Column 4
                        s.getInstructorName()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadInstructors() {
        try {
            instructorList = adminService.getAllInstructors();
            instructorDropdown.removeAllItems();
            for (Instructor i : instructorList) instructorDropdown.addItem(i);
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void onAssignInstructor() {
        int viewRow = sectionsTable.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        int modelRow = sectionsTable.convertRowIndexToModel(viewRow);
        Section selectedSection = sectionList.get(modelRow);
        Instructor selectedInstructor = (Instructor) instructorDropdown.getSelectedItem();
        if (selectedInstructor == null) return;

        try {
            adminService.assignInstructor(selectedSection.getSectionId(), selectedInstructor.getUserId());
            JOptionPane.showMessageDialog(this, "Assigned!");
            loadSections();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
}