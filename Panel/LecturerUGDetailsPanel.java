package ums.gui.panels;

import ums.db.DBConnection;
import ums.gui.UITheme;
import ums.roles.Lecturer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Lecturer view: see undergraduate student details.
 * Shows students enrolled in this lecturer's courses.
 */
public class LecturerUGDetailsPanel extends JPanel {

    private final Lecturer lecturer;
    private DefaultTableModel tableModel;

    public LecturerUGDetailsPanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private JTextField courseFilterField;

    private void buildUI() {
        JLabel title = new JLabel("👥  Undergraduate Student Details");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"Reg No", "First Name", "Last Name", "DOB", "Email", "Type",
                        "Enrolled Course", "Course Type"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        courseFilterField = new JTextField(10);
        courseFilterField.setToolTipText("Filter by course code (leave blank for all my courses)");

        JButton loadBtn  = UITheme.primaryBtn("Load My Students");
        JButton searchBtn = UITheme.primaryBtn("Search by Course");

        loadBtn.addActionListener(e -> loadStudents());
        searchBtn.addActionListener(e -> loadByCourse(courseFilterField.getText().trim()));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setBackground(UITheme.BG);
        topBar.add(loadBtn);
        topBar.add(new JLabel("  Course Code (opt.):"));
        topBar.add(courseFilterField);
        topBar.add(searchBtn);

        add(topBar,                 BorderLayout.NORTH);
        add(UITheme.scroll(table),  BorderLayout.CENTER);

        // Load on open
        loadStudents();
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        // Get all students enrolled in courses taught by this lecturer
        String sql = "SELECT s.Reg_no, s.Fname, s.Lname, s.dob, s.email, s.S_type, " +
                "       ce.C_code, ce.C_type " +
                "FROM Student s " +
                "JOIN Course_Enrollment ce ON s.Reg_no = ce.Regno " +
                "JOIN Course c ON ce.C_code = c.C_code AND ce.C_type = c.C_type " +
                "WHERE c.Lec_id = ? " +
                "ORDER BY ce.C_code, ce.C_type, s.Reg_no";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, lecturer.getUserId());
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("Reg_no"),
                        rs.getString("Fname"),
                        rs.getString("Lname"),
                        rs.getDate("dob"),
                        rs.getString("email"),
                        rs.getString("S_type"),
                        rs.getString("C_code"),
                        rs.getString("C_type")
                });
                count++;
            }
            if (count == 0) {
                JOptionPane.showMessageDialog(this,
                        "No students found for your assigned courses.\n" +
                                "You can use 'Search by Course' to look up any course code directly.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    private void loadByCourse(String courseCode) {
        if (courseCode.isEmpty()) { loadStudents(); return; }
        tableModel.setRowCount(0);
        String sql = "SELECT s.Reg_no, s.Fname, s.Lname, s.dob, s.email, s.S_type, " +
                "       ce.C_code, ce.C_type " +
                "FROM Student s " +
                "JOIN Course_Enrollment ce ON s.Reg_no = ce.Regno " +
                "WHERE ce.C_code = ? " +
                "ORDER BY ce.C_type, s.Reg_no";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, courseCode.toUpperCase());
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("Reg_no"),
                        rs.getString("Fname"),
                        rs.getString("Lname"),
                        rs.getDate("dob"),
                        rs.getString("email"),
                        rs.getString("S_type"),
                        rs.getString("C_code"),
                        rs.getString("C_type")
                });
                count++;
            }
            if (count == 0)
                JOptionPane.showMessageDialog(this, "No students found enrolled in course: " + courseCode.toUpperCase());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
