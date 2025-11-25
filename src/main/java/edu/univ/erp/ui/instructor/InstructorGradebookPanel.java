package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.GradeRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InstructorGradebookPanel extends JPanel {

    private final UserSession session;
    private final InstructorService instructorService;
    private JComboBox<Section> sectionDropdown;
    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;

    // To hold the currently loaded list
    private List<GradeRecord> currentRecords;

    public InstructorGradebookPanel(UserSession session) {
        this.session = session;
        this.instructorService = new InstructorService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top: Section Selector ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Section:"));
        sectionDropdown = new JComboBox<>();
        loadSections(); // Populate dropdown
        topPanel.add(sectionDropdown);
        JButton loadButton = new JButton("Load Gradebook");
        loadButton.addActionListener(this::loadGradebook);
        topPanel.add(loadButton);
        add(topPanel, BorderLayout.NORTH);

        // --- Center: Table ---
        // Columns: ID, Roll No, Name, Quiz(20%), Midterm(30%), Final(50%), Total Grade
        String[] columns = {"Enroll ID", "Roll No", "Name", "Quiz", "Midterm", "Final Exam", "Letter Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing the score columns (3, 4, 5)
                return column >= 3 && column <= 5;
            }
        };

        gradeTable = new JTable(tableModel);
        add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        // --- Bottom: Stats & Save ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statsLabel = new JLabel("Class Average: N/A");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        JButton saveButton = new JButton("Save Grades & Compute Stats");
        saveButton.addActionListener(this::saveGrades);
        bottomPanel.add(saveButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadSections() {
        try {
            List<Section> sections = instructorService.getSectionsForInstructor(session.getUserId());
            for (Section s : sections) {
                // toString needed on Section or we wrap it. Assuming Section has a decent toString or we adjust:
                // For simplicity here, we add the object. You might need to override toString() in Section.java
                // to show "CS101" instead of hashcode.
                sectionDropdown.addItem(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGradebook(ActionEvent e) {
        Section selected = (Section) sectionDropdown.getSelectedItem();
        if (selected == null) return;

        try {
            currentRecords = instructorService.getGradebook(selected.getSectionId());
            tableModel.setRowCount(0);
            for (GradeRecord r : currentRecords) {
                tableModel.addRow(new Object[]{
                        r.enrollmentId, r.rollNo, r.name, r.quiz, r.midterm, r.finals, r.letterGrade
                });
            }
            calculateStats();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage());
        }
    }

    private void saveGrades(ActionEvent e) {
        if (gradeTable.isEditing()) gradeTable.getCellEditor().stopCellEditing();

        try {
            double totalSum = 0;
            int count = tableModel.getRowCount();

            for (int i = 0; i < count; i++) {
                int enrollId = (Integer) tableModel.getValueAt(i, 0);

                // Parse scores (handle potential nulls or strings)
                double quiz = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                double mid = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
                double fin = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

                // Compute Final Grade (Simple Weighting)
                // Quiz 20%, Mid 30%, Final 50%
                double total = (quiz * 0.2) + (mid * 0.3) + (fin * 0.5);
                totalSum += total;

                String letter = total >= 90 ? "A" : total >= 80 ? "B" : total >= 70 ? "C" : total >= 60 ? "D" : "F";

                // Update DB
                instructorService.updateGrades(enrollId, quiz, mid, fin, letter);

                // Update UI letter grade column
                tableModel.setValueAt(letter, i, 6);
            }

            // Update Stats Label
            double avg = count > 0 ? totalSum / count : 0;
            statsLabel.setText(String.format("Class Average: %.2f%%", avg));

            JOptionPane.showMessageDialog(this, "Grades Saved & Computed!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage() + "\nCheck number formats.");
        }
    }

    private void calculateStats() {
        // Optional helper to calc stats on load without saving
    }
}
