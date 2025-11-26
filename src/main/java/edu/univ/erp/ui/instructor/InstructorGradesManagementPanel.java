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
    private JComboBox<StudentItem> studentDropdown;

    private JTextField quizField, midField, finalField;
    private JButton saveButton;

    // Config Fields
    private JTextField wQuiz, wMid, wFinal;
    private JTextField maxQuiz, maxMid, maxFinal;
    private JTextField cutoffA, cutoffB, cutoffC;

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

        // --- 1. Top Panel: Improved Searchable Section ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBorder(BorderFactory.createTitledBorder("Step 1: Choose Section"));

        topPanel.add(new JLabel("Search/Select Course:"));

        // WIDER DROPDOWN + EDITABLE FOR SEARCH
        sectionDropdown = new JComboBox<>();
        sectionDropdown.setPreferredSize(new Dimension(400, 35));
        sectionDropdown.setEditable(true);

        // Autocomplete Logic
        JTextField editor = (JTextField) sectionDropdown.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignore navigation keys to allow user to select from dropdown
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) return;

                SwingUtilities.invokeLater(() -> filterSections(editor.getText()));
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

        // Weights
        gbc.gridx=0; gbc.gridy=0; configPanel.add(new JLabel("Weights (%):"), gbc);
        gbc.gridx=1; configPanel.add(new JLabel("Quiz"), gbc);
        gbc.gridx=2; wQuiz = new JTextField("20", 3); configPanel.add(wQuiz, gbc);
        gbc.gridx=3; configPanel.add(new JLabel("Mid"), gbc);
        gbc.gridx=4; wMid = new JTextField("30", 3); configPanel.add(wMid, gbc);
        gbc.gridx=5; configPanel.add(new JLabel("Final"), gbc);
        gbc.gridx=6; wFinal = new JTextField("50", 3); configPanel.add(wFinal, gbc);

        // Max Scores
        gbc.gridx=0; gbc.gridy=1; configPanel.add(new JLabel("Max Scores:"), gbc);
        gbc.gridx=2; maxQuiz = new JTextField("20", 3); configPanel.add(maxQuiz, gbc);
        gbc.gridx=4; maxMid = new JTextField("50", 3); configPanel.add(maxMid, gbc);
        gbc.gridx=6; maxFinal = new JTextField("100", 3); configPanel.add(maxFinal, gbc);

        // Cutoffs
        gbc.gridx=0; gbc.gridy=2; configPanel.add(new JLabel("Min % for:"), gbc);
        gbc.gridx=1; configPanel.add(new JLabel("A"), gbc);
        gbc.gridx=2; cutoffA = new JTextField("90", 3); configPanel.add(cutoffA, gbc);
        gbc.gridx=3; configPanel.add(new JLabel("B"), gbc);
        gbc.gridx=4; cutoffB = new JTextField("80", 3); configPanel.add(cutoffB, gbc);
        gbc.gridx=5; configPanel.add(new JLabel("C"), gbc);
        gbc.gridx=6; cutoffC = new JTextField("70", 3); configPanel.add(cutoffC, gbc);

        mainContent.add(configPanel);

        // --- 3. Grading Panel ---
        JPanel gradePanel = new JPanel(new GridBagLayout());
        gradePanel.setBorder(BorderFactory.createTitledBorder("Step 3: Enter Grades"));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        gradePanel.add(new JLabel("Select Student:", SwingConstants.CENTER), gbc);
        studentDropdown = new JComboBox<>();
        studentDropdown.setPreferredSize(new Dimension(250, 30));
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

    private void loadSections() {
        try {
            allSections = instructorService.getSectionsForInstructor(session.getUserId());
            updateDropdown(allSections);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- AUTO-COMPLETE FILTER LOGIC ---
    private void filterSections(String input) {
        isAdjusting = true; // Prevent firing ActionEvents while we modify the list
        try {
            List<Section> filtered = new ArrayList<>();
            if (allSections != null) {
                for (Section s : allSections) {
                    if (s.toString().toLowerCase().contains(input.toLowerCase())) {
                        filtered.add(s);
                    }
                }
            }

            updateDropdown(filtered);

            // Restore text because updating dropdown clears it
            JTextField editor = (JTextField) sectionDropdown.getEditor().getEditorComponent();
            editor.setText(input);

            // Show popup if we have matches and user has typed something
            if (!filtered.isEmpty() && !input.isEmpty()) {
                sectionDropdown.showPopup();
            } else {
                sectionDropdown.hidePopup();
            }
        } finally {
            isAdjusting = false;
        }
    }

    private void updateDropdown(List<Section> items) {
        sectionDropdown.removeAllItems();
        for (Section s : items) {
            sectionDropdown.addItem(s);
        }
    }

    private void onSectionSelected(ActionEvent e) {
        if (isAdjusting) return; // Ignore events during filtering

        Object item = sectionDropdown.getSelectedItem();
        if (item instanceof Section) {
            Section selected = (Section) item;
            try {
                currentRecords = instructorService.getGradebook(selected.getSectionId());
                studentDropdown.removeAllItems();
                for (GradeRecord r : currentRecords) studentDropdown.addItem(new StudentItem(r));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
            }
        }
    }

    private void onStudentSelected(ActionEvent e) {
        StudentItem selectedItem = (StudentItem) studentDropdown.getSelectedItem();
        if (selectedItem == null) {
            quizField.setText(""); midField.setText(""); finalField.setText("");
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
            double cB = Double.parseDouble(cutoffB.getText());
            double cC = Double.parseDouble(cutoffC.getText());

            String letter = totalPct >= cA ? "A" : totalPct >= cB ? "B" : totalPct >= cC ? "C" : totalPct >= 60 ? "D" : "F";

            GradeRecord r = selectedItem.record;
            instructorService.updateGrades(r.enrollmentId, quiz, mid, fin, letter);

            r.quiz = quiz; r.midterm = mid; r.finals = fin; r.letterGrade = letter;

            JOptionPane.showMessageDialog(this, "Grade Updated! Total: " + String.format("%.2f", totalPct) + "% (" + letter + ")");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please check all numbers (weights, scores).", "Format Error", JOptionPane.ERROR_MESSAGE);
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