package ums.gui.panels;

import ums.dao.ICourseDAO;
import ums.dao.CourseDAOImpl;
import ums.gui.UITheme;
import ums.model.Course;
import ums.roles.Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CoursePanel extends JPanel {

    private final Admin admin;
    private final ICourseDAO courseDAO = new CourseDAOImpl();
    private DefaultTableModel tableModel;
    private JTable table;

    // Form fields
    private JTextField codeF, nameF, typeF, creditF, lecF, depF;

    public CoursePanel(Admin admin) {
        this.admin = admin;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadCourses();
    }

    private void buildUI() {
        JLabel title = new JLabel("📚  Manage Courses");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{"Code","Type","Name","Credits","Coordinator","Department"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> populateFormFromTable());

        add(UITheme.scroll(table), BorderLayout.CENTER);

        // Form at bottom
        JPanel form = UITheme.sectionPanel("Add / Edit Course");
        form.setLayout(new GridLayout(3, 4, 10, 8));

        codeF   = new JTextField(8);
        typeF   = new JTextField(3);
        nameF   = new JTextField(16);
        creditF = new JTextField(4);
        lecF    = new JTextField(10);
        depF    = new JTextField(6);

        form.add(label("Course Code:")); form.add(codeF);
        form.add(label("Type (T/P):")); form.add(typeF);
        form.add(label("Course Name:")); form.add(nameF);
        form.add(label("Credits:"));    form.add(creditF);
        form.add(label("Lecturer ID:")); form.add(lecF);
        form.add(label("Dept ID:"));    form.add(depF);

        JButton addBtn  = UITheme.primaryBtn("➕ Add");
        JButton updBtn  = UITheme.primaryBtn("✏ Update");
        JButton delBtn  = UITheme.dangerBtn("🗑 Delete");
        JButton clrBtn  = UITheme.primaryBtn("✖ Clear");
        JButton refBtn  = UITheme.primaryBtn("↻ Refresh");

        addBtn.addActionListener(e -> addCourse());
        updBtn.addActionListener(e -> updateCourse());
        delBtn.addActionListener(e -> deleteCourse());
        clrBtn.addActionListener(e -> clearForm());
        refBtn.addActionListener(e -> loadCourses());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        btnRow.setBackground(UITheme.BG);
        btnRow.add(addBtn); btnRow.add(updBtn); btnRow.add(delBtn);
        btnRow.add(clrBtn); btnRow.add(refBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(UITheme.BG);
        south.add(form, BorderLayout.CENTER);
        south.add(btnRow, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private JLabel label(String t) { JLabel l = new JLabel(t); l.setFont(UITheme.BODY); return l; }

    private Course buildFromForm() {
        Course c = new Course();
        c.setCourseCode(codeF.getText().trim());
        c.setCourseType(typeF.getText().trim().toUpperCase());
        c.setCourseName(nameF.getText().trim());
        try { c.setCredits(Integer.parseInt(creditF.getText().trim())); } catch (NumberFormatException ex) { c.setCredits(0); }
        c.setCoordinator(lecF.getText().trim());
        c.setDepartment(depF.getText().trim());
        return c;
    }

    private void addCourse() {
        Course c = buildFromForm();
        if (c.getCourseCode().isEmpty() || c.getCourseName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Code and Name are required."); return;
        }
        if (admin.createCourse(c)) { loadCourses(); clearForm(); JOptionPane.showMessageDialog(this, "Course added."); }
    }

    private void updateCourse() {
        if (table.getSelectedRow() < 0) { JOptionPane.showMessageDialog(this, "Select a course first."); return; }
        if (admin.updateCourse(buildFromForm())) { loadCourses(); JOptionPane.showMessageDialog(this, "Course updated."); }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a course first."); return; }
        String code = tableModel.getValueAt(row, 0).toString();
        // BUG FIX: also get the type so we delete only the selected (code, type) pair
        String type = tableModel.getValueAt(row, 1).toString();
        if (JOptionPane.showConfirmDialog(this,
                "Delete course " + code + " (" + type + ")? Only this type will be removed.",
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (admin.deleteCourse(code, type)) { loadCourses(); clearForm(); }
        }
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        codeF.setText(tableModel.getValueAt(row, 0).toString());
        typeF.setText(tableModel.getValueAt(row, 1).toString());
        nameF.setText(tableModel.getValueAt(row, 2).toString());
        creditF.setText(tableModel.getValueAt(row, 3).toString());
        lecF.setText(tableModel.getValueAt(row, 4).toString());
        depF.setText(tableModel.getValueAt(row, 5).toString());
    }

    private void clearForm() {
        codeF.setText(""); typeF.setText(""); nameF.setText("");
        creditF.setText(""); lecF.setText(""); depF.setText("");
        table.clearSelection();
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        for (Course c : courseDAO.getAll()) {
            tableModel.addRow(new Object[]{c.getCourseCode(), c.getCourseType(),
                c.getCourseName(), c.getCredits(), c.getCoordinator(), c.getDepartment()});
        }
    }
}
