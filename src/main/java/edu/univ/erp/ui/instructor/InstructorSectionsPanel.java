package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserSession;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorSectionsPanel extends JPanel {
    public InstructorSectionsPanel(UserSession session) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Section ID", "Course", "Day/Time", "Room", "Capacity"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        InstructorService service = new InstructorService();
        try {
            List<Section> sections = service.getSectionsForInstructor(session.getUserId());
            for (Section s : sections) {
                model.addRow(new Object[]{s.getSectionId(), s.getCourseCode(), s.getDayTime(), s.getRoom(), s.getCapacity()});
            }
        } catch (Exception e) {
            add(new JLabel("Error loading sections: " + e.getMessage()), BorderLayout.NORTH);
        }
    }
}
