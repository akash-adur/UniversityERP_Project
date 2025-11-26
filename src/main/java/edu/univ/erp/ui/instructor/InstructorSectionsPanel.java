package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class InstructorSectionsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    public InstructorSectionsPanel(UserSession session) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Top Panel: Search ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Filter by Term:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        // --- Table Setup ---
        String[] columns = {"Term", "Section ID", "Course", "Day/Time", "Room", "Capacity"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is Read-Only
            }
        };

        table = new JTable(model);

        // Sorter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Lock down columns
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Styling
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Load Data ---
        InstructorService service = new InstructorService();
        try {
            List<Section> sections = service.getSectionsForInstructor(session.getUserId());
            for (Section s : sections) {
                model.addRow(new Object[]{
                        s.getTerm(),
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getDayTime(),
                        s.getRoom(),
                        s.getCapacity()
                });
            }
        } catch (Exception e) {
            add(new JLabel("Error loading sections: " + e.getMessage()), BorderLayout.SOUTH);
        }
    }

    private void filter() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // Filter based on the first column ("Term")
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
        }
    }
}