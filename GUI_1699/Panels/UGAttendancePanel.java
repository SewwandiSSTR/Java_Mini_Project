package ums.gui.panels;

import ums.gui.UITheme;
import ums.model.Attendance;
import ums.model.Course;
import ums.roles.Undergraduate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UGAttendancePanel extends JPanel {

    private final Undergraduate ug;

    public UGAttendancePanel(Undergraduate ug) {
        this.ug = ug;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("📊  My Attendance");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Course","Type","Week","Date","Status","Hours"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);

        List<Course> courses = ug.getMyCourses();
        String[] codes = courses.stream().map(c -> c.getCourseCode() + "|" + c.getCourseType()).toArray(String[]::new);
        JComboBox<String> courseBox = new JComboBox<>(codes.length > 0 ? codes : new String[]{"No courses"});
        JLabel rateLabel = new JLabel("Rate: –");
        rateLabel.setFont(UITheme.HEADING);
        rateLabel.setForeground(UITheme.PRIMARY);

        JButton viewBtn = UITheme.primaryBtn("View Attendance");
        viewBtn.addActionListener(e -> {
            String sel = (String) courseBox.getSelectedItem();
            if (sel == null || !sel.contains("|")) return;
            String[] parts = sel.split("\\|");
            model.setRowCount(0);
            for (Attendance a : ug.getMyAttendance(parts[0], parts[1])) {
                model.addRow(new Object[]{a.getC_code(), a.getC_type(), a.getWeek(),
                    a.getAttDate(), a.getAttStatus(), a.getL_hours()});
            }
            double rate = ug.getMyAttendanceRate(parts[0], parts[1]);
            String status = rate >= 80 ? "✔ Eligible" : "✗ Not Eligible";
            // BUG FIX: label clarifies this is the with-medical attendance rate
            rateLabel.setText(String.format("Attendance Rate (with Medical): %.2f%%  %s", rate, status));
            rateLabel.setForeground(rate >= 80 ? UITheme.SUCCESS : UITheme.DANGER);
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setBackground(UITheme.BG);
        filter.add(new JLabel("Course:")); filter.add(courseBox);
        filter.add(viewBtn); filter.add(rateLabel);

        add(filter, BorderLayout.NORTH);
        add(UITheme.scroll(table), BorderLayout.CENTER);
    }
}
