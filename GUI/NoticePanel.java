package ums.gui.panels;

import ums.dao.INoticeDAO;
import ums.dao.NoticeDAOImpl;
import ums.gui.UITheme;
import ums.model.Notice;
import ums.roles.AbstractUser;
import ums.roles.Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NoticePanel extends JPanel {

    private final AbstractUser user;
    private final INoticeDAO noticeDAO = new NoticeDAOImpl();
    private DefaultTableModel tableModel;
    private JTable table;

    public NoticePanel(AbstractUser user) {
        this.user = user;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadNotices();
    }

    private void buildUI() {
        JLabel title = new JLabel("📢  Notices");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"#", "Notice", "Date / Time"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(600);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);

        add(UITheme.scroll(table), BorderLayout.CENTER);

        // Admin-only control panel
        if (user instanceof Admin admin) {
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            controls.setBackground(UITheme.BG);

            JTextArea noticeText = new JTextArea(3, 40);
            noticeText.setFont(UITheme.BODY);
            noticeText.setLineWrap(true);
            noticeText.setWrapStyleWord(true);
            JScrollPane textScroll = new JScrollPane(noticeText);
            textScroll.setPreferredSize(new Dimension(460, 70));

            JButton addBtn = UITheme.primaryBtn("➕ Add Notice");
            JButton delBtn = UITheme.dangerBtn("🗑 Delete Selected");
            JButton refreshBtn = UITheme.primaryBtn("↻ Refresh");

            addBtn.addActionListener(e -> {
                String txt = noticeText.getText().trim();
                if (txt.isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter notice text."); return; }
                if (admin.createNotice(txt)) {
                    noticeText.setText("");
                    loadNotices();
                    JOptionPane.showMessageDialog(this, "Notice added.");
                }
            });

            delBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select a notice to delete."); return; }
                Object idObj = tableModel.getValueAt(row, 0);
                // BUG FIX: guard against null/empty notice ID before parsing
                if (idObj == null || idObj.toString().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Cannot determine notice ID.");
                    return;
                }
                try {
                    int noticeId = Integer.parseInt(idObj.toString().trim());
                    if (admin.deleteNotice(noticeId)) {
                        loadNotices();
                        JOptionPane.showMessageDialog(this, "Notice deleted.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid notice ID: " + idObj, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            refreshBtn.addActionListener(e -> loadNotices());

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            topRow.setBackground(UITheme.BG);
            topRow.add(new JLabel("New Notice:"));
            topRow.add(textScroll);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            btnRow.setBackground(UITheme.BG);
            btnRow.add(addBtn); btnRow.add(delBtn); btnRow.add(refreshBtn);

            JPanel south = new JPanel(new BorderLayout());
            south.setBackground(UITheme.BG);
            south.add(topRow, BorderLayout.NORTH);
            south.add(btnRow, BorderLayout.SOUTH);
            add(south, BorderLayout.SOUTH);
        } else {
            JButton refreshBtn = UITheme.primaryBtn("↻ Refresh");
            refreshBtn.addActionListener(e -> loadNotices());
            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
            south.setBackground(UITheme.BG);
            south.add(refreshBtn);
            add(south, BorderLayout.SOUTH);
        }
    }

    private void loadNotices() {
        tableModel.setRowCount(0);
        List<Notice> list = noticeDAO.getAllActive();
        for (Notice n : list) {
            tableModel.addRow(new Object[]{
                n.getNoticeId(),
                n.getContent(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""
            });
        }
    }
}
