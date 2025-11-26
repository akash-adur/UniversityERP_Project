package edu.univ.erp.ui.student;

import edu.univ.erp.domain.EnrollmentDetails;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentTimetablePanel extends JPanel {

    private final UserSession session;
    private final StudentService studentService;
    private JTable timetableTable;

    public StudentTimetablePanel(UserSession session, StudentService studentService) {
        this.session = session;
        this.studentService = studentService;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("My Weekly Timetable");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Columns: Days of Week
        String[] columns = {"Day", "08:00", "09:00", "10:00", "11:00", "12:00", "01:00", "02:00", "03:00", "04:00"};

        // Rows: Days
        String[][] data = {
                {"Monday", "", "", "", "", "", "", "", "", ""},
                {"Tuesday", "", "", "", "", "", "", "", "", ""},
                {"Wednesday", "", "", "", "", "", "", "", "", ""},
                {"Thursday", "", "", "", "", "", "", "", "", ""},
                {"Friday", "", "", "", "", "", "", "", "", ""}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        timetableTable = new JTable(model);
        timetableTable.setRowHeight(50); // Taller rows
        timetableTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        timetableTable.getTableHeader().setReorderingAllowed(false);
        timetableTable.getTableHeader().setResizingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        timetableTable.setDefaultRenderer(Object.class, centerRenderer);

        add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Timetable");
        refreshBtn.addActionListener(e -> loadTimetable(model));
        add(refreshBtn, BorderLayout.SOUTH);

        loadTimetable(model);
    }

    private void loadTimetable(DefaultTableModel model) {
        // Clear grid (keep first column "Day" names)
        for (int r = 0; r < model.getRowCount(); r++) {
            for (int c = 1; c < model.getColumnCount(); c++) {
                model.setValueAt("", r, c);
            }
        }

        try {
            List<EnrollmentDetails> enrollments = studentService.getEnrollmentsForStudent(session.getUserId());

            for (EnrollmentDetails e : enrollments) {
                String dayTime = e.getDayTime();
                if (dayTime == null) continue;

                // --- CHANGED HERE: Use getCourseTitle() instead of getCourseCode() ---
                String cellText = e.getCourseTitle() + " (" + e.getRoom() + ")";

                fillGrid(model, dayTime, cellText);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fillGrid(DefaultTableModel model, String dayTime, String cellText) {
        String upper = dayTime.toUpperCase();

        // 1. Determine COLUMN (Time) - Shifted by +1 because col 0 is label
        int col = -1;
        if (upper.contains("08:00") || upper.contains("8:00") || upper.contains("8 AM")) col = 1;
        else if (upper.contains("09:00") || upper.contains("9:00") || upper.contains("9 AM")) col = 2;
        else if (upper.contains("10:00") || upper.contains("10 AM")) col = 3;
        else if (upper.contains("11:00") || upper.contains("11 AM")) col = 4;
        else if (upper.contains("12:00") || upper.contains("12 PM")) col = 5;
        else if (upper.contains("13:00") || upper.contains("01:00") || upper.contains("1 PM")) col = 6;
        else if (upper.contains("14:00") || upper.contains("02:00") || upper.contains("2 PM")) col = 7;
        else if (upper.contains("15:00") || upper.contains("03:00") || upper.contains("3 PM")) col = 8;
        else if (upper.contains("16:00") || upper.contains("04:00") || upper.contains("4 PM")) col = 9;

        if (col == -1) return;

        // 2. Determine ROW (Days)
        if (upper.contains("MON") || upper.contains("MWF")) model.setValueAt(cellText, 0, col);
        if (upper.contains("TUE") || upper.contains("TTH") || (upper.contains("TU") && !upper.contains("THU"))) model.setValueAt(cellText, 1, col);
        if (upper.contains("WED") || upper.contains("MWF")) model.setValueAt(cellText, 2, col);
        if (upper.contains("THU") || upper.contains("TTH") || upper.contains("TH")) model.setValueAt(cellText, 3, col);
        if (upper.contains("FRI") || upper.contains("MWF")) model.setValueAt(cellText, 4, col);
    }
}