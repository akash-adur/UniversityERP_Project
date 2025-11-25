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

        // --- Table Setup ---
        String[] columns = {"Section ID", "Course", "Day/Time", "Room", "Capacity"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is Read-Only
            }
        };

        JTable table = new JTable(model);

        // --- Styling 1: Center Align Cells ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        // --- Styling 2: Center Align Headers (NEW) ---
        // We cast the default renderer to a Label and set alignment
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // --- Add to Scroll Pane ---
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Load Data ---
        InstructorService service = new InstructorService();
        try {
            List<Section> sections = service.getSectionsForInstructor(session.getUserId());
            for (Section s : sections) {
                model.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getDayTime(),
                        s.getRoom(),
                        s.getCapacity()
                });
            }
        } catch (Exception e) {
            add(new JLabel("Error loading sections: " + e.getMessage()), BorderLayout.NORTH);
        }
    }
}