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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminSectionsPanel extends JPanel {

    private final AdminService adminService;
    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private List<Section> sectionList = new ArrayList<>();

    // Cached list of instructors for the edit dialog
    private List<Instructor> instructorList = new ArrayList<>();

    public AdminSectionsPanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Manage Sections"));

        // --- Top: Search ---
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

        // --- Center: Table ---
        String[] columnNames = {"Section ID", "Course Code", "Sec", "Days", "Time", "Room", "Current Instructor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(tableModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        sectionsTable.setDefaultRenderer(Object.class, centerRenderer);

        // Prevent column reordering
        sectionsTable.getTableHeader().setReorderingAllowed(false);

        sectionsTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        sectionsTable.getColumnModel().getColumn(2).setMaxWidth(60);

        sorter = new TableRowSorter<>(tableModel);
        sectionsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- South: Edit Button ---
        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit Selected Section");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editButton.addActionListener(e -> onEditSection());
        editPanel.add(editButton);
        add(editPanel, BorderLayout.SOUTH);

        loadSections();
        // Pre-load instructors for the dialog
        try {
            instructorList = adminService.getAllInstructors();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JSpinner createTimeSpinner() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor de = new JSpinner.DateEditor(s, "HH:mm");
        s.setEditor(de);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        s.setValue(cal.getTime());
        return s;
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
                        days,
                        time,
                        s.getRoom(),
                        s.getInstructorName()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void onEditSection() {
        int viewRow = sectionsTable.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Select a section to edit."); return; }
        int modelRow = sectionsTable.convertRowIndexToModel(viewRow);
        Section section = sectionList.get(modelRow);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Section: " + section.getCourseCode(), true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Days
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Days:"), gbc);
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JComboBox<String> day1Combo = new JComboBox<>(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
        JComboBox<String> day2Combo = new JComboBox<>(new String[]{"None", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
        dayPanel.add(day1Combo);
        dayPanel.add(new JLabel(" & "));
        dayPanel.add(day2Combo);
        gbc.gridx = 1; dialog.add(dayPanel, gbc);

        // Time
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Time:"), gbc);
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JSpinner startTimeSpinner = createTimeSpinner();
        JSpinner endTimeSpinner = createTimeSpinner();
        timePanel.add(startTimeSpinner);
        timePanel.add(new JLabel(" to "));
        timePanel.add(endTimeSpinner);
        gbc.gridx = 1; dialog.add(timePanel, gbc);

        // Pre-fill Logic
        try {
            String current = section.getDayTime();
            if (current != null && current.contains(" ")) {
                String[] mainParts = current.split(" ");
                if (mainParts.length > 0) {
                    String[] days = mainParts[0].split("/");
                    if (days.length > 0) day1Combo.setSelectedItem(days[0]);
                    if (days.length > 1) day2Combo.setSelectedItem(days[1]);
                    else day2Combo.setSelectedItem("None");
                }
                if (mainParts.length > 1 && mainParts[1].contains("-")) {
                    String[] times = mainParts[1].split("-");
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    if (times.length > 0) startTimeSpinner.setValue(sdf.parse(times[0]));
                    if (times.length > 1) endTimeSpinner.setValue(sdf.parse(times[1]));
                }
            }
        } catch (Exception e) {}

        // Room
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        JTextField roomField = new JTextField(section.getRoom(), 15);
        dialog.add(roomField, gbc);

        // Capacity
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(section.getCapacity(), 1, 500, 1));
        dialog.add(capacitySpinner, gbc);

        // Instructor
        gbc.gridx = 0; gbc.gridy = 4; dialog.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1;
        JComboBox<Instructor> instDropdown = new JComboBox<>();
        Instructor currentInstObj = null;
        for (Instructor i : instructorList) {
            instDropdown.addItem(i);
            if (i.getName().equalsIgnoreCase(section.getInstructorName())) currentInstObj = i;
        }
        if (currentInstObj != null) instDropdown.setSelectedItem(currentInstObj);
        else if (section.getInstructorName().equalsIgnoreCase("TBD")) instDropdown.setSelectedIndex(-1);
        dialog.add(instDropdown, gbc);

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Delete Button
        JButton deleteBtn = new JButton("Delete Section");
        deleteBtn.setBackground(new Color(200, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete this section?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    adminService.deleteSection(section.getSectionId());
                    JOptionPane.showMessageDialog(dialog, "Section deleted successfully.");
                    dialog.dispose();
                    loadSections();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(deleteBtn);

        // Save Button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(0, 100, 0));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                String d1 = (String) day1Combo.getSelectedItem();
                String d2 = (String) day2Combo.getSelectedItem();
                String days = d1;
                if (!"None".equals(d2) && !d2.equals(d1)) days += "/" + d2;

                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");
                String start = timeFmt.format((Date) startTimeSpinner.getValue());
                String end = timeFmt.format((Date) endTimeSpinner.getValue());
                String newDayTime = days + " " + start + "-" + end;

                String newRoom = roomField.getText().trim();
                int newCap = (Integer) capacitySpinner.getValue();
                Instructor selectedInst = (Instructor) instDropdown.getSelectedItem();
                int instId = (selectedInst != null) ? selectedInst.getUserId() : 0;

                if (newRoom.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Room cannot be empty."); return;
                }

                String currentName = section.getInstructorName();
                String newName = (selectedInst != null) ? selectedInst.getName() : "TBD";

                if (!currentName.equalsIgnoreCase("TBD") && !currentName.equalsIgnoreCase(newName)) {
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                            "Instructor '" + currentName + "' is assigned. Overwrite with '" + newName + "'?",
                            "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                adminService.updateSection(section.getSectionId(), newDayTime, newRoom, newCap, instId);
                JOptionPane.showMessageDialog(dialog, "Section updated successfully!");
                dialog.dispose();
                loadSections();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving: " + ex.getMessage());
            }
        });
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }
}