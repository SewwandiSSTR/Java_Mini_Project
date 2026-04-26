package ums.gui.panels;

import ums.gui.UITheme;
import ums.model.Course;
import ums.roles.Undergraduate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UGCoursesPanel extends JPanel {

    private final Undergraduate ug;

    public UGCoursesPanel(Undergraduate ug) {
        this.ug = ug;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("📚  My Enrolled Courses");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Code","Type","Name","Credits","Coordinator","Department"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);

        for (Course c : ug.getMyCourses()) {
            model.addRow(new Object[]{c.getCourseCode(), c.getCourseType(), c.getCourseName(),
                c.getCredits(), c.getCoordinator(), c.getDepartment()});
        }

        JButton refresh = UITheme.primaryBtn("↻ Refresh");
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            for (Course c : ug.getMyCourses()) {
                model.addRow(new Object[]{c.getCourseCode(), c.getCourseType(), c.getCourseName(),
                    c.getCredits(), c.getCoordinator(), c.getDepartment()});
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setBackground(UITheme.BG);
        south.add(refresh);

        add(UITheme.scroll(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }
}
