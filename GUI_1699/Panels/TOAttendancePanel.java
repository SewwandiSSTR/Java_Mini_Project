package ums.gui.panels;

import ums.dao.AttendanceDAOImpl;
import ums.dao.IAttendanceDAO;
import ums.gui.UITheme;
import ums.model.Attendance;
import ums.roles.TechnicalOfficer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class TOAttendancePanel extends JPanel {

    private final TechnicalOfficer to;
    private final IAttendanceDAO attendDAO = new AttendanceDAOImpl();
    private DefaultTableModel tableModel;
    private JTextField codeF, typeF, regnoF, weekF, dateF, hoursF;
    private JComboBox<String> statusBox;

    public TOAttendancePanel(TechnicalOfficer to) {
        this.to = to;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("📊  Attendance Entry");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[]{"Regno", "Course", "Type", "Week", "Date", "Status", "Hours"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);

        // Fix column widths so Regno is always fully visible
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] colWidths = {120, 90, 55, 55, 110, 90, 65};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            codeF.setText(safe(tableModel.getValueAt(row, 1)));
            typeF.setText(safe(tableModel.getValueAt(row, 2)));
            regnoF.setText(safe(tableModel.getValueAt(row, 0)));
            weekF.setText(safe(tableModel.getValueAt(row, 3)));
            dateF.setText(safe(tableModel.getValueAt(row, 4)));
            statusBox.setSelectedItem(safe(tableModel.getValueAt(row, 5)));
            hoursF.setText(safe(tableModel.getValueAt(row, 6)));
        });

        // ── Filter panel (two rows) ────────────────────────────────────────
        // Row 1: Batch filter  —  Course | Type | Week | [Load Batch]
        JPanel batchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        batchRow.setBackground(UITheme.BG);
        JTextField fCode = new JTextField(8), fType = new JTextField(3), fWeek = new JTextField(4);
        JButton loadBtn = UITheme.primaryBtn("Load Batch");
        JLabel typeHint = new JLabel("(blank=both T&P)");
        typeHint.setFont(UITheme.SMALL); typeHint.setForeground(UITheme.TEXT_MUTED);
        batchRow.add(new JLabel("Course:")); batchRow.add(fCode);
        batchRow.add(new JLabel("Type (opt.):")); batchRow.add(fType);
        batchRow.add(typeHint);
        batchRow.add(new JLabel("Week:")); batchRow.add(fWeek);
        batchRow.add(loadBtn);

        // Row 2: Student filter  —  Regno | Course (opt.) | [Search by Regno]
        JPanel regnoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        regnoRow.setBackground(UITheme.BG);
        JTextField fRegno = new JTextField(12), fRegCourse = new JTextField(8);
        JButton searchBtn = UITheme.primaryBtn("Search by Regno");
        JLabel regHint = new JLabel("(course optional)");
        regHint.setFont(UITheme.SMALL); regHint.setForeground(UITheme.TEXT_MUTED);
        regnoRow.add(new JLabel("Regno:")); regnoRow.add(fRegno);
        regnoRow.add(new JLabel("Course (opt.):")); regnoRow.add(fRegCourse);
        regnoRow.add(regHint);
        regnoRow.add(searchBtn);

        // Load Batch action
        loadBtn.addActionListener(e -> {
            try {
                String code = fCode.getText().trim();
                String type = fType.getText().trim().toUpperCase();
                if (code.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a course code."); return; }
                int w = Integer.parseInt(fWeek.getText().trim());
                tableModel.setRowCount(0);
                java.util.List<Attendance> rows = type.isEmpty()
                        ? attendDAO.getByBatchBothTypes(code, w)
                        : attendDAO.getByBatch(code, type, w);
                for (Attendance a : rows) {
                    tableModel.addRow(new Object[]{a.getRegno(), a.getC_code(), a.getC_type(),
                            a.getWeek(), a.getAttDate(), a.getAttStatus(), a.getL_hours()});
                }
                if (rows.isEmpty()) JOptionPane.showMessageDialog(this, "No records found for that batch.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid week number.");
            }
        });

        // Search by Regno action
        searchBtn.addActionListener(e -> {
            String regno = fRegno.getText().trim();
            String course = fRegCourse.getText().trim();
            if (regno.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a Reg No to search."); return; }
            tableModel.setRowCount(0);
            java.util.List<Attendance> rows = course.isEmpty()
                    ? attendDAO.getByRegno(regno)
                    : attendDAO.getByRegnoAndCourse(regno, course);
            for (Attendance a : rows) {
                tableModel.addRow(new Object[]{a.getRegno(), a.getC_code(), a.getC_type(),
                        a.getWeek(), a.getAttDate(), a.getAttStatus(), a.getL_hours()});
            }
            if (rows.isEmpty()) JOptionPane.showMessageDialog(this, "No attendance records found for " + regno + ".");
        });

        JPanel filterArea = new JPanel(new GridLayout(2, 1));
        filterArea.setBackground(UITheme.BG);
        filterArea.add(batchRow);
        filterArea.add(regnoRow);

        // ── Entry Form ─────────────────────────────────────────────────────
        JPanel form = UITheme.sectionPanel("Add / Update Attendance");
        form.setLayout(new GridLayout(4, 4, 8, 6));
        codeF = new JTextField(8); typeF = new JTextField(3); regnoF = new JTextField(12);
        weekF = new JTextField(4); dateF = new JTextField(10); hoursF = new JTextField(4);
        statusBox = new JComboBox<>(new String[]{"Present", "Absent", "Medical"});

        form.add(lbl("Course Code:")); form.add(codeF);
        form.add(lbl("Type (T/P):")); form.add(typeF);
        form.add(lbl("Reg No:"));     form.add(regnoF);
        form.add(lbl("Week:"));       form.add(weekF);
        form.add(lbl("Date (yyyy-mm-dd):")); form.add(dateF);
        form.add(lbl("Status:"));     form.add(statusBox);
        form.add(lbl("L_Hours:"));    form.add(hoursF);
        form.add(new JLabel());

        JButton loadStudentBtn = UITheme.primaryBtn("🔍 Load Student");
        loadStudentBtn.setToolTipText("Enter Course, Type, Reg No and Week, then click to load that attendance record");
        loadStudentBtn.addActionListener(e -> {
            String regno = regnoF.getText().trim();
            String code  = codeF.getText().trim();
            String type  = typeF.getText().trim().toUpperCase();
            String weekTxt = weekF.getText().trim();
            if (regno.isEmpty() || code.isEmpty() || type.isEmpty() || weekTxt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Course Code, Type, Reg No and Week first.");
                return;
            }
            try {
                int week = Integer.parseInt(weekTxt);
                java.util.List<Attendance> rows = attendDAO.getByBatch(code, type, week);
                Attendance found = rows.stream()
                        .filter(a -> regno.equals(a.getRegno()))
                        .findFirst().orElse(null);
                if (found == null) {
                    JOptionPane.showMessageDialog(this, "No attendance record found for " + regno
                            + " in " + code + " (" + type + ") Week " + week + ".\nFields cleared — ready for new entry.");
                    dateF.setText(""); hoursF.setText(""); statusBox.setSelectedIndex(0);
                } else {
                    dateF.setText(found.getAttDate() == null ? "" : found.getAttDate().toString());
                    statusBox.setSelectedItem(found.getAttStatus());
                    hoursF.setText(String.valueOf(found.getL_hours()));
                    JOptionPane.showMessageDialog(this, "Record loaded for " + regno + ". Edit and click Update Status.", "Record Loaded", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid week number.");
            }
        });

        JButton addBtn = UITheme.primaryBtn("➕ Add");
        JButton updBtn = UITheme.primaryBtn("✏ Update Status");
        addBtn.addActionListener(e -> addAttendance());
        updBtn.addActionListener(e -> updateAttendance());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        btnRow.setBackground(UITheme.BG);
        btnRow.add(loadStudentBtn); btnRow.add(addBtn); btnRow.add(updBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(UITheme.BG);
        south.add(form, BorderLayout.NORTH);
        south.add(btnRow, BorderLayout.SOUTH);

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(UITheme.BG);
        topArea.add(filterArea, BorderLayout.NORTH);

        add(topArea, BorderLayout.NORTH);
        add(UITheme.scroll(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setFont(UITheme.BODY); return l; }
    private String safe(Object o) { return o == null ? "" : o.toString(); }

    private void addAttendance() {
        try {
            Attendance a = new Attendance();
            a.setC_code(codeF.getText().trim());
            a.setC_type(typeF.getText().trim().toUpperCase());
            a.setRegno(regnoF.getText().trim());
            a.setWeek(Integer.parseInt(weekF.getText().trim()));
            a.setAttDate(Date.valueOf(dateF.getText().trim()));
            a.setAttStatus((String) statusBox.getSelectedItem());
            a.setL_hours(Integer.parseInt(hoursF.getText().trim()));
            if (to.addAttendance(a)) JOptionPane.showMessageDialog(this,"Attendance added.");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input: " + ex.getMessage()); }
    }

    private void updateAttendance() {
        try {
            Attendance a = new Attendance();
            a.setC_code(codeF.getText().trim());
            a.setC_type(typeF.getText().trim().toUpperCase());
            a.setRegno(regnoF.getText().trim());
            a.setWeek(Integer.parseInt(weekF.getText().trim()));
            a.setAttDate(dateF.getText().isBlank() ? null : Date.valueOf(dateF.getText().trim()));
            a.setAttStatus((String) statusBox.getSelectedItem());
            if (to.updateAttendance(a)) {
                JOptionPane.showMessageDialog(this, "Status updated successfully.");
                // Refresh the table immediately to show the new status
                refreshTable(a.getC_code(), a.getC_type(), a.getWeek());
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"Invalid input: " + ex.getMessage()); }
    }

    /** Reload the current batch in the table after an update so changes are visible. */
    private void refreshTable(String code, String type, int week) {
        tableModel.setRowCount(0);
        for (ums.model.Attendance att : to.getAttendanceByBatch(code, type, week)) {
            tableModel.addRow(new Object[]{att.getRegno(), att.getC_code(), att.getC_type(),
                    att.getWeek(), att.getAttDate(), att.getAttStatus(), att.getL_hours()});
        }
    }
}
