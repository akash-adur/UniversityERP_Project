package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.GradeRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class InstructorGradesManagementPanel extends JPanel {

    private final UserSession session;
    private final InstructorService instructorService;

    // UI Components
    private JComboBox<Section> sectionDropdown;
    private JComboBox<StudentItem> studentDropdown; // Now Searchable

    private JTextField quizField, midField, finalField;
    private JButton saveButton;

    // Config Fields
    private JTextField wQuiz, wMid, wFinal;
    private JTextField maxQuiz, maxMid, maxFinal;

    private JTextField cutoffA, cutoffAMinus;
    private JTextField cutoffB, cutoffBMinus;
    private JTextField cutoffC, cutoffCMinus;
    private JTextField cutoffD;

    private List<GradeRecord> currentRecords;
    private List<Section> allSections;
    private boolean isAdjusting = false; // Guard against recursive events

    public InstructorGradesManagementPanel(UserSession session) {
        this.session = session;
        this.instructorService = new InstructorService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));

        // --- 1. Top Panel: Searchable Section ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBorder(BorderFactory.createTitledBorder("Step 1: Choose Section"));

        topPanel.add(new JLabel("Search/Select Course:"));

        sectionDropdown = new JComboBox<>();
        sectionDropdown.setPreferredSize(new Dimension(400, 35));
        sectionDropdown.setEditable(true);

        JTextField sectionEditor = (JTextField) sectionDropdown.getEditor().getEditorComponent();
        sectionEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (isNavigationKey(e)) return;
                SwingUtilities.invokeLater(() -> filterSections(sectionEditor.getText()));
            }
        });

        sectionDropdown.addActionListener(this::onSectionSelected);
        topPanel.add(sectionDropdown);
        mainContent.add(topPanel);

        // --- 2. Config Panel ---
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Step 2: Configure Grading Scheme"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // -- Row 0: Weights --
        gbc.gridx=0; gbc.gridy=0; configPanel.add(new JLabel("Weights (%):"), gbc);
        gbc.gridx=1; configPanel.add(new JLabel("Quiz"), gbc);
        gbc.gridx=2; wQuiz = new JTextField("20", 3); configPanel.add(wQuiz, gbc);
        gbc.gridx=3; configPanel.add(new JLabel("Mid"), gbc);
        gbc.gridx=4; wMid = new JTextField("30", 3); configPanel.add(wMid, gbc);
        gbc.gridx=5; configPanel.add(new JLabel("Final"), gbc);
        gbc.gridx=6; wFinal = new JTextField("50", 3); configPanel.add(wFinal, gbc);

        // -- Row 1: Max Scores --
        gbc.gridx=0; gbc.gridy=1; configPanel.add(new JLabel("Max Scores:"), gbc);
        gbc.gridx=2; maxQuiz = new JTextField("20", 3); configPanel.add(maxQuiz, gbc);
        gbc.gridx=4; maxMid = new JTextField("50", 3); configPanel.add(maxMid, gbc);
        gbc.gridx=6; maxFinal = new JTextField("100", 3); configPanel.add(maxFinal, gbc);

        // -- Row 2: Cutoffs (A to B-) --
        gbc.gridx=0; gbc.gridy=2; configPanel.add(new JLabel("Min % for:"), gbc);
        gbc.gridx=1; configPanel.add(new JLabel("A"), gbc);
        gbc.gridx=2; cutoffA = new JTextField("90", 3); configPanel.add(cutoffA, gbc);
        gbc.gridx=3; configPanel.add(new JLabel("A-"), gbc);
        gbc.gridx=4; cutoffAMinus = new JTextField("85", 3); configPanel.add(cutoffAMinus, gbc);
        gbc.gridx=5; configPanel.add(new JLabel("B"), gbc);
        gbc.gridx=6; cutoffB = new JTextField("80", 3); configPanel.add(cutoffB, gbc);
        gbc.gridx=7; configPanel.add(new JLabel("B-"), gbc);
        gbc.gridx=8; cutoffBMinus = new JTextField("75", 3); configPanel.add(cutoffBMinus, gbc);

        // -- Row 3: Cutoffs (C to D) --
        gbc.gridx=1; gbc.gridy=3; configPanel.add(new JLabel("C"), gbc);
        gbc.gridx=2; cutoffC = new JTextField("70", 3); configPanel.add(cutoffC, gbc);
        gbc.gridx=3; configPanel.add(new JLabel("C-"), gbc);
        gbc.gridx=4; cutoffCMinus = new JTextField("65", 3); configPanel.add(cutoffCMinus, gbc);
        gbc.gridx=5; configPanel.add(new JLabel("D"), gbc);
        gbc.gridx=6; cutoffD = new JTextField("60", 3); configPanel.add(cutoffD, gbc);

        mainContent.add(configPanel);

        // --- 3. Grading Panel ---
        JPanel gradePanel = new JPanel(new GridBagLayout());
        gradePanel.setBorder(BorderFactory.createTitledBorder("Step 3: Enter Grades"));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        gradePanel.add(new JLabel("Search/Select Student:", SwingConstants.CENTER), gbc);

        // --- UPDATED: Searchable Student Dropdown ---
        studentDropdown = new JComboBox<>();
        studentDropdown.setPreferredSize(new Dimension(250, 30));
        studentDropdown.setEditable(true);

        JTextField studentEditor = (JTextField) studentDropdown.getEditor().getEditorComponent();
        studentEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (isNavigationKey(e)) return;
                SwingUtilities.invokeLater(() -> filterStudents(studentEditor.getText()));
            }
        });

        studentDropdown.addActionListener(this::onStudentSelected);
        gbc.gridx = 1; gradePanel.add(studentDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gradePanel.add(new JLabel("Quiz Score:", SwingConstants.CENTER), gbc);
        quizField = new JTextField(10); gbc.gridx = 1; gradePanel.add(quizField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gradePanel.add(new JLabel("Midterm Score:", SwingConstants.CENTER), gbc);
        midField = new JTextField(10); gbc.gridx = 1; gradePanel.add(midField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gradePanel.add(new JLabel("End Sem Score:", SwingConstants.CENTER), gbc);
        finalField = new JTextField(10); gbc.gridx = 1; gradePanel.add(finalField, gbc);

        saveButton = new JButton("Update Grade");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 120, 215));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(this::saveStudentGrade);

        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        gradePanel.add(saveButton, gbc);

        mainContent.add(gradePanel);

        add(new JScrollPane(mainContent), BorderLayout.CENTER);

        loadSections();
    }

    private boolean isNavigationKey(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_UP ||
                e.getKeyCode() == KeyEvent.VK_DOWN ||
                e.getKeyCode() == KeyEvent.VK_ENTER;
    }

    private void loadSections() {
        try {
            allSections = instructorService.getSectionsForInstructor(session.getUserId());
            updateDropdown(sectionDropdown, allSections);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- SECTION FILTER LOGIC ---
    private void filterSections(String input) {
        isAdjusting = true;
        try {
            List<Section> filtered = new ArrayList<>();
            if (allSections != null) {
                for (Section s : allSections) {
                    if (s.toString().toLowerCase().contains(input.toLowerCase())) {
                        filtered.add(s);
                    }
                }
            }
            sectionDropdown.removeAllItems();
            for(Section s : filtered) sectionDropdown.addItem(s);

            JTextField editor = (JTextField) sectionDropdown.getEditor().getEditorComponent();
            editor.setText(input);

            if (!filtered.isEmpty() && !input.isEmpty()) sectionDropdown.showPopup();
            else sectionDropdown.hidePopup();
        } finally {
            isAdjusting = false;
        }
    }

    // --- STUDENT FILTER LOGIC (NEW) ---
    private void filterStudents(String input) {
        isAdjusting = true;
        try {
            List<StudentItem> filtered = new ArrayList<>();
            if (currentRecords != null) {
                for (GradeRecord r : currentRecords) {
                    StudentItem item = new StudentItem(r);
                    // Check matches in Name OR Roll No
                    if (item.toString().toLowerCase().contains(input.toLowerCase())) {
                        filtered.add(item);
                    }
                }
            }

            studentDropdown.removeAllItems();
            for (StudentItem item : filtered) {
                studentDropdown.addItem(item);
            }

            JTextField editor = (JTextField) studentDropdown.getEditor().getEditorComponent();
            editor.setText(input);

            if (!filtered.isEmpty() && !input.isEmpty()) {
                studentDropdown.showPopup();
            } else {
                studentDropdown.hidePopup();
            }
        } finally {
            isAdjusting = false;
        }
    }

    private <T> void updateDropdown(JComboBox<T> box, List<T> items) {
        box.removeAllItems();
        for (T item : items) box.addItem(item);
    }

    private void onSectionSelected(ActionEvent e) {
        if (isAdjusting) return;

        Object item = sectionDropdown.getSelectedItem();
        if (item instanceof Section) {
            Section selected = (Section) item;
            try {
                currentRecords = instructorService.getGradebook(selected.getSectionId());
                // Reset student dropdown with full list
                studentDropdown.removeAllItems();
                JTextField editor = (JTextField) studentDropdown.getEditor().getEditorComponent();
                editor.setText("");

                for (GradeRecord r : currentRecords) studentDropdown.addItem(new StudentItem(r));

                // Clear fields
                quizField.setText(""); midField.setText(""); finalField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
            }
        }
    }

    private void onStudentSelected(ActionEvent e) {
        if (isAdjusting) return;

        // IMPORTANT: Check if the selected item is actually a StudentItem object.
        // If the user is typing "Akash", getSelectedItem() returns String "Akash".
        // We only want to update fields when they click/select a valid Object.
        Object item = studentDropdown.getSelectedItem();

        if (item == null || !(item instanceof StudentItem)) {
            // If it's just text (not a selection), don't clear fields immediately to allow typing
            // OR clear them if you prefer. For now, we do nothing.
            return;
        }

        StudentItem selectedItem = (StudentItem) item;
        GradeRecord r = selectedItem.record;
        quizField.setText(String.valueOf(r.quiz));
        midField.setText(String.valueOf(r.midterm));
        finalField.setText(String.valueOf(r.finals));
    }

    private void saveStudentGrade(ActionEvent e) {
        Object item = studentDropdown.getSelectedItem();
        if (item == null || !(item instanceof StudentItem)) {
            JOptionPane.showMessageDialog(this, "Please select a valid student from the list.");
            return;
        }
        StudentItem selectedItem = (StudentItem) item;

        try {
            double wQ = Double.parseDouble(wQuiz.getText());
            double wM = Double.parseDouble(wMid.getText());
            double wF = Double.parseDouble(wFinal.getText());

            if (Math.abs(wQ + wM + wF - 100.0) > 0.1) {
                JOptionPane.showMessageDialog(this, "Weights must add up to 100%.", "Config Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double mQ = Double.parseDouble(maxQuiz.getText());
            double mM = Double.parseDouble(maxMid.getText());
            double mF = Double.parseDouble(maxFinal.getText());

            double quiz = Double.parseDouble(quizField.getText());
            double mid = Double.parseDouble(midField.getText());
            double fin = Double.parseDouble(finalField.getText());

            if (quiz > mQ || mid > mM || fin > mF) {
                JOptionPane.showMessageDialog(this,
                        String.format("Scores cannot exceed Max Score (Q:%.0f, M:%.0f, F:%.0f)", mQ, mM, mF),
                        "Invalid Score", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double pctQ = (quiz / mQ) * wQ;
            double pctM = (mid / mM) * wM;
            double pctF = (fin / mF) * wF;
            double totalPct = pctQ + pctM + pctF;

            double cA = Double.parseDouble(cutoffA.getText());
            double cAM = Double.parseDouble(cutoffAMinus.getText());
            double cB = Double.parseDouble(cutoffB.getText());
            double cBM = Double.parseDouble(cutoffBMinus.getText());
            double cC = Double.parseDouble(cutoffC.getText());
            double cCM = Double.parseDouble(cutoffCMinus.getText());
            double cD = Double.parseDouble(cutoffD.getText());

            String letter;
            if (totalPct >= cA) letter = "A";
            else if (totalPct >= cAM) letter = "A-";
            else if (totalPct >= cB) letter = "B";
            else if (totalPct >= cBM) letter = "B-";
            else if (totalPct >= cC) letter = "C";
            else if (totalPct >= cCM) letter = "C-";
            else if (totalPct >= cD) letter = "D";
            else letter = "F";

            GradeRecord r = selectedItem.record;
            instructorService.updateGrades(r.enrollmentId, quiz, mid, fin, letter);

            r.quiz = quiz; r.midterm = mid; r.finals = fin; r.letterGrade = letter;

            JOptionPane.showMessageDialog(this, "Grade Updated! Total: " + String.format("%.2f", totalPct) + "% (" + letter + ")");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please check all numbers (weights, scores, cutoffs).", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }

    private static class StudentItem {
        GradeRecord record;
        public StudentItem(GradeRecord record) { this.record = record; }
        @Override public String toString() { return record.name + " (" + record.rollNo + ")"; }
    }
}