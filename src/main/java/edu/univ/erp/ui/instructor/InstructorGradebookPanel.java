package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.GradeRecord;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorGradebookPanel extends JPanel {

    private final UserSession session;
    private final InstructorService instructorService;

    // Filters
    private JComboBox<String> termFilter;
    private JComboBox<Section> sectionSearchDropdown;

    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JButton btnStats;
    private List<GradeRecord> currentRecords;
    private List<Section> allSections;
    private boolean isAdjusting = false;

    public InstructorGradebookPanel(UserSession session) {
        this.session = session;
        this.instructorService = new InstructorService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- NORTH PANEL: Filters ---
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // 1. Filter Row
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Select Course"));

        filterPanel.add(new JLabel("Term:"));
        termFilter = new JComboBox<>(new String[]{"All", "Monsoon", "Winter", "Summer"});
        termFilter.addActionListener(e -> resetAndFilter());
        filterPanel.add(termFilter);

        filterPanel.add(Box.createHorizontalStrut(15));

        filterPanel.add(new JLabel("Search/Select Course:"));

        sectionSearchDropdown = new JComboBox<>();
        sectionSearchDropdown.setPreferredSize(new Dimension(300, 25));
        sectionSearchDropdown.setEditable(true);

        JTextField editor = (JTextField) sectionSearchDropdown.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) return;
                SwingUtilities.invokeLater(() -> filterSections(editor.getText()));
            }
        });
        filterPanel.add(sectionSearchDropdown);

        JButton loadButton = new JButton("Load Grades");
        loadButton.setBackground(new Color(0, 100, 0));
        loadButton.setForeground(Color.WHITE);
        loadButton.addActionListener(this::loadGradebook);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(loadButton);

        northPanel.add(filterPanel);

        // 2. CSV Controls Row
        JPanel csvPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("Download CSV");
        exportButton.addActionListener(e -> showFileOperation(true));
        csvPanel.add(exportButton);

        JButton importButton = new JButton("Upload CSV");
        importButton.addActionListener(e -> showFileOperation(false));
        csvPanel.add(importButton);

        northPanel.add(csvPanel);

        add(northPanel, BorderLayout.NORTH);

        // --- CENTER: Table ---
        String[] columns = {"Enroll ID", "Roll No", "Name", "Quiz", "Midterm", "Final Exam", "Letter Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        gradeTable = new JTable(tableModel);
        gradeTable.setRowHeight(25);
        gradeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        gradeTable.getTableHeader().setReorderingAllowed(false);
        gradeTable.getTableHeader().setResizingAllowed(false);

        sorter = new TableRowSorter<>(tableModel);
        gradeTable.setRowSorter(sorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        gradeTable.setDefaultRenderer(Object.class, centerRenderer);
        add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        // --- SOUTH: Stats Only ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        btnStats = new JButton("View Class Statistics");
        btnStats.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStats.addActionListener(this::showStatistics);
        bottomPanel.add(btnStats, BorderLayout.WEST);

        add(bottomPanel, BorderLayout.SOUTH);

        loadSectionsData();
    }

    private void loadSectionsData() {
        try {
            allSections = instructorService.getSectionsForInstructor(session.getUserId());
            resetAndFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetAndFilter() {
        JTextField editor = (JTextField) sectionSearchDropdown.getEditor().getEditorComponent();
        editor.setText("");
        filterSections("");
    }

    private void filterSections(String input) {
        isAdjusting = true;
        try {
            String term = (String) termFilter.getSelectedItem();
            List<Section> filtered = new ArrayList<>();

            if (allSections != null) {
                for (Section s : allSections) {
                    boolean termMatch = "All".equals(term) || s.getTerm().contains(term);
                    boolean nameMatch = s.toString().toLowerCase().contains(input.toLowerCase());

                    if (termMatch && nameMatch) {
                        filtered.add(s);
                    }
                }
            }

            sectionSearchDropdown.removeAllItems();
            for (Section s : filtered) {
                sectionSearchDropdown.addItem(s);
            }

            JTextField editor = (JTextField) sectionSearchDropdown.getEditor().getEditorComponent();
            editor.setText(input);

            if (!filtered.isEmpty() && !input.isEmpty()) {
                sectionSearchDropdown.showPopup();
            } else {
                sectionSearchDropdown.hidePopup();
            }
        } finally {
            isAdjusting = false;
        }
    }

    private void loadGradebook(ActionEvent e) {
        Object item = sectionSearchDropdown.getSelectedItem();
        if (item == null || !(item instanceof Section)) {
            JOptionPane.showMessageDialog(this, "Please select a valid course.");
            return;
        }
        Section selected = (Section) item;

        try {
            currentRecords = instructorService.getGradebook(selected.getSectionId());
            tableModel.setRowCount(0);
            for (GradeRecord r : currentRecords) {
                tableModel.addRow(new Object[]{
                        r.enrollmentId, r.rollNo, r.name, r.quiz, r.midterm, r.finals, r.letterGrade
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage());
        }
    }

    private void showStatistics(ActionEvent e) {
        int count = tableModel.getRowCount();
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No data.");
            return;
        }

        List<Double> scores = new ArrayList<>();
        double mQ = 20, mM = 50, mF = 100;
        double wQ = 20, wM = 30, wF = 50;

        for (int i = 0; i < count; i++) {
            double q = parseScore(tableModel.getValueAt(i, 3));
            double m = parseScore(tableModel.getValueAt(i, 4));
            double f = parseScore(tableModel.getValueAt(i, 5));

            double total = ((q/mQ)*wQ) + ((m/mM)*wM) + ((f/mF)*wF);
            scores.add(total);
        }

        GradeStatsDialog dialog = new GradeStatsDialog(SwingUtilities.getWindowAncestor(this), scores);
        dialog.setVisible(true);
    }

    private double parseScore(Object obj) {
        if (obj == null) return 0.0;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void showFileOperation(boolean isSave) {
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
            @Override protected JFileChooser doInBackground() { return new JFileChooser(); }
            @Override protected void done() {
                waitDialog.dispose();
                try {
                    JFileChooser fc = get();
                    if (isSave) performExport(fc); else performImport(fc);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
        waitDialog.setVisible(true);
    }

    private void performExport(JFileChooser fileChooser) {
        fileChooser.setDialogTitle("Save Gradebook CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) file = new File(file.getParentFile(), file.getName() + ".csv");
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("EnrollID,RollNo,Name,Quiz,Midterm,Final,Grade");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("%s,%s,%s,%s,%s,%s,%s%n",
                            tableModel.getValueAt(i, 0), tableModel.getValueAt(i, 1), tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3), tableModel.getValueAt(i, 4), tableModel.getValueAt(i, 5), tableModel.getValueAt(i, 6));
                }
                JOptionPane.showMessageDialog(this, "Export Successful!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    private void performImport(JFileChooser fileChooser) {
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
                JOptionPane.showMessageDialog(this, "Imported " + updatedCount + " records.\n(View only - changes not saved to DB)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage());
            }
        }
    }
}