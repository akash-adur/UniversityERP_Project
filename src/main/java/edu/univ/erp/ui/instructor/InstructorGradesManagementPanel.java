package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.GradeRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InstructorGradesManagementPanel extends JPanel {

    private final UserSession session;
    private final InstructorService instructorService;

    // UI Components
    private JComboBox<Section> sectionDropdown;
    private JComboBox<StudentItem> studentDropdown; // Custom wrapper for dropdown display

    private JTextField quizField;
    private JTextField midField;
    private JTextField finalField;
    private JButton saveButton;

    // Data Cache
    private List<GradeRecord> currentRecords;

    public InstructorGradesManagementPanel(UserSession session) {
        this.session = session;
        this.instructorService = new InstructorService();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- 1. Top Panel: Section Selection ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBorder(BorderFactory.createTitledBorder("Step 1: Choose Section"));

        sectionDropdown = new JComboBox<>();
        sectionDropdown.setPreferredSize(new Dimension(300, 30));
        sectionDropdown.addActionListener(this::onSectionSelected);

        topPanel.add(new JLabel("Course / Section:"));
        topPanel.add(sectionDropdown);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Panel: Student & Grading Form ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Step 2: Grade Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Student Dropdown
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Select Student:", SwingConstants.CENTER), gbc);

        studentDropdown = new JComboBox<>();
        studentDropdown.setPreferredSize(new Dimension(250, 30));
        studentDropdown.addActionListener(this::onStudentSelected);
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(studentDropdown, gbc);

        // Row 1: Quiz Input
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Quiz Score (20%):", SwingConstants.CENTER), gbc);

        quizField = createCenteredField();
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(quizField, gbc);

        // Row 2: Midterm Input
        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("Midterm Score (30%):", SwingConstants.CENTER), gbc);

        midField = createCenteredField();
        gbc.gridx = 1; gbc.gridy = 2;
        centerPanel.add(midField, gbc);

        // Row 3: Final Input
        gbc.gridx = 0; gbc.gridy = 3;
        centerPanel.add(new JLabel("End Sem Score (50%):", SwingConstants.CENTER), gbc);

        finalField = createCenteredField();
        gbc.gridx = 1; gbc.gridy = 3;
        centerPanel.add(finalField, gbc);

        // Row 4: Save Button
        saveButton = new JButton("Update Grade");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 120, 215));
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(this::saveStudentGrade);

        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(saveButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Load Initial Data
        loadSections();
    }

    private JTextField createCenteredField() {
        JTextField field = new JTextField();
        field.setHorizontalAlignment(JTextField.CENTER); // Center text inside
        field.setPreferredSize(new Dimension(100, 30));
        return field;
    }

    private void loadSections() {
        try {
            java.util.List<Section> sections = instructorService.getSectionsForInstructor(session.getUserId());
            sectionDropdown.removeAllItems();
            for (Section s : sections) {
                sectionDropdown.addItem(s);
            }
            // Trigger load for the first item
            if (sectionDropdown.getItemCount() > 0) {
                sectionDropdown.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSectionSelected(ActionEvent e) {
        Section selected = (Section) sectionDropdown.getSelectedItem();
        if (selected == null) return;

        try {
            currentRecords = instructorService.getGradebook(selected.getSectionId());
            studentDropdown.removeAllItems();

            for (GradeRecord r : currentRecords) {
                // Add wrapper object to dropdown
                studentDropdown.addItem(new StudentItem(r));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    private void onStudentSelected(ActionEvent e) {
        StudentItem selectedItem = (StudentItem) studentDropdown.getSelectedItem();
        if (selectedItem == null) {
            // Clear fields if nothing selected
            quizField.setText("");
            midField.setText("");
            finalField.setText("");
            return;
        }

        GradeRecord r = selectedItem.record;
        quizField.setText(String.valueOf(r.quiz));
        midField.setText(String.valueOf(r.midterm));
        finalField.setText(String.valueOf(r.finals));
    }

    private void saveStudentGrade(ActionEvent e) {
        StudentItem selectedItem = (StudentItem) studentDropdown.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "No student selected.");
            return;
        }

        try {
            GradeRecord r = selectedItem.record;

            // 1. Read Inputs
            double quiz = Double.parseDouble(quizField.getText());
            double mid = Double.parseDouble(midField.getText());
            double fin = Double.parseDouble(finalField.getText());

            // 2. Calculate Grade
            double total = (quiz * 0.2) + (mid * 0.3) + (fin * 0.5);
            String letter = total >= 90 ? "A" : total >= 80 ? "B" : total >= 70 ? "C" : total >= 60 ? "D" : "F";

            // 3. Update Database
            instructorService.updateGrades(r.enrollmentId, quiz, mid, fin, letter);

            // 4. Update Local Cache (so it stays correct if we switch students and come back)
            r.quiz = quiz;
            r.midterm = mid;
            r.finals = fin;
            r.letterGrade = letter;

            JOptionPane.showMessageDialog(this, "Grade Updated for " + r.name);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Please enter numbers only.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }

    /**
     * Helper class to display "Name (RollNo)" nicely in the dropdown
     */
    private static class StudentItem {
        GradeRecord record;

        public StudentItem(GradeRecord record) {
            this.record = record;
        }

        @Override
        public String toString() {
            return record.name + " (" + record.rollNo + ")";
        }
    }
}