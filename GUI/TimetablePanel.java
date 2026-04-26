package ums.gui.panels;

import ums.dao.ITimetableDAO;
import ums.dao.TimetableDAOImpl;
import ums.gui.UITheme;
import ums.model.Timetable;
import ums.roles.AbstractUser;
import ums.roles.Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.util.List;

public class TimetablePanel extends JPanel {

    private final AbstractUser user;
    private final ITimetableDAO ttDAO = new TimetableDAOImpl();
    private DefaultTableModel tableModel;

    public TimetablePanel(AbstractUser user) {
        this.user = user;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = new JLabel("🗓  Timetable");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Day","Course Code","Type","Start","End","Lecturer"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        add(UITheme.scroll(table), BorderLayout.CENTER);

        if (user instanceof Admin admin) {
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 6));
            form.setBackground(UITheme.BG);
            form.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
            JComboBox<String> dayBox = new JComboBox<>(days);
            JTextField codeField  = new JTextField(10);
            JTextField typeField  = new JTextField(4);
            JTextField startField = new JTextField(8);
            JTextField endField   = new JTextField(8);
            JTextField lecField   = new JTextField(10);

            form.add(new JLabel("Day:")); form.add(dayBox);
            form.add(new JLabel("Course Code:")); form.add(codeField);
            form.add(new JLabel("Type (T/P):")); form.add(typeField);
            form.add(new JLabel("Start (HH:mm:ss):")); form.add(startField);
            form.add(new JLabel("End (HH:mm:ss):")); form.add(endField);
            form.add(new JLabel("Lecturer ID:")); form.add(lecField);

            JButton addBtn = UITheme.primaryBtn("➕ Add Slot");
            JButton refreshBtn = UITheme.primaryBtn("↻ Refresh");

            addBtn.addActionListener(e -> {
                try {
                    Timetable t = new Timetable();
                    t.setDay((String) dayBox.getSelectedItem());
                    t.setC_code(codeField.getText().trim());
                    t.setC_type(typeField.getText().trim().toUpperCase());
                    t.setStartTime(Time.valueOf(startField.getText().trim()));
                    t.setEndTime(Time.valueOf(endField.getText().trim()));
                    t.setLec_id(lecField.getText().trim());
                    if (admin.createTimetableSlot(t)) { loadData(); JOptionPane.showMessageDialog(this, "Slot added."); }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
                }
            });
            refreshBtn.addActionListener(e -> loadData());

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            btnRow.setBackground(UITheme.BG);
            btnRow.add(addBtn); btnRow.add(refreshBtn);

            JPanel south = new JPanel(new BorderLayout());
            south.setBackground(UITheme.BG);
            south.add(form, BorderLayout.CENTER);
            south.add(btnRow, BorderLayout.SOUTH);
            add(south, BorderLayout.SOUTH);
        } else {
            JButton refresh = UITheme.primaryBtn("↻ Refresh");
            refresh.addActionListener(e -> loadData());
            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
            south.setBackground(UITheme.BG);
            south.add(refresh);
            add(south, BorderLayout.SOUTH);
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Timetable t : ttDAO.getFullTimetable()) {
            tableModel.addRow(new Object[]{t.getDay(), t.getC_code(), t.getC_type(),
                t.getStartTime(), t.getEndTime(), t.getLec_id()});
        }
    }
}
