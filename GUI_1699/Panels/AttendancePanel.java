package ums.gui.panels;

import ums.dao.AttendanceDAOImpl;
import ums.dao.CourseDAOImpl;
import ums.dao.IAttendanceDAO;
import ums.dao.ICourseDAO;
import ums.gui.UITheme;
import ums.model.Attendance;
import ums.model.Course;
import ums.roles.Lecturer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AttendancePanel extends JPanel {

    private final Lecturer lecturer;
    private final IAttendanceDAO attendDAO = new AttendanceDAOImpl();
    private final ICourseDAO courseDAO = new CourseDAOImpl();
    private DefaultTableModel tableModel;

    private JComboBox<String> courseBox, typeBox;  // courseBox kept for compile compat (unused)
    private JTextField courseField, studentIdField, weekField;

    public AttendancePanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("📊  Attendance Management");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // Filter bar — all text fields so lecturer can type any course code
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filter.setBackground(UITheme.BG);

        // Pre-populate courseField with this lecturer's assigned courses as hint
        List<Course> courses = courseDAO.getByLecturer(lecturer.getUserId());
        String hint = courses.isEmpty() ? "" : courses.get(0).getCourseCode();

        courseBox      = null;
        courseField    = new JTextField(hint, 10);
        courseField.setToolTipText("Enter exact course code e.g. ICT2113");
        typeBox        = new JComboBox<>(new String[]{"T","P","Both"});
        studentIdField = new JTextField(12);
        studentIdField.setToolTipText("Student Reg No (optional)");
        weekField      = new JTextField(4);
        weekField.setToolTipText("Week number");

        // Course hint label listing assigned courses
        String assigned = courses.isEmpty()
                ? "(no courses assigned in DB)"
                : "Assigned: " + courses.stream()
                .map(c -> c.getCourseCode() + "(" + c.getCourseType() + ")")
                .reduce((a, b) -> a + ", " + b).orElse("");
        JLabel assignedLbl = new JLabel("  " + assigned);
        assignedLbl.setFont(UITheme.SMALL);
        assignedLbl.setForeground(UITheme.TEXT_MUTED);

        filter.add(new JLabel("Course:")); filter.add(courseField);
        filter.add(new JLabel("Type:"));   filter.add(typeBox);
        filter.add(new JLabel("Reg No:")); filter.add(studentIdField);
        filter.add(new JLabel("Week:"));   filter.add(weekField);
        filter.add(assignedLbl);

        // View buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnPanel.setBackground(UITheme.BG);

        String[] btnLabels = {"By Student","By Batch/Week","Theory","Practical","T+P Combined",
                "Eligible (with Med)","Not Eligible (Med)","Equal 80%","Above 80%","Below 80%","Rate (Individual)","Rate (Batch)"};
        Runnable[] actions = {
                () -> viewByStudent(), () -> viewByBatch(),
                () -> viewByType("T"), () -> viewByType("P"), () -> viewBoth(),
                () -> viewMedicalElig("eligible"), () -> viewMedicalElig("notEligible"),
                () -> viewEligibility("equal"), () -> viewEligibility("above"), () -> viewEligibility("less"),
                () -> viewRateIndividual(), () -> viewRateBatch()
        };
        for (int i = 0; i < btnLabels.length; i++) {
            final Runnable action = actions[i];
            JButton b = new JButton(btnLabels[i]);
            b.setFont(UITheme.SMALL);
            b.setFocusPainted(false);
            b.addActionListener(e -> action.run());
            btnPanel.add(b);
        }

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(UITheme.BG);
        topArea.add(filter, BorderLayout.NORTH);
        topArea.add(btnPanel, BorderLayout.CENTER);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Regno","Course","Type","Week","Date","Status","Hours"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] colWidths = {120, 90, 55, 55, 110, 90, 65};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
        }

        add(topArea, BorderLayout.NORTH);
        add(UITheme.scroll(table), BorderLayout.CENTER);
    }

    private String[] parseCourse() {
        String code = courseField == null ? "" : courseField.getText().trim().toUpperCase();
        String type = typeBox == null ? "T" : (String) typeBox.getSelectedItem();
        if (type == null || type.equals("Both")) type = "T"; // fallback; callers handle Both
        return new String[]{code, type};
    }

    private String courseCode() {
        return courseField == null ? "" : courseField.getText().trim().toUpperCase();
    }

    private String courseType() {
        return typeBox == null ? "T" : (String) typeBox.getSelectedItem();
    }

    private void showList(List<Attendance> list) {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Regno","Course","Type","Week","Date","Status","Hours"});
        for (Attendance a : list) {
            tableModel.addRow(new Object[]{a.getRegno(), a.getC_code(), a.getC_type(),
                    a.getWeek(), a.getAttDate(), a.getAttStatus(), a.getL_hours()});
        }
    }

    private void showRateList(List<Attendance> list) {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Regno","Course","Type","Attendance Rate %"});
        for (Attendance a : list) {
            tableModel.addRow(new Object[]{a.getRegno(), a.getC_code(), a.getC_type(),
                    String.format("%.2f%%", a.getRate())});
        }
    }

    private void viewByStudent() {
        String code  = courseCode();
        String type  = courseType();
        String regno = studentIdField.getText().trim();
        if (regno.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Reg No."); return; }
        if (code.isEmpty())  { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        showList(attendDAO.getByStudent(regno, code, type));
    }

    private void viewByBatch() {
        String code = courseCode();
        String type = courseType();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        try {
            int week = Integer.parseInt(weekField.getText().trim());
            showList(attendDAO.getByBatch(code, type, week));
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Enter valid week number."); }
    }

    private void viewByType(String type) {
        String code = courseCode();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        showList(attendDAO.getByType(code, type));
    }

    private void viewBoth() {
        String code = courseCode();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        showList(attendDAO.getByType(code));
    }

    private void viewEligibility(String key) {
        String code = courseCode();
        String type = courseType();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        Map<String, List<Attendance>> map = attendDAO.getEligibleStudents(code, type);
        showRateList(map.getOrDefault(key, List.of()));
    }

    private void viewMedicalElig(String key) {
        String code = courseCode();
        String type = courseType();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        Map<String, List<Attendance>> map = attendDAO.getEligibleMedical(code, type);
        showRateList(map.getOrDefault(key, List.of()));
    }

    private void viewRateIndividual() {
        String code  = courseCode();
        String type  = courseType();
        String regno = studentIdField.getText().trim();
        if (regno.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Reg No."); return; }
        if (code.isEmpty())  { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        double rate = attendDAO.getAttendanceRate(code, type, regno);
        JOptionPane.showMessageDialog(this, String.format("Attendance rate for %s: %.2f%%", regno, rate));
    }

    private void viewRateBatch() {
        String code = courseCode();
        String type = courseType();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter Course Code."); return; }
        showRateList(attendDAO.getAttendanceRateBatch(code, type));
    }
}
