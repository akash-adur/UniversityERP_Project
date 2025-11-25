package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.GradeRecord;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
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

        // --- Top: Section Selector & CSV Controls ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Section Selection
        topPanel.add(new JLabel("Select Section:"));
        sectionDropdown = new JComboBox<>();
        loadSections(); // Populate dropdown
        topPanel.add(sectionDropdown);

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(this::loadGradebook);
        topPanel.add(loadButton);

        // Spacer
        topPanel.add(Box.createHorizontalStrut(20));

        // CSV Buttons
        JButton exportButton = new JButton("Download CSV");
        exportButton.addActionListener(this::exportToCSV);
        topPanel.add(exportButton);

        JButton importButton = new JButton("Upload CSV");
        importButton.addActionListener(this::importFromCSV);
        topPanel.add(importButton);

        add(topPanel, BorderLayout.NORTH);

        // --- Center: Table ---
        // Columns: ID, Roll No, Name, Quiz(20%), Midterm(30%), Final(50%), Total Grade
        String[] columns = {"Enroll ID", "Roll No", "Name", "Quiz", "Midterm", "Final Exam", "Letter Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-Only (User cannot double click to edit)
            }
        };

        gradeTable = new JTable(tableModel);

        // --- Center Alignment Logic ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        gradeTable.setDefaultRenderer(Object.class, centerRenderer);

        // Center Align Headers too
        ((DefaultTableCellRenderer)gradeTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        // -----------------------------

        add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        // --- Bottom: Stats & Save Button ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        statsLabel = new JLabel("Class Average: N/A");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        // --- NEW: Save Button to persist CSV changes ---
        JButton saveButton = new JButton("Save Uploaded Changes");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 120, 215)); // Blue
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(this::saveGrades); // Calls the save logic

        bottomPanel.add(saveButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadSections() {
        try {
            List<Section> sections = instructorService.getSectionsForInstructor(session.getUserId());
            sectionDropdown.removeAllItems();
            for (Section s : sections) {
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
            // Auto-calculate stats for display
            calculateStats();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage());
        }
    }

    /**
     * Iterates through the table (which may have been updated by CSV)
     * and saves all values to the database.
     */
    private void saveGrades(ActionEvent e) {
        try {
            int count = tableModel.getRowCount();
            if (count == 0) return;

            for (int i = 0; i < count; i++) {
                int enrollId = (Integer) tableModel.getValueAt(i, 0);

                // Parse scores from the table
                double quiz = parseScore(tableModel.getValueAt(i, 3));
                double mid = parseScore(tableModel.getValueAt(i, 4));
                double fin = parseScore(tableModel.getValueAt(i, 5));

                // Recalculate Logic (just to be safe)
                double total = (quiz * 0.2) + (mid * 0.3) + (fin * 0.5);
                String letter = total >= 90 ? "A" : total >= 80 ? "B" : total >= 70 ? "C" : total >= 60 ? "D" : "F";

                // Update Database
                instructorService.updateGrades(enrollId, quiz, mid, fin, letter);

                // Update UI Letter Grade (in case CSV didn't have it)
                tableModel.setValueAt(letter, i, 6);
            }

            // Update Stats Label
            calculateStats();

            JOptionPane.showMessageDialog(this, "Grades Saved Successfully to Database!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving grades: " + ex.getMessage());
        }
    }

    private void calculateStats() {
        try {
            double totalSum = 0;
            int count = tableModel.getRowCount();

            for (int i = 0; i < count; i++) {
                double quiz = parseScore(tableModel.getValueAt(i, 3));
                double mid = parseScore(tableModel.getValueAt(i, 4));
                double fin = parseScore(tableModel.getValueAt(i, 5));

                double total = (quiz * 0.2) + (mid * 0.3) + (fin * 0.5);
                totalSum += total;
            }

            double avg = count > 0 ? totalSum / count : 0;
            statsLabel.setText(String.format("Class Average: %.2f%%", avg));

        } catch (Exception ex) {
            statsLabel.setText("Error calculating stats");
        }
    }

    private double parseScore(Object obj) {
        if (obj == null) return 0.0;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // --- CSV EXPORT ---
    private void exportToCSV(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Gradebook CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("EnrollID,RollNo,Name,Quiz,Midterm,Final,Grade");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%s%n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4),
                            tableModel.getValueAt(i, 5),
                            tableModel.getValueAt(i, 6));
                }
                JOptionPane.showMessageDialog(this, "Export Successful!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    // --- CSV IMPORT (Matches by Enrollment ID) ---
    private void importFromCSV(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Gradebook CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;
                int updatedCount = 0;

                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }

                    String[] parts = line.split(",");
                    if (parts.length < 6) continue;

                    String csvEnrollIdStr = parts[0].trim();

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String tableEnrollIdStr = tableModel.getValueAt(i, 0).toString();

                        if (tableEnrollIdStr.equals(csvEnrollIdStr)) {
                            try {
                                double quiz = Double.parseDouble(parts[3]);
                                double mid = Double.parseDouble(parts[4]);
                                double fin = Double.parseDouble(parts[5]);

                                tableModel.setValueAt(quiz, i, 3);
                                tableModel.setValueAt(mid, i, 4);
                                tableModel.setValueAt(fin, i, 5);
                                updatedCount++;
                            } catch (NumberFormatException ex) {
                                System.err.println("Skipping invalid number for ID " + csvEnrollIdStr);
                            }
                        }
                    }
                }

                calculateStats();
                JOptionPane.showMessageDialog(this, "Imported " + updatedCount + " records.\nClick 'Save Uploaded Changes' to commit to database.");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage());
            }
        }
    }
}