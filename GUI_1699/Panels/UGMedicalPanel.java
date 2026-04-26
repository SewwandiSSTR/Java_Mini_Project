package ums.gui.panels;

import ums.gui.UITheme;
import ums.model.Course;
import ums.model.MedicalRecord;
import ums.roles.Undergraduate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UGMedicalPanel extends JPanel {

    private final Undergraduate ug;

    public UGMedicalPanel(Undergraduate ug) {
        this.ug = ug;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("🏥  My Medical Records");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // Students may only VIEW their medical records.
        // Submission is handled by Admin / Technical Officer.
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.BODY);
        tabs.addTab("Attendance Medical", buildViewTab("attendance"));
        tabs.addTab("Mid Medical",        buildViewTab("mid"));
        tabs.addTab("End Medical",        buildViewTab("end"));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildViewTab(String type) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Medical ID", "Course", "Type", "Status", "Submitted", "Start", "End"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        List<Course> courses = ug.getMyCourses();
        String[] codes = courses.stream()
                .map(c -> c.getCourseCode() + "|" + c.getCourseType())
                .toArray(String[]::new);
        JComboBox<String> courseBox = new JComboBox<>(
                codes.length > 0 ? codes : new String[]{"No courses"});

        JButton viewBtn = UITheme.primaryBtn("🔍 View");
        viewBtn.addActionListener(e -> {
            String sel = (String) courseBox.getSelectedItem();
            if (sel == null || !sel.contains("|")) return;
            String[] parts = sel.split("\\|");
            model.setRowCount(0);
            for (MedicalRecord r : ug.getMyMedical(parts[0], parts[1], type)) {
                model.addRow(new Object[]{
                        r.getMedical_Id(), r.getC_code(), r.getC_type(),
                        r.getMedical_status(),
                        r.getSubmitted_Date(), r.getMedical_Start_Date(), r.getMedical_End_Date()
                });
            }
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(p,
                        "No " + type + " medical records found for this course.",
                        "No Records", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setBackground(UITheme.BG);
        filter.add(new JLabel("Course:"));
        filter.add(courseBox);
        filter.add(viewBtn);

        p.add(filter, BorderLayout.NORTH);
        p.add(UITheme.scroll(table), BorderLayout.CENTER);
        return p;
    }
}
