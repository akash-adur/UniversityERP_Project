package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorSectionsPanel extends JPanel {
    public InstructorSectionsPanel(UserSession session) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- UPDATED COLUMNS ---
        String[] columns = {"Section ID", "Course", "Sec", "Days", "Time", "Room", "Capacity"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // --- ADDED: Disable column reordering ---
        table.getTableHeader().setReorderingAllowed(false);

        // Set Width for "Sec"
        table.getColumnModel().getColumn(2).setPreferredWidth(40);
        table.getColumnModel().getColumn(2).setMaxWidth(60);

        add(new JScrollPane(table), BorderLayout.CENTER);

        InstructorService service = new InstructorService();
        try {
            List<Section> sections = service.getSectionsForInstructor(session.getUserId());
            for (Section s : sections) {
                String dayTime = s.getDayTime();
                String days = dayTime;
                String time = "";
                if (dayTime != null && dayTime.contains(" ")) {
                    String[] parts = dayTime.split(" ", 2);
                    days = parts[0];
                    time = parts[1];
                }

                model.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getSectionName(),
                        days,
                        time,
                        s.getRoom(),
                        s.getCapacity()
                });
            }
        } catch (Exception e) {
            add(new JLabel("Error: " + e.getMessage()), BorderLayout.NORTH);
        }
    }
}