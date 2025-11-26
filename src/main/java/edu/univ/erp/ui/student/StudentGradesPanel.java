package edu.univ.erp.ui.student;

import edu.univ.erp.domain.EnrollmentDetails;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StudentGradesPanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;

    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> semesterFilter;
    private JLabel sgpaLabel;
    private List<EnrollmentDetails> allEnrollments = new ArrayList<>();

    private Map<String, String> displayToTermMap = new LinkedHashMap<>();

    public StudentGradesPanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Top Panel (Title + Filter) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("My Academic Record");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titleLabel);

        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(new JLabel("Filter by Term:"));

        semesterFilter = new JComboBox<>();
        semesterFilter.addItem("All Semesters");
        semesterFilter.addActionListener(e -> filterTable());
        topPanel.add(semesterFilter);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Table Setup ---
        String[] columnNames = {"Term", "Course", "Credits", "Quiz", "Midterm", "Final", "Grade"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        gradesTable = new JTable(tableModel);
        gradesTable.setRowHeight(30);
        gradesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        gradesTable.getTableHeader().setReorderingAllowed(false);

        // Center Align
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        gradesTable.setDefaultRenderer(Object.class, centerRenderer);

        add(new JScrollPane(gradesTable), BorderLayout.CENTER);

        // --- 3. Bottom Panel (Buttons + SGPA) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // LEFT: SGPA Label
        sgpaLabel = new JLabel("SGPA: N/A");
        sgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sgpaLabel.setForeground(new Color(0, 102, 204));
        sgpaLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        bottomPanel.add(sgpaLabel, BorderLayout.WEST);

        // RIGHT: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Download Transcript (CSV)");
        exportButton.setBackground(new Color(0, 100, 0));
        exportButton.setForeground(Color.WHITE);
        exportButton.addActionListener(e -> showExportDialog());

        JButton refreshButton = new JButton("Refresh Grades");
        refreshButton.addActionListener(e -> loadData());

        buttonPanel.add(exportButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(refreshButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        try {
            allEnrollments = studentService.getEnrollmentsForStudent(session.getUserId());

            Set<String> rawTerms = new HashSet<>();
            for(EnrollmentDetails e : allEnrollments) rawTerms.add(e.getTerm());

            List<String> sortedTerms = new ArrayList<>(rawTerms);
            sortedTerms.sort((t1, t2) -> {
                String[] p1 = t1.split(" ");
                String[] p2 = t2.split(" ");
                int y1 = Integer.parseInt(p1[1]);
                int y2 = Integer.parseInt(p2[1]);
                if (y1 != y2) return Integer.compare(y1, y2);
                int s1 = getSeasonWeight(p1[0]);
                int s2 = getSeasonWeight(p2[0]);
                return Integer.compare(s1, s2);
            });

            displayToTermMap.clear();
            semesterFilter.removeAllItems();

            String allOption = "All Semesters";
            semesterFilter.addItem(allOption);
            displayToTermMap.put(allOption, "ALL");

            int semCounter = 1;
            for (String term : sortedTerms) {
                String displayName = "Semester " + semCounter + " (" + term + ")";
                semesterFilter.addItem(displayName);
                displayToTermMap.put(displayName, term);
                semCounter++;
            }

            filterTable();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage());
        }
    }

    private int getSeasonWeight(String season) {
        if ("Winter".equalsIgnoreCase(season)) return 1;
        if ("Summer".equalsIgnoreCase(season)) return 2;
        if ("Monsoon".equalsIgnoreCase(season)) return 3;
        return 4;
    }

    private void filterTable() {
        tableModel.setRowCount(0);
        String selectedDisplay = (String) semesterFilter.getSelectedItem();
        if (selectedDisplay == null) return;

        String targetTerm = displayToTermMap.get(selectedDisplay);

        List<EnrollmentDetails> filtered = allEnrollments;
        if (!"ALL".equals(targetTerm)) {
            filtered = allEnrollments.stream()
                    .filter(e -> e.getTerm().equals(targetTerm))
                    .collect(Collectors.toList());
        }

        // --- SGPA CALCULATION ---
        double totalPoints = 0;
        double totalCredits = 0;

        for (EnrollmentDetails e : filtered) {
            tableModel.addRow(new Object[]{
                    e.getTerm(),
                    e.getCourseCode() + ": " + e.getCourseTitle(),
                    String.valueOf(e.getCredits()),
                    String.format("%.1f", e.getQuiz()),
                    String.format("%.1f", e.getMidterm()),
                    String.format("%.1f", e.getFinals()),
                    e.getFinalGrade()
            });

            String g = e.getFinalGrade();
            // Ignore N/A, S, X
            if (g != null && !g.equals("N/A") && !g.equals("S") && !g.equals("X")) {
                double points = getGradePoints(g);
                if (points >= 0) {
                    totalPoints += points * e.getCredits();
                    totalCredits += e.getCredits();
                }
            }
        }

        // Update Label
        if (totalCredits > 0) {
            double sgpa = totalPoints / totalCredits;
            String labelText = "ALL".equals(targetTerm) ? "CGPA" : "SGPA";
            sgpaLabel.setText(String.format("%s: %.2f", labelText, sgpa));
        } else {
            sgpaLabel.setText("SGPA: N/A");
        }
    }

    // --- UPDATED SCALE ---
    private double getGradePoints(String grade) {
        if (grade == null) return -1.0;

        switch (grade.toUpperCase()) {
            case "A": return 10.0;
            case "A-": return 9.0;
            case "B": return 8.0;
            case "B-": return 7.0;
            case "C": return 6.0;
            case "C-": return 5.0;
            case "D": return 4.0;
            case "F": return 0.0;
            default: return -1.0; // Ignore other text (e.g. "Absent")
        }
    }

    private void showExportDialog() {
        JDialog waitDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Please Wait", true);
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("Opening File Manager...", SwingConstants.CENTER), BorderLayout.NORTH);
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        p.add(pb, BorderLayout.CENTER);
        waitDialog.add(p);
        waitDialog.setSize(300, 120);
        waitDialog.setLocationRelativeTo(this);
        waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        new SwingWorker<JFileChooser, Void>() {
            @Override
            protected JFileChooser doInBackground() { return new JFileChooser(); }
            @Override
            protected void done() {
                waitDialog.dispose();
                try { performExport(get()); } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();

        waitDialog.setVisible(true);
    }

    private void performExport(JFileChooser fileChooser) {
        fileChooser.setDialogTitle("Save Transcript");
        fileChooser.setSelectedFile(new File("Transcript.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("Term,Course,Credits,Quiz,Midterm,Final,Grade");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%s%n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4),
                            tableModel.getValueAt(i, 5),
                            tableModel.getValueAt(i, 6)
                    );
                }
                JOptionPane.showMessageDialog(this, "Transcript saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}